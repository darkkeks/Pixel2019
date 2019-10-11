package ru.darkkeks.pixel;

import java.awt.*;
import java.util.Objects;

public class Pixel {

    public static final int PLACE = 0;
    public static final int BOMB = 1;
    public static final int FREEZE = 2;
    public static final int PIXEL = 3;

    private int x;
    private int y;
    private Color color;
    private int flag;

    private int groupId;
    private int userId;

    public Pixel(int x, int y, Color color, int flag) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.flag = flag;
    }

    public static Pixel place(int x, int y, Color color) {
        return new Pixel(x, y, color, PLACE);
    }

    public static Pixel unpack(int a, int b, int c) {
        int x, y, color, flag;
        x = a % Constants.WIDTH;
        a /= Constants.WIDTH;
        y = a % Constants.HEIGHT;
        a /= Constants.HEIGHT;
        color = a % Constants.COLOR_MAP.length;
        a /= Constants.COLOR_MAP.length;
        flag = a;

        Pixel result = new Pixel(x, y, Constants.RGB_MAP[color], flag);
        result.userId = b;
        result.groupId = c;
        return result;
    }

    public int pack() {
        return x + y * Constants.WIDTH +
                Constants.PIXEL_COUNT * (Constants.COLOR_BY_ID.get(color) + flag * Constants.COLOR_MAP.length);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Color getColor() {
        return color;
    }

    public int getFlag() {
        return flag;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pixel pixel = (Pixel) o;
        return x == pixel.x &&
                y == pixel.y &&
                flag == pixel.flag &&
                color.equals(pixel.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, color, flag);
    }

    @Override
    public String toString() {
        return "Pixel{" +
                "x=" + x +
                ", y=" + y +
                ", color=" + color +
                ", flag=" + flag +
                ", groupId=" + groupId +
                ", userId=" + userId +
                '}';
    }
}