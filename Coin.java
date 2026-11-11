package Proyecto;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Rectangle;

public class Coin {
    
    public float x, y;
    public int size = 80;
    private boolean collected = false;

    // Reference to parent platform for movement logic
    private Platform parentPlatform;

    public Coin(int x, int y) {
        this.x = x;
        this.y = y;
        this.parentPlatform = null;
    }
    
    public Coin(int x, int y, Platform parentPlatform) {
        this.x = x;
        this.y = y;
        this.parentPlatform = parentPlatform;
    }
    
    public void update() {
        if (parentPlatform != null) {
            // Move horizontally with the platform
            this.x += parentPlatform.getMoveSpeed(); 
        }
    }
    
    public void draw(Graphics g) {
        if (!collected) {
            if (Assets.coinImage != null) {
                g.drawImage(Assets.coinImage, (int) x, (int) y, size, size, null);
            } else {
                // Fallback drawing
                g.setColor(Color.YELLOW);
                g.fillOval((int) x, (int) y, size, size);
                g.setColor(Color.ORANGE);
                g.drawOval((int) x, (int) y, size, size);
            }
        }
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, size, size);
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }
}