package com.indoormap.tools;

public class AP {
    public String wifiMac;
    public String wifiSSID;
    public int wifiLevel;

    public AP(String mac,String ssid,int level){
        this.wifiMac=mac;
        this.wifiSSID=ssid;
        this.wifiLevel=level;
    }

    public String getWifiMac() {
        return wifiMac;
    }

    public void setWifiMac(String wifiMac) {
        this.wifiMac = wifiMac;
    }

    public String getWifiSSID() {
        return wifiSSID;
    }

    public void setWifiSSID(String wifiSSID) {
        this.wifiSSID = wifiSSID;
    }

    public int getWifiLevel() {
        return wifiLevel;
    }

    public void setWifiLevel(int wifiLevel) {
        this.wifiLevel = wifiLevel;
    }

    @Override
    public String toString() {
        return "AP{" +
                "wifiMac='" + wifiMac + '\'' +
                ", wifiSSID='" + wifiSSID + '\'' +
                ", wifiLevel=" + wifiLevel +
                '}';
    }
}
