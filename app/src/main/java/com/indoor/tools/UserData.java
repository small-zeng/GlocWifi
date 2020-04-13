package com.indoor.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Environment;
import android.text.format.Time;
import android.widget.Toast;

import com.indoor.map.PublicData;

public class UserData {
	
    public ArrayList<Integer> userWifiLevelList = new ArrayList<Integer>();
    public ArrayList<String>  userWifiMacList = new ArrayList<String>();
    public ArrayList<String>  userWifiApList = new ArrayList<String>();
    public ArrayList<Integer> relativeUserWifiBinList = new ArrayList<Integer> ();
	public ArrayList<String>  relativeUserWifiMacList = new ArrayList<String>();
    public ArrayList<String>  relativeUserWifiApList = new ArrayList<String>();
    public ArrayList<String> userBuffer[][] = new ArrayList[(PublicData.relativelength/10)][3];
    public static int buff=0;
    public static int dir=0;

    public WifiAdmin wifiAdmin;  
	public List<ScanResult> results; 
	Time timestamp = new Time();
    
    public void GetUserWifiVector()
    {
		ArrayList<String> list_bssid = new ArrayList<String>();
		userWifiMacList.clear();
		userWifiApList.clear();
		userWifiLevelList.clear();
		wifiAdmin.startScan();
		results = wifiAdmin.getWifiList();
		for (ScanResult result : results)
		{
			list_bssid.add(result.BSSID);
		}
		for (int i=0;i<PublicData.Maclist.length;i++)
		{
			int index=list_bssid.indexOf(PublicData.Maclist[i]);
			if(index!=-1){		    
			userWifiLevelList.add(results.get(index).level);
			userWifiMacList.add(results.get(index).BSSID);
			userWifiApList.add(results.get(index).SSID);
			}	
		}
//		System.out.println("userWifiMacList:"+userWifiMacList);
//		System.out.println("userWifiApList:"+userWifiApList);
//		System.out.println("userWifiLevelList:"+userWifiLevelList);
    }
    
    public void BuffUserWifiVector(int stepcount)
    {
    	buff=(stepcount-1)%4;
    	userBuffer[buff][0]=new ArrayList<String>(userWifiMacList);
    	userBuffer[buff][1]=new ArrayList<String>(userWifiApList);
    	userBuffer[buff][2]=new ArrayList<String>(fromIntegerToStringList(userWifiLevelList));
    }
    
    public void GetUserRelativeWifiVector(int stepcount)
    {
        relativeUserWifiBinList.clear();
		relativeUserWifiMacList.clear();
		relativeUserWifiApList.clear();
		buff=(stepcount-1)%4;
		ArrayList<Integer> oldUserWifiLevelList=fromStringToIntegerList(userBuffer[buff][2]);
		ArrayList<String>  oldUserWifiMacList=userBuffer[buff][0];
		ArrayList<String>  oldUserWifiApList=userBuffer[buff][1];
		int index1=0;
		int index2=0;
		relativeUserWifiMacList = intersectionOfArrayList(userWifiMacList,oldUserWifiMacList);
		for(String mac : relativeUserWifiMacList)	
		{
			index1 = userWifiMacList.indexOf(mac);
			index2 = oldUserWifiMacList.indexOf(mac);
			relativeUserWifiApList.add(oldUserWifiApList.get(index2));
			if(  userWifiLevelList.get(index1) - oldUserWifiLevelList.get(index2) < PublicData.thresholdpara
					&userWifiLevelList.get(index1) - oldUserWifiLevelList.get(index2) > - PublicData.thresholdpara  )
			{
				relativeUserWifiBinList.add(0);
			}
			else if (  userWifiLevelList.get(index1) - oldUserWifiLevelList.get(index2) > PublicData.thresholdpara  )
			{
				relativeUserWifiBinList.add(1);
			}
			else
			{
				relativeUserWifiBinList.add(-1);
			}
		}
    }
    
	
	public int getUserDirection(float x)
	{			
			//0 to 180
			if(  x>=0 & x<=50 )
			{
				dir=3;
			}			
			if(x>50 & x<=140)
			{
				dir=1;
			}
			if(x>140 )
			{
				dir=4;
			}
					
			//0 to -180
			if(x<0 & x >=-40)
			{
				dir=3;
			}
			if(x<-40 & x>=-130)
			{
				dir=2;
			}
			if(x<-130 )
			{
				dir=4;
			}		
			return dir;
		}
	
	public String getUserStringDirection(float x)
	{
		int dir=getUserDirection(x);
		String stringDir = null;
		switch(dir)
		{
		case 0 :
			stringDir="unknown";
			break;
		case 1 :
			stringDir="1 LEFT";
			break;
		case 2 :
			stringDir="2 RIGHT";
			break;
		case 3 :
			stringDir="3 DOWN";
			break;
		case 4 :
			stringDir="4 UP";
			break;
		}
		return stringDir;
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
	
	private ArrayList<Integer> fromStringToIntegerList(ArrayList<String> stringList)
	{
		ArrayList<Integer> integerList = new ArrayList<Integer>();
		if(stringList.size()>0)
		{
			for(String item : stringList)
			{
				integerList.add( Integer.parseInt(item) );
			}
		}
		return integerList;
	}
	
	private ArrayList<String> intersectionOfArrayList(ArrayList<String> lst1 , ArrayList<String> lst2)
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
	
	public void exportuserdata(int stepcount,int dir,int partLength,Context context)
	{
		String res = stepcount +";";
		res= res + dir + ";" ;		 
		for(int i = 0 ; i<userWifiApList.size(); i++)
		{
			res = res + userWifiMacList.get(i)+";" + userWifiApList.get(i)+";"+userWifiLevelList.get(i)+";";
		}
		File myOutputFile = new File(Environment.getExternalStorageDirectory() + File.separator + "Gloc","user_data"+".txt");
		File myDir = new File(Environment.getExternalStorageDirectory() + File.separator + "Gloc");
		Boolean success = true;
		if(!myDir.exists()) 
		{
		success = myDir.mkdir();
		}
		if(success)
		{
			try
			{
				timestamp.setToNow();
				String sTime = timestamp.format("%Y-%m-%d %H:%M:%S");
				String resultWifi= sTime+";"+res+partLength+"\n";				
				FileOutputStream out = new FileOutputStream(myOutputFile,true);
				out.write(resultWifi.getBytes());
				out.close();
			}
			catch(FileNotFoundException e) 
			{
			e.printStackTrace();
			Toast.makeText(context, " error ",Toast.LENGTH_SHORT).show();
			}
			catch(Exception e)
			{
			e.printStackTrace();
			Toast.makeText(context, " error ",Toast.LENGTH_SHORT).show();
			}
		}			
	}
	
	
	public void exportuserlocation(int stepcount,int LocationX,int LocationY)
	{	
		File myOutputFile = new File(Environment.getExternalStorageDirectory() + File.separator + "Gloc","output_locations"+".txt");
		File myDir = new File(Environment.getExternalStorageDirectory() + File.separator + "Gloc");
		Boolean success = true;
		if(!myDir.exists()) 
		{
		success = myDir.mkdir();
		}

		if(success)
		{

			try
			{
				String res = (stepcount+";"+LocationX+";"+LocationY+";");
				timestamp.setToNow();
				String sTime = timestamp.format("%Y-%m-%d %H:%M:%S");
				String resultWifi= sTime+";"+res+"\n";
				FileOutputStream outlocation = new FileOutputStream(myOutputFile,true);
				outlocation.write(resultWifi.getBytes());
				outlocation.close();
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
	
	public void exportgroundtruth(int stepcount)
	{
		File myOutputFile = new File(Environment.getExternalStorageDirectory() + File.separator + "Gloc","groundtruth"+".txt");
		File myDir = new File(Environment.getExternalStorageDirectory() + File.separator + "Gloc");
		Boolean success = true;
		if(!myDir.exists()) 
		{
		success = myDir.mkdir();
		}
		if(success)
		{
			try
			{
				String res = (stepcount+";");
				timestamp.setToNow();
				String sTime = timestamp.format("%Y-%m-%d %H:%M:%S");
				String resultWifi= sTime+";"+res+"\n";
				FileOutputStream outlocation = new FileOutputStream(myOutputFile,true);
				outlocation.write(resultWifi.getBytes());
				outlocation.close();
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
