package ru.darkkeks.pixel.graphics;

import ru.darkkeks.pixel.Constants;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Template {

    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    private BufferedImage image;

    public Template(BufferedImage image) {
        this.image = image;
        normalizeColors();
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public BufferedImage getImage() {
        return image;
    }

    public Color getColor(int x, int y) {
        int rgb = image.getRGB(x, y);
        return Constants.getColorIdByRGB(rgb);
    }

    private int colorDist(Color a, Color b) {
        return (a.getRed() - b.getRed()) * (a.getRed() - b.getRed()) +
                (a.getGreen() - b.getGreen()) * (a.getGreen() - b.getGreen()) +
                (a.getBlue() - b.getBlue()) * (a.getBlue() - b.getBlue());
    }

    private void normalizeColors() {
        for (int x = 0; x < image.getWidth(); ++x) {
            for (int y = 0; y < image.getHeight(); ++y) {
                int rgb = image.getRGB(x, y);
                Color color = new Color(rgb, image.getAlphaRaster() != null);
                if(color.getAlpha() < 40) {
                    image.setRGB(x, y, TRANSPARENT.getRGB());
                } else {
                    Color best = null;
                    double bestDist = Integer.MAX_VALUE;
                    for (Color palette : Constants.RGB_MAP) {
                        int dist = colorDist(palette, color);
                        if (dist < bestDist) {
                            best = palette;
                            bestDist = dist;
                        }
                    }
                    assert best != null;
                    image.setRGB(x, y, best.getRGB());
                }
            }
        }


//        int[] rgb_data = image.getRaster().getPixels(0, 0, getWidth(), getHeight(), (int[])null);
//        boolean hasAlpha = image.getAlphaRaster() != null;

//        int pixelLen = (hasAlpha ? 4 : 3);
//        int current = 0;
//        for(int x = 0; x < getWidth(); ++x) {
//            for(int y = 0; y < getHeight(); ++y) {
//                if(!hasAlpha || rgb_data[pixelLen * current + 3] == 255) {
//                    byte closest = -1;
//                    double distance = Double.POSITIVE_INFINITY;
//                    for(byte i = 0; i < Constants.COLOR_MAP.length; ++i) {
//                        Color color = Constants.RGB_MAP[i];
//                        double dist = Math.pow(rgb_data[pixelLen * current] - color.getRed(), 2) +
//                                Math.pow(rgb_data[pixelLen * current + 1] - color.getGreen(), 2) +
//                                Math.pow(rgb_data[pixelLen * current + 2] - color.getBlue(), 2);
//                        if(dist < distance) {
//                            distance = dist;
//                            closest = i;
//                        }
//                    }
//                    image.setRGB(x, y, Constants.RGB_MAP[closest].getRGB());
//                } else {
//                    image.setRGB(x, y, TRANSPARENT.getRGB());
//                }
//                current++;
//            }
//        }
    }
}
