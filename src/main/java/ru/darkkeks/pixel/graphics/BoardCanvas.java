package ru.darkkeks.pixel.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

public class BoardCanvas extends JPanel {

    private final int width;
    private final int height;
    private BufferedImage canvas;
    private final AffineTransform transform;

    private Template template;

    private float templateOpacity;

    public BoardCanvas(int width, int height, BufferedImage canvas) {
        this.width = width;
        this.height = height;
        this.canvas = canvas;
        this.transform = new AffineTransform();

        this.templateOpacity = 0.7f;

        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(canvas, transform, null);

        if(template != null) {
            RescaleOp filter = new RescaleOp(new float[]{1f, 1f, 1f, templateOpacity}, new float[4], null);
            BufferedImage filtered = filter.filter(template.getImage(), null);

            g2.drawImage(filtered, transform, null);
        }
    }

    public void updateBoard(BufferedImage board) {
        this.canvas = board;
        repaint();
    }

    public void setTemplate(Template template) {
        this.template = template;
        repaint();
    }

    public AffineTransform getTransform() {
        return transform;
    }

    public void setPixel(int x, int y, Color color) {
        canvas.setRGB(x, y, color.getRGB());
        repaint();
    }

    public BufferedImage getImage() {
        return canvas;
    }
}
