package com.indoormap.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UserStepDetection {
	public static float accelerometer_averagre;
	float accelerometerMin,accelerometerMax;
	float accelerometerMin_thres,accelerometerMax_thres;
	public static float threshold_high,threshold_low;
	int accel_flag=0;
	float time_init,time_m,time_show;
	float time_temp1,time_temp2,time_temp3;
	public boolean stepflag=false;
	public static double steplength = 6.5;
	public float frequency=1;
	
	public void StepParaInit(float accStrengthsum)
	{
		accelerometer_averagre=accStrengthsum/40;
		accelerometerMin=accelerometer_averagre;
		accelerometerMin_thres=-10;
		accelerometerMax=accelerometer_averagre;
		accelerometerMax_thres=36;
		threshold_high=(float) (accelerometer_averagre+0.2);
		threshold_low=(float) (accelerometer_averagre-0.2);
	}
	
	public boolean StepDetection(float accelerometerStrength, float time_init)
 	{
 		if(accelerometerMin>accelerometerStrength)
 		{
 			accelerometerMin=accelerometerStrength;
 		}
 		if(accelerometerMin<accelerometerMin_thres)
 		{
 			accel_flag=0;
 			accelerometerMin=accelerometer_averagre;
 		}

 		if(accelerometerMax<accelerometerStrength)
 		{
 			accelerometerMax=accelerometerStrength;
 		}
 		if(accelerometerMax>accelerometerMax_thres)
 		{
 			accel_flag=0;
 			accelerometerMax=accelerometer_averagre;
 		}

 		time_m=get_time_m();
 		time_show=time_m-time_init;
 		
 		switch(accel_flag){
 		case 0:
 			if(accelerometerStrength>threshold_high)
 			{
 				time_temp1=time_show;
 				accel_flag=1;
 			}
 			break;
 		case 1:	
 			time_temp2=time_show;
 			if(accelerometerStrength<threshold_low)
 			{
 				if((time_temp2-time_temp1)>0.125&&(time_temp2-time_temp1)<0.75)
 				{
 					accel_flag=2;
 				}
 				else
 				{
 					accel_flag=0;
 				}
 			}	 
 			else
 			{
 				if((time_temp2-time_temp1)>0.75)
 				{
 					accel_flag=0;
 				}
 			}
 			break;
 		case 2:
 			time_temp3=time_show;
 			if(accelerometerStrength>threshold_high)
 			{
 				if((time_temp3-time_temp1)>0.25&&(time_temp3-time_temp1)<1.5)
 				{
 					frequency=1/(time_temp3-time_temp1);
 					accel_flag=1;
 					time_temp1=time_temp3;
 					stepflag=true;
 				}
 				else
 				{
 					accel_flag=1;
 					time_temp1=time_temp3;
 				}
 			}	 
 			else
 			{
 				if((time_temp2-time_temp1)>1.5)
 				{
 					accel_flag=0;
 				}
 			}
 			break;
 		}
 		return stepflag;
 	}

 	public float get_time_m()
 	{
 		Date d = new Date();
 		float time;
 		SimpleDateFormat ms=new SimpleDateFormat("SSS");
 		SimpleDateFormat s=new SimpleDateFormat("ss");
 		SimpleDateFormat m=new SimpleDateFormat("mm");
 		String ms_S=new String();
 		String s_S=new String();
 		String m_S=new String();

 		ms_S=ms.format(d);
 		s_S=s.format(d);
 		m_S=m.format(d);
 		float ms_I=Integer.parseInt(ms_S);
 		float s_I=Integer.parseInt(s_S);
 		float m_I=Integer.parseInt(m_S);
 		time=m_I*60+s_I+ms_I/1000;
 		return time;
 	}

 	public float get_time_init()
 	{
 		time_init=get_time_m();
 		return time_init;
 	}

 	public double steplengthestimate(float accVar , float h){
	    if(accVar<1){
            UserData.walkStateType = "slow";
            UserData.walkState = 1;
            steplength= 2.164 +0.452*frequency +1.996 *accVar;
        }
		else if(accVar<5){
            UserData.walkStateType = "normal";
            UserData.walkState = 2;
 			steplength= 3.42 +1.50*frequency +0.29 *accVar;
		}
		else{
            UserData.walkStateType = "fast";
            UserData.walkState = 3;
            steplength= 1.71 +3.01*frequency +0.06 *accVar;
        }

        steplength = h *steplength;
 		return steplength;
 	}

}
