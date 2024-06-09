package main.java;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class WheelOfFortune extends JPanel {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }

        JFrame frame = new JFrame("Testing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new WheelOfFortune());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private BufferedImage master;
    private BufferedImage rotated;

    public WheelOfFortune() {
        try {
            master = ImageIO.read(new File("resources/pack.png"));
            rotated = rotateImageByDegrees(master, 0.0);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Timer timer = new Timer(40, new ActionListener() {
            private double angle = 0;
            private double delta = 1.0;

            @Override
            public void actionPerformed(ActionEvent e) {
                angle += delta;
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
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);

        int x = w / 2;
        int y = h / 2;

        at.rotate(rads, x, y);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, this);
        g2d.dispose();

        return rotated;
    }
}