package com.indoormap.map;

import android.os.Environment;
import android.widget.ProgressBar;

import com.indoormap.tools.Point;
import com.indoormap.tools.availablePoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Map {
	public static Point map[][];
	public static int offset_x;     //地图下标偏移量
	public static int offset_y;
	public static int floor;        //地图对应楼层

	private ProgressBar probar;     //地图加载进度条指示

	public static ArrayList<availablePoint> availableArea;      //为地图中可以到达的点建立索引

	public Map(ProgressBar pb) {
		probar = pb;
	}

	public Map(){

	}

	public void createMap(int floorID) {
		initMap();
		availableArea = new ArrayList<availablePoint>();
		String dir = Environment.getExternalStorageDirectory() + File.separator + "Gloc"+ File.separator + floorID + File.separator + "map_200m.txt";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(dir)));
			String line;
			String[] str = null;
			String[] str1 = null;
			int P_x = 0;
			int P_y = 0;
			while ((line = br.readLine()) != null) {
				str = line.split(";");
				if (str[0].equals("UWB")) {
					str1 = str[1].split(",");
					P_x = Integer.parseInt(str1[0]);
					P_y = Integer.parseInt(str1[1]);
					map[P_x][P_y] = new Point(Integer.parseInt(str1[2]));
					if(map[P_x][P_y].freq>0){
						availableArea.add(new availablePoint(P_x,P_y));
					}
				}
				else{
					map[P_x][P_y].wifiRssiList.add(Integer.parseInt(str[4]));
					map[P_x][P_y].wifiMacList.add(str[2]);
				}
			}
			//print map for debug
			//printMap(floorID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		probar.setProgress(100);
	}

	private void initMap(){
		map = new Point[PublicData.MapWidth][PublicData.MapHeight];
		for(int i = 0 ; i < PublicData.MapWidth ; i++){
			for(int j = 0 ; j < PublicData.MapHeight ; j++){
				map[i][j] = new Point(0);
			}
		}
		System.out.println("map created");
		if(map[200][200]==null)
			System.out.println("point doesn't exist");
	}

	public void printMap(int floorID){
		System.out.println("start printing map ");
		for(int i = 0 ; i < PublicData.MapWidth ; i++){
			for(int j = 0 ; j < PublicData.MapHeight ; j++){
				int average = calcuateAverage(map[i][j].wifiRssiList);
				int variance = calcuateVariance(average,map[i][j].wifiRssiList);
				System.out.print(average+":"+variance+" ");
			}
		}
		System.out.println("map print complete");
	}

	private int calcuateAverage(ArrayList<Integer> arrayList){
		int sum = 0;
		for(int i = 0 ; i < arrayList.size() ; i++){
			sum = sum + arrayList.get(i);
		}
		return sum;
	}

	private int calcuateVariance(int average,ArrayList<Integer> arrayList ){
		int variance = 0;
		for(int i = 0 ; i < arrayList.size() ; i++){
			variance = variance + (arrayList.get(i) - average)*(arrayList.get(i) - average);
		}
		return variance;
	}

	public static boolean assertCoordinates(int x, int y)
	{
		if((x < 0)||(x >= PublicData.MapWidth)) return false;
		if((y < 0)||(y >= PublicData.MapHeight))return false;
		if(map[x][y].freq < 1) return false;
		return true;
	}
}
