package com.indoormap.tools;

import java.util.ArrayList;
//地图中所有的点
public class Point {

    public int freq;        //该点在建图过程中被访问的次数。0代表从未到达，为障碍物
    public ArrayList<Integer> wifiRssiList;     //该点的WiFi信号强度列表 与wifiMacList一一对应
    public ArrayList<String>  wifiMacList;      //该点的WiFi Mac地址列表

    public Point(int freq){
        this.freq = freq;
        wifiRssiList = new ArrayList<Integer>();
        wifiMacList = new ArrayList<String>();
    }    
}