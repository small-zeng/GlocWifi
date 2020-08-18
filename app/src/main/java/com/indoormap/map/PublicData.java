package com.indoormap.map;

import android.os.Environment;

import com.indoormap.tools.Point;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PublicData {

	public static int MapWidth=750;
	public static int MapHeight=700;
	public static int thresholdpara=5;
	public static int relativelength=40;  //TODO 减半时注意这边也应减半
	public static int N=0;
	public static int initialpartlength=8;
	public static int resamplingfreq =10;
	public static int [] rout;
	public static ArrayList<Point> uwbPoints;
	public static ArrayList<Point> matchPoints = new ArrayList<>();
	public static boolean uwbFileExit ;
	public static int overrideGidNum =10;  //TODO 路径插值覆盖网格数
	public static int overrideGidNumPoint =5;  //TODO 点插值覆盖网格数
	public static int rSSThreshold=-80;

	//特征权重参数
	public static double wMacSetPram = 0.1;
	public static double wStrengthOrderPram = 0.7;
	public static double wRssPram = 0.2;
	public static double wGradientParm = 0.0;

	public static int muteRangeGeneral = 50;//普通区域粒子位置突变范围
    public static int muteRangeStair = 10;//粒子位置突变范围
	public static double muteRate=0.2;//粒子位置突变率
	public static double highWeightRate = 0.2;//高权重粒子比例，用于估计粒子位置
	public static int[] regionRange;  //分区范围（左下端点与右上端点坐标）
    public static int intialInterval=5;//粒子初始位置在配置路径是上的间隔距离

  public static void getrout(int floorID)
  {
	  List<String> files = new ArrayList<String>();
	  files=getFiles(Environment.getExternalStorageDirectory() + File.separator + "Gloc"+File.separator+floorID);
	  uwbFileExit=false;
//	  System.out.println(files.size());
	  if(files.contains(Environment.getExternalStorageDirectory() + File.separator + "Gloc"+File.separator+floorID+ File.separator+"UWBPoints.txt")){
		  rout = new int [(files.size()-2)*4];
	  }
	  else{
		  rout = new int [(files.size())*4];
	  }
	  int num=0;
	  uwbPoints=new ArrayList<Point>();
	  for(String file: files) {
		  String fileName = file.replace(Environment.getExternalStorageDirectory() + File.separator + "Gloc"+File.separator+floorID+ File.separator, "");
		  fileName = fileName.replace(".txt", "");
//		  System.out.println("存在文件--"+fileName);
		  //UWB数据点处理
		  if(fileName.equals("UWBPoints")){
		  	uwbFileExit=true;
		  }
		  //路径数据文件处理
		  else {
			  String[] str = fileName.split("-|,");
			  if (str.length == 4) {
				  for (String substr : str) {
					  rout[num] = Integer.parseInt(substr);
					  num++;
				  }
			  }
		 }
	  }

  }

	//分区范围（左下端点与右上端点坐标）
	public static void setRegionRange(int floorID) {
		regionRange = new int[]{
				//region1
				0, 0, 80, 48,
				//region2
				0, 52, 80, 100,
				//region3
				85, 0, 160, 48,
				//region4
				85, 52, 160, 100,
				//region5
				165, 20, 220, 50,
		};
	}

	//判断点在哪个区域
	public static int[] getRegionRange(int x, int y){
		int[] region =null;
		for(int i=0;i<regionRange.length/4;i++){
			if(x>regionRange[4*i] && x<regionRange[4*i+2] && y>regionRange[4*i+1] && y<regionRange[4*i+3]){
				region = new int[]{regionRange[4*i],regionRange[4*i+1],regionRange[4*i+2],regionRange[4*i+3]};
				break;
			}
		}
		return region;
	}

//    @Test
//    public void regionTest(){
//        setRegionRange(6);
//        int[] region=getRegionRange(166,30);
//
//        if(region !=null) {
//            for (int i = 0; i < 4; i++) {
//                System.out.print(region[i] + ";");
//            }
//            System.out.println();
//        }
//        else{
//            System.out.println("无法判断在哪个区域");
//        }
//    }


    /**
	 * @Description：获取某个目录下所有直接下级文件，不包括目录下的子目录的下的文件，所以不用递归获取
	 * @Date：
	 */
	public static List<String> getFiles(String path) {
		List<String> files = new ArrayList<String>();
		File file = new File(path);
		File[] tempList = file.listFiles();

		for (int i = 0; i < tempList.length; i++) {
			if (tempList[i].isFile()) {
				files.add(tempList[i].toString());
				//文件名，不包含路径
//				String fileName = tempList[i].getName();
//				System.out.println(fileName);
			}
			if (tempList[i].isDirectory()) {
				//这里就不递归了，
			}
		}
		return files;
	}

	public static int GetPartNumber()
	{

//	  int PathNumber=rout.length/4;
//	  for (int i=0;i<PathNumber;i++)
//	  {
//		  N=N+rout[4*i+3]-rout[4*i+1]+rout[4*i+2]-rout[4*i+0];
//	  }
//	  N=N/2;
		return N;
	}


	public static void ResetPartNumber()
  {		
	  N=0;
  }
  
  public static String[] Maclist=new String[]{
	  "58:66:ba:23:be:50",
	  "58:66:ba:23:cd:30",
	  "58:66:ba:23:db:10",
	  "58:66:ba:67:ad:30",
	  "58:66:ba:67:af:90",
	  "58:66:ba:67:b4:10",
	  "58:66:ba:68:0b:d0",
	  "58:66:ba:7a:f3:b0",
	  "58:66:ba:7b:27:90",
	  "58:66:ba:7b:28:10",
	  "58:66:ba:7b:28:30",
	  "58:66:ba:7b:28:90",
	  "58:66:ba:7b:28:f0",
	  "58:66:ba:7b:29:50",
	  "80:f6:2e:12:40:d0",
	  "80:f6:2e:14:fd:70",
	  "80:f6:2e:15:01:b0",
	  "80:f6:2e:15:04:d0",
	  "80:f6:2e:15:04:f0",
	  "80:f6:2e:15:05:50",
	  "80:f6:2e:15:05:70",
	  "80:f6:2e:15:05:d0",
	  "80:f6:2e:15:05:f0",
	  "80:f6:2e:15:06:10",
	  "80:f6:2e:15:22:f0",
	  "80:f6:2e:15:23:90",
	  "80:f6:2e:15:a7:50",
	  "80:f6:2e:1a:60:b0",
	  "80:f6:2e:1a:80:90",
	  "80:f6:2e:1a:81:90",
	  "80:f6:2e:1a:82:70",
	  "80:f6:2e:1a:86:b0",
	  "80:f6:2e:1a:87:30",
	  "80:f6:2e:1a:ec:70",
	  "80:f6:2e:1a:ed:10",
	  "80:f6:2e:1a:ed:30",
	  "80:f6:2e:1a:f4:d0",
	  "80:f6:2e:1a:f5:b0",
	  "80:f6:2e:1a:f6:10",
	  "80:f6:2e:1a:f6:f0",
	  "80:f6:2e:1d:6e:10",
	  "80:f6:2e:1d:6e:50",
	  "80:f6:2e:1d:ac:30",
	  "80:f6:2e:1d:b6:30",
	  "80:f6:2e:1d:cf:f0",
	  "80:f6:2e:1d:e3:90",
	  "80:f6:2e:1e:50:30",
	  "80:f6:2e:1e:51:50",
	  "80:f6:2e:1e:51:b0",
	  "80:f6:2e:1e:70:f0",
	  "80:f6:2e:1e:78:10",
	  "80:f6:2e:1e:78:70",
	  "80:f6:2e:1e:79:70",
	  "80:f6:2e:1e:81:f0",
	  "80:f6:2e:1e:89:b0",
	  "80:f6:2e:1e:94:b0",
	  "80:f6:2e:1e:95:f0",
	  "c4:ca:d9:ea:ba:d0",
	  "c4:ca:d9:ea:bb:f0",
	  "c4:ca:d9:ec:38:d0",
	  "c4:ca:d9:ec:3b:90",
	  "c4:ca:d9:ec:f0:50",
	  "c4:ca:d9:ec:f7:10",
	  "c4:ca:d9:ed:52:b0",
	  "c4:ca:d9:ed:5c:50",
	  "c4:ca:d9:ed:8d:d0",
	  "c4:ca:d9:ed:9d:30",
	  "c4:ca:d9:ed:ac:30",
	  "c4:ca:d9:ed:b1:90",
	  "c4:ca:d9:ed:b9:d0",
	  "c4:ca:d9:ee:e7:b0",
	  "c4:ca:d9:ee:ec:70",
	  "c4:ca:d9:ef:21:90" 
  };
}
