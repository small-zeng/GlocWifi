package com.indoor.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UserStepDetection {
	float accelerometer_averagre;
	float accelerometerMin;
	float accelerometerMin_thres;
	float accelerometerMax;
	float accelerometerMax_thres;
	float threshold_high;
	float threshold_low;
	int accel_flag=0;
	float time_init;
	float time_show;
	float time_temp1;
	float time_temp2;
	float time_temp3;
	float time_m;
	public boolean stepflag=false;
	public static int steplength=6;
	public float frequency=1;
	
	public void StepParaInit(float accStrengthsum)
	{
		accelerometer_averagre=accStrengthsum/20;
		accelerometerMin=accelerometer_averagre;
		accelerometerMin_thres=4;
		accelerometerMax=accelerometer_averagre;
		accelerometerMax_thres=36;
		threshold_high=(float) (accelerometer_averagre+0.5);
		threshold_low=(float) (accelerometer_averagre-0.5);
	}
	
	public boolean StepDetection(float accelerometerLength, float time_init)
 	{
 		if(accelerometerMin>accelerometerLength)
 		{
 			accelerometerMin=accelerometerLength;
 		}
 		if(accelerometerMin<accelerometerMin_thres)
 		{
 			accel_flag=0;
 			accelerometerMin=accelerometer_averagre;
 		}

 		if(accelerometerMax<accelerometerLength)
 		{
 			accelerometerMax=accelerometerLength;
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
 			if(accelerometerLength>threshold_high)
 			{
 				time_temp1=time_show;
 				accel_flag=1;
 			}
 			break;
 		case 1:	
 			time_temp2=time_show;
 			if(accelerometerLength<threshold_low)
 			{
 				if((time_temp2-time_temp1)>0.15&&(time_temp2-time_temp1)<0.75)
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
 			if(accelerometerLength>threshold_high)
 			{
 				if((time_temp3-time_temp1)>0.3&&(time_temp3-time_temp1)<1.5)
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

 	public int steplengthestimate(){
 		steplength=(int) (0.95*(4+2*frequency));
 		return steplength;
 	}
}
