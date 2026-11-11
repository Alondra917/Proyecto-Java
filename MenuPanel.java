package Proyecto;

import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.Cursor;
import java.awt.RenderingHints;

public class MenuPanel extends JPanel {

    private ActionListener actionListener;

    // --- CHARACTER CONFIGURATION ---
    private int charX = 500;  
    private int charY = 200; 
    private int charWidth = 310; 
    private int charHeight = 310; 

    public MenuPanel(ActionListener listener) {
        this.actionListener = listener;
        setLayout(null); 

        int btnWidth = 310;
        int btnHeight = 80; 

        // --- PLAY BUTTON ---
        JButton playButton = createButton(Assets.playBtn, "PLAY", "PLAY");
        playButton.setBounds(130, 330, btnWidth, btnHeight); 
        add(playButton);

        // --- EXIT BUTTON ---
        JButton exitButton = createButton(Assets.exitBtn, "EXIT", "EXIT");
        exitButton.setBounds(130, 410, btnWidth, btnHeight); 
        add(exitButton);

        setPreferredSize(new Dimension(HomeWindow.GAME_WIDTH, HomeWindow.GAME_HEIGHT));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (Assets.menuBg != null) {
            g.drawImage(Assets.menuBg, 0, 0, getWidth(), getHeight(), null);
        }

        if (Assets.menuCharacter != null) { 
            g.drawImage(Assets.menuCharacter, charX, charY, charWidth, charHeight, null);
        }
    }

    private JButton createButton(BufferedImage image, String fallbackText, String command) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                if (image == null) {
                    super.paintComponent(g);
                    return;
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                double scale = 1.0;
                if (getModel().isPressed()) {
                    scale = 0.9; 
                } else if (getModel().isRollover()) {
                    scale = 1.1; 
                }

                int baseWidth = 200;
                double ratio = (double) image.getHeight() / image.getWidth();
                int baseHeight = (int) (baseWidth * ratio);

                int drawWidth = (int) (baseWidth * scale);
                int drawHeight = (int) (baseHeight * scale);
                
                int x = (getWidth() - drawWidth) / 2;
                int y = (getHeight() - drawHeight) / 2;

                g2.drawImage(image, x, y, drawWidth, drawHeight, null);
                g2.dispose();
            }
        };

        button.setActionCommand(command);
        button.addActionListener(actionListener);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);

        if (image == null) {
            button.setText(fallbackText);
            button.setFont(new Font("Arial", Font.BOLD, 24));
            button.setBackground(Color.GREEN.darker());
            button.setForeground(Color.WHITE);
            button.setContentAreaFilled(true);
            button.setOpaque(true);
        }
        
        return button;
    }
}