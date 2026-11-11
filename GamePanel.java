package Proyecto;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.awt.Cursor;
import java.awt.Image;

public class GamePanel extends JPanel implements Runnable, KeyListener, ActionListener {
    private GameEngine gameEngine;
    private static final int FPS_SET = 60;
    private Thread gameThread;

    // Game Over Buttons
    private JButton restartButton;
    private JButton menuButton;

    // HUD and Pause Buttons
    private JButton pauseButton;
    private JButton settingsButton;
    private JButton resumeCenterButton;

    // Config Buttons
    private JButton closeConfigButton;
    private JButton musicButton;
    private JButton sfxButton;
    private JButton homeButton;

    public GamePanel() {
        gameEngine = new GameEngine();
        setPanelSize();
        setLayout(null);

        setupGameOverButtons();
        setupHUDButtons();
        setupConfigButtons();

        addKeyListener(this);
        setFocusable(true);
        setBackground(Color.LIGHT_GRAY);
    }

    private void setPanelSize() {
        setPreferredSize(new java.awt.Dimension(HomeWindow.GAME_WIDTH, HomeWindow.GAME_HEIGHT));
    }

    public void startGameLoop() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void stopGameLoop() {
        if (gameThread != null) {
            gameThread = null;
        }
    }

    @Override
    public void run() {
        double timePerFrame = 1000000000.0 / FPS_SET;
        long lastTime = System.nanoTime();
        double delta = 0;

        while (gameThread != null) {
            long now = System.nanoTime();
            delta += (now - lastTime) / timePerFrame;
            lastTime = now;

            if (delta >= 1) {
                updateGame();
                repaint();
                delta--;
            }
        }
    }

    private void updateGame() {
        gameEngine.update();

        // 1. GAME OVER / WIN
        if (gameEngine.isGameOver() || gameEngine.isGameWon()) {
            hideAllHUD();
            hideAllConfig();

            if (!restartButton.isVisible()) {
                restartButton.setVisible(true);
                menuButton.setVisible(true);
                setComponentZOrder(restartButton, 0);
                setComponentZOrder(menuButton, 0);
                repaint();
                stopGameLoop();
            }
        } else {
            // Check States
            boolean isPaused = gameEngine.isPaused();
            boolean isConfig = gameEngine.isConfigOpen();

            if (isConfig) {
                // --- CONFIG MODE ---
                hideAllHUD();

                closeConfigButton.setVisible(true);
                musicButton.setVisible(true);
                sfxButton.setVisible(true);
                homeButton.setVisible(true);

                setComponentZOrder(closeConfigButton, 0);
                setComponentZOrder(musicButton, 0);
                setComponentZOrder(sfxButton, 0);
                setComponentZOrder(homeButton, 0);

                // Update Icons
                musicButton.setIcon(new ImageIcon(gameEngine.isMusicEnabled() ? Assets.soundOnIcon : Assets.soundOffIcon));
                sfxButton.setIcon(new ImageIcon(gameEngine.isSfxEnabled() ? Assets.soundOnIcon : Assets.soundOffIcon));

            } else if (isPaused) {
                // --- PAUSE MODE ---
                hideAllConfig();
                pauseButton.setVisible(false);
                settingsButton.setVisible(false);

                resumeCenterButton.setVisible(true);
                setComponentZOrder(resumeCenterButton, 0);

            } else {
                // --- NORMAL GAME MODE ---
                hideAllConfig();
                pauseButton.setVisible(true);
                pauseButton.setIcon(new ImageIcon(Assets.pauseIcon));
                settingsButton.setVisible(true);
                resumeCenterButton.setVisible(false);
            }
        }
    }

    private void hideAllHUD() {
        pauseButton.setVisible(false);
        settingsButton.setVisible(false);
        resumeCenterButton.setVisible(false);
        restartButton.setVisible(false);
        menuButton.setVisible(false);
    }

    private void hideAllConfig() {
        closeConfigButton.setVisible(false);
        musicButton.setVisible(false);
        sfxButton.setVisible(false);
        homeButton.setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int currentScore = gameEngine.getScore();
        Color backgroundColor = getBackgroundColor(currentScore);
        gameEngine.draw(g);
    }

    private Color getBackgroundColor(int currentScore) {
        int level = gameEngine.getCurrentLevel();
        switch (level) {
            case 1: return Color.LIGHT_GRAY;
            case 2: return new Color(135, 206, 235);
            case 3: return new Color(72, 61, 139);
            case 4: return Color.BLACK;
            default: return new Color(139, 0, 0);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameEngine.isPaused() || gameEngine.isConfigOpen() || gameEngine.isInstructionsOpen())
            return;

        Player player = gameEngine.getPlayer();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                player.setMovingLeft(true);
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                player.setMovingRight(true);
                break;
            case KeyEvent.VK_SPACE:
                player.jump();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Player player = gameEngine.getPlayer();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                player.setMovingLeft(false);
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                player.setMovingRight(false);
                break;
        }
    }

    // --- BUTTON SETUP ---
    private void setupGameOverButtons() {
        int boxW = 400;
        int boxH = 250;
        int boxX = (HomeWindow.GAME_WIDTH - boxW) / 2;
        int boxY = (HomeWindow.GAME_HEIGHT - boxH) / 2;

        int buttonSize = 70;
        int startY = boxY + 90;

        restartButton = createJuicyImageButton(Assets.restartBtn, "RESTART_GAME");
        restartButton.setBounds(boxX + 80, startY+20, buttonSize, buttonSize);

        menuButton = createJuicyImageButton(Assets.menuBtn, "GO_TO_MENU");
        menuButton.setBounds(boxX + 240, startY+20, buttonSize, buttonSize);

        add(restartButton);
        add(menuButton);
        restartButton.setVisible(false);
        menuButton.setVisible(false);
    }

    private void setupHUDButtons() {
        settingsButton = createJuicyImageButton(Assets.settingsIcon, "OPEN_CONFIG");
        settingsButton.setBounds(HomeWindow.GAME_WIDTH - 60, 5, 45, 45);

        pauseButton = createJuicyImageButton(Assets.pauseIcon, "TOGGLE_PAUSE");
        pauseButton.setBounds(HomeWindow.GAME_WIDTH - 115, 5, 45, 45);

        resumeCenterButton = createJuicyImageButton(Assets.resumeIcon, "TOGGLE_PAUSE");
        resumeCenterButton.setBounds(HomeWindow.GAME_WIDTH / 2 - 40, HomeWindow.GAME_HEIGHT / 2 - 20, 80, 80);
        resumeCenterButton.setVisible(false);

        add(settingsButton);
        add(pauseButton);
        add(resumeCenterButton);
    }

    private void setupConfigButtons() {
        int boxW = 500;
        int boxH = 400;
        int boxX = (HomeWindow.GAME_WIDTH - boxW) / 2;
        int boxY = (HomeWindow.GAME_HEIGHT - boxH) / 2;

        closeConfigButton = createJuicyImageButton(Assets.cancelIcon, "CLOSE_CONFIG");
        closeConfigButton.setBounds(boxX + boxW - 47, boxY + 78, 45, 45);

        musicButton = createJuicyImageButton(Assets.soundOnIcon, "TOGGLE_MUSIC");
        musicButton.setBounds(boxX + 58, boxY + 165, 80, 80);

        sfxButton = createJuicyImageButton(Assets.soundOnIcon, "TOGGLE_SFX");
        sfxButton.setBounds(boxX + 212, boxY + 165, 80, 80);

        homeButton = createJuicyImageButton(Assets.homeIcon, "GO_TO_MENU");
        homeButton.setBounds(boxX + 360, boxY + 165, 80, 80);

        add(closeConfigButton);
        add(musicButton);
        add(sfxButton);
        add(homeButton);

        hideAllConfig();
    }

    private JButton createJuicyImageButton(BufferedImage initialImg, String command) {
        JButton button = new JButton();
        if (initialImg != null)
            button.setIcon(new ImageIcon(initialImg));
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, javax.swing.JComponent c) {
                JButton b = (JButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                double scale = 1.0;
                if (b.getModel().isPressed())
                    scale = 0.90;
                else if (b.getModel().isRollover())
                    scale = 1.10;
                int w = b.getWidth();
                int h = b.getHeight();
                int cx = w / 2;
                int cy = h / 2;
                g2.translate(cx, cy);
                g2.scale(scale, scale);
                g2.translate(-cx, -cy);
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillOval(5, 5, w - 10, h - 10);
                ImageIcon currentIcon = (ImageIcon) b.getIcon();
                if (currentIcon != null) {
                    Image img = currentIcon.getImage();
                    g2.drawImage(img, 0, 0, w, h, null);
                }
                g2.dispose();
            }
        });
        button.setActionCommand(command);
        button.addActionListener(this);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("RESTART_GAME")) {
            HomeWindow.showGameStatic();
        } else if (command.equals("GO_TO_MENU")) {
            HomeWindow.showMenuStatic();
        } else if (command.equals("TOGGLE_PAUSE")) {
            boolean currentState = gameEngine.isPaused();
            gameEngine.setPaused(!currentState);
            requestFocusInWindow();
            repaint();
        } else if (command.equals("OPEN_CONFIG")) {
            gameEngine.setConfigOpen(true);
            repaint();
        } else if (command.equals("CLOSE_CONFIG")) {
            gameEngine.setConfigOpen(false);
            repaint();
        } else if (command.equals("TOGGLE_MUSIC")) {
            gameEngine.setMusicEnabled(!gameEngine.isMusicEnabled());
            repaint();
        } else if (command.equals("TOGGLE_SFX")) {
            gameEngine.setSfxEnabled(!gameEngine.isSfxEnabled());
            repaint();
        }
    }
}