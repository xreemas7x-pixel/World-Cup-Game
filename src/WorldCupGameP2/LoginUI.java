package WorldCupGameP2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;

public class LoginUI {

    JFrame frame;
    JTextField usernameField;
    JButton connectServerButton;
    JButton connectButton;

    JTextArea connectedArea;
    JTextArea roomArea;

    JLabel statusLabel;
    JLabel roomCountLabel;

    Client client = new Client();

    Timer waitingTimer;
    int dotCount = 0;
    int roomPlayersCount = 0;

    Clip musicClip;

    Color gold = new Color(255, 215, 0);
    Color green = new Color(0, 180, 90);

    public LoginUI() {

        frame = new JFrame("لوبي كأس العالم");
        frame.setSize(950, 650);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel panel = new BackgroundPanel("stadium_bg.jpeg");
        panel.setLayout(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(35, 45, 35, 45));

        JPanel roomsPanel = new JPanel(new GridLayout(1, 2, 25, 25));
        roomsPanel.setOpaque(false);

        connectedArea = createArea();
        roomArea = createArea();

        roomsPanel.add(createRoomPanel(" قائمة المتصلون", connectedArea));
        roomsPanel.add(createRoomPanel(" قائمة اللاعبون داخل الغرفة", roomArea));

        panel.add(roomsPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setPreferredSize(new Dimension(0, 220));

        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(320, 45));
        usernameField.setFont(new Font("Arial", Font.BOLD, 20));
        usernameField.setHorizontalAlignment(JTextField.CENTER);
        usernameField.setBorder(BorderFactory.createTitledBorder("اكتب اسمك"));

        connectServerButton = createButton("الاتصال بالسيرفر");
        connectButton = createButton("دخول الغرفة");
        connectButton.setEnabled(false);

        roomCountLabel = new JLabel("اللاعبون داخل الغرفة: 0 / 4", JLabel.CENTER);
        roomCountLabel.setFont(new Font("Arial", Font.BOLD, 18));
        roomCountLabel.setForeground(gold);
        roomCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusLabel = new JLabel("بانتظار الاتصال...", JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        statusLabel.setForeground(gold);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectServerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        bottomPanel.add(usernameField);
        bottomPanel.add(Box.createVerticalStrut(12));
        bottomPanel.add(connectServerButton);
        bottomPanel.add(Box.createVerticalStrut(12));
        bottomPanel.add(connectButton);
        bottomPanel.add(Box.createVerticalStrut(12));
        bottomPanel.add(roomCountLabel);
        bottomPanel.add(Box.createVerticalStrut(8));
        bottomPanel.add(statusLabel);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        connectServerButton.addActionListener(e -> {

            String username = usernameField.getText().trim();

            if (username.isEmpty()) {
                statusLabel.setText(" اكتب اسمك أولًا");
                playSound();
                return;
            }

            usernameField.setEditable(false);
            connectServerButton.setEnabled(false);

            statusLabel.setText(" تم الاتصال بالسيرفر");
            statusLabel.setForeground(gold);

            playSound();

            client.connect(username, this);
            connectButton.setEnabled(false);
        });

        connectButton.addActionListener(e -> {

            if (connectButton.getText().equals("ابدأ اللعب")) {
                playSound();
                client.handleButtonAction();
                return;
            }

            client.joinRoom();
            connectButton.setEnabled(false);

            statusLabel.setText(" تم دخول الغرفة — بانتظار اللاعبين");
            statusLabel.setForeground(gold);

            playSound();
            startWaitingAnimation();
        });

        frame.add(panel);
        frame.setVisible(true);

        playMusic();
    }

    private JTextArea createArea() {

        JTextArea area = new JTextArea();

        area.setEditable(false);
        area.setOpaque(false);
        area.setForeground(Color.WHITE);
        area.setFont(new Font("Arial", Font.BOLD, 28));
        area.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        return area;
    }

    private JPanel createRoomPanel(String title, JTextArea area) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        JLabel label = new JLabel(title, JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 28));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(20, 0, 15, 0));

        panel.add(label, BorderLayout.NORTH);
        panel.add(area, BorderLayout.CENTER);

        return panel;
    }

    private JButton createButton(String text) {

        JButton button = new JButton(text);

        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setBackground(gold);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setMaximumSize(new Dimension(320, 45));
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(new Color(255, 235, 90));
                }
            }

            public void mouseExited(MouseEvent e) {
                if (button.getText().equals("ابدأ اللعب")) {
                    button.setBackground(green);
                } else {
                    button.setBackground(gold);
                }
            }
        });

        return button;
    }

    public void enableRoomButton() {

        connectButton.setEnabled(true);
        statusLabel.setText(" يمكنك الآن دخول الغرفة");
        statusLabel.setForeground(gold);
        playSound();
    }

    public void updateConnectedPlayersList(String[] names) {

        connectedArea.setText("");

        for (String name : names) {
            if (!name.trim().isEmpty()) {
                connectedArea.append("" + name.trim() + "\n");
            }
        }
    }

    public void updateWaitingRoomList(String[] names) {

        roomArea.setText("");

        int count = 0;

        for (String name : names) {
            if (!name.trim().isEmpty()) {
                roomArea.append(" " + name.trim() + "\n");
                count++;
            }
        }

        roomPlayersCount = count;
        roomCountLabel.setText("اللاعبون داخل الغرفة: " + roomPlayersCount + " / 4");
    }

    public void showStartButton() {

        stopWaitingAnimation();

        connectButton.setText("ابدأ اللعب");
        connectButton.setEnabled(true);
        connectButton.setBackground(green);
        connectButton.setForeground(Color.WHITE);

        roomCountLabel.setText(" الغرفة مكتملة: 4 / 4");
        roomCountLabel.setForeground(gold);

        statusLabel.setText(" اكتمل اللاعبون — ابدأ اللعبة");
        statusLabel.setForeground(gold);

        playSound();
    }

    private void startWaitingAnimation() {

        stopWaitingAnimation();

        waitingTimer = new Timer(600, e -> {
            dotCount = (dotCount + 1) % 4;

            String dots = "";
            for (int i = 0; i < dotCount; i++) {
                dots += ".";
            }

            statusLabel.setText(" بانتظار اللاعبين" + dots);
        });

        waitingTimer.start();
    }

    private void stopWaitingAnimation() {
        if (waitingTimer != null) {
            waitingTimer.stop();
        }
    }

    private void playSound() {
        Toolkit.getDefaultToolkit().beep();
    }

    private void playMusic() {
        try {
            AudioInputStream audio =
                    AudioSystem.getAudioInputStream(
                            LoginUI.class.getResource("rawan.wav"));

            musicClip = AudioSystem.getClip();
            musicClip.open(audio);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void launchGame() {

        stopWaitingAnimation();
        frame.dispose();

        new GamePlayUI(client, usernameField.getText().trim());
    }

    public void append(String text) {
        System.out.println(text);
    }

    static class BackgroundPanel extends JPanel {

        Image background;

        public BackgroundPanel(String path) {

            background = new ImageIcon(
                    LoginUI.class.getResource(path)
            ).getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            g.drawImage(
                    background,
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    this);
        }
    }

    public static void main(String[] args) {
        new LoginUI();
    }
}