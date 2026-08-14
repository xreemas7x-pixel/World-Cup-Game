package WorldCupGameP2;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GamePlayUI {

    JFrame frame;
    JLabel questionLabel;
    JButton[] options = new JButton[4];
    JButton selectedButton = null;

    JPanel scoreListPanel;
    JPanel eventListPanel;

    JButton leaveButton;
    JLabel timerLabel;

    Client client;
    String username;

    javax.swing.Timer questionTimer;
    int timeLeft = 120;

    boolean quizStarted = false;
    String pendingQuestion = null;
    String[] pendingOptions = null;

    CardLayout centerLayout;
    JPanel centerCards;

    Color gold = new Color(255, 215, 0);
    Color green = new Color(0, 170, 80);
    Color red = new Color(190, 0, 0);

    Color[] rankColors = {
            new Color(255, 215, 0),
            new Color(0, 200, 100),
            new Color(0, 150, 255),
            new Color(170, 70, 255)
    };

    public GamePlayUI(Client client, String username) {
        this.client = client;
        this.username = username;

        client.setGameUI(this);

        frame = new JFrame("WorldCup - " + username);
        frame.setSize(1200, 720);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel mainPanel = new BackgroundPanel("stadium_bg.jpeg");
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        timerLabel = new JLabel("⏱ الوقت: 02:00", JLabel.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 30));
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setOpaque(false);
        timerLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        timerLabel.setPreferredSize(new Dimension(0, 60));

        JPanel timerPanel = transparentBox();
        timerPanel.setLayout(new BorderLayout());
        timerPanel.add(timerLabel, BorderLayout.CENTER);
        mainPanel.add(timerPanel, BorderLayout.NORTH);

        JPanel leftPanel = transparentBox();
        leftPanel.setPreferredSize(new Dimension(285, 0));
        leftPanel.setLayout(new BorderLayout());

        leftPanel.add(titleLabel("النقاط"), BorderLayout.NORTH);

        scoreListPanel = listPanel();
        scoreListPanel.add(textLabel("بانتظار النقاط..."));

        leftPanel.add(scoreListPanel, BorderLayout.CENTER);
        mainPanel.add(leftPanel, BorderLayout.WEST);

        centerLayout = new CardLayout();
        centerCards = new JPanel(centerLayout);
        centerCards.setOpaque(false);

        centerCards.add(createIntroPanel(), "intro");
        centerCards.add(createQuizPanel(), "quiz");

        mainPanel.add(centerCards, BorderLayout.CENTER);

        JPanel rightPanel = transparentBox();
        rightPanel.setPreferredSize(new Dimension(290, 0));
        rightPanel.setLayout(new BorderLayout());

        rightPanel.add(titleLabel("الأحداث"), BorderLayout.NORTH);

        eventListPanel = listPanel();
        eventListPanel.add(textLabel("🔥 بدأت اللعبة! من سيكون بطل كأس العالم؟"));

        rightPanel.add(eventListPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);

        leaveButton = new JButton("خروج من اللعبة");
        leaveButton.setFont(new Font("Arial", Font.BOLD, 24));
        leaveButton.setBackground(red);
        leaveButton.setForeground(Color.WHITE);
        leaveButton.setFocusPainted(false);
        leaveButton.setPreferredSize(new Dimension(300, 55));
        leaveButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        leaveButton.addActionListener(e -> {
            client.sendLeave();
            frame.dispose();
        });

        bottomPanel.add(leaveButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
        frame.repaint();
    }

    private JPanel createIntroPanel() {
        JPanel introPanel = transparentBox();
        introPanel.setLayout(new BorderLayout(20, 20));

        JLabel introText = new JLabel(
                "<html><div style='text-align:center;'>"
                        + " مرحبًا بك في تحدي كأس العالم 🔥<br><br>"
                        + "أثبت أنك الأفضل<br>"
                        + "واجمع أعلى عدد من النقاط 🏆"
                        + "</div></html>",
                JLabel.CENTER
        );

        introText.setFont(new Font("Arial", Font.BOLD, 34));
        introText.setForeground(Color.WHITE);

        JButton readyButton = new JButton("جاهز للتحدي ");
        readyButton.setFont(new Font("Arial", Font.BOLD, 28));
        readyButton.setBackground(gold);
        readyButton.setForeground(Color.BLACK);
        readyButton.setFocusPainted(false);
        readyButton.setPreferredSize(new Dimension(320, 65));
        readyButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        readyButton.addActionListener(e -> startCountdown(introText, readyButton));

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(readyButton);

        introPanel.add(introText, BorderLayout.CENTER);
        introPanel.add(bottom, BorderLayout.SOUTH);

        return introPanel;
    }

    private JPanel createQuizPanel() {
        JPanel centerPanel = transparentBox();
        centerPanel.setLayout(new BorderLayout(18, 18));

        questionLabel = new JLabel("استعد للسؤال", JLabel.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 30));
        questionLabel.setForeground(Color.WHITE);
        questionLabel.setOpaque(false);
        questionLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(25, 20, 25, 20)
        ));

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 14, 14));
        optionsPanel.setOpaque(false);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 65, 25, 65));

        for (int i = 0; i < 4; i++) {
            options[i] = new TransparentButton("اختيار " + (i + 1));
            options[i].setFont(new Font("Arial", Font.BOLD, 23));
            options[i].setForeground(Color.WHITE);
            options[i].setFocusPainted(false);
            options[i].setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

            int index = i;
            options[i].addActionListener(e -> {
                selectedButton = options[index];
                client.sendAnswer(options[index].getText());
                disableButtons();
            });

            optionsPanel.add(options[i]);
        }

        centerPanel.add(questionLabel, BorderLayout.NORTH);
        centerPanel.add(optionsPanel, BorderLayout.CENTER);

        return centerPanel;
    }

    private void startCountdown(JLabel introText, JButton readyButton) {
        readyButton.setEnabled(false);

        final int[] count = {3};

        javax.swing.Timer countdownTimer = new javax.swing.Timer(1000, null);

        countdownTimer.addActionListener(e -> {
            if (count[0] > 0) {
                introText.setText(
                        "<html><div style='text-align:center;'>"
                                + "<span style='font-size:60px;'>" + count[0] + "</span><br>"
                                + "استعدوا..."
                                + "</div></html>"
                );
                count[0]--;
            } else {
                countdownTimer.stop();

                introText.setText(
                        "<html><div style='text-align:center;'>🚀 ابدأ!</div></html>"
                );

                javax.swing.Timer startDelay = new javax.swing.Timer(700, ev -> {
                    quizStarted = true;
                    centerLayout.show(centerCards, "quiz");

                    if (pendingQuestion != null && pendingOptions != null) {
                        showQuestionNow(pendingQuestion, pendingOptions);
                        pendingQuestion = null;
                        pendingOptions = null;
                    }

                    ((javax.swing.Timer) ev.getSource()).stop();
                });

                startDelay.setRepeats(false);
                startDelay.start();
            }
        });

        countdownTimer.setInitialDelay(0);
        countdownTimer.start();
    }

    private JPanel transparentBox() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 45));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();
            }
        };

        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        return panel;
    }

    private JPanel listPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        return panel;
    }

    private JLabel titleLabel(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 25));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 5));
        return label;
    }

    private JLabel textLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(Color.WHITE);
        label.setOpaque(false);
        label.setAlignmentX(Component.RIGHT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return label;
    }

    private JPanel scoreCard(String name, int score, int rank) {
        Color color = rankColors[Math.min(rank, rankColors.length - 1)];

        JPanel card = new JPanel(new BorderLayout(10, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 95));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setMaximumSize(new Dimension(245, 62));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        String icon = rank == 0 ? "👑" : "⚽";

        JLabel rankLabel = new JLabel(icon + " " + (rank + 1));
        rankLabel.setFont(new Font("Arial", Font.BOLD, 18));
        rankLabel.setForeground(color);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        nameLabel.setForeground(Color.WHITE);

        JLabel scoreLabel = new JLabel(String.valueOf(score));
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 28));
        scoreLabel.setForeground(color);

        card.add(rankLabel, BorderLayout.WEST);
        card.add(nameLabel, BorderLayout.CENTER);
        card.add(scoreLabel, BorderLayout.EAST);

        return card;
    }

    private void startTimer() {
        if (questionTimer != null) {
            questionTimer.stop();
        }

        timeLeft = 120;
        updateTimerLabel();

        questionTimer = new javax.swing.Timer(1000, e -> {
            timeLeft--;
            updateTimerLabel();

            if (timeLeft <= 0) {
                questionTimer.stop();
                disableButtons();
                appendEvent(" انتهى وقت السؤال");
            }
        });

        questionTimer.start();
    }

    private void updateTimerLabel() {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        timerLabel.setText(String.format("⏱ الوقت: %02d:%02d", minutes, seconds));
    }

    private void disableButtons() {
        for (JButton btn : options) {
            btn.setEnabled(false);
        }
    }

    public void updateQuestion(String q, String[] opts) {
        if (!quizStarted) {
            pendingQuestion = q;
            pendingOptions = opts;
            return;
        }

        showQuestionNow(q, opts);
    }

    private void showQuestionNow(String q, String[] opts) {
        questionLabel.setText("<html><div style='text-align:center;'>" + q + "</div></html>");

        selectedButton = null;

        for (int i = 0; i < 4; i++) {
            options[i].setText(opts[i]);
            options[i].setEnabled(true);
            options[i].setForeground(Color.WHITE);
            options[i].setBackground(new Color(0, 0, 0, 0));
        }

        appendEvent("📌 وصل سؤال جديد");
        startTimer();
    }

    public void updateScores(String scores) {
        scoreListPanel.removeAll();

        ArrayList<PlayerScore> list = new ArrayList<>();

        String[] parts = scores.split(" ");

        for (String s : parts) {
            if (!s.trim().isEmpty() && s.contains("=")) {
                String[] data = s.split("=");

                if (data.length == 2) {
                    try {
                        String name = data[0].trim();
                        int score = Integer.parseInt(data[1].trim());

                        list.add(new PlayerScore(name, score));
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        list.sort((a, b) -> b.score - a.score);

        if (list.isEmpty()) {
            scoreListPanel.add(textLabel("بانتظار النقاط..."));
        } else {
            for (int i = 0; i < list.size(); i++) {
                PlayerScore ps = list.get(i);
                scoreListPanel.add(scoreCard(ps.name, ps.score, i));
                scoreListPanel.add(Box.createVerticalStrut(10));
            }
        }

        scoreListPanel.revalidate();
        scoreListPanel.repaint();
    }

    public void appendEvent(String msg) {
        String arabicMsg = msg;

        arabicMsg = arabicMsg.replace("answered correctly", "أجاب إجابة صحيحة ✅");
        arabicMsg = arabicMsg.replace("answered incorrectly", "أجاب إجابة خاطئة ❌");

        eventListPanel.add(textLabel(arabicMsg));
        eventListPanel.revalidate();
        eventListPanel.repaint();

        if (msg.contains(username) && msg.contains("incorrectly")) {
            if (selectedButton != null) {
                selectedButton.setBackground(red);
                selectedButton.setForeground(Color.WHITE);
            }
        } else if (msg.contains(username) && msg.contains("correctly")) {
            if (selectedButton != null) {
                selectedButton.setBackground(green);
                selectedButton.setForeground(Color.WHITE);
            }
        }
    }

    public void showWinner(String winner) {
        if (questionTimer != null) {
            questionTimer.stop();
        }

        disableButtons();
        showWinnerScreen(winner);
    }

    public void showNoWinner() {
        if (questionTimer != null) {
            questionTimer.stop();
        }

        disableButtons();

        JOptionPane.showMessageDialog(frame, "لا يوجد فائز");
    }

    private void showWinnerScreen(String winner) {

        JDialog dialog = new JDialog(frame, "الفائز", true);
        dialog.setSize(680, 450);
        dialog.setLocationRelativeTo(frame);
        dialog.setUndecorated(true);

        JPanel panel = new JPanel(new BorderLayout(15, 15)) {

            @Override
            protected void paintComponent(Graphics g) {

                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();

                // خلفية داكنة فخمة
                g2.setColor(new Color(5, 10, 25, 245));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // لمعة خفيفة فقط
                g2.setColor(new Color(255, 215, 0, 35));
                g2.fillOval(getWidth() / 2 - 160, 30, 320, 120);

                g2.dispose();
            }
        };

        panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));

        JLabel trophy = new JLabel("🏆", JLabel.CENTER);
        trophy.setFont(new Font("Arial", Font.BOLD, 90));

        JLabel congrats = new JLabel("🎉 مبروك!", JLabel.CENTER);
        congrats.setFont(new Font("Arial", Font.BOLD, 38));
        congrats.setForeground(gold);

        JLabel title = new JLabel("بطل كأس العالم هو", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setForeground(Color.WHITE);

        JLabel winnerName = new JLabel(winner, JLabel.CENTER);
        winnerName.setFont(new Font("Arial", Font.BOLD, 64));
        winnerName.setForeground(gold);

        JLabel message = new JLabel("حقق أعلى عدد من النقاط ", JLabel.CENTER);
        message.setFont(new Font("Arial", Font.BOLD, 21));
        message.setForeground(Color.WHITE);

        JButton closeButton = new JButton("إنهاء اللعبة");
        closeButton.setFont(new Font("Arial", Font.BOLD, 22));
        closeButton.setBackground(gold);
        closeButton.setForeground(Color.BLACK);
        closeButton.setFocusPainted(false);
        closeButton.setPreferredSize(new Dimension(180, 45));
        closeButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        closeButton.addActionListener(e -> {
            dialog.dispose();
            frame.dispose();
        });

        JPanel center = new JPanel(new GridLayout(5, 1, 5, 5));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        center.add(trophy);
        center.add(congrats);
        center.add(title);
        center.add(winnerName);
        center.add(message);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        bottom.add(closeButton);

        panel.add(center, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    static class BackgroundPanel extends JPanel {
        Image background;

        public BackgroundPanel(String path) {
            background = new ImageIcon(
                    GamePlayUI.class.getResource(path)
            ).getImage();

            setOpaque(true);
            setDoubleBuffered(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.drawImage(
                    background,
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    this);

            g2.dispose();
        }
    }

    static class TransparentButton extends JButton {

        public TransparentButton(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            Color bg = getBackground();

            if (bg.getAlpha() == 0) {
                g2.setColor(new Color(0, 0, 0, 65));
            } else {
                g2.setColor(bg);
            }

            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

            g2.dispose();

            super.paintComponent(g);
        }
    }

    static class PlayerScore {
        String name;
        int score;

        PlayerScore(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }
}