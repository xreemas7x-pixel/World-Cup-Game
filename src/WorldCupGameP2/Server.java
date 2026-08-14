package WorldCupGameP2;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    static Vector<ClientHandler> players = new Vector<>();
    static Vector<GameRoom> rooms = new Vector<>();
    static int nextRoomId = 1;

    static String[][] worldCupTrivia = {
        {"مين المنتخب اللي هجد الأرجنتين في 2022؟", "السعودية", "البرازيل", "فرنسا", "البرتغال", "السعودية"},
        {"وش لبس ميسي عند التتويج بكأس العالم؟", "قميص نوم", "تاج", "لبس سمكة", "بشت", "بشت"},
        {"مين سجل الهدف الأول التاريخي ضد الأرجنتين؟", "صالح الشهري", "سالم الدوسري", "ميسي", "البريكان", "صالح الشهري"},
        {"أين أقيم أول مونديال في دولة عربية؟", "السعودية", "قطر", "مصر", "الإمارات", "قطر"},
        {"مين سجل الهدف الثاني ضد الأرجنتين؟", "سالم الدوسري", "صالح الشهري", "ياسر الشهراني", "رونالدو", "سالم الدوسري"}
    };

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5011)) {
            System.out.println("Server started on port 5011");

            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class GameRoom {
        int id;
        Vector<ClientHandler> players = new Vector<>();
        HashMap<String, Integer> scores = new HashMap<>();
        int questionIndex = 0;
        boolean gameStarted = false;
        boolean timerStarted = false;

        GameRoom(int id) {
            this.id = id;
        }

        boolean isFull() {
            return players.size() >= 4;
        }
    }

    static class ClientHandler extends Thread {

        Socket socket;
        PrintWriter out;
        BufferedReader in;
        String username;
        GameRoom currentRoom;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                username = in.readLine();

                if (username == null || username.trim().isEmpty()) {
                    return;
                }

                players.add(this);
                out.println("CONNECTED");
                broadcastAllPlayers();

                System.out.println(username + " connected");

                String cmd;

                while ((cmd = in.readLine()) != null) {

                    if (cmd.equals("CONNECT_ROOM")) {
                        connectToRoom();
                    }

                    else if (cmd.equals("START_GAME")) {
                        if (currentRoom != null && currentRoom.players.size() == 4) {
                            startGame(currentRoom);
                        }
                    }

                    else if (cmd.startsWith("ANSWER:")) {
                        String answer = cmd.substring(7);
                        handleAnswer(answer);
                    }

                    else if (cmd.equals("LEAVE")) {
                        handleLeave();
                        break;
                    }

                    else if (cmd.startsWith("ACTION:")) {
                        broadcastToRoom(currentRoom, "CHAT:" + username + ": " + cmd.substring(7));
                    }
                }

            } catch (Exception e) {
                System.out.println(username + " disconnected");

            } finally {
                handleLeave();
                closeConnection();
            }
        }

        void connectToRoom() {
            if (currentRoom != null) return;

            GameRoom targetRoom = null;

            for (GameRoom room : rooms) {
                if (!room.isFull() && !room.gameStarted) {
                    targetRoom = room;
                    break;
                }
            }

            if (targetRoom == null) {
                targetRoom = new GameRoom(nextRoomId++);
                rooms.add(targetRoom);
            }

            targetRoom.players.add(this);
            targetRoom.scores.put(username, 0);
            currentRoom = targetRoom;

            broadcastRoomPlayers(targetRoom);
            startRoomTimer(targetRoom);
        }

        void startRoomTimer(GameRoom room) {
            if (room == null || room.timerStarted) return;

            room.timerStarted = true;

            new Thread(() -> {
                try {
                    Thread.sleep(30000);

                    if (!room.gameStarted && room.players.size() > 1) {
                        startGame(room);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        void broadcastAllPlayers() {
            StringBuilder list = new StringBuilder("PLAYERS:");

            for (ClientHandler p : players) {
                list.append(p.username).append(",");
            }

            for (ClientHandler p : players) {
                p.out.println(list.toString());
            }
        }

        void broadcastRoomPlayers(GameRoom room) {
            if (room == null) return;

            StringBuilder list = new StringBuilder("ROOM_PLAYERS:");

            for (ClientHandler p : room.players) {
                list.append(p.username).append(",");
            }

            for (ClientHandler p : room.players) {
                p.out.println(list.toString());

                if (room.players.size() == 4) {
                    p.out.println("ROOM_READY");
                }
            }
        }

        void startGame(GameRoom room) {
            if (room == null || room.gameStarted) return;

            room.gameStarted = true;

            for (ClientHandler p : room.players) {
                p.out.println("SIGNAL:GAME HAS STARTED");
                p.out.println("GAME_START");
            }

            sendQuestion(room);
            broadcastScores(room);
        }

        void sendQuestion(GameRoom room) {
            if (room == null) return;

            if (room.questionIndex >= worldCupTrivia.length) {
                finishGame(room);
                return;
            }

            String[] q = worldCupTrivia[room.questionIndex];

            String message =
                    "QUESTION:" + q[0] + "|" + q[1] + "|" + q[2] + "|" + q[3] + "|" + q[4];

            broadcastToRoom(room, message);
        }

        void handleAnswer(String answer) {
            if (currentRoom == null || !currentRoom.gameStarted) return;

            String correctAnswer = worldCupTrivia[currentRoom.questionIndex][5];

            if (answer.equals(correctAnswer)) {
                int oldScore = currentRoom.scores.get(username);
                currentRoom.scores.put(username, oldScore + 1);
                broadcastToRoom(currentRoom, "CHAT:✅ " + username + " answered correctly");
            } else {
                broadcastToRoom(currentRoom, "CHAT:❌ " + username + " answered incorrectly");
            }

            broadcastScores(currentRoom);

            currentRoom.questionIndex++;

            if (currentRoom.questionIndex >= worldCupTrivia.length) {
                finishGame(currentRoom);
            } else {
                sendQuestion(currentRoom);
            }
        }

        void broadcastScores(GameRoom room) {
            if (room == null) return;

            StringBuilder scores = new StringBuilder("SCORES:");

            for (String name : room.scores.keySet()) {
                scores.append(name)
                      .append("=")
                      .append(room.scores.get(name))
                      .append(" ");
            }

            broadcastToRoom(room, scores.toString());
        }

        void finishGame(GameRoom room) {
            if (room == null) return;

            int maxScore = -1;
            String winner = "";

            for (String name : room.scores.keySet()) {
                int score = room.scores.get(name);

                if (score > maxScore) {
                    maxScore = score;
                    winner = name;
                }
            }

            if (maxScore <= 0) {
                broadcastToRoom(room, "NO_WINNER");
            } else {
                broadcastToRoom(room, "WINNER:" + winner);
            }
        }

        void handleLeave() {
            if (currentRoom != null) {
                GameRoom room = currentRoom;

                room.players.remove(this);
                room.scores.remove(username);

                broadcastToRoom(room, "PLAYER_LEFT:" + username);

                if (room.players.size() <= 1 && room.gameStarted) {
                    broadcastToRoom(room, "NO_WINNER");
                }

                if (room.players.isEmpty()) {
                    rooms.remove(room);
                } else {
                    broadcastRoomPlayers(room);
                    broadcastScores(room);
                }

                currentRoom = null;
            }

            players.remove(this);
            broadcastAllPlayers();
        }

        void broadcastToRoom(GameRoom room, String message) {
            if (room == null) return;

            for (ClientHandler p : room.players) {
                p.out.println(message);
            }
        }

        void closeConnection() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();

                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}