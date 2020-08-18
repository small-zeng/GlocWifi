package com.indoormap.map;

import java.util.ArrayList;


import com.indoormap.tools.UserData;

public class Floor {
	public static int floorID=5;//教九五楼
	public boolean reloadflag=false;
	private int countdown=0;
	private int countup=0;
	private int oldLocationX=0;
	private int oldLocationY=0;
	public Map map=new Map();
	public UserData userdata=new UserData();
	
//	public void floorcheck(Context context)
//	{
//		int index1;
//		int index2;
//		int level1;
//		int level2;
//		float min=10000;
//		ArrayList<String> intersectionMaclst = new ArrayList<String>();
//		userdata.wifiAdmin=new WifiAdmin(context);
//		for(int floor=1;floor<6;floor++)
//		{
//			userdata.GetUserWifiVector();
//			map.createMap(floor);
//			for(int i=0;i<PublicData.MapWidth;i++)
//			{
//				for(int j=0;j<PublicData.MapHeight;j++)
//				{
//					float sum=0;
//					if(Map.binaryMap[i][j]==1){
//						intersectionMaclst = intersectionOfArrayList(Map.xwifiMap[i][j][2],userdata.userWifiMacList);
//						if(intersectionMaclst.size()>0)
//						{
//							for(String mac : intersectionMaclst)
//							{
//								index1 = Map.xwifiMap[i][j][2].indexOf(mac);
//								index2 = userdata.userWifiMacList.indexOf(mac);
//								level1=Integer.parseInt(Map.xwifiMap[i][j][1].get(index1));
//								level2=userdata.userWifiLevelList.get(index2);
//								sum=sum+(level1-level2)*(level1-level2);
//							}
//							sum=((float)sum/(float)intersectionMaclst.size());
//							if(sum<min)
//							{
//								min=sum;
//								floorID=floor;
//							}
//						}
//					}
//				}
//			}
//
//		}
//	}
	
	public ArrayList<String> intersectionOfArrayList(ArrayList<String> lst1 , ArrayList<String> lst2)
	{
		ArrayList<String> lst = new ArrayList<String> ();
		
		for(String str : lst1)	
		{
			if(lst2.contains(str))
			{
				lst.add(str);
			}
		}
		return lst;
	}
	
 	public void floorchange(int LocationX,int LocationY,int direction)
 	{	
 		if(LocationY!=oldLocationY)
 		{
 			countdown=0;
 			countup=0;
 		}
 		else 
 		{
 			reloadflag=false;
 			if(LocationY==144/2||LocationY==155/2||LocationY==166/2)
 			{
 				if(LocationX>580/2&LocationX<620/2&direction==2&oldLocationX!=LocationX)
 				{
 					countdown=countdown+1;
 					countup=countup-1;
 				}
 				if(LocationX>580/2&LocationX<620/2&direction==1&oldLocationX!=LocationX)
 				{
 					countup=countup+1;
 					countdown=countdown-1;
 				}
 				if(countdown>4)
 				{
 					floorID=floorID-1;
 					reloadflag=true;
 					countdown=-100;// 避免一直出现加地图的情况
 					countup=0;
 				}
 				if(countup>4)
 				{
 					floorID=floorID+1;
 					reloadflag=true;
 					countup=-100;// 避免一直出现加地图的情况
 					countdown=0;
 				}
 			}
 		}
 		oldLocationY=LocationY;
 		oldLocationX=LocationX;
 	}
 	
 	
}
