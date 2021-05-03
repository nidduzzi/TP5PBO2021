/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modulgame;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.Random;
import java.util.ArrayList;
import javax.sound.sampled.Mixer;

/**
 *
 * @author Fauzan
 */
public class Game extends Canvas implements Runnable {

    Window window;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    private int score_p1 = 0;
    private int score_p2 = 0;

    private int timer = 10;
    private int ellapsed = 0;
    private Random rng = new Random();
    private Thread thread;
    private boolean running = false;
    private String player1;
    private String player2;
    private Handler handler;
    private Difficulty difficulty;
    private Clip bgMusic;

    public enum STATE {
        Game,
        GameOver
    };

    public STATE gameState = STATE.Game;

    public Game(String player1, String player2, Difficulty difficulty) {
        window = new Window(WIDTH, HEIGHT, "Modul praktikum 5", this);
        this.player1 = player1;
        this.player2 = player2;
        this.difficulty = difficulty;
        handler = new Handler();
        this.addKeyListener(new KeyInput(handler, this));
        requestFocus();
        if (gameState == STATE.Game) {
            int speed = 0;
            switch (difficulty) {
                case Easy:
                    this.timer = 20;
                    speed = 2;
                    break;
                case Normal:
                    this.timer = 10;
                    speed = 5;
                    break;
                case Hard:
                    this.timer = 5;
                    speed = 7;
                    break;
            }
            handler.addObject(new Items(100, 150, ID.Item, 0, 20));
            handler.addObject(new Items(200, 350, ID.Item, 0, 20));
            handler.addObject(new Player(200, 200, ID.Player, 1, 50));
            int x = rng.nextInt(WIDTH);
            int y = rng.nextInt(HEIGHT);
            handler.addObject(new Enemy(clamp((Math.abs(x - 200) > 300) ? x : 500, 0, WIDTH), clamp((Math.abs(y - 200) > 300) ? y : 500, 0, HEIGHT), ID.Enemy, 0, 50, handler.object.getLast(), rng, speed));
            if (!player2.equals("")) {
                handler.addObject(new Player(250, 250, ID.Player, 2, 50));
            }
        }
    }

    public int getScore_winner() {
        return ((this.score_p1 >= this.score_p2) ? this.score_p1 : this.score_p2);
    }

    public String getUsername_winner() {
        return (this.score_p1 >= this.score_p2) ? this.player1 : this.player2;
    }

    public int getScore_p1() {
        return this.score_p1;
    }

    public String getUsername_p1() {
        return this.player1;
    }

    public int getScore_p2() {
        return this.score_p2;
    }

    public String getUsername_p2() {
        return this.player2;
    }

    public int getTime() {
        return this.ellapsed;
    }

    public synchronized void start() {
        thread = new Thread(this);
        thread.start();
        running = true;
    }

    public synchronized void stop() {
        try {
            thread.join();
            running = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long time = System.currentTimeMillis();
        int frames = 0;
        bgMusic = playSound("/bensound-endlessmotion.wav");
        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            while (delta >= 1) {
                tick();
                delta--;
            }
            if (running) {
                render();
                frames++;
            }

            if (System.currentTimeMillis() - time > 1000) {
                time += 1000;
                //System.out.println("FPS: " + frames);
                frames = 0;
                if (gameState == STATE.Game) {
                    if (this.timer > 0) {
                        --time;
                        --this.timer;
                        ++ellapsed;
                    } else {
                        gameState = STATE.GameOver;
                    }
                }
            }
        }
        stop();
    }

    private void tick() {
        handler.tick();
        boolean itemEmpty = true;
        if (gameState == STATE.Game) {
            ArrayList<GameObject> playerObjects = new ArrayList();
            for (int i = 0; i < handler.object.size(); i++) {
                if (handler.object.get(i).getId() == ID.Player) {
                    playerObjects.add(handler.object.get(i));
                }
            }
            if (!(playerObjects.isEmpty())) {
                for (int i = 0; i < handler.object.size(); ++i) {
                    if (handler.object.get(i).getId() == ID.Item) {
                        itemEmpty = false;

                        if (checkCollision(playerObjects.get(0), handler.object.get(i))) {
                            playSound("/Eat.wav");
                            handler.removeObject(handler.object.get(i));
                            score_p1 = score_p1 + rng.nextInt(10);
                            timer = timer + rng.nextInt(5);
                        } else if (playerObjects.size() > 1) {
                            if (checkCollision(playerObjects.get(1), handler.object.get(i))) {
                                playSound("/Eat.wav");
                                handler.removeObject(handler.object.get(i));
                                score_p2 = score_p2 + rng.nextInt(10);
                                timer = timer + rng.nextInt(5);
                            }
                        }
                    } else if (handler.object.get(i).getId() == ID.Enemy) {
                        if (checkCollision(playerObjects.get(0), handler.object.get(i))) {
                            playSound("/Eat.wav");
                            gameState = STATE.GameOver;
                        }
                        if (playerObjects.size() > 1) {
                            if (checkCollision(playerObjects.get(1), handler.object.get(i))) {
                                playSound("/Eat.wav");
                                gameState = STATE.GameOver;
                            }
                        }
                    }
                }
            } else {
                System.err.println("Error, Player Null");
            }
            if (itemEmpty) {
                int x = clamp(rng.nextInt(WIDTH), 0, WIDTH - 70);
                int y = clamp(rng.nextInt(HEIGHT), 0, HEIGHT - 90);
//                System.out.println("x: " + Integer.toString(x) + ", " + "y: " + Integer.toString(y));
                handler.addObject(new Items(x, y, ID.Item, 0, 20));
            }
        }
    }

    public static boolean checkCollision(GameObject a, GameObject b) {
        boolean result = false;

        int sizePlayer = a.getSize();
        int sizeItem = b.getSize();

        int playerLeft = a.x;
        int playerRight = a.x + sizePlayer;
        int playerTop = a.y;
        int playerBottom = a.y + sizePlayer;

        int itemLeft = b.x;
        int itemRight = b.x + sizeItem;
        int itemTop = b.y;
        int itemBottom = b.y + sizeItem;

        if ((playerRight > itemLeft)
                && (playerLeft < itemRight)
                && (itemBottom > playerTop)
                && (itemTop < playerBottom)) {
            result = true;
        }

        return result;
    }

    private void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();

        g.setColor(Color.decode("#F1f3f3"));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        if (gameState == STATE.Game) {
            handler.render(g);

            Font currentFont = g.getFont();
            Font newFont = currentFont.deriveFont(currentFont.getSize() * 1.4F);
            g.setFont(newFont);

            g.setColor(Color.BLACK);
            g.drawString("Total Time: " + Integer.toString(ellapsed), 20, 20);
            g.setColor(Color.BLACK);
            g.drawString("Player 1 Score: " + Integer.toString(score_p1), 20, 35);
            if (!this.player2.equals("")) {
                g.setColor(Color.BLACK);
                g.drawString("Player 2 Score: " + Integer.toString(score_p2), 20, 50);
            }
            g.setColor(Color.BLACK);
            g.drawString("Time: " + Integer.toString(timer), WIDTH - 120, 20);
        } else {
            Font currentFont = g.getFont();
            Font newFont = currentFont.deriveFont(currentFont.getSize() * 3F);
            g.setFont(newFont);

            g.setColor(Color.BLACK);
            g.drawString("GAME OVER", WIDTH / 2 - 120, HEIGHT / 2 - 30);

            currentFont = g.getFont();
            Font newScoreFont = currentFont.deriveFont(currentFont.getSize() * 0.5F);
            g.setFont(newScoreFont);
            g.setColor(Color.BLACK);
            g.drawString("Total Time: " + Integer.toString(ellapsed), WIDTH / 2 - 55, HEIGHT / 2 + 20);
            g.setColor(Color.BLACK);
            g.drawString("Player 1 Score: " + Integer.toString(score_p1), WIDTH / 2 - 75, HEIGHT / 2 - 10);
            if (!this.player2.equals("")) {
                g.setColor(Color.BLACK);
                g.drawString("Player 2 Score: " + Integer.toString(score_p2), WIDTH / 2 - 75, HEIGHT / 2 + 5);
            }
            g.setColor(Color.BLACK);
            g.drawString("Press Space to Continue", WIDTH / 2 - 100, HEIGHT / 2 + 35);

        }

        g.dispose();
        bs.show();
    }

    public static int clamp(int var, int min, int max) {
        if (var >= max) {
            return var = max;
        } else if (var <= min) {
            return var = min;
        } else {
            return var;
        }
    }

    public void close() {
        running = false;
        if (bgMusic != null) {
            bgMusic.stop();
        }
        window.CloseWindow();
    }

    public Clip playSound(String filename) {
        try {
            // Open an audio input stream.
            URL url = this.getClass().getResource(filename);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            // Get a sound clip resource.
            Clip clip = AudioSystem.getClip();
            // Open audio clip and load samples from the audio input stream.
            clip.open(audioIn);
            clip.start();
            return clip;
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        return null;
    }
}
