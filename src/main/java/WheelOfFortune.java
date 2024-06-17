package main.java;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

public class WheelOfFortune extends JPanel {

    private static final int MIN_DELAY = 5;
    private static final int RADIUS = 350;
    private static final double SCALING_FACTOR = 0.75;
    private static final double ANGULAR_TORQUE= 0.2137;
    private double angle = 0;
    private final int rotations = 4;
    private double finalAngle = 1.0;
    private double finalAngleClamped;
    private double angularVelocity = 1.0;
    private double currentVelocity = 1.0;
    private Student winner;
    private Ticker ticker;
    private final BufferedImage master;
    private BufferedImage rotated;
    private final Image triangle;
    private final Image background;

    private final AudioManager audio;

    private final ArrayList<Student> students;
    private final double totalStudentWeight;

    public double nextClickAngle;
    public int nextClickIndex;

    private Instant timestamp;
    private Instant timeSinceClick;
    private final double minMillisBetweenClick = 69.0;

    public static class Ticker {

        public interface Callback {
            void didTick(Ticker ticker);
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


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            throw new RuntimeException("Błąd przy wczytywaniu systemowych stylów: ", ex);
        }
        //System.out.println("Current dir is " + Paths.get("").toAbsolutePath());

        JFrame frame = new JFrame("Koło adiutantury");
        boolean flag = true;
        File selectedFile;// = new File("resources/studenci2.txt");
        while (flag) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory().getAbsoluteFile());
            int result = fileChooser.showOpenDialog(frame);

            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                System.out.println("Selected file: " + selectedFile.getAbsolutePath());

                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                var panel = new JPanel();
                panel.setLayout(new BorderLayout());
                WheelOfFortune wf = new WheelOfFortune(selectedFile);
//                frame.setLayout(new GridLayout(1, 2));
                panel.add(wf);
                frame.add(panel);

                JButton zakrecButton = new JButton("Zakręć kołem");
                zakrecButton.addActionListener(e -> {
                    Random random = new Random();
                    wf.angle = 0;
                    wf.finalAngleClamped = random.nextDouble() * 2 * Math.PI;
                    wf.finalAngle = wf.finalAngleClamped + 2*Math.PI*wf.rotations;

                    wf.angularVelocity = Math.sqrt(2*ANGULAR_TORQUE*wf.finalAngle);
                    wf.currentVelocity = wf.angularVelocity;
                    wf.ticker = new Ticker();

                    wf.nextClickAngle = 0.0;
                    wf.nextClickIndex = -1;
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
                    wf.timestamp = Instant.now();
                    wf.timeSinceClick = Instant.now();
                    wf.ticker.setCallback(someTicker -> {
                        wf.repaint();
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
                        wf.currentVelocity = wf.angularVelocity - time * ANGULAR_TORQUE;
                        wf.angle = wf.currentVelocity > 0.0 ? time * (wf.angularVelocity - time * ANGULAR_TORQUE * 0.5) : wf.finalAngle;
                        if (wf.currentVelocity <= 0.0) {
                            wf.currentVelocity = 0.0;
                            wf.ticker.stop();
                            wf.audio.playSound("fanfare.wav");
                            JOptionPane.showMessageDialog(wf, "Zwycięzcą zostaje:\n"+wf.winner.fullName + "!\nTwoje \"v\" poruszania się w stronę egzaminu dąży do 0, bo właśnie Cię z niego zwolniono!", "Wybrano zwycięzcę", JOptionPane.INFORMATION_MESSAGE);

                        }
                        wf.validateClick();
                        wf.rotated = wf.rotateImageByDegrees(wf.master, wf.angle);
                    });
                    wf.ticker.start();
                });
                panel.add(zakrecButton, BorderLayout.SOUTH);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setResizable(false);
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
        master = WheelGenerator.generate((int) (RADIUS/SCALING_FACTOR), students);
        rotated = rotateImageByDegrees(master, 0.0);
        triangle = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/triangle.png"));
        background = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/background.png"));

        //ticker.start();
    }

    public void validateClick() {
        if (angle > nextClickAngle) {
            nextClickIndex++;
            if (nextClickIndex >= students.size()) {
                nextClickIndex = 0;
            }
            nextClickAngle += students.get(nextClickIndex).angleInRadians(totalStudentWeight);
            double timeDelta = Duration.between(timeSinceClick, Instant.now()).toMillis();
            if (timeDelta > minMillisBetweenClick) {
                audio.playSound("click.wav");
                timeSinceClick = Instant.now();
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return master == null
                ? new Dimension(200, 200)
                : new Dimension(2*RADIUS+100, 2*RADIUS);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (rotated != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            int x = (getWidth() - rotated.getWidth()) / 2;
            int y = (getHeight() - rotated.getHeight()) / 2;

            g2d.drawImage(background, (getWidth()-background.getWidth(null))/2, (getHeight()-background.getHeight(null))/2, this);
            g2d.drawImage(rotated, x, y, this);
            //System.out.println("INPUT FILE:" + Toolkit.getDefaultToolkit().getImage(getClass().getResource("/triangle.png")).toString());
            g2d.drawImage(triangle, getWidth()-triangle.getWidth(null), (getHeight()-triangle.getHeight(null))/2, this);

            g2d.dispose();
        }
    }

    public BufferedImage rotateImageByDegrees(BufferedImage img, double angle) {

        //double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
        //int w = img.getWidth();
        //int newWidth = (int) Math.floor(w * cos + h * sin);
        //int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(2*RADIUS+100, 2*RADIUS, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        WheelGenerator.antialiasing(g2d);
        AffineTransform at = new AffineTransform();
        //at.translate(newWidth - w, newHeight - h);

        //int x = w / 2;
        //int y = h / 2;
        double scalar = angle/finalAngle;
        double totalScalingFactor = SCALING_FACTOR * (1 + scalar);

        at.translate(-RADIUS*scalar + 50, 0);
        at.rotate(angle, RADIUS, RADIUS);

        at.translate(-RADIUS*scalar, -RADIUS*scalar);
        at.scale(totalScalingFactor, totalScalingFactor);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, this);
        g2d.dispose();

        //System.out.printf("angle: %s, final_angle: %s\n", angle, finalAngle);

        return rotated;
    }
}