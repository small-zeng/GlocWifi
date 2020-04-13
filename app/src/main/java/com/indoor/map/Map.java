package com.indoor.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.os.Environment;

public class Map {
	
	private int boundary1;
	private int boundary2;
	private int boundary3;
	private int boundary4;
	public int pathNumber;
	ArrayList<Integer> wifiLevelList = new ArrayList<Integer> ();
	ArrayList<String>  wifiMacList = new ArrayList<String>();
	ArrayList<String>  wifiApList = new ArrayList<String>();
	ArrayList<Integer> listOfWifiVectorsLength = new ArrayList<Integer> ();
	public static int binaryMap[][] = new int[PublicData.MapWidth][PublicData.MapHeight];
	public static ArrayList<String> xwifiMap[][][] = new ArrayList[PublicData.MapWidth][PublicData.MapHeight][4] ;
	public static ArrayList<String> relativeWifiMap[][][] = new ArrayList[PublicData.MapWidth][PublicData.MapHeight][9] ;
	public int file_count=0;

	public void createmap(int floorID)
	{
		PublicData.getrout(floorID);
		createBinaryMap();
		createWifiMap(floorID);
		createRelativeMap(PublicData.relativelength/2);
	}
	
	//创建二维地图
	public int[][] createBinaryMap()
	{
		pathNumber=PublicData.rout.length/4;
        //binaryMap清零
		for(int i=0;i<PublicData.MapWidth;i++)
		{
			for(int j=0;j<PublicData.MapHeight;j++)
			{
				binaryMap[i][j]= 0 ;
			}
		}
		
	    for(int pathID=0;pathID<pathNumber;pathID++)
	    {
	    	boundary1=PublicData.rout[4*pathID];
	    	boundary2=PublicData.rout[4*pathID+1];
	    	boundary3=PublicData.rout[4*pathID+2];
	    	boundary4=PublicData.rout[4*pathID+3];
			System.out.println(boundary1+","+boundary2+","+boundary3+","+boundary4);
	    	if(boundary2==boundary4){
	    		for(int i=boundary1;i<=boundary3;i++){
	    			binaryMap[i][boundary2]=1;
                    System.out.println(i+"--:"+binaryMap[i][boundary2]);
	    		}
	    	}
	    	
	    	if(boundary1==boundary3){
	    		for(int i=boundary2;i<=boundary4;i++){
	    			binaryMap[boundary1][i]=1;
					System.out.println(i+"|:"+binaryMap[boundary1][i]);
	    		}
	    	}	    	
	    }		
		return binaryMap;	
	}
	
	//创建wifimap
	public ArrayList<String>[][][] createWifiMap(int floorID)
	{
		ArrayList<String> binarylist = new ArrayList<String>();
		ArrayList<String> emptylist = new ArrayList<String>();
		binarylist.add("1"); 
		emptylist.add("0");

//		System.out.println("xwifiMap[0][0][0]--"+xwifiMap[0][0][0]);
        //xwifiMap清零
        for(int i=0;i<PublicData.MapWidth;i++) {
            for(int j=0;j<PublicData.MapHeight;j++) {
                for (int k = 0; k < 4; k++) {
                    xwifiMap[i][j][0] = null;
                }
            }
        }

		//copy of binary map----------------------------------------------------------------------
		for(int i=0;i<PublicData.MapWidth;i++)
		{
			for(int j=0;j<PublicData.MapHeight;j++)
			{
				if(binaryMap[i][j]==1)
				{
					xwifiMap[i][j][0]=binarylist;
				}
				else
				{
					xwifiMap[i][j][0]=emptylist;
				}
			}
		}
		
		//for every path
		for(int pathID=0;pathID<pathNumber;pathID++)
		{
	    	boundary1=PublicData.rout[4*pathID];
	    	boundary2=PublicData.rout[4*pathID+1];
	    	boundary3=PublicData.rout[4*pathID+2];
	    	boundary4=PublicData.rout[4*pathID+3];	    	
			wifiValuesOnPath(pathID+1,floorID); 
			int index=0;
			int begin=0;
			int end=0;		
			int nbSquares=0;
			if(boundary2==boundary4){
				nbSquares=(boundary3-boundary1+1)/listOfWifiVectorsLength.size();
				for(int i=boundary1;i<=boundary3;i++){
					if((xwifiMap[i][boundary2][0].get(0)).equals("1"))
					{
						if ( ( (i-boundary1) % (nbSquares) == 0) && (i-boundary1>0) && index < listOfWifiVectorsLength.size()-1)
						{
							begin = end;
							index++;
						}
						end = begin + listOfWifiVectorsLength.get(index);
						xwifiMap[i][boundary2][1] = new ArrayList<String>();
						xwifiMap[i][boundary2][2] = new ArrayList<String>();
						xwifiMap[i][boundary2][3] = new ArrayList<String>();					
						for(int it=begin;it<end;it++){
							xwifiMap[i][boundary2][1].add(String.valueOf(wifiLevelList.get(it)));
							xwifiMap[i][boundary2][2].add(wifiMacList.get(it));
							xwifiMap[i][boundary2][3].add(wifiApList.get(it));
						}
					}
				}
			}
			
			if(boundary1==boundary3){
				nbSquares=(boundary4-boundary2+1)/listOfWifiVectorsLength.size();
				for(int j=boundary2;j<=boundary4;j++){
					if((xwifiMap[boundary1][j][0].get(0)).equals("1"))
					{
						if ( ( (j-boundary2) % (nbSquares) == 0) && (j-boundary2>0) && index < listOfWifiVectorsLength.size()-1)
						{
							begin = end;
							index++;
						}
						end = begin + listOfWifiVectorsLength.get(index);
						xwifiMap[boundary1][j][1] = new ArrayList<String>();
						xwifiMap[boundary1][j][2] = new ArrayList<String>();
						xwifiMap[boundary1][j][3] = new ArrayList<String>();					
						for(int it=begin;it<end;it++){
							xwifiMap[boundary1][j][1].add(String.valueOf(wifiLevelList.get(it)));
							xwifiMap[boundary1][j][2].add(wifiMacList.get(it));
							xwifiMap[boundary1][j][3].add(wifiApList.get(it));
						}
					}
				}
			}
			
		}
		return xwifiMap;
	}
	
	
	//创建relativemap
	public ArrayList<String>[][][] createRelativeMap(int relative_length)
	{
		ArrayList<String> binarylist = new ArrayList<String>();
		ArrayList<String> emptylist = new ArrayList<String>();

        //relativeWifiMap清零
        for(int i=0;i<PublicData.MapWidth;i++) {
            for(int j=0;j<PublicData.MapHeight;j++) {
                for (int k = 0; k < 9; k++) {
                    relativeWifiMap[i][j][0] = null;
                }
            }
        }

		binarylist.add("1");
		emptylist.add("0");
		for(int i=0;i<PublicData.MapWidth;i++)
		{
			for(int j=0;j<PublicData.MapHeight;j++)
			{
				if(binaryMap[i][j]==1)
				{
//					System.out.println(";"+i+" ; "+j);
					//creation of binary layer
					relativeWifiMap[i][j][0]=binarylist;
					//creation of LEFT neighbor relative list
					relativeWifiMap[i][j][1]=getMACNeighborList(i,j, relative_length,1);
					relativeWifiMap[i][j][2]=fromIntegerToStringList ( getBINNeighborList(i,j, relative_length,1) ) ;
					//creation of RIGHT neighbor relative list
					relativeWifiMap[i][j][3]=getMACNeighborList(i,j, relative_length,2);
					relativeWifiMap[i][j][4]=fromIntegerToStringList ( getBINNeighborList(i,j, relative_length,2) ) ;
					//creation of BOTTOM neighbor relative list
					relativeWifiMap[i][j][5]=getMACNeighborList(i,j, relative_length,3);
					relativeWifiMap[i][j][6]=fromIntegerToStringList ( getBINNeighborList(i,j, relative_length,3) ) ;
					//creation of TOP neighbor relative list
					relativeWifiMap[i][j][7]=getMACNeighborList(i,j, relative_length,4);
					relativeWifiMap[i][j][8]=fromIntegerToStringList ( getBINNeighborList(i,j, relative_length,4) ) ;	
				}
				else
				{
					relativeWifiMap[i][j][0]=emptylist;
				}
			}
		}
		return relativeWifiMap;
	}	
	
	public ArrayList<String> getMACNeighborList (int x, int y, int lengthRelative,int dir)
	 {
		ArrayList<String> intersectionMaclst = new ArrayList<String>();
		boolean isNeighborAvailable = false;
		int newx=0;
		int newy=0;
		switch(dir)
		{
		case 1:
			newx=x-lengthRelative;
			newy=y;
			break;
		case 2:
			newx=x+lengthRelative;
			newy=y;
			break;
		case 3:
			newx=x;
			newy=y+lengthRelative;
			break;
		case 4:
			newx=x;
			newy=y-lengthRelative;
			break;
		}
//		System.out.println("newx:"+newx+"newy"+newy);
		if(newx>0&newx<PublicData.MapWidth&newy>0&newy<PublicData.MapHeight)
		{
			if(xwifiMap[newx][newy][0].get(0).equals("1"))
			{
				isNeighborAvailable=true;
			}
			else //在图内没有邻居节点
			{
				for(int j=0;j<10;j++)
				{
					intersectionMaclst.add("null");
				}
			}
		}
		else //This place need to be considered
		{
			for(int j=0;j<10;j++)
			{
				intersectionMaclst.add("null");
			}
		}
		if(isNeighborAvailable==true)
		{
			intersectionMaclst = intersectionOfArrayList(xwifiMap[x][y][2],xwifiMap[newx][newy][2]);
		}	

		return intersectionMaclst;
	} 

	public ArrayList<Integer> getBINNeighborList(int x, int y, int lengthRelative,int dir)
	{
		ArrayList<Integer> intersectionLevellst = new ArrayList<Integer>();
		ArrayList<String> intersectionMaclst = new ArrayList<String>();
		int index1 = 0;
		int index2 =0;
		int newx=0;
		int newy=0;
		boolean isNeighborAvailable = false;
		switch(dir)
		{
		case 1:
			newx=x-lengthRelative;
			newy=y;
			break;
		case 2:
			newx=x+lengthRelative;
			newy=y;
			break;
		case 3:
			newx=x;
			newy=y+lengthRelative;
			break;
		case 4:
			newx=x;
			newy=y-lengthRelative;
			break;
		}
//		System.out.println("newx:"+newx+"newy"+newy);
		if(newx>0&newx<PublicData.MapWidth&newy>0&newy<PublicData.MapHeight)
		{
			if( xwifiMap[newx][newy][0].get(0).equals("1") )
			{
				isNeighborAvailable=true;
			}
			else
			{
				for(int j=0;j<10;j++)
				{
					intersectionLevellst.add(2);
				}
			} 
		}
		else
		{
			for(int j=0;j<10;j++)
			{
				intersectionLevellst.add(2);
			}
		}
		if(isNeighborAvailable==true)//和粒子滤波相减顺序对不上？
		{			
			intersectionMaclst = intersectionOfArrayList(xwifiMap[x][y][2],xwifiMap[newx][newy][2]);
			for(String mac : intersectionMaclst)	
			{
				index1 = xwifiMap[x][y][2].indexOf(mac);
				index2 = xwifiMap[newx][newy][2].indexOf(mac);
				if(  Integer.parseInt(xwifiMap[newx][newy][1].get(index2)) - Integer.parseInt(xwifiMap[x][y][1].get(index1)) <  PublicData.thresholdpara
						&Integer.parseInt(xwifiMap[newx][newy][1].get(index2)) - Integer.parseInt(xwifiMap[x][y][1].get(index1)) > -PublicData.thresholdpara )
				{
					intersectionLevellst.add(0);
				}
				else if (  Integer.parseInt(xwifiMap[newx][newy][1].get(index2)) - Integer.parseInt(xwifiMap[x][y][1].get(index1)) > PublicData.thresholdpara )
				{
					intersectionLevellst.add(-1);
				}
				else
				{
					intersectionLevellst.add(1);
				}
			}
		}
 
		return intersectionLevellst;
	}
	
	private boolean isWifiFileExists(String file,int floorID) 
	{
		File myWifiFile = new File(Environment.getExternalStorageDirectory() + File.separator + "Gloc"+File.separator+floorID,file+"_wifi"+".txt");
		return myWifiFile.exists();
	}

	public boolean allPathsOk(int floorID)
	{
		file_count=0;
		for(int i=1;i<pathNumber+1;i++)
		{
			if(isWifiFileExists("path"+i,floorID))
			{
				file_count++;
			}
		}
//		System.out.println(" "+file_count);
		if(file_count==pathNumber)
		{
			return true ;
		}
		else
		{
			return false ;
		}
	}
	
	private void wifiValuesOnPath(int pathID,int floorID) 
	{
		String file = "path"+pathID+"_wifi";
		String dir = Environment.getExternalStorageDirectory()+File.separator+ "Gloc" + File.separator+floorID+File.separator+file+".txt";
//		System.out.println(""+dir);
		listOfWifiVectorsLength = getWifiValue(dir);	
	}
	
	private  ArrayList<Integer>  getWifiValue(String file) 
	{
		int num = -1;
		ArrayList<Integer> listOfLengthOfScans =  new ArrayList<Integer> ();
		String oldTime="-1";
		String newTime="-1";
		int newNumber=1;
		int counter=0;
		int nbLines = countNbLines(file);		
		try
		{
			InputStream ips=new FileInputStream(file); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line;		
			wifiMacList.clear();
			wifiApList.clear();
			wifiLevelList.clear();					
			while ((line=br.readLine())!= null) // read path file line by line
			{					
				String[] decompose =line.split(";");					
				wifiMacList.add(decompose[2]);
				wifiApList.add(decompose[3]);
				wifiLevelList.add(Integer.parseInt((decompose[4])));				
				newTime = decompose[0];
				newNumber = Integer.parseInt(decompose[1]);
				num++; 
				if(num==0)
				{
					oldTime=newTime;
					counter=1;
				}
				else
				{
					if(newTime.equals(oldTime))
					{
						counter++;
						oldTime=newTime;
					}
					else
					{
						listOfLengthOfScans.add(counter);
						counter=1;
						oldTime=newTime;
					}
				}
				if(num==nbLines-1)
				{
					listOfLengthOfScans.add(counter);
				}				
			}						
			br.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}				
		return listOfLengthOfScans;			
		//This part is OK 
	}
	
	private int countNbLines(String file)
	{
		int nbLines=0;
		try
		{
			InputStream ips=new FileInputStream(file); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line;
			while ((line=br.readLine())!= null) // read path file line by line
			{	
				nbLines++;
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return nbLines;
	}
	
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
	
	private ArrayList<String> fromIntegerToStringList(ArrayList<Integer> integerList)
	{
		ArrayList<String> stringList = new ArrayList<String>();
		
		if(integerList.size()>0)
		{
			for(Integer item : integerList)
			{
			stringList.add(String.valueOf(item));
			}
		}
		return stringList;
	}	
	
	public void saveFingerprintMapToStorage(int wid, int len)
	{
		File myWifiFile = new File(Environment.getExternalStorageDirectory() + File.separator + "Gloc","fingerprint_map"+".txt");
		File myDir = new File(Environment.getExternalStorageDirectory() + File.separator + "Gloc");	
		Boolean success = true;
		String res = "";
		if(!myDir.exists()) 
		{
		success = myDir.mkdir();
		}
		if(success)
		{
			try
			{
				FileOutputStream outWifi = new FileOutputStream(myWifiFile,true);				
				for(int i=0;i<wid;i++)
				{
					for(int j=0;j<len;j++)
					{
						if(  xwifiMap[i][j][0].get(0).equals("1") ) 
						{
							res = i+";" //x location
								 +j+";" //y location
								 +xwifiMap[i][j][1].size()+";" //nb AP
								 ;						
								 for(int k=0 ; k < xwifiMap[i][j][2].size(); k++)
								 {
									 res = res +xwifiMap[i][j][2].get(k)+";" //mac 
											   +xwifiMap[i][j][3].get(k)+";" //ap
											   +xwifiMap[i][j][1].get(k)+";" //level
											   ;
								 }									 
								 res = res +"\n";							
							String resultWifi= res;
							outWifi.write(resultWifi.getBytes());
						}							
					}
				}		
				outWifi.close();
//				System.out.println("finish print map");
			}		
			catch(FileNotFoundException e) 
			{
			e.printStackTrace();
			}
			catch(Exception e)
			{
			e.printStackTrace();
			}		
		}
	}
	
	public void saveRelativeMapToStorage (int wid, int len)
	{
//		System.out.println("hello,I am already Busy saving things");
		File myWifiFile = new File(Environment.getExternalStorageDirectory() + File.separator + "Gloc","relative_map"+".txt");
		File myDir = new File(Environment.getExternalStorageDirectory() + File.separator + "Gloc");	
		Boolean success = true;
		String res = "";	
		if(!myDir.exists()) 
		{
			success = myDir.mkdir();
		}
		if(success)
		{
			try
			{
				//create file 
				FileOutputStream outWifi = new FileOutputStream(myWifiFile,true);
				for(int i=0;i<wid;i++)
				{
					for(int j=0;j<len;j++)
					{
						if(  binaryMap[i][j]==1 ) 
						{				
							//LEFT 
							res = i+";" //x location
									+j+";" //y location
									+relativeWifiMap[i][j][1].size()+";"
									;

							for(int k=0 ; k < relativeWifiMap[i][j][1].size(); k++)
							{
								res = res +relativeWifiMap[i][j][1].get(k)+";" //mac 
										+relativeWifiMap[i][j][2].get(k)+";" //level
										;
							}
							res = res +"\n";							
							//RIGHT
							res = res +i+";" //x location
									+j+";" //y location
									+relativeWifiMap[i][j][3].size()+";" //nb AP
									;

							for(int k=0 ; k < relativeWifiMap[i][j][3].size(); k++)
							{
								res = res +relativeWifiMap[i][j][3].get(k)+";" //mac 
										+relativeWifiMap[i][j][4].get(k)+";" //level
										;
							}

							res = res +"\n";							
							//BOTTOM
							res = res +i+";" //x location
									+j+";" //y location
									+relativeWifiMap[i][j][5].size()+";" //nb AP
									;

							for(int k=0 ; k < relativeWifiMap[i][j][5].size(); k++)
							{
								res = res +relativeWifiMap[i][j][5].get(k)+";" //mac 
										+relativeWifiMap[i][j][6].get(k)+";" //level
										;
							}

							res = res +"\n";						
							//TOP
							res = res +i+";" //x location
									+j+";" //y location
									+relativeWifiMap[i][j][7].size()+";" //nb AP
									;
							for(int k=0 ; k < relativeWifiMap[i][j][7].size(); k++)
							{
								res = res +relativeWifiMap[i][j][7].get(k)+";" //mac 
										+relativeWifiMap[i][j][8].get(k)+";" //level
										;
							}								 
							res = res +"\n";							
							String resultWifi= res;
							outWifi.write(resultWifi.getBytes());
						}
					}
				}			
				outWifi.close();
			}		
			catch(FileNotFoundException e) 
			{
				e.printStackTrace();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}		
		}
	}
}
