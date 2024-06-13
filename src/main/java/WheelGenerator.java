package main.java;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.Arrays;

public class WheelGenerator {

    public static void antialiasing(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    public static double calculateTotalWeight (ArrayList<Student> students) {
        return students.stream().mapToDouble(s -> s.weight).sum();
    }

    public static BufferedImage generate(int radius, ArrayList<Student> students) {

        BufferedImage img = new BufferedImage(2*radius, 2*radius, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        antialiasing(g2d);
        double totalWeight = calculateTotalWeight(students);
        double totalAngle = 0.0;
        for (Student student : students){
            String text = student.fullName;
            double angle = student.angleInRadians(totalWeight);

            //Example font
            int testSize = 32;
            Font testFont = new Font(null, Font.PLAIN, testSize);
            FontMetrics metrics = g2d.getFontMetrics(testFont);
            int h = metrics.getHeight();
            int w = metrics.stringWidth(text);
            //Cool math
            double s = radius / (h / (2 * Math.tan(angle/2)) + w);

            //Scaled correctly font
            Font font = new Font(null, Font.PLAIN, (int) (testSize * s));
            metrics = g2d.getFontMetrics(font);

            //Rotating font
            AffineTransform affineTransform = new AffineTransform();
            affineTransform.rotate(-totalAngle -angle / 2, -radius + metrics.stringWidth(text), -(double) metrics.getHeight() / 4);
            Font rotatedFont = font.deriveFont(affineTransform);

            g2d.setFont(rotatedFont);

            //Drawing arc
            Arc2D.Float arc = new Arc2D.Float(Arc2D.PIE);
            arc.setFrame(0, 0, 2 * radius, 2 * radius);
            //System.out.printf("Kąt dla %s wynosi: %s stopni. Jest on rysowany od %s stopni\n", student.fullName, Math.toDegrees(angle), Math.toDegrees(totalAngle));
            arc.setAngleStart(Math.toDegrees(totalAngle));
            arc.setAngleExtent(Math.toDegrees(angle));
            g2d.setColor(student.color);
            g2d.fill(arc);
            g2d.setColor(Color.black);
            g2d.drawString(text, 2*radius - metrics.stringWidth(text), radius + metrics.getHeight()/4);

            totalAngle += angle;
        }

        g2d.dispose();

        return img;
    }
}
