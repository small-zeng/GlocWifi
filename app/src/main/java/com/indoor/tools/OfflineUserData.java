package com.indoor.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.os.Environment;
import android.text.format.Time;

import com.indoor.map.PublicData;

public class OfflineUserData {
	
    public ArrayList<Integer> userWifiLevelList = new ArrayList<Integer>();
    public ArrayList<String>  userWifiMacList = new ArrayList<String>();
    public ArrayList<String>  userWifiApList = new ArrayList<String>();
    public ArrayList<Integer> relativeUserWifiBinList = new ArrayList<Integer>();
	public ArrayList<String>  relativeUserWifiMacList = new ArrayList<String>();
    public ArrayList<String>  relativeUserWifiApList = new ArrayList<String>();
    public ArrayList<String> userBuffer[][] = new ArrayList[(PublicData.relativelength/10)][3];
    public static int buff=0;
    public static int dir=0;
    public static int nbLinesUserFile=0;
	public int steplength=6;
    //offline
    int linenumber=300;
    int elementnumber=40;
    private String[][] offline_usertab = new String[linenumber][elementnumber];
    Time timestamp = new Time();
	
    public void GetUserWifiVector(int stepcount)
    {
		userWifiMacList.clear();
		userWifiApList.clear();
		userWifiLevelList.clear();
		int i=stepcount;
		int j=2;
		while(j<elementnumber)
		{
			if( !offline_usertab[i][j].equals("0") )
			{
				userWifiMacList.add(offline_usertab[i][j-1]);
				userWifiApList.add(offline_usertab[i][j]);
				userWifiLevelList.add(Integer.valueOf(offline_usertab[i][j+1]));
			}
			else
			{
			    steplength=Integer.valueOf(offline_usertab[i][j-1]);
				break;
			}
			j=j+3;
		}
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
    
    public String getUserStringDirection(int stepcount){
    	int i=stepcount;
    	dir=Integer.valueOf(offline_usertab[i][0]);
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
    
    public int getUserDirection(int stepcount){
    	int i=stepcount;
    	dir=Integer.valueOf(offline_usertab[i][0]);
    	return dir;
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
	
	public void readuserfile()
	{
		int counter=-1;
		String file = "user_data";
		String dir = Environment.getExternalStorageDirectory()+File.separator+ "Gloc" + File.separator+file+".txt";
		nbLinesUserFile = countNbLines(dir);
		for(int i=0; i<linenumber; i++)
		{
			for(int j = 0 ; j <elementnumber; j++)
			{
				offline_usertab[i][j]="0";
			}
		}
		try
		{
			InputStream ips=new FileInputStream(dir); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line;	
			while ((line=br.readLine())!= null) // read path file line by line
			{
				String[] decompose =line.split(";");
				counter++;		
				for(int i=0; i<decompose.length-2; i++)
				{
					offline_usertab[counter][i]=decompose[i+2];
				}	
			}		
			br.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}	
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
}
