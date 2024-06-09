package main.java;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.geom.Arc2D;

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

    public static BufferedImage generate(int radius) {

        BufferedImage img = new BufferedImage(2*radius, 2*radius, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        antialiasing(g2d);

        String text = "Jan Uziembło";
        double angle = 45.0d;

        //Example font
        int testSize = 32;
        Font testFont = new Font(null, Font.PLAIN, testSize);
        FontMetrics metrics = g2d.getFontMetrics(testFont);
        int h = metrics.getHeight();
        int w = metrics.stringWidth(text);
        //Cool math
        double s = radius/(h/(2*Math.tan(Math.toRadians(angle/2)))+w);

        //Scaled correctly font
        System.out.println((testSize * s));
        Font font = new Font(null, Font.PLAIN, (int) (testSize * s));
        metrics = g2d.getFontMetrics(font);

        //Rotating font
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate(Math.toRadians(-angle/2), -radius + metrics.stringWidth(text), - (double) metrics.getHeight() /4);
        Font rotatedFont = font.deriveFont(affineTransform);

        g2d.setFont(rotatedFont);

        //Drawing arc
        Arc2D.Float arc = new Arc2D.Float(Arc2D.PIE);
        arc.setFrame(0, 0, 2*radius, 2*radius);
        arc.setAngleStart(0);
        arc.setAngleExtent(angle);
        g2d.setColor(Color.green);
        g2d.fill(arc);
        g2d.setColor(Color.black);
        g2d.drawString(text, 2*radius - metrics.stringWidth(text), radius + metrics.getHeight()/4);

        g2d.dispose();

        return img;
    }
}
