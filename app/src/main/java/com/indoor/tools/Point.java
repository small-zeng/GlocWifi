package com.indoor.tools;


public class Point {
    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point pos) {
        this.x = pos.x;
        this.y = pos.y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Point(" +
                x +
                "," + y +
                ')';
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Point)
            return (((Point)obj).x==(this.x)&& ((Point)obj).y==(this.y));
        return false;
    }

    @Override
    public int hashCode() {
        //哈希地址
        return x+y;
    }
}
