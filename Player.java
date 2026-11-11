package Proyecto;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;
import java.awt.image.BufferedImage;

public class Player {

    // --- Properties ---
    public float x, y;
    public int width = 90, height = 160;
    private GameEngine engine;

    private float xSpeed = 7.0f;
    public float ySpeed = 1;
    private float GRAVITY = 0.5f;
    private final float JUMP_POWER = -15.0f;
    private int jumpCount = 0;

    private boolean movingLeft, movingRight;
    private boolean facingRight = true; 
    
    public Platform lastSafePlatform;
    private final int MAX_JUMPS = 2;

    // Cooldown: 14 seconds (7 images * 2 seconds each)
    private final float COOLDOWN_TIME = 14.0f; 
    
    private float currentCooldown = 0.0f;
    private final float COOLDOWN_RATE = 1.0f / 60.0f;

    private boolean isFalling = true;

    // --- Constructor ---
    public Player(int startX, int startY, GameEngine engine) {
        this.x = startX;
        this.y = startY;
        this.engine = engine;
    }

    // --- Update Logic ---
    public void update(List<Platform> platforms) {

        // 1. Apply Gravity
        if (isFalling) {
            ySpeed += GRAVITY;
        }

        float nextY = y + ySpeed;
        Rectangle nextPlayerBounds = new Rectangle((int) x, (int) nextY, width, height);

        boolean onPlatform = false;
        for (Platform p : platforms) {
            Rectangle platformBounds = p.getBounds();

            if (nextPlayerBounds.intersects(platformBounds)) {
                if (ySpeed >= 0 && (y + height) <= platformBounds.y + (ySpeed + 5)) {
                    y = platformBounds.y - height;
                    ySpeed = 0;
                    isFalling = false;
                    onPlatform = true;
                    jumpCount = 0;
                    lastSafePlatform = p;
                    break;
                }
                else if (ySpeed < 0 && (y) >= platformBounds.y + platformBounds.height) {
                    y = platformBounds.y + platformBounds.height;
                    ySpeed = 0;
                }
            }
        }

        if (!onPlatform) {
            y = nextY;
            if (ySpeed != 0) {
                isFalling = true;
            }
        }

        if (!isFalling) {
            boolean platformFoundBelow = false;
            Rectangle checkBounds = new Rectangle((int) x, (int) (y + height), width, 10);
            for (Platform p : platforms) {
                if (checkBounds.intersects(p.getBounds())) {
                    platformFoundBelow = true;
                    if (p.getMoveYSpeed() != 0 || p.getMoveSpeed() != 0) {
                        this.y += p.getMoveYSpeed();
                        this.x += p.getMoveSpeed(); 
                        ySpeed = 0;
                        isFalling = false;
                    } else {
                        ySpeed = 0;
                    }
                    break;
                }
            }

            if (!platformFoundBelow) {
                isFalling = true;
                if (ySpeed == 0) ySpeed = GRAVITY;
            } else {
                isFalling = false;
                jumpCount = 0;
            }
        }

        float originalX = x;
        
        if (movingLeft) {
            x -= xSpeed;
            facingRight = false;
        }
        if (movingRight) {
            x += xSpeed;
            facingRight = true; 
        }

        Rectangle newXBounds = getBounds();
        for (Platform p : platforms) {
            if (newXBounds.intersects(p.getBounds())) {
                x = originalX;
                break;
            }
        }

        if (x < 0) x = 0;
        if (x + width > HomeWindow.GAME_WIDTH) x = HomeWindow.GAME_WIDTH - width;
    }

    // --- Draw Logic ---
    public void draw(Graphics g) {
        BufferedImage imageToDraw = Assets.playerIdle; 

        if (isFalling) {
            if (ySpeed > 0) {
                imageToDraw = Assets.fall;
            } 
            else {
                if (jumpCount == 2) {
                    if (movingRight) imageToDraw = Assets.jumpRight2;
                    else if (movingLeft) imageToDraw = Assets.jumpLeft2;
                    else imageToDraw = facingRight ? Assets.jumpRight2 : Assets.jumpLeft2;
                } 
                else {
                    if (movingRight) imageToDraw = Assets.jumpRight1;
                    else if (movingLeft) imageToDraw = Assets.jumpLeft1;
                    else imageToDraw = Assets.jump; 
                }
            }
        } 
        else {
            if (movingRight) imageToDraw = Assets.walkRight;
            else if (movingLeft) imageToDraw = Assets.walkLeft;
            else imageToDraw = Assets.playerIdle; 
        }

        if (imageToDraw != null) {
            g.drawImage(imageToDraw, (int) x, (int) y, width, height, null);
        } else {
            g.setColor(Color.GREEN);
            g.fillRect((int) x, (int) y, width, height);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    // --- Actions ---
    public void jump() {
        if (!isFalling) {
            jumpCount = 0;
        }
        int jumpLimit = (currentCooldown >= COOLDOWN_TIME) ? MAX_JUMPS : 1;

        if (jumpCount < jumpLimit) {
            ySpeed = JUMP_POWER;
            isFalling = true;
            jumpCount++;
            
            engine.playJumpSound();
            
            if (jumpCount == MAX_JUMPS) {
                currentCooldown = 0.0f;
            }
        }
    }

    public void resetJumpCount() { this.jumpCount = 0; }
    public void setFalling(boolean isFalling) { this.isFalling = isFalling; }
    public void setMovingLeft(boolean movingLeft) { this.movingLeft = movingLeft; }
    public void setMovingRight(boolean movingRight) { this.movingRight = movingRight; }
    public void setYSpeed(float ySpeed) { this.ySpeed = ySpeed; }
    public boolean isFalling() { return isFalling; }
    
    public void updateCooldown() {
        if (currentCooldown < COOLDOWN_TIME) {
            currentCooldown += COOLDOWN_RATE;
            if (currentCooldown > COOLDOWN_TIME) currentCooldown = COOLDOWN_TIME;
        }
    }

    public float getCurrentCooldown() { return currentCooldown; }
    public float getMaxCooldown() { return COOLDOWN_TIME; }
    public void setGravity(float gravity) { this.GRAVITY = gravity; }
    public void setXSpeed(float xSpeed) { this.xSpeed = xSpeed; }
}