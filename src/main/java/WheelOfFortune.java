package main.java;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class WheelOfFortune extends JPanel {

    private static final double FPS = 50.0;
    private static final int RADIUS = 350;
    private static final double ANGULAR_VELOCITY = 50.0;

    private static final double DELTA_TIME = 1/FPS;

    private final BufferedImage master;
    private BufferedImage rotated;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            throw new RuntimeException("Błąd przy wczytywaniu systemowych stylów: ", ex);
        }

        JFrame frame = new JFrame("Testing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new WheelOfFortune());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public WheelOfFortune() {
        Student[] students = new Student[] {
                new Student("Jan Uziembło", 1, Color.green),
                new Student("Rafał Pyda", 2, Color.yellow),
                new Student("Karol Pacwa", 1, Color.blue)
        };
        master = WheelGenerator.generate(RADIUS, students);
        rotated = rotateImageByDegrees(master, 0.0);

        Timer timer = new Timer((int) (DELTA_TIME*1000), new ActionListener() {
            private double angle = 0;

            @Override
            public void actionPerformed(ActionEvent e) {

                angle += ANGULAR_VELOCITY * DELTA_TIME;
                rotated = rotateImageByDegrees(master, angle);
                repaint();
            }
        });
        timer.start();
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

        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
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

        at.rotate(rads, x, y);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, this);
        g2d.dispose();

        return rotated;
    }
}