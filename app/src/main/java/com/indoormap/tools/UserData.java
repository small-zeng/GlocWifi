package com.indoormap.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Environment;
import android.text.format.Time;
import android.widget.Toast;

import com.indoormap.map.PublicData;

public class UserData {

    public static ArrayList<Integer> userWifiLevelList = new ArrayList<Integer>();
    public static ArrayList<String>  userWifiMacList = new ArrayList<String>();
    private static ArrayList<Integer> userWifiLevelListOld = null;
    private static ArrayList<String> userWifiMacListOld = null;
    public ArrayList<String>  userWifiApList = new ArrayList<String>();
    public ArrayList<Integer> relativeUserWifiBinList = new ArrayList<Integer> ();
	public ArrayList<String>  relativeUserWifiMacList = new ArrayList<String>();
    public ArrayList<String>  relativeUserWifiApList = new ArrayList<String>();
    public ArrayList<String> userBuffer[][] = new ArrayList[(PublicData.relativelength/10)][3];
    public static int buff=0;
    public static int dir=0;

    //PDR相关
    public ArrayList<Float> accBuffValue = new ArrayList<>();//加速度窗口N缓存
    public ArrayList<Float> accStepBuffValue = new ArrayList<>();//一步加速度值缓存

	public ArrayList<Float> accStepBuffValue_Z = new ArrayList<>();//一步z轴加速度值缓存
    public ArrayList<Float> accStepBuffValue_XY = new ArrayList<>();//一步xy平面加速度值缓存

    public ArrayList<Float> oriStepBuffValue_X = new ArrayList<>();//一步x轴方向值缓存
    public ArrayList<Float> oriStepBuffValue_Y = new ArrayList<>();//一步y轴方向值缓存
    public ArrayList<Float> oriStepBuffValue_Z = new ArrayList<>();//一步z轴方向值缓存

    public ArrayList<Float> oriBuffValue = new ArrayList<>();//方位角值缓存
    public float PdrOriEst, PdrOriEst_Last;//PDR方位角估计
    public static float pdrDirection;
    public int pdrIndex;//PDR索引

    public static float calibrationAngle,calibrationAngleOld = 0;
    public static int walkState = 0;
    public static String walkStateType = "stop";
    public float walkDis = 0.0f;
    public float walkVecDis =0.0f;
    public float[] PA_ATbuff= new float[]{0,0,0};

    public float[] stepIntervalBuff= new float[]{0,0,0};
    public long lastStep_Ts =0 , newStep_Ts = 0;

    //PDR统计数据
    public float[] meanVar_acc_Strength ,meanVar_acc_z,meanVar_acc_xy;
    public float[] meanVar_ori_x ,meanVar_ori_y ,meanVar_ori_z;
    public float[] minMax_acc_Strength ;
    public float[] meanVar_acc_N;


    public WifiAdmin wifiAdmin;
	public List<ScanResult> results;
	Time timestamp = new Time();

	//计算PDR方向
    public float getPdrDirection(int stepcount,float angle ){
        if(stepcount == 1){
            PdrOriEst = angle;
            PdrOriEst_Last = angle;
        }
        if(stepcount>=2){
            float oriBuffSum = 0,accBuffSum = 0;
            for(int i=0;i<oriBuffValue.size();i++){//两步积分
                oriBuffSum += oriBuffValue.get(i);
            }
            float value = oriBuffSum/oriBuffValue.size();
            if(Math.abs(value-PdrOriEst_Last)<45){//判断是否转弯
                PdrOriEst = value ;
            }
            else{
                PdrOriEst = angle;
            }
            PdrOriEst_Last = PdrOriEst;
            for(int i =0;i<pdrIndex;i++) { //删除前一步方位缓存
                oriBuffValue.remove(0);
            }

			//更新加速度平均值阈值
			float sum=0;
			for(int i =0;i<accStepBuffValue.size();i++){
				sum +=accStepBuffValue.get(i);
			}
			UserStepDetection.accelerometer_averagre=sum/accStepBuffValue.size();
			UserStepDetection.threshold_high=(float) (UserStepDetection.accelerometer_averagre+0.2);
			UserStepDetection.threshold_low=(float) (UserStepDetection.accelerometer_averagre-0.2);

        }
        pdrIndex = oriBuffValue.size()-1;

        pdrDirection = PdrOriEst - calibrationAngle;

        //超过阈值
        if(pdrDirection >180){
            pdrDirection -= 360;
        }
        if(pdrDirection < -180){
            pdrDirection += 360;
        }

        //重力感应
        if(SensorsAdmin.screenStatus.equals("portrait(+)")){
            pdrDirection = pdrDirection - 90;
        }
        else if(SensorsAdmin.screenStatus.equals("landscape(-)")){
            pdrDirection = pdrDirection - 180;
        }
        else if(SensorsAdmin.screenStatus.equals("portrait(-)")){
            pdrDirection = pdrDirection - 270;
        }

        //超过阈值
        if(pdrDirection<-180){
            pdrDirection += 360;
        }
        return pdrDirection;
    }

    //得到PDR结果
    public void getPdrResult(){
        ;
    }

    //得到八个方向
    public int getUserPdrDirection(float x)
    {
        int pdrDIR =0;
        float deta = 22.5f;
        x = x +155;
        if(x>0-deta && x<= 0+deta){
            pdrDIR = 1;
        }
        else if(x>45-deta && x<= 45+deta){
            pdrDIR = 2;
        }
        else if(x>90-deta && x<= 90+deta){
            pdrDIR = 3;
        }
        else if(x>135-deta && x<= 135+deta){
            pdrDIR = 4;
        }
        else if(x>180-deta && x<= 180+deta){
            pdrDIR = 5;
        }
        else if(x>225-deta && x<= 225+deta){
            pdrDIR = 6;
        }
        else if(x>270-deta && x<= 270+deta){
            pdrDIR = 7;
        }
        else {
            pdrDIR = 8;

        }
        return pdrDIR;

    }

    //得到四个方向
    public int getUserFourDirection(float x) {
       if(x>-45 && x<=45){
           dir = 2;
       }
       else if(x>45 && x<=135){
           dir = 3;
       }
       else if((x>135 && x<=180)||(x>-180 && x<=-135)){
           dir = 1;
       }
       else {
           dir = 4;
       }
        return dir;
    }

    //得到前进距离
    public void getWalkDis(){
        walkDis += UserStepDetection.steplength;
        walkVecDis += UserStepDetection.steplength*Math.cos(Math.toRadians(pdrDirection));
    }

    //更新传感器数据
    public void updateSensorData(){
        oriBuffValue.add(SensorsAdmin.state[2]);
        accStepBuffValue.add(SensorsAdmin.accStrength);
        accStepBuffValue_Z.add(SensorsAdmin.accelerometerVector[2]);
        accStepBuffValue_XY.add((float) Math.sqrt(SensorsAdmin.ax*SensorsAdmin.ax+SensorsAdmin.ay*SensorsAdmin.ay));
        oriStepBuffValue_X.add(SensorsAdmin.state[0]);//roll
        oriStepBuffValue_Y.add(SensorsAdmin.state[1]);//pitch
        oriStepBuffValue_Z.add(SensorsAdmin.state[2]);//yaw
        accBuffValue.add(SensorsAdmin.state[2]);
    }

    //判断手机是否静止（窗口长度N<=10）
    public boolean isPhoneStatic(int N){
        if(accBuffValue.size() < N){
            return true;
        }
        else{
            meanVar_acc_N = getMeanVar(accBuffValue,N);
            accBuffValue.remove(0);
            if(meanVar_acc_N[1]<0.25){//静止
                walkState = 0;
                walkStateType ="stop";
                return true;
            }
            else{
                return false;
            }
        }

    }

    //判断手机是否晃动
    public boolean isPhoneShaking(){
//        if ((meanVar_acc_z[1] >0.8 && meanVar_acc_z[1] > 10)|| meanVar_acc_xy[0]>3) {//判断是否由手机晃动造成
//            System.out.println("错误计步");
//            stepcount = stepcount - 1;
//            return true;
//        }

        if ( (meanVar_acc_z[1] <0.25 || meanVar_acc_z[1]>30 )
               ||(meanVar_ori_x[1]>100 || meanVar_ori_y[1]>100 || meanVar_ori_z[1]>1000)) {//判断是否由手机晃动造成
            System.out.println("错误计步");
            walkStateType = "shake";
            return true;
        }
        else{
            return false;
        }

    }

    //计算均值和方差
    public float[] getMeanVar(ArrayList<Float> data , int num){
        float sum1=0,sum2=0,mean=0;
        float[]  meanVar = new float[2];
        int len = num;
        if(data.size() < len){
            len = data.size();
        }
//        System.out.println("当前步加速度："+len);
        for(int i =0;i<len;i++){
            sum1 += data.get(data.size()-1-i);
//            System.out.println("加速度("+i+"):"+data.get(data.size()-1-i));
        }
        mean = sum1/len;
        meanVar[0]=mean;

        for(int i =0;i<len;i++){
            sum2 +=(data.get(data.size()-1-i)-mean)*(data.get(data.size()-1-i)-mean);
        }
        meanVar[1] =sum2/len;
        return meanVar;
    }

    //PDR统计数据
    public void pdrStatisticData(){
        meanVar_acc_Strength = getMeanVar(accStepBuffValue,15);
        meanVar_acc_z =  getMeanVar( accStepBuffValue_Z ,15);
        meanVar_acc_xy = getMeanVar( accStepBuffValue_XY ,15);
        meanVar_ori_x = getMeanVar( oriStepBuffValue_X ,15);
        meanVar_ori_y = getMeanVar( oriStepBuffValue_Y ,15);
        meanVar_ori_z = getMeanVar( oriStepBuffValue_Z ,15);
        minMax_acc_Strength = getAccMinMax( accStepBuffValue);

//        //清除缓存
//        pdrBuffClear();

    }

    //PDR清除缓存
    public void pdrBuffClear(){
        accStepBuffValue = new ArrayList<>();
        accStepBuffValue_Z = new ArrayList<>();
        accStepBuffValue_XY = new ArrayList<>();
        oriStepBuffValue_X = new ArrayList<>();
        oriStepBuffValue_Y = new ArrayList<>();
        oriStepBuffValue_Z = new ArrayList<>();
    }

    //计算最大最小值
    public float[] getAccMinMax(ArrayList<Float> data) {
        float[] accMinMax = new float[2];
        accMinMax[0] = data.get(0);
        accMinMax[1] = data.get(0);
        for(int i =0;i<data.size();i++ ){
            if(data.get(i)<accMinMax[0]){
                accMinMax[0] = data.get(i);
            }
            if(data.get(i)>accMinMax[1]){
                accMinMax[1] = data.get(i);
            }

        }
        return accMinMax;
    }

    //判断是否走大小步
    public boolean isBigSmallStep(){
        if((PA_ATbuff[0]>2 || PA_ATbuff[1]>2 || PA_ATbuff[2]>2)&&
            (stepIntervalBuff[0]>1.5 || stepIntervalBuff[1]>1.5 || stepIntervalBuff[2]>1.5)){
            return true;
        }
        else{
            return  false;
        }
    }

    //更新PA_TA缓存
    public void updatePA_ATbuff(float data){
        PA_ATbuff[0] =  PA_ATbuff[1];
        PA_ATbuff[1] =  PA_ATbuff[2];
        PA_ATbuff[2] =  data;
    }

    //更新StepInterval缓存
    public void updateStepIntervalBuff(long data) {
        float interval =(float)((data - lastStep_Ts)/1000.0);
        stepIntervalBuff[0] = stepIntervalBuff[1];
        stepIntervalBuff[1] = stepIntervalBuff[2];
        stepIntervalBuff[2] = interval;
        lastStep_Ts = data;
    }

    //存储修正角度到文件
    public static void storageCalibrationAngle() {
        File myAngleFile=new File(Environment.getExternalStorageDirectory()+File.separator+"Gloc"+ File.separator,"CalibrationAngle.txt");
        File myDir=new File(Environment.getExternalStorageDirectory()+File.separator+"Gloc"+ File.separator);
        Boolean success=true;
        if(!myDir.exists())
        {
            success = myDir.mkdir();
        }
        if(success)
        {
            try
            {
                String res=String.valueOf(calibrationAngle);
                FileOutputStream outAngle = new FileOutputStream(myAngleFile,false);
                outAngle.write(res.getBytes());
                outAngle.close();
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

    //读取修正角度
    public static void getCalibrationAngle() {
        File myAngleFile=new File(Environment.getExternalStorageDirectory()+File.separator+"Gloc"+ File.separator+"CalibrationAngle.txt");
            try
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(myAngleFile)));
                String line = br.readLine();
                calibrationAngle = Float.parseFloat(line);
                calibrationAngleOld = calibrationAngle;
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

    //得到当前wifi数据
    public void GetUserWifiVector()
    {
        userWifiLevelListOld = userWifiLevelList;
        userWifiMacListOld = userWifiMacList;
		ArrayList<String> list_bssid = new ArrayList<String>();
		userWifiMacList = new ArrayList<>();
		userWifiApList.clear();
		userWifiLevelList = new ArrayList<>();
		wifiAdmin.startScan();
		results = wifiAdmin.getWifiList();
        List<AP> ApList = new ArrayList<AP>();
		for (ScanResult result : results)
		{
            ApList.add(new AP(result.BSSID,result.SSID,result.level));
		}
        //wifi排序
        System.out.println("定位实时采集AP按RSS降序：");
        Collections.sort(ApList, new UserData.SortByRss());
        for (AP ap : ApList) {
            if(ap.getWifiLevel()>PublicData.rSSThreshold) {
              System.out.println(ap.toString());
                userWifiLevelList.add(ap.getWifiLevel());
                userWifiMacList.add(ap.getWifiMac());
                userWifiApList.add(ap.getWifiSSID());
            }
        }
    }

    //added
	public static boolean wifiListUpdate(){
    	if(UserData.userWifiLevelList.size() != UserData.userWifiMacListOld.size())
    		return true;
    	for(int i = 0 ; i < UserData.userWifiLevelList.size() ; i ++){
    		if(!UserData.userWifiMacListOld.get(i).equals(UserData.userWifiMacList.get(i)))
    			return true;
    		if(!UserData.userWifiLevelListOld.get(i).equals(UserData.userWifiLevelList.get(i)))
    			return true;
    		System.out.println(UserData.userWifiLevelList.size()+":"+i);
		}
    	return false;
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
			if(  x>=0 & x<=45 )
			{
				dir=1;
			}
			if(x>45 & x<=135)
			{
				dir=4;
			}
			if(x>135 )
			{
				dir=2;
			}

			//0 to -180
			if(x<0 & x >=-40)
			{
				dir=1;
			}
			if(x<-40 & x>=-130)
			{
				dir=3;
			}
			if(x<-130 )
			{
				dir=2;
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

    //按RSS降序
    class SortByRss implements Comparator {
        public int compare(Object o1, Object o2) {
            AP s1 = (AP) o1;
            AP s2 = (AP) o2;
            if (s1.getWifiLevel() < s2.getWifiLevel())
                return 1;
            return -1;
        }
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
