package main.java;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;

public class WheelOfFortune extends JPanel {

    private static final int MIN_DELAY = 5;
    private static final int RADIUS = 350;
    private static final double ANGULAR_TORQUE= 0.3;
    private double angle = 0;
    private final int rotations = 7;
    private double finalAngle;
    private double finalAngleClamped;
    private double angularVelocity;
    private Student winner;
    private Ticker ticker;
    private final BufferedImage master;
    private BufferedImage rotated;

    private AudioManager audio;

    private ArrayList<Student> students;
    private double totalStudentWeight;

    public double nextClickAngle = 0.0;
    public int nextClickIndex = -1;

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
        boolean flag = true;
        File selectedFile = new File("resources/studenci.txt");
        while (flag) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            int result = fileChooser.showOpenDialog(frame);

            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                System.out.println("Selected file: " + selectedFile.getAbsolutePath());
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                var panel = new JPanel();
                panel.setLayout(new BorderLayout());
                WheelOfFortune wf = new WheelOfFortune(selectedFile);
                frame.setLayout(new GridLayout(1, 2));
                panel.add(wf);
                frame.add(panel);

                frame.add(new JTextArea("<---"));
                JButton zakrecButton = new JButton("Zakręć kołem");
                zakrecButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Random random = new Random();
                        wf.finalAngleClamped = random.nextDouble(2 * Math.PI);
                        wf.finalAngle = wf.finalAngleClamped + 2*Math.PI*wf.rotations;

                        wf.angularVelocity = Math.sqrt(2*ANGULAR_TORQUE*wf.finalAngle);
                        wf.ticker = new Ticker();
                        // Check who is the winner
                        double weightRange = wf.finalAngleClamped/(2*Math.PI) * wf.totalStudentWeight;
                        double weightSum = 0.0;
                        for (Student student : wf.students) {
                            weightSum +=student.weight;
                            if (weightSum > weightRange) {
                                wf.winner = student;
                                break;
                            }
                        }
                        //System.out.println(wf.winner.fullName);

                        //Ticker ticker = new Ticker();
                        wf.ticker.setCallback(someTicker -> {
                            if (wf.timestamp == null) {
                                wf.timestamp = Instant.now();
                            }
                            Duration runtime = Duration.between(wf.timestamp, Instant.now());
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
                            double currentVelocity = wf.angularVelocity - time * ANGULAR_TORQUE;
                            wf.angle = currentVelocity > 0.0 ? time * (wf.angularVelocity - time * ANGULAR_TORQUE * 0.5) : wf.finalAngle;
                            wf.validateClick();
                            wf.rotated = wf.rotateImageByDegrees(wf.master, wf.angle);
                            wf.repaint();
                        });
                        wf.ticker.start();
                    }
                });
                panel.add(zakrecButton, BorderLayout.SOUTH);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                flag = false;
            } else System.exit(0);
        }

    }

    public WheelOfFortune(File file) {
        audio = new AudioManager();
        this.add(audio);


        students = Utils.wczytajPlik(file);
        totalStudentWeight = WheelGenerator.calculateTotalWeight(students);
        master = WheelGenerator.generate(RADIUS, students);
        rotated = rotateImageByDegrees(master, 0.0);


        //ticker.start();
    }

    public void validateClick() {
        if (angle > nextClickAngle) {
            nextClickIndex++;
            if (nextClickIndex >= students.size()) {
                nextClickIndex = 0;
            }
            nextClickAngle += students.get(nextClickIndex).angleInRadians(totalStudentWeight);
            audio.playSound("click.wav");
        }
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
            try {
                BufferedImage image = ImageIO.read(new File("resources/triangle.png"));
                g2d.drawImage(image, getWidth()-image.getWidth(), (getHeight()-image.getHeight())/2, this);

            } catch (IOException e) {
                e.printStackTrace();
            }
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