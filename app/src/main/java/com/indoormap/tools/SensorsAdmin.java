package com.indoormap.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;

import com.indoormap.gloc.CollectDataActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Locale;

public class SensorsAdmin implements SensorEventListener{

    public static float ax,ay,az;
    public float lax,lay,laz;
    public float lax_n,lay_n,laz_n;
    public float mx,my,mz;
    public float grax,gray,graz;
    public float gx,gy,gz;
    public float x,y,z;
    public int filterNum=1;
    private float alpha =(float) 0.25;
    public ArrayList<ArrayList<Float>> accBuff = new ArrayList<ArrayList<Float>>();
    public ArrayList<ArrayList<Float>> magBuff = new ArrayList<ArrayList<Float>>();

    float R11,R12,R13,R21,R22,R23,R31,R32,R33; //旋转矩阵
    public static float[] state=new float[3];
    public static float[] accelerometerVector=new float[3];
    public float[] laccelerometerVector=new float[3];
    public float[] gravityVector=new float[3];
    public float[] magVector=new float[3];
    public float[] gyroVector=new float[3];
    float[] accelerometerVectorLast=new float[3];
    float[] magVectorLast=new float[3];
    float[] resultMatrix=new float[9];
    float[] values=new float[3];
    float [] A_D ;
    public float [] A_W = new float[3];
    public float [] A_W_Valid = new float[3];
    public float [] mag_n = new float[3];
    public float MagnInclination;//磁倾角
    public float MagnDeclination;//磁偏角
    public static float accStrength = 0;
    public float laccStrength = 0;
    public float gravityStrength = 0;
    public float magnetStrength = 0;
    public float gyroStrength = 0;
    public float magnettransStrength = 0;
    public static String magres ="";
    public static String magdata = "";
    SensorManager sensorManager;
    Sensor magnet;
    Sensor accelerometer;
    Sensor gravity;
    Sensor gyro;
    Sensor baro;
    Sensor linearacc;
    Sensor mOrientation ;

    public float baroValue;

    // text show
    private TextView mTextToShow_initialmag;
    private TextView mTextToShow_transformedmag;
    private TextView mTextToShow_magangle;
    private Button tickButton;
    private TextView mTextToShow_uwbposition;
    private boolean showFlag=false;
    private int nSamplingShow=0;
    private int nSamplingMagFilter=0;
    private float aveTransMagX=0,aveTransMagY=0,aveTransMagZ=0,aveTransMagStrength=0;
    private float sumTransMagX=0,sumTransMagY=0,sumTransMagZ=0,sumTransMagStrength=0;
    private boolean isTransValid=false;
    private boolean magRecordFlag=false;
    Boolean success=true;//文件是否生成成功
    File myMagFile=new File(Environment.getExternalStorageDirectory()+File.separator+"Gloc"+ File.separator,"tick"+".txt");;

    // text show
    private TextView mTextToShow_gyro;
    private TextView mTextToShow_mag;
    private TextView mTextToShow_acc;
    private TextView mTextToShow_gra;
    private TextView mTextToShow_ori;
    private TextView mTextToShow_ori1;
    private TextView mTextToShow_baro;
    private TextView mTextToShow_linearacc;
    public  static String screenStatus = "landscape";

    //IMU测试相关
    private boolean sensorTestFlag=false;
    private float q0=0,q1=0,q2=0,q3=1;
    private float exInt=0,eyInt=0,ezInt=0,Kp= 20,Ki=(float)0.05,Kp1=(float)100,Ki1=(float)1;
    private long nSample=0;
    private long gTs_last=0,gTs_now=0;
    private int magSampleNum=0;

    //定位IMU实时更新相关
    private boolean locImuUpdateFlag = false;

    public void locImuUpdateStart(){
        locImuUpdateFlag = true;
    }


    public SensorsAdmin(Context context){
//        showFlag=false;
    }

    public SensorsAdmin(Context context, TextView initialmag, TextView transformedmag, TextView magangle, Button tick,TextView uwbposition){
        mTextToShow_initialmag=initialmag;
        mTextToShow_transformedmag=transformedmag;
        mTextToShow_magangle=magangle;
        tickButton = tick;
        mTextToShow_uwbposition = uwbposition;
        showFlag=true;
    }

    public SensorsAdmin(Context context, TextView text1, TextView text2, TextView text3, TextView text4,
                        TextView text5, TextView text6, TextView text7,TextView text8, boolean testFlag){
        mTextToShow_gyro  = text1;
        mTextToShow_mag = text2;
        mTextToShow_acc  = text3;
        mTextToShow_gra  = text4;
        mTextToShow_ori= text5;
        mTextToShow_ori1= text6;
        mTextToShow_baro = text7;
        mTextToShow_linearacc = text8;
        sensorTestFlag   = testFlag;
    }

    public boolean isMagRecordFlag() {
        return magRecordFlag;
    }

    public void setMagRecordFlag(boolean magRecordFlag) {
        this.magRecordFlag = magRecordFlag;
        File myDir=new File(Environment.getExternalStorageDirectory()+File.separator+"Gloc"+ File.separator);
        if(!myDir.exists())
        {
            success = myDir.mkdir();
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];
            accelerometerVector=event.values;
            accStrength= (float) (Math.sqrt(ax*ax+ay*ay+az*az));
            //Sensor 测试
            if(sensorTestFlag){
                String textToUpdate_acc = String.format(Locale.US, "accVal = (%03f, %03f, %03f, %03f)", ax, ay, az, accStrength);
                mTextToShow_acc.setText(textToUpdate_acc);
            }

        }

        else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION)
        {
            lax = event.values[0];
            lay = event.values[1];
            laz = event.values[2];
            laccelerometerVector=event.values;
            laccStrength= (float) (Math.sqrt(lax*lax+lay*lay+laz*laz));
            //Sensor 测试
            if(sensorTestFlag){
                String textToUpdate_lacc = String.format(Locale.US, "laccVal = (%03f, %03f, %03f, %03f)", lax, lay, laz, laccStrength);
                mTextToShow_linearacc.setText(textToUpdate_lacc);
            }

        }

        else if (event.sensor.getType() == Sensor.TYPE_GRAVITY)
        {
            grax = event.values[0];
            gray = event.values[1];
            graz = event.values[2];
            gravityVector=event.values;
            gravityStrength= (float) (Math.sqrt(grax*grax+gray*gray+graz*graz));

            //Sensor 测试
            if(sensorTestFlag){
                String textToUpdate_gra = String.format(Locale.US, "graVal = (%03f, %03f, %03f, %03f)", grax, gray, graz, gravityStrength);
                mTextToShow_gra.setText(textToUpdate_gra);
            }

        }

        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            mx = event.values[0];
            my = event.values[1];
            mz = event.values[2];
            magVector=event.values;
            magnetStrength= (float) (Math.sqrt(mx*mx+my*my+mz*mz));



            //Sensor 测试
            if(sensorTestFlag){
                String textToUpdate_mag = String.format(Locale.US, "magVal = (%03f, %03f, %03f, %03f)", mx, my, mz, magnetStrength);
                mTextToShow_mag.setText(textToUpdate_mag);
            }

        }

        else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            gx = event.values[0];
            gy = event.values[1];
            gz = event.values[2];
            gyroVector=event.values;
            gyroStrength=(float) (Math.sqrt(gx*gx+gy*gy+gz*gz));

            //Sensor 测试
            if(sensorTestFlag || locImuUpdateFlag){
                gTs_now=System.currentTimeMillis();
//                System.out.println("姿态解算");
                if(nSample>100){
                    float[] gravityVector1 = new float[]{gravityVector[0],gravityVector[1],gravityVector[2]};
                    float[] gyroVector1 = new float[]{gyroVector[0],gyroVector[1],gyroVector[2]};
                    float[] magVector1 = new float[]{magVector[0],magVector[1],magVector[2]};
                    imuUpdate(gravityVector1,gyroVector1,magVector1,state,(float)((gTs_now-gTs_last)/1000.0));
                }
                nSample++;
                gTs_last=gTs_now;
            }
            if(sensorTestFlag) {
                String textToUpdate_gyro = String.format(Locale.US, "gyroVal = (%03f, %03f, %03f, %03f)", gx, gy, gz, gyroStrength);
                mTextToShow_gyro.setText(textToUpdate_gyro);
            }

        }

        else if(event.sensor.getType() == Sensor.TYPE_PRESSURE){
            baroValue = event.values[0];
            if(sensorTestFlag) {
                String textToUpdate_baro = String.format(Locale.US, "baroValue = %03f", baroValue);
                System.out.println(textToUpdate_baro);
                mTextToShow_baro.setText(textToUpdate_baro);
            }
        }


       if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD )
        {
            SensorManager.getRotationMatrix(resultMatrix, null, gravityVector, magVector);
            SensorManager.getOrientation(resultMatrix, values);
            x = (float) Math.toDegrees(values[0]);
            y = (float) Math.toDegrees(values[1]);
            z = (float) Math.toDegrees(values[2]);
            if(sensorTestFlag){
                String textToUpdate_ori_1 = String.format(Locale.US, "oriVal_1 = (%03f, %03f, %03f)", z, -y, x);
                mTextToShow_ori1.setText(textToUpdate_ori_1);
            }
            nSamplingShow++;
            nSamplingMagFilter++;

            //坐标变换
            A_D = event.values.clone();
            A_W[0] = resultMatrix[0] * A_D[0] + resultMatrix[1] * A_D[1] + resultMatrix[2] * A_D[2];
            A_W[1] = resultMatrix[3] * A_D[0] + resultMatrix[4] * A_D[1] + resultMatrix[5] * A_D[2];
            A_W[2] = resultMatrix[6] * A_D[0] + resultMatrix[7] * A_D[1] + resultMatrix[8] * A_D[2];
            magnettransStrength = (float) (Math.sqrt(A_W[0] * A_W[0] + A_W[1] * A_W[1] + A_W[2] * A_W[2]));

            isTransValid=true;
            MagnInclination=(float) Math.toDegrees( Math.atan2(-A_W[2], Math.sqrt(A_W[0]*A_W[0]+A_W[1]*A_W[1])));
            MagnDeclination=(float) Math.toDegrees( Math.atan2(-A_W[0], A_W[1]));
            A_W_Valid[0]= A_W[0];A_W_Valid[1]= A_W[1];A_W_Valid[2]= A_W[2];

            sumTransMagX+=A_W[0];sumTransMagY+=A_W[1];sumTransMagZ+=A_W[2];sumTransMagStrength+=magnettransStrength;
            if(nSamplingMagFilter == 5){
                aveTransMagX = sumTransMagX/5;aveTransMagY = sumTransMagY/5;aveTransMagZ = sumTransMagZ/5;aveTransMagStrength = sumTransMagStrength/5;
                magres = aveTransMagX+","+ aveTransMagY+","+aveTransMagZ+","+aveTransMagStrength+ ";";
                sumTransMagX =0;sumTransMagY=0;sumTransMagZ=0;sumTransMagStrength=0;
                nSamplingMagFilter =0;
            }

            //记录地磁传感器数据进入文件
            if(magRecordFlag)
            {
                System.out.println("开始记录");
                magSampleNum ++;
                if(magSampleNum <=1000) {//记录2s数据
                    magdata = magdata+"mag;"+ magSampleNum+";"+ A_W[0] + "," + A_W[1] + "," + A_W[2] + "," + magnettransStrength + ";" + "\n";
                    if(magSampleNum ==1000) {
                        String str=mTextToShow_uwbposition.getText().toString();
                        magdata = "UWB;" + str+";" + "\n"+magdata;
//                            magdata = "UWB;" + CollectDataActivity.uwbLoc_x + "," + CollectDataActivity.uwbLoc_y + ";" + "\n"+magdata;
                        try {
                            FileOutputStream outMag = new FileOutputStream(myMagFile, true);
                            outMag.write(magdata.getBytes());
                            outMag.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        tickButton.setText("tick");
                        magRecordFlag = false;
                        magSampleNum = 0;
                        magdata = "";
                    }
                }
            }

//           //路径数据采集
//            if(magRecordFlag){
//                String magres="mag;"+ A_W[0]+","+ A_W[1]+","+ A_W[2]+ "," + magnettransStrength +";"+"\n";
//                try {
//                    FileOutputStream outMag = new FileOutputStream(myMagFile, true);
//                    outMag.write(magres.getBytes());
//                    outMag.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }


//                   System.out.println("变换后地磁数据：" + A_W[0] + "," + A_W[1] + ","  +A_W[2] + "," + magnettransStrength);
//                  System.out.println("重力数据：" + gx + "," + gy + "," + gz + "," + gravityStrength);
//                  System.out.println("地磁数据：" + mx + "," + my + "," + mz + "," + magnetStrength);
//                  System.out.println("方向数据：" + x + "," + y + "," + z);

            //显示
            if(showFlag) {
                System.out.println("timestamp="+System.currentTimeMillis());
                if(A_W[0]<1e-6){
                    A_W[0]=Float.parseFloat("0.000000");
                }
                if( nSamplingShow%10==0){
                    nSamplingShow=0;
                    mTextToShow_initialmag.setText("b_mag:" + magnetStrength + ";" + mx + "," + my + "," + mz);
                    mTextToShow_transformedmag.setText("n_mag:" + magnettransStrength + ";" + A_W[0] + "," + A_W[1] + "," + A_W[2]);
                    mTextToShow_magangle.setText("mag_angle:" + MagnInclination + "," + MagnDeclination);
                }
            }

        }

    }


    /*数据融合 互补滤波*/
    public void imuUpdate(float[] acc, float[] gyro, float[] mag,float[] state , float dt){
        float normalise;
        float ex, ey, ez;
        float q0s, q1s, q2s, q3s; /*四元数的平方*/
        float vecxZ, vecyZ, veczZ; /*机体坐标系下的Z方向向量*/
        float halfT =0.5f * dt;
        float[] tempacc =acc;
        float hx,hy,hz;
        float by,bz;
        float wx,wy,wz;

        /*单位化磁力计测量值*/
        normalise = (float) (1.0/Math.sqrt(mag[0] * mag[0] + mag[1] * mag[1] + mag[2] * mag[2]));
        mag[0] *= normalise;
        mag[1] *= normalise;
        mag[2] *= normalise;
        /*单位化加速度计测量值*/
        normalise = (float) (1.0/Math.sqrt(acc[0] * acc[0] + acc[1] * acc[1] + acc[2] * acc[2]));
        acc[0] *= normalise;
        acc[1] *= normalise;
        acc[2] *= normalise;
        /*将b系中测量的地磁分量[mx,my,mz]转换到n系，得到[hx,hy,hz]*/
        hx=(q0*q0+q1*q1-q2*q2-q3*q3)*mag[0]+2*(q1*q2-q0*q3)*mag[1]+2*(q1*q3+q0*q2)*mag[2];
        hy=2*(q1*q2+q0*q3)*mag[0]+(q0*q0-q1*q1+q2*q2-q3*q3)*mag[1]+2*(q2*q3-q0*q1)*mag[2];
        hz=2*(q1*q3-q0*q2)*mag[0]+2*(q2*q3+q0*q1)*mag[1]+(q0*q0-q1*q1-q2*q2+q3*q3)*mag[2];
        /*得到n系中的地磁向量的真实值[bx,by,bz],其中bx=0*/
        by=(float) Math.sqrt(hx*hx+hy*hy);
        bz=hz;
        /*从n系中的地磁向量[bx，by,bz]转换到b系中，得到[wx,wy,wz]*/
        wx=2*(q1*q2+q0*q3)*by+2*(q1*q3-q0*q2)*bz;
        wy=(q0*q0-q1*q1+q2*q2-q3*q3)*by+2*(q2*q3+q0*q1)*bz;
        wz=2*(q2*q3-q0*q1)*by+(q0*q0-q1*q1-q2*q2+q3*q3)*bz;
        /*机体坐标系下的Z方向向量*/
        vecxZ = 2 * (q1 * q3 - q0 * q2); /*矩阵(3,1)项*/
        vecyZ = 2 * (q0 * q1 + q2 * q3); /*矩阵(3,2)项*/
        veczZ = q0*q0 - q1*q1 - q2*q2 + q3*q3; /*矩阵(3,3)项*/
        /*加速计读取的方向与重力方向的差值，磁力计读取的磁场与经过四元数矩阵变换的磁场的差值，用向量叉乘计算*/
        ex = (acc[1] * veczZ - acc[2] * vecyZ)+(mag[1]*wz-mag[2]*wy);
        ey = (acc[2] * vecxZ - acc[0] * veczZ)+(mag[2]*wx-mag[0]*wz);
        ez = (acc[0] * vecyZ - acc[1] * vecxZ)+(mag[0]*wy-mag[1]*wx);
        /*误差累计，与积分常数相乘*/
        exInt += Ki*ex*dt;
        eyInt += Ki*ey*dt;
        ezInt += Ki*ez*dt;
        /*积分限幅*/
        if(Math.abs(exInt)>0.1){
            exInt=Math.signum(exInt)*(float) 0.1;
        }
        if(Math.abs(eyInt)>0.1){
            eyInt=Math.signum(eyInt)*(float) 0.1;
        }
        if(Math.abs(ezInt)>0.1){
            ezInt=Math.signum(ezInt)*(float) 0.1;
        }
        /*用叉积误差来做PI修正陀螺零偏，即抵消陀螺读数中的偏移量*/
        gyro[0] += (Kp * ex + exInt);
        gyro[1] += (Kp * ey + eyInt);
        gyro[2] += (Kp * ez + ezInt);
        /* 一阶近似算法，四元数运动学方程的离散化形式和积分 */
        float q0Last = q0;
        float q1Last = q1;
        float q2Last = q2;
        float q3Last = q3;
        q0 += (-q1Last * gyro[0] - q2Last * gyro[1] - q3Last * gyro[2]) * halfT;
        q1 += ( q0Last * gyro[0] + q2Last * gyro[2] - q3Last * gyro[1]) * halfT;
        q2 += ( q0Last * gyro[1] - q1Last * gyro[2] + q3Last * gyro[0]) * halfT;
        q3 += ( q0Last * gyro[2] + q1Last * gyro[1] - q2Last * gyro[0]) * halfT;

        //单位化四元数在空间旋转时不会拉伸，仅有旋转角度，这类似线性代数里的正交变换
        float norm = (float) Math.sqrt(q0*q0 + q1*q1 + q2*q2 + q3*q3);
//        System.out.println("norm="+norm);
        q0 = q0 / norm;
        q1 = q1 / norm;
        q2 = q2 / norm;
        q3 = q3 / norm;

        R12 = 2 * (q1 * q2 - q0 * q3); /*矩阵(1,2)项*/
        R22 = q0*q0 - q1*q1 + q2*q2 - q3*q3; /*矩阵(2,2)项*/

        /*机体坐标系下的Z方向向量*/
        R31 = 2 * (q1 * q3 - q0 * q2); /*矩阵(3,1)项*/
        R32 = 2 * (q0 * q1 + q2 * q3); /*矩阵(3,2)项*/
        R33 = q0*q0 - q1*q1 - q2*q2 + q3*q3; /*矩阵(3,3)项*/

        double Epsilon = 0.0009765625f;
        double Threshold = (0.5f - Epsilon)*2;
        if(R32<-Threshold || R32>Threshold){
            int sign = (int)Math.signum(R32);
            state[2] = (float) Math.toDegrees(-2 * sign * (float)Math.atan2(q2, q0)); // yaw
            state[1] =  (float) Math.toDegrees(sign * (Math.PI / 2.0)); // pitch
            state[0] =  (float) Math.toDegrees(0); // roll
        }
        else {

            state[0] = (float) Math.toDegrees(Math.atan2(-R31, R33));//roll
            state[1] = (float) Math.toDegrees(Math.asin(R32));//pitch
            state[2] = (float) Math.toDegrees(Math.atan2(R12, R22));//yaw
        }
        //重力感应
        if( gravityVector[1] > 4){
            screenStatus = "portrait(+)";
        }
        else if(gravityVector[1] < -4){
            screenStatus = "portrait(-)";
        }
        else if(gravityVector[0] > 4){
            screenStatus = "landscape(+)";
        }
        else if(gravityVector[0] < -4){
            screenStatus = "landscape(-)";
        }

        R11= q0*q0 + q1*q1 - q2*q2 - q3*q3;
        R13= 2*(q1*q3+q0*q2);
        R21= 2*(q1*q2+q0*q3);
        R23= 2*(q2*q3-q0*q1);

        if(sensorTestFlag) {

            lax_n = R11*lax+R12*lay+R13*laz;
            lay_n = R21*lax+R22*lay+R23*laz;
            laz_n = R31*lax+R32*lay+R33*laz;

            String textToUpdate_ori = String.format(Locale.US, "oriVal = (%03f, %03f, %03f),"
                            + "\nR=(%03f, %03f, %03f)" + "\nn_mag=(%03f, %03f, %03f)"
                            + "\ne=(%03f, %03f, %03f)" + "\neInt=(%03f, %03f, %03f)"
                            + "\nlax=(%03f, %03f, %03f)"
                            + "\nmag_n1=(%03f, %03f, %03f)"
                            +"\nmag_n2=(%03f, %03f, %03f)"
                            +"\n"+screenStatus,
                    state[0], state[1], state[2],
                    -R31, R32, R33, (float) hx, hy, hz,
                    ex, ey, ez, exInt, eyInt, ezInt,
                    lax_n,lay_n,laz_n,mag_n[0],mag_n[1],mag_n[2],A_W[0],A_W[1],A_W[2]);
            mTextToShow_ori.setText(textToUpdate_ori);

        }

    }



    //滑动平均滤波
    public float[] movingAverageFilter(ArrayList<ArrayList<Float>> buff){
        float[] sum = new float[3];
        float[] average = new float[3];
        for(int i=0;i<buff.size();i++){
            for(int j=0;j<3;j++){
                sum[j]+=buff.get(i).get(j);
            }
        }
        for(int j=0;j<3;j++){
            average[j]=sum[j]/buff.size();
        }
        return average;
    }

    //清除二维数组缓存
    public  <T> void arrayListClear( ArrayList<ArrayList<T>> array){
        for(int i=0;i<array.size();i++){
            array.get(i).clear();
        }
        array.clear();
    }

    public void registerSensor(Context context)
    {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        magnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        baro = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        gravity=sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        gyro=sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        linearacc =sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, magnet, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, baro, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this,gyro,sensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this,linearacc,sensorManager.SENSOR_DELAY_GAME);
    }

    public void unregister(){
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, magnet);
        sensorManager.unregisterListener(this, baro);
        sensorManager.unregisterListener(this, gravity);
        sensorManager.unregisterListener(this, gyro);

    }


}
