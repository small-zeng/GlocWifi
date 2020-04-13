package com.indoor.tools;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiAdmin {
	
	private int current_state = -1;
	//定义一个WifiManager对象
	private WifiManager myWifiManager;
	//扫描出的网络连接列表
	private List<ScanResult> myWifiList;
	
	public WifiAdmin(Context context){
		//取得WifiManager对象
		myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		//取得WifiInfo对象
	}
	
	//打开wifi
	public void openWifi(){
		current_state = checkState();
		if (current_state == 3)
			return;
		if(!myWifiManager.isWifiEnabled()){
			myWifiManager.setWifiEnabled(true);
		}
		System.out.println("openWifi");
	}
	
	//关闭wifi
	public void closeWifi(){
		if(current_state == 3)
			return;
		if(myWifiManager.isWifiEnabled()){
			myWifiManager.setWifiEnabled(false);
		}
		System.out.println("closeWifi");	
	}
	
	public void startScan(){
//		myWifiList.clear();
		myWifiManager.startScan();
		//得到扫描结果
		myWifiList=myWifiManager.getScanResults();
//		System.out.println("startScan");
	}
	
    public int checkState() {
        return myWifiManager.getWifiState();  
    }  
	
	//得到网络列表
	public List<ScanResult> getWifiList(){
		return myWifiList;
	}
	
	
}
