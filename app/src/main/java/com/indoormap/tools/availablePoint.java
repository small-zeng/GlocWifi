package com.indoormap.tools;

public class availablePoint {
    public int x;
    public int y;
    public int speed;
    public int direction;
    public availablePoint(int x, int y){
        this.x = x;
        this.y = y;
    }
    public availablePoint(int x, int y, int speed, int direction){
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.direction = direction;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof availablePoint)
            return (((availablePoint)obj).x==(this.x)&& ((availablePoint)obj).y==(this.y));
        return false;
    }
}
