package Proyecto;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.IOException;
import java.net.URL;

public class Assets {
    // Audio
    public static Clip jumpSound;
    public static Clip backgroundMusic;

    // General Images
    public static BufferedImage menuCharacter;
    public static BufferedImage platformImage; // Generic fallback
    public static BufferedImage playerIdle;
    public static BufferedImage fall;
    public static BufferedImage walkLeft;
    public static BufferedImage walkRight;
    public static BufferedImage jump;
    public static BufferedImage jumpLeft1;
    public static BufferedImage jumpRight1;
    public static BufferedImage jumpLeft2;
    public static BufferedImage jumpRight2;
    public static BufferedImage coinImage;
    
    // UI & Backgrounds
    public static BufferedImage menuBg;
    public static BufferedImage playBtn;
    public static BufferedImage exitBtn;
    public static BufferedImage restartBtn;
    public static BufferedImage menuBtn;
    public static BufferedImage barEmpty;
    public static BufferedImage bar15;
    public static BufferedImage bar25;
    public static BufferedImage bar50;
    public static BufferedImage bar75;
    public static BufferedImage bar90;
    public static BufferedImage bar100;
    public static BufferedImage emptyHeart;
    public static BufferedImage fullHeart;
    public static BufferedImage settingsIcon;
    public static BufferedImage pauseIcon;
    public static BufferedImage resumeIcon;
    public static BufferedImage soundOnIcon;
    public static BufferedImage soundOffIcon;
    public static BufferedImage cancelIcon;
    public static BufferedImage homeIcon;
    public static BufferedImage finalBg;

    // Level Backgrounds
    public static BufferedImage level1Bg;
    public static BufferedImage level2Bg;
    public static BufferedImage level3Bg;
    public static BufferedImage level4Bg;
    public static BufferedImage level5Bg;

    // Level 1 Platforms (Sea)
    public static BufferedImage seaPl1, seaPl2, seaPl3, seaPl4, seaPl5, seaPl6, seaPl7;

    // Level 2 Platforms (Forest)
    public static BufferedImage forestPl1, forestPl2, forestPl3, forestPl4, forestPl5, forestBranch, forestPl7;

    // Level 3 Platforms (Ice)
    public static BufferedImage icePl1, icePl2, icePl3, icePl4, icePl5, icePl6, icePl7, icePl8, icePl9;

    // Level 4 Platforms (Zen)
    public static BufferedImage zenPl1, zenPl2, zenPl3, zenPl4, zenPl5, zenPl6, zenPl7;

    // Level 5 Platforms (Mystic)
    public static BufferedImage mysticPl1, mysticPl2, mysticPl3, mysticPl4, mysticPl5, mysticPl6, mysticPl7;

    private static Clip loadSound(String path) {
        try {
            URL url = Assets.class.getResource(path);
            if (url == null) {
                System.err.println("Sound not found: " + path);
                return null;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            return clip;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void init() {
        try {
            // Audio
            jumpSound = loadSound("Imgs/salto.wav");
            backgroundMusic = loadSound("Imgs/music.wav");

            // Player & UI
            menuCharacter = ImageIO.read(Assets.class.getResource("Imgs/personaje.png"));
            playerIdle = ImageIO.read(Assets.class.getResource("Imgs/9.png"));
            fall = ImageIO.read(Assets.class.getResource("Imgs/4.png"));
            jump = ImageIO.read(Assets.class.getResource("Imgs/10.png"));
            walkRight = ImageIO.read(Assets.class.getResource("Imgs/13.png"));
            walkLeft = ImageIO.read(Assets.class.getResource("Imgs/15.png"));
            jumpLeft1 = ImageIO.read(Assets.class.getResource("Imgs/16.png"));
            jumpRight1 = ImageIO.read(Assets.class.getResource("Imgs/14.png"));
            jumpLeft2 = ImageIO.read(Assets.class.getResource("Imgs/12.png"));
            jumpRight2 = ImageIO.read(Assets.class.getResource("Imgs/11.png"));
            
            menuBg = ImageIO.read(Assets.class.getResource("Imgs/fondo_inic.png"));
            playBtn = ImageIO.read(Assets.class.getResource("Imgs/play.png"));
            exitBtn = ImageIO.read(Assets.class.getResource("Imgs/exit.png"));
            restartBtn = ImageIO.read(Assets.class.getResource("Imgs/Reiniciar.png"));
            menuBtn = ImageIO.read(Assets.class.getResource("Imgs/Regresar.png"));
            
            barEmpty = ImageIO.read(Assets.class.getResource("Imgs/V_0.png"));
            bar15 = ImageIO.read(Assets.class.getResource("Imgs/V_15.png"));
            bar25 = ImageIO.read(Assets.class.getResource("Imgs/V_25.png"));
            bar50 = ImageIO.read(Assets.class.getResource("Imgs/V_50.png"));
            bar75 = ImageIO.read(Assets.class.getResource("Imgs/V_75.png"));
            bar90 = ImageIO.read(Assets.class.getResource("Imgs/V_90.png"));
            bar100 = ImageIO.read(Assets.class.getResource("Imgs/V_100.png"));
            
            emptyHeart = ImageIO.read(Assets.class.getResource("Imgs/corazon1.png"));
            fullHeart = ImageIO.read(Assets.class.getResource("Imgs/corazon.png"));
            settingsIcon = ImageIO.read(Assets.class.getResource("Imgs/configuracion.png"));
            pauseIcon = ImageIO.read(Assets.class.getResource("Imgs/pausa.png"));
            resumeIcon = ImageIO.read(Assets.class.getResource("Imgs/reanudar.png"));
            soundOnIcon = ImageIO.read(Assets.class.getResource("Imgs/sonido.png"));
            soundOffIcon = ImageIO.read(Assets.class.getResource("Imgs/sin_sonido.png"));
            cancelIcon = ImageIO.read(Assets.class.getResource("Imgs/cancelar.png"));
            homeIcon = ImageIO.read(Assets.class.getResource("Imgs/menu.png"));
            coinImage = ImageIO.read(Assets.class.getResource("Imgs/Gema.png"));
            finalBg = ImageIO.read(Assets.class.getResource("Imgs/Final.png"));

            // Level Backgrounds
            level1Bg = ImageIO.read(Assets.class.getResource("Imgs/mar1/FondoMarC.jpg"));
            level2Bg = ImageIO.read(Assets.class.getResource("Imgs/bosque2/FondoBosque.png"));
            level3Bg = ImageIO.read(Assets.class.getResource("Imgs/bosquehelado3/FondoHelado.png"));
            level4Bg = ImageIO.read(Assets.class.getResource("Imgs/zen4/FondoZen.jpg"));
            level5Bg = ImageIO.read(Assets.class.getResource("Imgs/mistico5/FondoMistico.png"));

            // Level 1 Assets
            seaPl1 = ImageIO.read(Assets.class.getResource("Imgs/mar1/Pl_Mar1.png"));
            seaPl2 = ImageIO.read(Assets.class.getResource("Imgs/mar1/Pl_Mar2.png"));
            seaPl3 = ImageIO.read(Assets.class.getResource("Imgs/mar1/Pl_Mar3.png"));
            seaPl4 = ImageIO.read(Assets.class.getResource("Imgs/mar1/Pl_Mar4.png"));
            seaPl5 = ImageIO.read(Assets.class.getResource("Imgs/mar1/Pl_Mar5.png"));
            seaPl6 = ImageIO.read(Assets.class.getResource("Imgs/mar1/Pl_Mar6.png"));
            seaPl7 = ImageIO.read(Assets.class.getResource("Imgs/mar1/Pl_Mar7.png"));

            // Level 2 Assets
            forestPl1 = ImageIO.read(Assets.class.getResource("Imgs/bosque2/plBosque1.png"));
            forestPl2 = ImageIO.read(Assets.class.getResource("Imgs/bosque2/plBosque2.png"));
            forestPl3 = ImageIO.read(Assets.class.getResource("Imgs/bosque2/plBosque3.png"));
            forestPl4 = ImageIO.read(Assets.class.getResource("Imgs/bosque2/plBosque4.png"));
            forestPl5 = ImageIO.read(Assets.class.getResource("Imgs/bosque2/plBosque5.png"));
            forestBranch = ImageIO.read(Assets.class.getResource("Imgs/bosque2/RamaBosque.png"));
            forestPl7 = ImageIO.read(Assets.class.getResource("Imgs/bosque2/plataforma_Bosque1.png"));

            // Level 3 Assets
            icePl1 = ImageIO.read(Assets.class.getResource("Imgs/bosquehelado3/PlHelada1.png"));
            icePl2 = ImageIO.read(Assets.class.getResource("Imgs/bosquehelado3/PlHelada2.png"));
            icePl3 = ImageIO.read(Assets.class.getResource("Imgs/bosquehelado3/PlHelada3.png"));
            icePl4 = ImageIO.read(Assets.class.getResource("Imgs/bosquehelado3/PlHelada4.png"));
            icePl5 = ImageIO.read(Assets.class.getResource("Imgs/bosquehelado3/PlHelada5.png"));
            icePl6 = ImageIO.read(Assets.class.getResource("Imgs/bosquehelado3/PlHelada6.png"));
            icePl7 = ImageIO.read(Assets.class.getResource("Imgs/bosquehelado3/PlHelada7.png"));
            icePl8 = ImageIO.read(Assets.class.getResource("Imgs/bosquehelado3/PlHelada8.png"));
            icePl9 = ImageIO.read(Assets.class.getResource("Imgs/bosquehelado3/PlHelada11.png"));

            // Level 4 Assets
            zenPl1 = ImageIO.read(Assets.class.getResource("Imgs/zen4/Pl_Zen1.png"));
            zenPl2 = ImageIO.read(Assets.class.getResource("Imgs/zen4/Pl_Zen2.png"));
            zenPl3 = ImageIO.read(Assets.class.getResource("Imgs/zen4/Pl_Zen3.png"));
            zenPl4 = ImageIO.read(Assets.class.getResource("Imgs/zen4/Pl_Zen4.png"));
            zenPl5 = ImageIO.read(Assets.class.getResource("Imgs/zen4/Pl_Zen5.png"));
            zenPl6 = ImageIO.read(Assets.class.getResource("Imgs/zen4/Pl_Zen6.png"));
            zenPl7 = ImageIO.read(Assets.class.getResource("Imgs/zen4/Pl_Zen7.png"));

            // Level 5 Assets
            mysticPl1 = ImageIO.read(Assets.class.getResource("Imgs/mistico5/plMistico1.png"));
            mysticPl2 = ImageIO.read(Assets.class.getResource("Imgs/mistico5/plMistico2.png"));
            mysticPl3 = ImageIO.read(Assets.class.getResource("Imgs/mistico5/plMistico3.png"));
            mysticPl4 = ImageIO.read(Assets.class.getResource("Imgs/mistico5/plMistico4.png"));
            mysticPl5 = ImageIO.read(Assets.class.getResource("Imgs/mistico5/plMistico5.png"));
            mysticPl6 = ImageIO.read(Assets.class.getResource("Imgs/mistico5/plMistico6.png"));
            mysticPl7 = ImageIO.read(Assets.class.getResource("Imgs/mistico5/plMistico7.png"));

            System.out.println("Images loaded successfully.");
        } catch (IOException e) {
            System.err.println("Error loading images. Check file paths.");
            e.printStackTrace();
        }
    }

    public static void playMusic() {
        if (backgroundMusic != null) {
            if (!backgroundMusic.isRunning()) {
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            }
        }
    }

    public static void stopMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
    }
}