package com.indoormap.gloc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.indoormap.R;
import com.indoormap.tools.DeleteFileUtil;
import com.indoormap.tools.SensorsAdmin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

public class InformationActivity extends Activity implements View.OnClickListener {

    private TextView gyroTextShow, magTextShow, accTextShow,graTextShow, oriTextShow,oriTextShow1, baroTextShow,laccTextShow;
    private Button oriButton,imuButton;
    private boolean oriRecordFlag=false;
    private boolean imuRecordFlag=false;
    private SensorsAdmin sensorsAdmin;
    Timer imuRecordTimer=new Timer();
    TimerTask task1;
    String systemPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    File myOriFile=new File(Environment.getExternalStorageDirectory()+File.separator+"Gloc"+ File.separator,"orientation"+".txt");
    File myImuFile=new File(Environment.getExternalStorageDirectory()+File.separator+"Gloc"+ File.separator,"imu"+".txt");
    private int oriSampleNum=0;
    private long Ts=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_information);

        gyroTextShow = (TextView) findViewById(R.id.gyrotextshow);
        magTextShow = (TextView) findViewById(R.id.magnettextshow);
        accTextShow = (TextView) findViewById(R.id.acctextshow);
        laccTextShow = (TextView) findViewById(R.id.linearacctextshow);
        graTextShow = (TextView) findViewById(R.id.gratextshow);
        oriTextShow = (TextView) findViewById(R.id.oritextshow);
        oriTextShow1 = (TextView) findViewById(R.id.oritextshow1);
        baroTextShow = (TextView) findViewById(R.id.barotextshow);
        oriButton = (Button) findViewById(R.id.record);
        oriButton.setOnClickListener(this);
        imuButton =  (Button) findViewById(R.id.record1);
        imuButton.setOnClickListener(this);
        sensorsAdmin= new SensorsAdmin( InformationActivity.this,gyroTextShow,magTextShow,
                accTextShow,graTextShow,oriTextShow,oriTextShow1,baroTextShow,laccTextShow,true);

        //开启定时器，记录IMU数据
        imuTimerBegin();

    }

    //Timer and Tasks
    private void imuTimerBegin(){
        if (imuRecordTimer == null) {
            imuRecordTimer = new Timer();
        }
        if (task1 == null) {
            task1 = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            };
        }
        if(imuRecordTimer != null && task1 != null ) {
            imuRecordTimer.scheduleAtFixedRate(task1, 200, 50);
        }
    }
    void imuTimerStop(){
        if (imuRecordTimer != null) {
            imuRecordTimer.cancel();
            imuRecordTimer = null;
        }
        if (task1 != null) {
            task1.cancel();
            task1 = null;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.record:
                if(!oriRecordFlag){
                    oriRecordFlag=true;
                    oriButton.setText("Recording");
//                    //开启定时器，记录IMU数据
//                    imuTimerBegin();
                    Ts=System.currentTimeMillis();
                    Toast.makeText(getBaseContext(),"开始记录姿态角数据",Toast.LENGTH_SHORT).show();
                }
//                else{
//                    imuRecordFlag=false;
//                    imuButton.setText("start");
//                    Toast.makeText(getBaseContext(),"停止记录姿态角数据",Toast.LENGTH_SHORT).show();
//                }
                break;
            case R.id.record1:
                if(!imuRecordFlag){
                    imuRecordFlag=true;
                    imuButton.setText("Stop");
                    String dir = systemPath + "Gloc" + File.separator +"imu.txt";
                    DeleteFileUtil.deleteFile(dir);
                    Toast.makeText(getBaseContext(),"开始记录IMU数据",Toast.LENGTH_SHORT).show();
                }
                else{
                    imuRecordFlag=false;
                    imuButton.setText("Start");
                    Toast.makeText(getBaseContext(),"停止记录IMU数据",Toast.LENGTH_SHORT).show();
                }


                break;
        }

    }


    @SuppressLint("HandlerLeak")
    final Handler handler=new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                    case 1:
                    if(oriRecordFlag){
                        oriSampleNum++;
                        try
                        {
                            String orires="ori;" +Ts+";"+sensorsAdmin.state[0]+","+sensorsAdmin.state[1]+","+sensorsAdmin.state[2]
                                    + ";"+sensorsAdmin.z+","+(-sensorsAdmin.y)+","+sensorsAdmin.x+";"+"\n";
                            FileOutputStream outOri = new FileOutputStream(myOriFile,true);
                            outOri.write(orires.getBytes());
                            outOri.close();
                        }
                        catch(FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                        if(oriSampleNum==100){
                            oriSampleNum=0;
//                            imuTimerStop();
                            oriRecordFlag=false;
                            oriButton.setText("Record");
                            Toast.makeText(getBaseContext(),"停止记录姿态角数据",Toast.LENGTH_SHORT).show();
                        }

                    }
                    else  if(imuRecordFlag) {
                        saveImuToStorage();
                    }
                    break;
            }
        }
    };

    private void saveImuToStorage()
    {
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
                Ts=System.currentTimeMillis();
                String imures="imu;" +Ts+";"+sensorsAdmin.state[0]+","+sensorsAdmin.state[1]+","+sensorsAdmin.state[2]+";"
                        +sensorsAdmin.accelerometerVector[0]+","+sensorsAdmin.accelerometerVector[1]+","+sensorsAdmin.accelerometerVector[2]+";"
                        +sensorsAdmin.gravityVector[0]+","+sensorsAdmin.gravityVector[1]+","+sensorsAdmin.gravityVector[2]+";"
                        +sensorsAdmin.gyroVector[0]+","+sensorsAdmin.gyroVector[1]+","+sensorsAdmin.gyroVector[2]+";"
                        +sensorsAdmin.magVector[0]+","+sensorsAdmin.magVector[1]+","+sensorsAdmin.magVector[2]+";"
                        +sensorsAdmin.baroValue+";"
                        +sensorsAdmin.laccelerometerVector[0]+","+sensorsAdmin.laccelerometerVector[1]+","+sensorsAdmin.laccelerometerVector[2]+";"
                        +sensorsAdmin.lax_n+","+sensorsAdmin.lay_n+","+sensorsAdmin.laz_n+";"
                        +"\n";
                System.out.println("Ts="+Ts);
                FileOutputStream outImu = new FileOutputStream(myImuFile,true);
                outImu.write(imures.getBytes());
                outImu.close();
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

    @Override
    protected void onPause() {
        super.onPause();
        sensorsAdmin.unregister();
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorsAdmin.registerSensor(InformationActivity.this);
    }

    @Override
    protected void onDestroy(){
        imuTimerStop();
        super.onDestroy();
        sensorsAdmin.unregister();

    }



}
