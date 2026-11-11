package Proyecto;

import javax.swing.JFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.CardLayout;
import javax.swing.JPanel;

public class HomeWindow implements ActionListener {

    public static final int GAME_WIDTH = 1000;
    public static final int GAME_HEIGHT = 700;

    private JFrame frame;
    private JPanel cardPanel;
    private GamePanel gamePanel;
    private MenuPanel menuPanel;

    private static HomeWindow instance;

    private static final String MENU_VIEW = "Menu";
    private static final String GAME_VIEW = "Game";

    public HomeWindow() {
        instance = this;
        frame = new JFrame();
        frame.setTitle("Greenie Climb");
        frame.setSize(GAME_WIDTH, GAME_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        cardPanel = new JPanel(new CardLayout());
        menuPanel = new MenuPanel(this);

        gamePanel = new GamePanel();

        cardPanel.add(menuPanel, MENU_VIEW);
        cardPanel.add(gamePanel, GAME_VIEW);

        frame.add(cardPanel);
        frame.setVisible(true);

        if (GameEngine.isMusicEnabled()) {
            Assets.playMusic();
        }

        showMenu();
    }

    public static void showMenuStatic() {
        if (instance != null) {
            instance.showMenu();
        }
    }

    private void showMenu() {
        CardLayout cl = (CardLayout) (cardPanel.getLayout());
        cl.show(cardPanel, MENU_VIEW);

        if (GameEngine.isMusicEnabled()) {
            Assets.playMusic();
        } else {
            Assets.stopMusic();
        }
        menuPanel.requestFocusInWindow();
    }

    private void startGame() {
        gamePanel.stopGameLoop();

        // Re-initialize GamePanel for a fresh start
        gamePanel = new GamePanel();
        cardPanel.add(gamePanel, GAME_VIEW);

        CardLayout cl = (CardLayout) (cardPanel.getLayout());
        cl.show(cardPanel, GAME_VIEW);

        gamePanel.requestFocusInWindow();
        gamePanel.startGameLoop();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch (command) {
            case "PLAY":
                startGame();
                break;
            case "EXIT":
                frame.dispose();
                System.exit(0);
                break;
        }
    }

    public static void showGameStatic() {
        if (instance != null) {
            instance.startGame();
        }
    }

    public static void main(String[] args) {
        Assets.init();
        new HomeWindow();
    }
}