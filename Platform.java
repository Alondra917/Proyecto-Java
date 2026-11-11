package Proyecto;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Platform {

    public float x, y;
    public int width;
    public int height;

    // Platform Image skin
    private BufferedImage skin;

    // Movement fields
    private float moveSpeed = 0;
    private int startX;
    private int endX;
    private float moveYspeed = 0;
    private int startY_limit;
    private int endY_limit;
    private int moveType = 0;

    public Platform(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setSkin(BufferedImage skin) {
        this.skin = skin;
    }

    public void draw(Graphics g) {
        // 1. Draw image if available
        if (skin != null) {
            g.drawImage(skin, (int) x, (int) y, width, height, null);
        }
        // 2. Fallback: Draw rectangles if no image (for debugging or errors)
        else {
            if (moveType != 0) {
                g.setColor(new Color(255, 165, 0));
                g.fillRect((int) x, (int) y, width, height);
                g.setColor(Color.RED);
                g.drawRect((int) x, (int) y, width, height);
            } else {
                g.setColor(new Color(150, 75, 0));
                g.fillRect((int) x, (int) y, width, height);
            }
        }
    }

    public void initMovement(int type, float speedX, int rangeX, float speedY, int rangeY) {
        this.moveType = type;
        if (type == 1) {
            // Horizontal movement (Screen boundaries)
            this.moveSpeed = speedX;
            this.moveYspeed = 0;
            this.startX = 0; 
            this.endX = rangeX; 
        } else if (type == 2) {
            // Vertical movement
            this.moveYspeed = speedY;
            this.moveSpeed = 0;
            this.startY_limit = (int) this.y;
            this.endY_limit = (int) this.y + rangeY;
        } else if (type == 3) {
            // Diagonal/Combined movement
            this.moveSpeed = speedX;
            this.moveYspeed = speedY;
            this.startX = 0;
            this.endX = rangeX;
            this.startY_limit = (int) this.y;
            this.endY_limit = (int) this.y + rangeY;
        }
    }

    public void update() {
        if (moveType != 0) {
            if (moveType == 1 || moveType == 3) {
                this.x += moveSpeed;
                if (this.x <= startX || this.x + width >= endX) {
                    moveSpeed *= -1;
                    if (this.x < startX)
                        this.x = startX;
                    else if (this.x + width > endX)
                        this.x = endX - width;
                }
            }
            if (moveType == 2 || moveType == 3) {
                this.y += moveYspeed;
                if (this.y <= startY_limit || this.y + height >= endY_limit) {
                    moveYspeed *= -1;
                    if (this.y < startY_limit)
                        this.y = startY_limit;
                    else if (this.y + height > endY_limit)
                        this.y = endY_limit - height;
                }
            }
        }
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public float getMoveYSpeed() {
        return moveYspeed;
    }

    public int getMoveType() {
        return moveType;
    }

    public void adjustVerticalLimits(float amount) {
        if (moveType == 2 || moveType == 3) {
            this.startY_limit += amount;
            this.endY_limit += amount;
        }
    }
}