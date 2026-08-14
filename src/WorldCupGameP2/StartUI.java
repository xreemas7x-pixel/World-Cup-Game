package WorldCupGameP2;

import javax.swing.*;
import java.awt.*;
import javax.sound.sampled.*;

public class StartUI {

    static Clip musicClip;

    public static void main(String[] args) {

        JFrame frame = new JFrame("WorldCup");

        frame.setSize(1000, 700);

        frame.setLocationRelativeTo(null);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // =========================
        // BACKGROUND
        // =========================

        BackgroundPanel panel = new BackgroundPanel("start_bg1.jpeg");

        panel.setLayout(new BorderLayout());

        // =========================
        // TITLE
        // =========================

        JLabel title = new JLabel("WORLDCUP", JLabel.CENTER);

        title.setFont(new Font("Arial", Font.BOLD, 54));

        title.setForeground(Color.WHITE);

        title.setBorder(BorderFactory.createEmptyBorder(600, 0, 0, 0));

        // =========================
        // START BUTTON
        // =========================

        JButton startButton = new JButton("ابدأ اللعبة");

        startButton.setFont(new Font("Arial", Font.BOLD, 28));

        startButton.setBackground(new Color(255, 215, 0));

        startButton.setForeground(new Color(30, 30, 30));

        startButton.setFocusPainted(false);

        startButton.setPreferredSize(new Dimension(320, 70));

        startButton.setBorder(
                BorderFactory.createLineBorder(
                        new Color(90, 60, 0), 3));

        startButton.addActionListener(e -> {

            if (musicClip != null) {
                musicClip.stop();
            }

            frame.dispose();

            new LoginUI();
        });

        // =========================
        // CENTER PANEL
        // =========================

        JPanel centerPanel = new JPanel();

        centerPanel.setOpaque(false);

        centerPanel.add(startButton);

        // =========================
        // FEATURES
        // =========================

        JPanel bottomPanel = new JPanel(new GridLayout(1, 3));

        bottomPanel.setOpaque(false);

        bottomPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        0, 40, 25, 40));

        bottomPanel.add(createText(" لعب جماعي مباشر "));

        bottomPanel.add(createText(" -أسئلة وتحديات "));

        bottomPanel.add(createText(" -تحديث النقاط مباشرة "));

        // =========================

        panel.add(title, BorderLayout.NORTH);

        panel.add(centerPanel, BorderLayout.CENTER);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(panel);

        frame.setVisible(true);

        playMusic();
    }

    // =========================
    // MUSIC
    // =========================

    static void playMusic() {

        try {

            AudioInputStream audio =
                    AudioSystem.getAudioInputStream(
                            StartUI.class.getResource("rawan.wav"));

            musicClip = AudioSystem.getClip();

            musicClip.open(audio);

            musicClip.loop(Clip.LOOP_CONTINUOUSLY);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // =========================
    // FEATURE TEXT
    // =========================

    static JLabel createText(String text) {

        JLabel label = new JLabel(text, JLabel.CENTER);

        label.setForeground(new Color(255, 215, 0));

        label.setFont(new Font("Arial", Font.BOLD, 24));

        return label;
    }

    // =========================
    // BACKGROUND CLASS
    // =========================

    static class BackgroundPanel extends JPanel {

        Image background;

        public BackgroundPanel(String path) {

            background = new ImageIcon(
                    StartUI.class.getResource(path)
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
}