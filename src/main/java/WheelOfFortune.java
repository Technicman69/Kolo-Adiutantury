package main.java;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class WheelOfFortune extends JPanel {

    private static final int MIN_DELAY = 5;
    private static final int RADIUS = 350;
    private static final double ANGULAR_TORQUE= 0.6;
    private double angle = 0;
    private final int rotations = 12;
    private final double finalAngle;
    private final double finalAngleClamped;
    private double angularVelocity;
    private Student winner;
    private Ticker ticker = new Ticker();
    private final BufferedImage master;
    private BufferedImage rotated;

    public static class Ticker {

        public interface Callback {
            public void didTick(Ticker ticker);
        }

        private Timer timer;

        private Callback callback;

        public void setCallback(Callback tick) {
            this.callback = tick;
        }

        public void start() {
            if (timer != null) {
                return;
            }
            timer = new Timer(MIN_DELAY, e -> {
                if (callback == null) {
                    return;
                }
                callback.didTick(Ticker.this);
            });
            timer.start();
        }

        public void stop() {
            if (timer == null) {
                return;
            }
            timer.stop();
            timer = null;
        }

    }

    private Instant timestamp;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            throw new RuntimeException("Błąd przy wczytywaniu systemowych stylów: ", ex);
        }

        JFrame frame = new JFrame("Testing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        WheelOfFortune wf = new WheelOfFortune();
        frame.add(wf);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        JButton zakręćButton = new JButton("Zakręć kołem");
        zakręćButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                wf.ticker.start();
            }
        });
        frame.add(zakręćButton, BorderLayout.SOUTH);
    }

    public WheelOfFortune() {
        Random random = new Random();
        finalAngleClamped = random.nextDouble(2 * Math.PI);
        finalAngle = finalAngleClamped + 2*Math.PI*rotations;

        angularVelocity = Math.sqrt(2*ANGULAR_TORQUE*finalAngle);

        ArrayList<Student> students = Utils.wczytajPlik("resources/studenci.txt");
        master = WheelGenerator.generate(RADIUS, students);
        rotated = rotateImageByDegrees(master, 0.0);

        // Check who is the winner
        double weightRange = finalAngleClamped/(2*Math.PI) * WheelGenerator.calculateTotalWeight(students);
        double weightSum = 0.0;
        for (Student student : students) {
            weightSum +=student.weight;
            if (weightSum > weightRange) {
                winner = student;
                break;
            }
        }
        System.out.println(winner.fullName);

        //Ticker ticker = new Ticker();
        ticker.setCallback(someTicker -> {
            if (timestamp == null) {
                timestamp = Instant.now();
            }
            Duration runtime = Duration.between(timestamp, Instant.now());
            double time = runtime.toMillis() * 0.001;
            /*timestamp = Instant.now();
            double deltaTime = (double) runtime.toNanos() * 0.00_000_000_1;
            //System.out.println(deltaTime);

            // Physics O_O
            angularVelocity -= ANGULAR_TORQUE * deltaTime;
            if (angularVelocity < 0.0) {
                angularVelocity = 0.0;
            }
            angle += angularVelocity * deltaTime;*/
            double currentVelocity = angularVelocity - time * ANGULAR_TORQUE;
            angle = currentVelocity > 0.0 ? time * (angularVelocity - time * ANGULAR_TORQUE * 0.5) : finalAngle;
            rotated = rotateImageByDegrees(master, angle);
            repaint();
        });
        //ticker.start();
    }

    @Override
    public Dimension getPreferredSize() {
        return master == null
                ? new Dimension(200, 200)
                : new Dimension(master.getWidth(), master.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (rotated != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            int x = (getWidth() - rotated.getWidth()) / 2;
            int y = (getHeight() - rotated.getHeight()) / 2;
            g2d.drawImage(rotated, x, y, this);
            g2d.dispose();
        }
    }

    public BufferedImage rotateImageByDegrees(BufferedImage img, double angle) {

        double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
        int w = img.getWidth();
        int h = img.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        WheelGenerator.antialiasing(g2d);
        AffineTransform at = new AffineTransform();
        at.translate((double) (newWidth - w) / 2, (double) (newHeight - h) / 2);

        int x = w / 2;
        int y = h / 2;

        at.rotate(angle, x, y);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, this);
        g2d.dispose();

        //System.out.printf("angle: %s, final_angle: %s\n", angle, finalAngle);

        return rotated;
    }
}