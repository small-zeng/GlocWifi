package com.indoor.tools;

import java.util.Locale;
import java.util.Vector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorsAdmin implements SensorEventListener{
	public float ax;
	public float ay;
	public float az;
	public float mx;
	public float my;
	public float mz;
	public float x;
	public float y;
	public float z;
	float[] accelerometerVector=new float[3];
	float[] magVector=new float[3];
	float[] resultMatrix=new float[9];
	float[] values=new float[3];
	public float magnetStrength = 0;
	public float accStrength = 0;
	Vector<Float> magnetVector=new Vector<Float>();	
	SensorManager sensorManager;
	Sensor magnet;
	Sensor accelerometer;
	Sensor baro;

    public float baroValue;
	
	public SensorsAdmin(Context context){

	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) 
		{
			ax = event.values[0];
			ay = event.values[1];
			az = event.values[2];
			accelerometerVector=event.values;
			accelerometerVector=event.values;//是否多余？
			accStrength= (float) (Math.sqrt(ax*ax+ay*ay+az*az));
		} 

		else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
		{
			mx = event.values[0];
			my = event.values[1];
			mz = event.values[2];
			magVector=event.values;
			magnetStrength= (float) (Math.sqrt(mx*mx+my*my+mz*mz));
			magnetVector.add(magnetStrength);

		}
		else if(event.sensor.getType() == Sensor.TYPE_PRESSURE){
            baroValue = event.values[0];
//            String textToUpdate_baro = String.format(Locale.US, "baroValue = %03f", baroValue);
        }
		SensorManager.getRotationMatrix(resultMatrix, null, accelerometerVector, magVector);
		SensorManager.getOrientation(resultMatrix, values);
		x =(float) Math.toDegrees(values[0]);
		y = (float) Math.toDegrees(values[1]);
		z = (float) Math.toDegrees(values[2]);


	}
	
	public void registerSensor(Context context)
	{
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		magnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		baro = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		sensorManager.registerListener(this, magnet, SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, baro, SensorManager.SENSOR_DELAY_GAME);
	}
	
	public void unregister(){
		sensorManager.unregisterListener(this, accelerometer);
		sensorManager.unregisterListener(this, magnet);
		sensorManager.unregisterListener(this, baro);
	}
}
