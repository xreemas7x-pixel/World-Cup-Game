package WorldCupGameP2;

import java.io.*;
import java.net.*;
import javax.swing.*;

public class Client {

    Socket socket;
    PrintWriter out;
    BufferedReader in;

    LoginUI ui;
    GamePlayUI gameUI;

    boolean roomReady = false;

    public void connect(String username, LoginUI ui) {
        this.ui = ui;

        new Thread(() -> {
            try {
                socket = new Socket("localhost", 5011);

                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println(username);

                String msg;

                while ((msg = in.readLine()) != null) {
                    String message = msg;

                    if (message.equals("CONNECTED")) {
                        SwingUtilities.invokeLater(() -> {
                            ui.enableRoomButton();
                        });
                    }

                    else if (message.startsWith("PLAYERS:")) {
                        String playersData = message.substring(8);
                        String[] namesArray = playersData.split(",");

                        SwingUtilities.invokeLater(() -> {
                            ui.updateConnectedPlayersList(namesArray);
                        });
                    }

                    else if (message.startsWith("ROOM_PLAYERS:")) {
                        String playersData = message.substring(13);
                        String[] namesArray = playersData.split(",");

                        SwingUtilities.invokeLater(() -> {
                            ui.updateWaitingRoomList(namesArray);
                        });
                    }

                    else if (message.equals("ROOM_READY")) {
                        roomReady = true;

                        SwingUtilities.invokeLater(() -> {
                            ui.showStartButton();
                        });
                    }

                    else if (message.startsWith("SIGNAL:")) {
                        String text = message.substring(7);

                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(null, text);
                        });
                    }

                    else if (message.equals("GAME_START")) {
                        SwingUtilities.invokeLater(() -> {
                            ui.launchGame();
                        });
                    }

                    else if (message.startsWith("QUESTION:")) {
                        String data = message.substring(9);
                        String[] parts = data.split("\\|");

                        if (parts.length >= 5 && gameUI != null) {
                            String question = parts[0];

                            String[] options = {
                                    parts[1],
                                    parts[2],
                                    parts[3],
                                    parts[4]
                            };

                            SwingUtilities.invokeLater(() -> {
                                gameUI.updateQuestion(question, options);
                            });
                        }
                    }

                    else if (message.startsWith("SCORES:")) {
                        String scores = message.substring(7);

                        if (gameUI != null) {
                            SwingUtilities.invokeLater(() -> {
                                gameUI.updateScores(scores);
                            });
                        }
                    }

                    else if (message.startsWith("PLAYER_LEFT:")) {
                        String name = message.substring(12);

                        if (gameUI != null) {
                            SwingUtilities.invokeLater(() -> {
                                gameUI.appendEvent("🚪 " + name + " خرج من اللعبة");
                            });
                        }
                    }

                    else if (message.startsWith("WINNER:")) {
                        String winner = message.substring(7);

                        if (gameUI != null) {
                            SwingUtilities.invokeLater(() -> {
                                gameUI.showWinner(winner);
                            });
                        }
                    }

                    else if (message.equals("NO_WINNER")) {
                        if (gameUI != null) {
                            SwingUtilities.invokeLater(() -> {
                                gameUI.showNoWinner();
                            });
                        }
                    }

                    else if (message.startsWith("CHAT:")) {
                        String text = message.substring(5);

                        if (gameUI != null) {
                            SwingUtilities.invokeLater(() -> {
                                gameUI.appendEvent(text);
                            });
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    public void setGameUI(GamePlayUI gameUI) {
        this.gameUI = gameUI;
    }

    public void joinRoom() {
        if (out != null) {
            out.println("CONNECT_ROOM");
        }
    }

    public void handleButtonAction() {
        if (roomReady && out != null) {
            out.println("START_GAME");
        }
    }

    public void sendAnswer(String answer) {
        if (out != null) {
            out.println("ANSWER:" + answer);
        }
    }

    public void sendLeave() {
        if (out != null) {
            out.println("LEAVE");
        }

        disconnect();
    }

    public void sendAction(String action) {
        if (out != null) {
            out.println("ACTION:" + action);
        }
    }

    public void disconnect() {
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