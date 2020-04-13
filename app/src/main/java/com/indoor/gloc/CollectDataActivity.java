package com.indoor.gloc;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.indoor.R;
import com.indoor.map.Floor;
import com.indoor.map.PublicData;
import com.indoor.tools.AccProcessor;
import com.indoor.tools.DeleteFileUtil;
import com.indoor.tools.SensorsAdmin;
import com.indoor.tools.UserStepDetection;

public class CollectDataActivity extends Activity implements OnClickListener{
	private ListView listview;
	private EditText pathname;
    private TextView mStepTextView;
	private ImageButton RecordButton;
	private Chronometer chrono;
	private WifiManager mywifiManager;
	private long elapsedTime;
	private StringBuilder sb;
	private int nb_recorded=0;
	Timer wifiTimer=new Timer();
	Time timestamp=new Time();
	List<ScanResult> results;
	ArrayList<String> AP = new ArrayList<String>();
	ArrayList<String>list_bssid=new ArrayList<String>();

	//文件记录标志
	boolean recordFlag = false;

	//记录传感器数据，用于计步
    public SensorsAdmin sensorsAdmin= new SensorsAdmin(CollectDataActivity.this);
    Timer step = new Timer() ;
    // acc
    ArrayList<Integer> steps ;
    ArrayList<ArrayList<Float>> accData ;
    ArrayList<ArrayList<Float>> accFilterData;
    AccProcessor accprocessor ;
    float accMag;
    private int nSampleTotal = 0, nSampleTrue = 0, downSamplingRate= 5;
    private TimerTask stepTask = null;
    private int lastStep = 0,newStep = 0;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.collectwifi);
		pathname = (EditText) findViewById(R.id.pathname);
		pathname.setText("path");
        mStepTextView = (TextView) findViewById(R.id.stepcounter) ;
        mStepTextView.setText("  step:0");
		RecordButton = (ImageButton)findViewById(R.id.record);
		RecordButton.setOnClickListener(this);
		listview=(ListView)findViewById(R.id.listView);
		mywifiManager=(WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		mywifiManager.startScan();
		results = mywifiManager.getScanResults();
		chrono = (Chronometer)findViewById(R.id.chronometer);
        chrono.setBase(SystemClock.elapsedRealtime());
		chrono.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener()
		{
		    @Override
			public void onChronometerTick(Chronometer chronometer) 
			{
			    //当前计时
				elapsedTime = SystemClock.elapsedRealtime() - chrono.getBase();
			}
		});
		wifiTimerBegin();
	}


	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.record:
		    if(!recordFlag){
                recordFlag = true;
                //删除对应的原来路径文件
                // acc
                steps = new  ArrayList<Integer>();
                accData = new ArrayList<ArrayList<Float>>();
                accFilterData = new ArrayList<ArrayList<Float>>();
                accprocessor = new AccProcessor();

                String systemPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
                String dir = systemPath + "Gloc"+File.separator + Floor.floorID+ File.separator+pathname.getText()+"_wifi"+".txt";
                DeleteFileUtil.deleteFile(dir);
                Toast.makeText(getBaseContext(), "start record data in file path : "+pathname.getText().toString(), Toast.LENGTH_SHORT).show();
                //开始计步和采样
                nSampleTrue=0;
                stepTimerBegin();
                RecordButton.setImageDrawable(getResources().getDrawable(R.drawable.record_run));
                //设置基准时间并开始计时
                chrono.setBase(SystemClock.elapsedRealtime());
                chrono.start();
            }
            else{
		        recordFlag = false;
                Toast.makeText(getBaseContext(), "stop record data in file path : "+pathname.getText().toString(), Toast.LENGTH_SHORT).show();
                //停止计时
                chrono.stop();
                //停止计步;
                stepTaskStop();
                RecordButton.setImageDrawable(getResources().getDrawable(R.drawable.record_stop));
                //清理缓存
                steps.clear();
                arrayListClear(accData);
                arrayListClear(accFilterData);
                accprocessor.hashCode();

            }
//			nb_recorded++;
//			savewifiToStorage(pathname.getText().toString(), writeAp());
//			Toast.makeText(getBaseContext(), "record one data in file path : "+pathname.getText().toString(), Toast.LENGTH_SHORT).show();
			break;		
		default:
			chrono.setBase(SystemClock.elapsedRealtime());
			break;
		}
	}
	
	//Timer and Tasks
	void wifiTimerBegin(){
		wifiTimer.scheduleAtFixedRate(task1,0,400);
	}

	TimerTask task1=new TimerTask()
	{
		public void run()
		{
			Message message=new Message();
			message.what=1;
			handler.sendMessage(message);
		}
	};


    private void stepTimerBegin(){
        if (step == null) {
            step = new Timer();
        }
        if (stepTask == null) {
            stepTask = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 2;
                    handler.sendMessage(message);
                }
            };
        }
        if(step != null && stepTask != null )
            step.scheduleAtFixedRate(stepTask,300,20 );

    }

   void stepTaskStop(){
       if (step != null) {
           step.cancel();
           step = null;
       }
       if (stepTask != null) {
           stepTask.cancel();
           stepTask = null;
       }

   }


    @SuppressLint("Handlerleak")
	final Handler handler=new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what)
			{
			case 1:
				APshow();
				break;
            case 2:
                //tem.out.println("recordFlag--"+recordFlag);
                if(recordFlag){
                    // acc
                    System.out.println("accData--get");
                    nSampleTrue++;
                    accData.add(new ArrayList<Float>());
                   //stem.out.println("nSampleTrue："+nSampleTrue);
                    accData.get(nSampleTrue-1).add(-sensorsAdmin.ax/SensorManager.GRAVITY_EARTH);
                    accData.get(nSampleTrue-1).add(-sensorsAdmin.ay/SensorManager.GRAVITY_EARTH);
                    accData.get(nSampleTrue-1).add(sensorsAdmin.az/SensorManager.GRAVITY_EARTH);
                    accData.get(nSampleTrue-1).add(sensorsAdmin.accStrength/SensorManager.GRAVITY_EARTH);
                   //stem.out.println("accDataSize："+accData.size());
                    accprocessor.stepDetection(nSampleTrue, steps, accData, accFilterData);

                    if (nSampleTrue >= 21 + 1) {  // 注意链表中是否有值
                        lastStep = newStep;
                        newStep = steps.get(nSampleTrue - 22);
                        if((newStep-lastStep)==1){//新走一步，记录进文件
//                            APshow();
                            savewifiToStorage(pathname.getText().toString(), writeAp());
                        }
                        mStepTextView.setText("  step:"+steps.get(nSampleTrue - 22));
                    }
                }

                break;
			default:
				break;
			}
		}
	};
	
	private StringBuilder writeAp()
	{
		//start the scan 
		sb = new StringBuilder();
		//collect results
		results = mywifiManager.getScanResults();
		timestamp.setToNow();
		list_bssid.clear();
		String sTime = timestamp.format("%Y-%m-%d %H:%M:%S");
		int counter=0;  
		for (ScanResult result : results)
		{
			list_bssid.add(result.BSSID);
		}
		for (int i=0;i<results.size();i++)
        {
            counter++;
            sb.append(sTime +";");
            sb.append(counter +";");
            sb.append(results.get(i).BSSID+";");
            sb.append(results.get(i).SSID+";");
            sb.append(results.get(i).level+";");
            sb.append("\n");
        }
		/*
//        设置index arraylist
		for (int i=0;i<PublicData.Maclist.length;i++)
		{
			int index=list_bssid.indexOf(PublicData.Maclist[i]);
			if(index!=-1){		    
			counter++;
			sb.append(sTime +";"); 
			sb.append(counter +";"); 
			sb.append(results.get(index).BSSID+";");
			sb.append(results.get(index).SSID+";");
			sb.append(results.get(index).level+";");
			sb.append("\n");
			}	
		}
		*/
		counter = 0;	    	
		return sb;
	}	
	
	private void APshow()
	{
		mywifiManager.startScan();
		results = mywifiManager.getScanResults();
		AP.clear();
		list_bssid.clear();
		for (ScanResult result : results)
		{
			Log.i("WiFi---", "wifi结果: " + result.SSID + "  RSSI:" + result.level  );
            AP.add("AP :"+result.SSID+ "/"+result.BSSID+ "/" +result.level);
			list_bssid.add(result.BSSID);
		}

	/*
		for(int i=0;i<PublicData.Maclist.length;i++)
		{
			int index = list_bssid.indexOf(PublicData.Maclist[i]);
			if(index!=-1)
			{
				AP.add("AP :"+results.get(index).SSID+ "/"+results.get(index).BSSID+ "/" +results.get(index).level);
			}
		}
	*/
		ArrayAdapter<String> adp = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,AP);
		listview.setAdapter(adp);

	}
	
	private void savewifiToStorage(String file,StringBuilder res)
	{
		File mywifiFile=new File(Environment.getExternalStorageDirectory()+File.separator+"Gloc"+ File.separator+Floor.floorID+ File.separator,file+"_wifi"+".txt");
		File myDir=new File(Environment.getExternalStorageDirectory()+File.separator+"Gloc"+ File.separator+Floor.floorID+ File.separator);
		Boolean success=true;
		if(!myDir.exists()) 
		{
			success = myDir.mkdir();
		}
		if(success)
		{
			try
			{
				String resultWifi= res+"";
				FileOutputStream outWifi = new FileOutputStream(mywifiFile,true);
				outWifi.write(resultWifi.getBytes());		
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

	//清理二维Arraylist
    public  <T> void arrayListClear( ArrayList<ArrayList<T>> array){
        for(int i=0;i<array.size();i++){
            array.get(i).clear();
        }
        array.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorsAdmin.unregister();
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorsAdmin.registerSensor(CollectDataActivity.this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        sensorsAdmin.unregister();
    }
}

