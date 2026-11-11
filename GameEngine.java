package Proyecto;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.awt.Color;
import java.util.Iterator;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.Random;
import java.awt.image.BufferedImage;

public class GameEngine {

    private Player player;
    private List<Platform> platforms;
    private List<Coin> coins;

    private int score = 0;
    private Map<String, Integer> resources; 

    public static final int UI_MARGIN_TOP = 50;

    private final int SCROLL_THRESHOLD = HomeWindow.GAME_HEIGHT / 3;
    private final int REVIVE_COST = 5;

    // Game States
    private boolean isGameWon = false;
    private boolean isGameOver = false;
    private boolean isPaused = false;
    private boolean showConfig = false;
    private boolean showInstructions = false;

    // Sound States (static to persist across resets)
    private static boolean musicEnabled = true;
    private static boolean sfxEnabled = true;

    private int currentLevel = 1;

    // Difficulty settings
    private final float GRAVITY_START = 0.5f;
    private final float SPEED_START = 7.0f;
    private float basePlatformSpeed = 3.0f;
    private int platformWidthMin = 180;
    private int platformWidthRand = 120;
    private int mobileChance = 4;
    private int currentMoveType = 1;
    private final int VERTICAL_RANGE = 250;

    private float lastPlatformY;
    private Random random = new Random();

    // Background & Scrolling
    private float bgScrollY = 0;
    private final float PARALLAX_FACTOR = 0.5f;
    private float levelScrollTracker = 0;
    private final int LEVEL_HEIGHT_PIXELS = 3584; 

    private Platform winningPlatform = null; 
    private float winDelayTimer = 0.0f;

    public GameEngine() {
        resources = new HashMap<>();
        resources.put("Coins", 0);
        coins = new ArrayList<>();
        platforms = new ArrayList<>();

        player = new Player(500, 0, this);
        resetGame();
    }

    public void resetGame() {
        currentLevel = 1;
        isGameOver = false;
        isGameWon = false;
        isPaused = false;
        showConfig = false;

        winningPlatform = null;
        winDelayTimer = 0;

        score = 0;
        bgScrollY = 0;
        levelScrollTracker = 0;

        resources.put("Coins", 0);
        coins.clear();
        platforms.clear();

        initializeLevel();

        if (player == null)
            player = new Player(100, 0, this);
        else
            player.resetJumpCount();

        int groundY = HomeWindow.GAME_HEIGHT - 30;
        player.y = groundY - player.height;
        player.x = 100;
        player.setFalling(false);

        if (!platforms.isEmpty()) {
            player.lastSafePlatform = platforms.get(0);
        }

        generateNewPlatforms(0);
    }

    private void initializeLevel() {
        if (currentLevel == 1) {
            loadLevel1();
        } else {
            platforms.clear();
            platforms.add(new Platform(0, HomeWindow.GAME_HEIGHT - 30, HomeWindow.GAME_WIDTH, 30));
            lastPlatformY = HomeWindow.GAME_HEIGHT - 30;
        }
    }

    public void update() {
        if (isPaused || isGameOver || isGameWon || showConfig) {
            return;
        }

        if (currentLevel == 5 && winningPlatform != null) {
            if (player.lastSafePlatform == winningPlatform) {
                winDelayTimer += (1.0f / 60.0f);
                if (winDelayTimer >= 0.3f) {
                    isGameWon = true;
                }
            } else {
                winDelayTimer = 0;
            }
        }
        collectCoins();
        player.update(platforms);
        player.updateCooldown();

        for (Platform p : platforms)
            p.update();
        for (Coin c : coins)
            c.update();

        if (player.y < SCROLL_THRESHOLD) {
            float scrollAmount = SCROLL_THRESHOLD - player.y;
            scrollWorld(scrollAmount);
            player.y += scrollAmount;
            generateNewPlatforms(scrollAmount);
        }

        cleanUpPlatforms();

        score = (int) Math.max(score, (HomeWindow.GAME_HEIGHT - player.y));

        if (player.y > HomeWindow.GAME_HEIGHT) {
            handlePlayerFall();
        }
    }

    private void handlePlayerFall() {
        int currentCoins = resources.get("Coins");
        if (currentCoins >= REVIVE_COST) {
            resources.put("Coins", currentCoins - REVIVE_COST);
            final int PLATFORM_BASE_TARGET = HomeWindow.GAME_HEIGHT - 100;
            Platform revivePlatform = player.lastSafePlatform;

            if (revivePlatform != null) {
                float currentPlatformBaseY = revivePlatform.y + revivePlatform.height;
                float scrollCorrection = PLATFORM_BASE_TARGET - currentPlatformBaseY;
                scrollWorld(scrollCorrection);
                player.y = revivePlatform.y - player.height;
                player.x = revivePlatform.x + (revivePlatform.width / 2) - (player.width / 2);
            } else {
                player.y = HomeWindow.GAME_HEIGHT - player.height;
                player.x = HomeWindow.GAME_WIDTH / 2 - (player.width / 2);
            }
            player.setFalling(false);
            player.setYSpeed(0);
            player.resetJumpCount();
        } else {
            isGameOver = true;
        }
    }

    private void scrollWorld(float amount) {
        // 1. Move elements
        for (Platform p : platforms) {
            p.y += amount;
            if (p.getMoveType() == 2 || p.getMoveType() == 3)
                p.adjustVerticalLimits(amount);
        }

        for (Coin c : coins)
            c.y += amount;

        lastPlatformY += amount;

        // 2. Parallax
        float bgMove = amount * PARALLAX_FACTOR;
        bgScrollY += bgMove;

        levelScrollTracker += bgMove;

        // 3. Level Switching
        if (!platforms.isEmpty()) {
            Platform lastPlatform = platforms.get(platforms.size() - 1);

            if (currentLevel < 5 && lastPlatform.y > 200 && player.y < (lastPlatform.y - 100)) {
                currentLevel++;
                bgScrollY = 0;
                levelScrollTracker = 0;
                applyDifficulty(currentLevel);
                System.out.println("Loading Level " + currentLevel);

                if (currentLevel == 2) loadLevel2();
                else if (currentLevel == 3) loadLevel3();
                else if (currentLevel == 4) loadLevel4();
                else if (currentLevel == 5) loadLevel5();
            }
        }
    }

    private void generateNewPlatforms(float scrollAmount) {
        if (currentLevel >= 1 && currentLevel <= 5) {
            return; // Fixed maps
        }

        // Generic logic (unused in current fixed map design but kept for safety)
        float platformGenerationY = lastPlatformY;
        while (platformGenerationY > UI_MARGIN_TOP) {
            int gap = 160 + random.nextInt(40);
            float newY = platformGenerationY - gap;

            if (newY < UI_MARGIN_TOP)
                break;

            int platformW = platformWidthMin + random.nextInt(platformWidthRand);
            int platformX = random.nextInt(HomeWindow.GAME_WIDTH - platformW);
            Platform newPlatform = new Platform(platformX, (int) newY, platformW, 40);

            if (random.nextInt(mobileChance) == 0) {
                float speedX = basePlatformSpeed + random.nextFloat() * 1.0f;
                float speedY = basePlatformSpeed / 2 + random.nextFloat() * 0.5f;
                int rangeX = HomeWindow.GAME_WIDTH - newPlatform.width;

                if (currentMoveType == 1 && rangeX > 0) {
                    newPlatform.x = 0;
                    newPlatform.initMovement(currentMoveType, speedX, HomeWindow.GAME_WIDTH, 0, 0);
                } else {
                    newPlatform.initMovement(currentMoveType, speedX, rangeX, speedY, VERTICAL_RANGE);
                }
            }

            platforms.add(newPlatform);
            platformGenerationY = newY;

            if (random.nextInt(4) == 0) {
                int coinX = (int) newPlatform.x + (newPlatform.width / 2) - (new Coin(0, 0).size / 2);
                int coinY = (int) newPlatform.y - new Coin(0, 0).size - 5;
                coins.add(new Coin(coinX, coinY, newPlatform));
            }
            lastPlatformY = platformGenerationY;
        }
        lastPlatformY = platformGenerationY;
    }

    // =========================================================
    // LEVEL LOADING
    // =========================================================

    private void loadLevel1() {
        platforms.clear();
        coins.clear();
        platforms.add(new Platform(0, HomeWindow.GAME_HEIGHT - 30, HomeWindow.GAME_WIDTH, 30));

        addFixedPlatform(2, 0, 550);
        addFixedPlatform(4, 400, 410);
        addFixedPlatform(5, 750, 280);
        addFixedPlatform(3, 910, 140);
        addFixedPlatform(5, 500, 0);
        addMovingPlatform(-160, 8.5f);
        addFixedPlatform(6, -10, -300);
        addFixedPlatform(4, 300, -500);
        addFixedPlatform(5, 600, -690);
        addFixedPlatform(3, 910, -830);
        addMovingPlatform(-1000, 9.5f);
        addFixedPlatform(4, 500, -1150);
        addFixedPlatform(2, 0, -1270);
        addFixedPlatform(4, 260, -1300);
        addFixedPlatform(5, 650, -1430);
        addFixedPlatform(3, 910, -1570);
        addMovingPlatform(-1860, 10.5f);
        addFixedPlatform(4, 400, -1715);
        addFixedPlatform(2, 0, -2050); 

        lastPlatformY = -3200; 
        spawnRandomCoins(600);
    }

    private void loadLevel2() {
        float startY = !platforms.isEmpty() ? platforms.get(platforms.size() - 1).y : 0;
        float refY = startY - 150;

        addFixedPlatform(11, 300, (int) (refY));
        addFixedPlatform(8, 600, (int) (refY - 150));
        addFixedPlatform(9, 910, (int) (refY - 300));

        addMovingPlatform((int) (refY - 450), 11.5f);
        addFixedPlatform(10, -10, (int) (refY - 600));

        addFixedPlatform(12, 270, (int) (refY - 790));
        addFixedPlatform(11, 550, (int) (refY - 900));
        addFixedPlatform(9, 910, (int) (refY - 1050));

        addMovingPlatform((int) (refY - 1200), 12.5f);
        addFixedPlatform(11, 400, (int) (refY - 1350));

        addFixedPlatform(13, -10, (int) (refY - 1500));
        addFixedPlatform(12, 300, (int) (refY - 1650));
        addFixedPlatform(11, 650, (int) (refY - 1700));
        addFixedPlatform(9, 910, (int) (refY - 1900)); 

        lastPlatformY = refY - 2300;
        spawnRandomCoins((int) startY);
    }

    private void loadLevel3() {
        float startY = !platforms.isEmpty() ? platforms.get(platforms.size() - 1).y : -100;
        float refY = startY - 200;

        addFixedPlatform(16, 630, (int) (refY)); 
        addFixedPlatform(15, 410, (int) (refY - 150)); 

        addFixedPlatform(19, -4, (int) (refY - 270)); 
        addFixedPlatform(21, 880, (int) (refY - 420)); 
        addFixedPlatform(16, 680, (int) (refY - 310)); 

        addMovingPlatform((int) (refY - 600), 13.5f);
        addMovingPlatform((int) (refY - 730), 13.5f);

        addFixedPlatform(19, -5, (int) (refY - 870)); 
        addFixedPlatform(16, 360, (int) (refY - 1010)); 
        addMovingPlatform((int) (refY - 1150), 14.0f);
        addFixedPlatform(17, -5, (int) (refY - 1360)); 

        addMovingPlatform((int) (refY - 1520), 14.5f); 
        addFixedPlatform(18, 550, (int) (refY - 1720)); 

        lastPlatformY = refY - 2000;
        spawnRandomCoins((int) startY);
    }

    private void loadLevel4() {
        float startY = !platforms.isEmpty() ? platforms.get(platforms.size() - 1).y : -100;
        float refY = startY - 200;

        addFixedPlatform(23, 250, (int) (refY)); 
        addFixedPlatform(25, 600, (int) (refY - 150)); 
        addFixedPlatform(29, 910, (int) (refY - 300)); 

        addMovingPlatform((int) (refY - 480), 14.5f);
        addFixedPlatform(26, -5, (int) (refY - 650)); 
        addMovingPlatform((int) (refY - 820), 15.5f); 

        addFixedPlatform(28, 800, (int) (refY - 1020)); 

        addMovingPlatform((int) (refY - 1210), 15.5f); 
        addFixedPlatform(28, 0, (int) (refY - 1390));
        addFixedPlatform(26, 400, (int) (refY - 1550));

        addMovingPlatform((int) (refY - 1730), 16.5f); 
        addFixedPlatform(25, 0, (int) (refY - 1890)); 
        addFixedPlatform(23, 300, (int) (refY - 2050));
        addMovingPlatform((int) (refY - 2220), 17.5f);

        lastPlatformY = refY - 2520; 
        spawnRandomCoins((int) startY);
    }

    private void loadLevel5() {
        float startY = !platforms.isEmpty() ? platforms.get(platforms.size() - 1).y : -100;
        float refY = startY - 200;

        addFixedPlatform(34, 0, (int) (refY));
        addMovingPlatform((int) (refY - 210), 17.5f);
        addFixedPlatform(31, 910, (int) (refY - 350));

        addMovingPlatform((int) (refY - 510), 18.5f);
        addFixedPlatform(30, 0, (int) (refY - 700));
        addFixedPlatform(34, 800, (int) (refY - 730));

        addMovingPlatform((int) (refY - 890), 17.0f);

        addFixedPlatform(35, 300, (int) (refY - 1040));
        addMovingPlatform((int) (refY - 1200), 18.5f);
        addFixedPlatform(34, 600, (int) (refY - 1360));
        addFixedPlatform(31, 910, (int) (refY - 1510));

        addMovingPlatform((int) (refY - 1650), 19.5f);
        addMovingPlatform((int) (refY - 1820), 17.5f);
        addMovingPlatform((int) (refY - 1960), 19.5f);

        addFixedPlatform(35, 600, (int) (refY - 2130));
        addMovingPlatform((int) (refY - 2300), 20.5f);
        addMovingPlatform((int) (refY - 2460), 17.5f);
        addFixedPlatform(34, 430, (int) (refY - 2610));

        Platform finalPl = new Platform(600, (int) (refY - 2750), 90, 30);
        finalPl.setSkin(Assets.mysticPl3); 
        platforms.add(finalPl);
        winningPlatform = finalPl;

        lastPlatformY = refY - 3000;
        spawnRandomCoins((int) startY);
    }

    private void addFixedPlatform(int type, int x, int y) {
        BufferedImage skin = null;
        switch (type) {
            // Level 1
            case 1: skin = Assets.seaPl1; break;
            case 2: skin = Assets.seaPl2; break;
            case 3: skin = Assets.seaPl3; break;
            case 4: skin = Assets.seaPl4; break;
            case 5: skin = Assets.seaPl5; break;
            case 6: skin = Assets.seaPl6; break;
            case 7: skin = Assets.seaPl7; break;

            // Level 2
            case 8: skin = Assets.forestPl1; break;
            case 9: skin = Assets.forestPl2; break;
            case 10: skin = Assets.forestPl3; break;
            case 11: skin = Assets.forestPl4; break;
            case 12: skin = Assets.forestPl5; break;
            case 13: skin = Assets.forestBranch; break;
            case 14: skin = Assets.forestPl7; break;

            // Level 3
            case 15: skin = Assets.icePl1; break;
            case 16: skin = Assets.icePl2; break;
            case 17: skin = Assets.icePl3; break;
            case 18: skin = Assets.icePl4; break;
            case 19: skin = Assets.icePl5; break;
            case 20: skin = Assets.icePl6; break;
            case 21: skin = Assets.icePl7; break;
            case 22: skin = Assets.icePl8; break;

            // Level 4
            case 23: skin = Assets.zenPl1; break;
            case 24: skin = Assets.zenPl2; break;
            case 25: skin = Assets.zenPl3; break;
            case 26: skin = Assets.zenPl4; break;
            case 27: skin = Assets.zenPl5; break;
            case 28: skin = Assets.zenPl6; break;
            case 29: skin = Assets.zenPl7; break;

            // Level 5
            case 30: skin = Assets.mysticPl1; break;
            case 31: skin = Assets.mysticPl2; break;
            case 32: skin = Assets.mysticPl3; break;
            case 33: skin = Assets.mysticPl4; break;
            case 34: skin = Assets.mysticPl5; break;
            case 35: skin = Assets.mysticPl6; break;
            case 36: skin = Assets.mysticPl7; break;
        }

        int w = (skin != null) ? skin.getWidth() : 150;
        int h = (skin != null) ? skin.getHeight() : 40;
        Platform p = new Platform(x, y, w, h);
        p.setSkin(skin);
        platforms.add(p);
    }

    private void addMovingPlatform(int y, float speed) {
        BufferedImage skin;

        if (currentLevel == 1) skin = Assets.seaPl1;
        else if (currentLevel == 2) skin = Assets.forestPl7;
        else if (currentLevel == 3) skin = Assets.icePl9;
        else if (currentLevel == 4) skin = Assets.zenPl3;
        else if (currentLevel == 5) skin = Assets.mysticPl4;
        else skin = Assets.forestPl7;

        int w = (skin != null) ? skin.getWidth() : 150;
        int h = (skin != null) ? skin.getHeight() : 40;

        int x = random.nextInt(HomeWindow.GAME_WIDTH - w);

        Platform p = new Platform(x, y, w, h);
        p.setSkin(skin);
        p.initMovement(1, speed, HomeWindow.GAME_WIDTH, 0, 0);

        platforms.add(p);
    }

    private void spawnRandomCoins(int maxY) {
        for (Platform p : platforms) {
            if (p.y < maxY && random.nextInt(100) < 40) {
                int coinX = (int) p.x + (p.width / 2) - 40;
                int coinY = (int) p.y - 60;
                coins.add(new Coin(coinX, coinY, p));
            }
        }
    }

    private void cleanUpPlatforms() {
        Iterator<Platform> it = platforms.iterator();
        while (it.hasNext()) {
            Platform p = it.next();
            if (p.y > HomeWindow.GAME_HEIGHT && p != player.lastSafePlatform)
                it.remove();
        }
        Iterator<Coin> coinIt = coins.iterator();
        while (coinIt.hasNext()) {
            Coin c = coinIt.next();
            if (c.y > HomeWindow.GAME_HEIGHT)
                coinIt.remove();
        }
    }

    private void collectCoins() {
        Rectangle playerBounds = player.getBounds();
        Iterator<Coin> it = coins.iterator();
        while (it.hasNext()) {
            Coin c = it.next();
            if (playerBounds.intersects(c.getBounds())) {
                it.remove();
                resources.put("Coins", resources.get("Coins") + 1);
            }
        }
    }

    public void draw(Graphics g) {
        int imgH = LEVEL_HEIGHT_PIXELS;
        int screenW = HomeWindow.GAME_WIDTH;
        int screenH = HomeWindow.GAME_HEIGHT;

        // A) Background
        float currentY = (screenH - imgH) + bgScrollY;
        BufferedImage currentBg = getBackgroundForLevel(currentLevel);

        if (currentBg != null) {
            g.drawImage(currentBg, 0, (int) currentY, screenW, imgH, null);
            g.drawImage(currentBg, 0, (int) currentY - imgH, screenW, imgH, null);
        }

        // B) Final Curtain (Level 5)
        if (currentLevel == 5 && winningPlatform != null && Assets.finalBg != null) {
            int finalImgH = 768;
            int finalBgY = (int) winningPlatform.y - finalImgH + 150;

            g.setColor(Color.BLACK);
            g.fillRect(0, -10000, screenW, 10000 + finalBgY);
            g.drawImage(Assets.finalBg, 0, finalBgY, 1152, finalImgH, null);
        }

        // C) Game Elements
        for (Platform p : platforms)
            p.draw(g);

        if (currentLevel == 5 && winningPlatform != null) {
            int cx = (int) winningPlatform.x + (winningPlatform.width / 2);
            int cy = (int) winningPlatform.y - 50;

            g.setColor(new Color(255, 255, 0, 100));
            g.fillOval(cx - 30, cy - 30, 60, 60);
            g.setColor(new Color(255, 255, 255, 180));
            g.fillOval(cx - 15, cy - 15, 30, 30);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("FINAL", cx - 20, cy - 50);
        }

        for (Coin c : coins)
            c.draw(g);

        player.draw(g);

        // D) Overlays
        if (isGameOver) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, HomeWindow.GAME_WIDTH, HomeWindow.GAME_HEIGHT);

            int boxW = 400;
            int boxH = 250;
            int boxX = (HomeWindow.GAME_WIDTH - boxW) / 2;
            int boxY = (HomeWindow.GAME_HEIGHT - boxH) / 2;

            g.setColor(new Color(50, 50, 50));
            g.fillRoundRect(boxX, boxY, boxW, boxH, 30, 30);
            g.setColor(Color.WHITE);
            g.drawRoundRect(boxX, boxY, boxW, boxH, 30, 30);

            g.setFont(new Font("Arial", Font.BOLD, 40));
            String text = "GAME OVER";
            int textW = g.getFontMetrics().stringWidth(text);
            g.drawString(text, boxX + (boxW - textW) / 2, boxY + 70);

            g.setFont(new Font("Arial", Font.PLAIN, 16));
            String txtRestart = "Restart";
            int txtRestartW = g.getFontMetrics().stringWidth(txtRestart);
            g.drawString(txtRestart, boxX + 115 - (txtRestartW / 2), boxY + 200);

            String txtMenu = "Menu";
            int txtMenuW = g.getFontMetrics().stringWidth(txtMenu);
            g.drawString(txtMenu, boxX + 275 - (txtMenuW / 2), boxY + 200);

        } else if (isGameWon) {
            // Optional background overlay if needed
            // if (Assets.menuBg != null) { g.drawImage(Assets.menuBg, 0, 0, ...); }

            g.setColor(new Color(0, 0, 0, 100));
            g.fillRect(0, 0, HomeWindow.GAME_WIDTH, HomeWindow.GAME_HEIGHT);

            int boxW = 400;
            int boxH = 250;
            int boxX = (HomeWindow.GAME_WIDTH - boxW) / 2;
            int boxY = (HomeWindow.GAME_HEIGHT - boxH) / 2;

            g.setColor(new Color(50, 50, 50));
            g.fillRoundRect(boxX, boxY, boxW, boxH, 30, 30);
            g.setColor(Color.WHITE);
            g.drawRoundRect(boxX, boxY, boxW, boxH, 30, 30);

            g.setFont(new Font("Arial", Font.BOLD, 40));
            String text = "WINNER!";
            int textW = g.getFontMetrics().stringWidth(text);
            g.drawString(text, boxX + (boxW - textW) / 2, boxY + 70);

            g.setFont(new Font("Arial", Font.PLAIN, 16));
            String txtRestart = "Restart";
            int txtRestartW = g.getFontMetrics().stringWidth(txtRestart);
            g.drawString(txtRestart, boxX + 115 - (txtRestartW / 2), boxY + 200);

            String txtMenu = "Menu";
            int txtMenuW = g.getFontMetrics().stringWidth(txtMenu);
            g.drawString(txtMenu, boxX + 275 - (txtMenuW / 2), boxY + 200);
        }

        if (!isGameOver && !isGameWon) {
            drawHUD(g);
        }
    }

    private void drawHUD(Graphics g) {
        // Lives
        int coins = resources.get("Coins");
        int lives = coins / REVIVE_COST; 
        int heartSize = 45;
        
        if (lives >= 1 && Assets.fullHeart != null)
            g.drawImage(Assets.fullHeart, 10, 3, heartSize, heartSize, null);
        else if (Assets.emptyHeart != null)
            g.drawImage(Assets.emptyHeart, 10, 3, heartSize, heartSize, null);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("x " + lives, 60, 28);

        // Cooldown Bar
        float currentCD = player.getCurrentCooldown();
        BufferedImage barImg = Assets.barEmpty; 
        if (currentCD >= 12.0f) barImg = Assets.bar100;
        else if (currentCD >= 10.0f) barImg = Assets.bar90;
        else if (currentCD >= 8.0f) barImg = Assets.bar75;
        else if (currentCD >= 6.0f) barImg = Assets.bar50;
        else if (currentCD >= 4.0f) barImg = Assets.bar25;
        else if (currentCD >= 2.0f) barImg = Assets.bar15;

        if (barImg != null)
            g.drawImage(barImg, 100, 5, 230, 40, null);

        // Top Buttons (Visuals)
        if (isPaused && Assets.resumeIcon != null)
            g.drawImage(Assets.resumeIcon, HomeWindow.GAME_WIDTH - 115, 5, 45, 45, null);
        else if (!isPaused && Assets.pauseIcon != null)
            g.drawImage(Assets.pauseIcon, HomeWindow.GAME_WIDTH - 115, 5, 45, 45, null);

        if ((showConfig || showInstructions) && Assets.settingsIcon != null) {
            g.drawImage(Assets.settingsIcon, HomeWindow.GAME_WIDTH - 60, 5, 45, 45, null);
        }

        // Overlay: PAUSE
        if (isPaused) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, HomeWindow.GAME_WIDTH, HomeWindow.GAME_HEIGHT);

            int boxW = 370;
            int boxH = 230;
            int boxX = (HomeWindow.GAME_WIDTH - boxW) / 2;
            int boxY = (HomeWindow.GAME_HEIGHT - boxH) / 2;

            g.setColor(new Color(50, 50, 50)); 
            g.fillRoundRect(boxX, boxY, boxW, boxH, 30, 30);
            g.setColor(Color.WHITE); 
            g.drawRoundRect(boxX, boxY, boxW, boxH, 30, 30);

            g.setFont(new Font("Arial", Font.BOLD, 40));
            String text = "PAUSED";
            g.drawString(text, boxX + 235 / 2, boxY + 70);
        }

        // Overlay: CONFIG
        if (showConfig) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, HomeWindow.GAME_WIDTH, HomeWindow.GAME_HEIGHT);

            int boxW = 500;
            int boxH = 250; 
            int boxX = (HomeWindow.GAME_WIDTH - boxW) / 2;
            int boxY = (HomeWindow.GAME_HEIGHT - boxH) / 2; 

            g.setColor(new Color(50, 50, 50));
            g.fillRoundRect(boxX, boxY, boxW, boxH, 30, 30);
            g.setColor(Color.WHITE);
            g.drawRoundRect(boxX, boxY, boxW, boxH, 30, 30);

            g.setFont(new Font("Arial", Font.BOLD, 28));
            String title = "CONFIGURATION";
            int titleW = g.getFontMetrics().stringWidth(title);
            g.drawString(title, boxX + (boxW - titleW) / 2, boxY + 50);

            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("Music", boxX + 75, boxY + 200);
            g.drawString("Effects", boxX + 226, boxY + 200);
            g.drawString("Home Menu", boxX + 362, boxY + 200);
        }
    }

    // Getters / Setters
    public boolean isGameOver() { return isGameOver; }
    public boolean isGameWon() { return isGameWon; }
    public Player getPlayer() { return player; }
    public int getScore() { return score; }
    public int getCurrentLevel() { return currentLevel; }
    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) { this.isPaused = paused; updateMusicState(); }
    public boolean isConfigOpen() { return showConfig; }
    public void setConfigOpen(boolean open) { this.showConfig = open; }
    public boolean isInstructionsOpen() { return showInstructions; }
    public void setInstructionsOpen(boolean open) { this.showInstructions = open; }
    public static boolean isMusicEnabled() { return musicEnabled; }
    public void setMusicEnabled(boolean enabled) { this.musicEnabled = enabled; updateMusicState(); }
    public boolean isSfxEnabled() { return sfxEnabled; }
    public void setSfxEnabled(boolean enabled) { this.sfxEnabled = enabled; }
    

    private void applyDifficulty(int level) {
        currentMoveType = (level == 1) ? 1 : (level == 2) ? 2 : 3;
        switch (level) {
            case 1: mobileChance = 4; break;
            case 2: mobileChance = 3; break;
            case 3: basePlatformSpeed = 4.0f; mobileChance = 2; break;
            default: basePlatformSpeed = 4.5f; mobileChance = 2; break;
        }
    }

    private BufferedImage getBackgroundForLevel(int level) {
        switch (level) {
            case 1: return Assets.level1Bg;
            case 2: return Assets.level2Bg;
            case 3: return Assets.level3Bg;
            case 4: return Assets.level4Bg;
            default: return Assets.level5Bg;
        }
    }

    public void playJumpSound() {
        if (sfxEnabled && Assets.jumpSound != null) {
            try {
                if (Assets.jumpSound.isRunning()) {
                    Assets.jumpSound.stop();
                }
                Assets.jumpSound.setFramePosition(0);
                Assets.jumpSound.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateMusicState() {
        if (Assets.backgroundMusic == null) return;
        boolean shouldPlay = musicEnabled && !isPaused;
        if (shouldPlay) {
            Assets.playMusic();
        } else {
            Assets.stopMusic();
        }
    }
}