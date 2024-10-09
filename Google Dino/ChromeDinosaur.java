import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class ChromeDinosaur extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 750;
    int boardHeight = 250;

    // Images 
    Image dinosaurImg;
    Image dinosaurDeadImg;
    Image dinosaurJumpImg;
    Image cactus1Img;
    Image cactus2Img;
    Image cactus3Img;
    Image trackImg;

    class Block {
        int x;
        int y;
        int width;
        int height;
        Image img;

        Block(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }
    }

    // Dinosaur
    int dinosaurWidth = 44;
    int dinosaurHeight = 47;
    int dinosaurX = 50;
    int dinosaurY = boardHeight - dinosaurHeight - 50;

    Block dinosaur;
    int trackWidth = 900;
    int trackHeight = 17;
    int trackX = 10;
    int trackY = boardHeight - 60;
    Block track;

    // Cactus
    int cactus1Width = 17;
    int cactus2Width = 35;
    int cactus3Width = 51;
    int cactusHeight = 35;
    int cactusX = 750;
    int cactusY = boardHeight - cactusHeight;
    ArrayList<Block> cactusArray;

    // Physics
    int velocityX = -6; // Cactus moving left speed
    int velocityY = 0;  // Dinosaur jump speed
    int gravity = 1;

    // Track / Ground
    int groundHeight = 10; // Height of the ground/track

    boolean gameOver = false;
    int score = 0;

    Timer gameLoop;
    Timer placeCactusTimer;

    public ChromeDinosaur() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        dinosaurImg = new ImageIcon(getClass().getResource("./img/dino-run.gif")).getImage();
        dinosaurDeadImg = new ImageIcon(getClass().getResource("./img/dino-dead.png")).getImage();
        dinosaurJumpImg = new ImageIcon(getClass().getResource("./img/dino-jump.png")).getImage();
        cactus1Img = new ImageIcon(getClass().getResource("./img/cactus1.png")).getImage();
        cactus2Img = new ImageIcon(getClass().getResource("./img/cactus2.png")).getImage();
        cactus3Img = new ImageIcon(getClass().getResource("./img/cactus3.png")).getImage();
        trackImg = new ImageIcon(getClass().getResource("./img/track.png")).getImage();
        // Dinosaur
        dinosaur = new Block(dinosaurX, dinosaurY, dinosaurWidth, dinosaurHeight, dinosaurImg);
        track = new Block(trackX, trackY, trackWidth, trackHeight, trackImg);
        // Cactus
        cactusArray = new ArrayList<>();

        // Game timer
        gameLoop = new Timer(1000 / 60, this); // 1000/60 = 60 frames per second
        gameLoop.start();

        // Place cactus timer
        placeCactusTimer = new Timer(800, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeCactus();
            }
        });
        placeCactusTimer.start();
    }

    void placeCactus() {
        if (gameOver) {
            return;
        }

        double placeCactusChance = Math.random(); // 0 - 0.999999
        if (placeCactusChance > .80) { // 10% chance of cactus3
            Block cactus = new Block(cactusX, cactusY - 50, cactus3Width, cactusHeight, cactus3Img);
            cactusArray.add(cactus);
        } else if (placeCactusChance > .70) { // 20% chance of cactus2
            Block cactus = new Block(cactusX, cactusY - 50, cactus2Width, cactusHeight, cactus2Img);
            cactusArray.add(cactus);
        } else if (placeCactusChance > .50) { // 20% chance of cactus1
            Block cactus = new Block(cactusX, cactusY - 50, cactus1Width, cactusHeight, cactus1Img);
            cactusArray.add(cactus);
        }
        
        if (cactusArray.size() > 5) {
            cactusArray.remove(0); // Remove the first cactus from ArrayList
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Draw ground/track
        g.drawImage(track.img, track.x, track.y, track.width, track.height, null);
 // Track rectangle

        // Dinosaur
        g.drawImage(dinosaur.img, dinosaur.x, dinosaur.y, dinosaur.width, dinosaur.height, null);

        // Cactus
        for (Block cactus : cactusArray) {
            g.drawImage(cactus.img, cactus.x, cactus.y, cactus.width, cactus.height, null);
        }

        // Score
        g.setColor(Color.red);
        g.setFont(new Font("Courier", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + score, 10, 35);
        } else {
            g.drawString(String.valueOf(score), 10, 35);
        }
    }

    public void move() {
        // Dinosaur
        velocityY += gravity;
        dinosaur.y += velocityY;

        if (dinosaur.y > dinosaurY) { // Stop the dinosaur from falling past the ground
            dinosaur.y = dinosaurY;
            velocityY = 0;
            dinosaur.img = dinosaurImg;
        }

        // Cactus
        for (Block cactus : cactusArray) {
            cactus.x += velocityX;

            if (collision(dinosaur, cactus)) {
                gameOver = true;
                dinosaur.img = dinosaurDeadImg;
            }
        }

        // Score
        score++;
    }

    boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&   // a's top left corner doesn't reach b's top right corner
               a.x + a.width > b.x &&   // a's top right corner passes b's top left corner
               a.y < b.y + b.height &&  // a's top left corner doesn't reach b's bottom left corner
               a.y + a.height > b.y;    // a's bottom left corner passes b's top left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placeCactusTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            // JUMP
            if (dinosaur.y == dinosaurY) {
                velocityY = -12;
                dinosaur.img = dinosaurJumpImg;
            }

            if (gameOver) {
                // Restart game by resetting conditions
                dinosaur.y = dinosaurY;
                dinosaur.img = dinosaurImg;
                velocityY = 0;
                cactusArray.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placeCactusTimer.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
