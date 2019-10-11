package ru.darkkeks.pixel;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Constants {

    public static final int HEIGHT = 400;
    public static final int WIDTH = 1590;

    public static final int PIXEL_COUNT = Constants.HEIGHT * Constants.WIDTH;


    public static final String[] COLOR_MAP = new String[]{
            "#FFFFFF", "#C2C2C2", "#858585", "#474747", "#000000", "#3AAFFF", "#71AAEB", "#4a76a8", "#074BF3",
            "#5E30EB", "#FF6C5B", "#FE2500", "#FF218B", "#99244F", "#4D2C9C", "#FFCF4A", "#FEB43F", "#FE8648",
            "#FF5B36", "#DA5100", "#94E044", "#5CBF0D", "#C3D117", "#FCC700", "#D38301"
    };

    public static final Color[] RGB_MAP = new Color[COLOR_MAP.length];

    public static final Map<Color, Integer> COLOR_BY_ID = new HashMap<>();
    public static final Map<Integer, Color> COLOR_BY_RGB = new HashMap<>();

    static {
        for (int i = 0; i < COLOR_MAP.length; ++i) {
            RGB_MAP[i] = hex2Rgb(COLOR_MAP[i]);
            COLOR_BY_ID.put(RGB_MAP[i], i);
            COLOR_BY_RGB.put(RGB_MAP[i].getRGB(), RGB_MAP[i]);
        }
    }

    private static Color hex2Rgb(String colorStr) {
        return new Color(
                Integer.valueOf(colorStr.substring(1, 3), 16),
                Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16));
    }

    public static int getColorId(Color color) {
        return COLOR_BY_ID.get(color);
    }

    public static Color getColorIdByRGB(int rgb) {
        return COLOR_BY_RGB.get(rgb);
    }

    public static Color charToColor(char c) {
        if(c >= '0' && c <= '9') {
            return RGB_MAP[c - '0'];
        } else {
            return RGB_MAP[c - 'a' + 10];
        }
    }

    public static boolean checkRange(int x, int y) {
        return x >= 0 && y >= 0 && x < WIDTH && y < HEIGHT;
    }
}
