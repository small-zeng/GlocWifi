package com.indoormap.gloc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.indoormap.R;
import com.indoormap.map.Floor;
import com.indoormap.map.Map;
import com.indoormap.map.PublicData;
import com.indoormap.mapshow.MySurfaceView;
import com.indoormap.particlefilter.ParticleFilter;
import com.indoormap.tools.BaroProcessor;
import com.indoormap.tools.NaviInstruction;
import com.indoormap.tools.SensorsAdmin;
import com.indoormap.tools.UserData;
import com.indoormap.tools.UserStepDetection;
import com.indoormap.tools.WifiAdmin;

public  class MapActivity extends Activity implements Runnable{
	
	//ImageButton
	private ImageButton record;
	private ImageButton createmap;
	private ImageButton localize;
	private ImageButton navigate;
	private ImageButton infos;
	private ImageButton exit;



	private TextView textview_step=null;
	private TextView textview_direction=null;
	private TextView textview_floor=null;
	private TextView textview_position=null;
	private TextView textview_staircase=null;
	private TextView textview_wifisignal = null;
	private AutoCompleteTextView startEditText=null;
	private AutoCompleteTextView endEditText=null;
    private Timer step = new Timer() ;
    private Timer floorLevel = new Timer() ;
	public SensorsAdmin sensorsAdmin= new SensorsAdmin(MapActivity.this);
	public UserData userdata=new UserData();
	public UserStepDetection userstepdetection=new UserStepDetection();
	public Map map;
	public Floor floor=new Floor();
	public ParticleFilter particlefilter=new ParticleFilter();
	public static int LocationX;
	public static int LocationY;
	public static int direction;
	//added
	int initialcount=0;
	float accStrengthsum=0;
    public float time_init;
    public static int stepcount;
	public static ParticleFilter p = null;

    public static  boolean createmapflag=false;
    public boolean firstBuildMap=true;


    //气压计检测楼层
    private ArrayList<Long> baroDataTsTrue = new ArrayList<Long>();
    ArrayList<Integer> climb = new ArrayList<Integer>();
    ArrayList<ArrayList<Integer>> level = new ArrayList<ArrayList<Integer>>();
    ArrayList<Float>   baroV = new ArrayList<Float>();
    ArrayList<ArrayList<Float>> baroFilter = new ArrayList<ArrayList<Float>>();
    BaroProcessor baroprocessor = new BaroProcessor();

    //导航方向相关
    private NaviInstruction navDir, navUp, navDown;
    //建图子线程、进度条
    Thread mapCreat;
    private ProgressBar mapprogressbar;


    DecimalFormat df=new DecimalFormat("0.00");

    //读写权限 具体权限加在字符串里面
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.UPDATE_DEVICE_STATS,
    };
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		textview_step = (TextView) findViewById(R.id.step);
		textview_direction = (TextView) findViewById(R.id.direction);
		textview_floor=(TextView) findViewById(R.id.floor);
		textview_position=(TextView) findViewById(R.id.position);
        textview_staircase=(TextView) findViewById(R.id.staircase);
        textview_wifisignal = (TextView) findViewById(R.id.wifisign);
		record=(ImageButton)findViewById(R.id.record1);//修改record-->record1
		record.setOnClickListener(new ButtonListener1());
		localize=(ImageButton)findViewById(R.id.localize);
		localize.setOnClickListener(new ButtonListener2());
		createmap=(ImageButton)findViewById(R.id.createmap);
		createmap.setOnClickListener(new ButtonListener3());
		infos=(ImageButton)findViewById(R.id.infos);
		infos.setOnClickListener(new ButtonListener4());
		exit=(ImageButton)findViewById(R.id.exit);
		exit.setOnClickListener(new ButtonListener5());
		navigate =(ImageButton)findViewById(R.id.navigate);
		navigate.setOnClickListener(new ButtonListener6());
        startEditText=findViewById(R.id.gueststartPointText);
        endEditText=findViewById(R.id.guestendPointText);

       // Initialize NaviInstruction image
        navDir = new NaviInstruction();
        navDir.img = (ImageView) findViewById(R.id.dir);
        navDir.bitmap0 = BitmapFactory.decodeResource(getResources(), R.drawable.dir);
        navDir.bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.arrival);
        navUp = new NaviInstruction();
        navUp.img = (ImageView) findViewById(R.id.upstairs);
        navUp.bitmap0 = BitmapFactory.decodeResource(getResources(), R.drawable.upstairs);
//        navUp.disappear();
        navDown = new NaviInstruction();
        navDown.img = (ImageView) findViewById(R.id.downstairs);
        navDown.bitmap0 = BitmapFactory.decodeResource(getResources(), R.drawable.downstairs);
//        navDown.disappear();

        //路径相关初始化
		p = particlefilter;
        arrayListClear(MySurfaceView.pointList);
        //建图子线程
        mapCreat = new Thread(this);
        mapprogressbar = (ProgressBar) findViewById(R.id.progressbar);
        map = new Map(mapprogressbar);

		//动态权限申请
		permissionRequest();

		//读取修正角度
		UserData.getCalibrationAngle();

	}

    @Override
    public void run() {
//        map.createmap(Floor.floorID);
		map.createMap(Floor.floorID);
        Message message = new Message();
        message.what = 3;
        handler.sendMessage(message);
    }

    //New Data
	class ButtonListener1 implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			Intent i=new Intent(MapActivity.this,CollectDataActivity.class);
			startActivity(i);
		}
	}
	//Localization
	class ButtonListener2 implements OnClickListener {

		@Override
		public void onClick(View v)
		{
			//启动wifi
			userdata.wifiAdmin=new WifiAdmin(MapActivity.this);
			if(userdata.wifiAdmin.checkState()!=3);
			{
				userdata.wifiAdmin.openWifi();
				userdata.wifiAdmin.startScan();
			    Toast.makeText(MapActivity.this, "please wait for 3s",Toast.LENGTH_SHORT).show();
			}
			if(createmapflag=true)
			{
				particlefilter.initParticles();
				if(firstBuildMap) {
                    TimerBegin();
                    sensorsAdmin.locImuUpdateStart();
                    firstBuildMap=false;
                }
                createmap.setImageAlpha(120);
                record.setImageAlpha(120);
                localize.setImageAlpha(120);
                navigate.setImageAlpha(120);
                infos.setImageAlpha(120);
                exit.setImageAlpha(120);
			}
		}
	}
	//Build Map
	class ButtonListener3 implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
            PublicData.getrout(Floor.floorID);
			if(!createmapflag)
			{
                Toast.makeText(MapActivity.this, "map is being creating,please wait for 10s", Toast.LENGTH_SHORT).show();
                mapCreat.start();
				createmapflag=true;
			}
			else
			{
                Toast.makeText(MapActivity.this, "map has been created", Toast.LENGTH_SHORT).show();
			}
            stepcount=0;
			textview_step.setText("Steps:");
		}
	}
	//Information
	class ButtonListener4 implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
//			Intent ii=new Intent(MapActivity.this,OfflineActivity.class);
//			startActivity(ii);
//			stepcount=stepcount-1;
			Intent ii=new Intent(MapActivity.this,InformationActivity.class);
			startActivity(ii);

		}
	}
	//Close
	class ButtonListener5 implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			System.exit(0);
		}
	}

	//Navigation
	class ButtonListener6 implements OnClickListener
	{
		@Override
		public void onClick(View v) {
//		    String start,end;
//		    start=startEditText.getText().toString();
//            end=endEditText.getText().toString();
//			RouteProgramming routeProgramming =new RouteProgramming(MapActivity.this,start,end);
//			//起点和终点坐标输入正确才计算最短路径
//			if(routeProgramming.inputValid){
//                Toast.makeText(MapActivity.this,"开始路径规划......",Toast.LENGTH_SHORT).show();
//                routeProgramming.test(routeProgramming.startPoint, routeProgramming.endPoint);
//                Toast.makeText(MapActivity.this,"完成路径规划......",Toast.LENGTH_SHORT).show();
//            }
//            //在图中显示规划的路径
//            arrayListClear(MySurfaceView.pointList);
//			int index=0;
//            for(Point point : routeProgramming.shortestPath){
//                MySurfaceView.pointList.add(new ArrayList<Integer>());
////                MySurfaceView.pointList.get(index).add(point.x);
////                MySurfaceView.pointList.get(index).add(point.y);
//                index++;
//            }
//
//			Toast.makeText(MapActivity.this,"Start Navigation...",Toast.LENGTH_SHORT).show();

		}
	}


	void TimerBegin(){
     	step.scheduleAtFixedRate(executeNewStep,100,50 );

//        floorLevel.scheduleAtFixedRate(executeNewBaro,100,40 );
	}
	
 	TimerTask executeNewStep = new TimerTask()
 	{

 			public void run()
 			{
 				Message message = new Message();
 				message.what = 1;
 				handler.sendMessage(message);
 			}
 	};

    TimerTask executeNewBaro = new TimerTask()
    {

        public void run()
        {
            Message message = new Message();
            message.what = 2;
            handler.sendMessage(message);
        }
    };

	@SuppressLint("Handlerleak")
 	final  Handler handler = new Handler()
 	{
 		public void handleMessage(Message msg){
 			switch(msg.what){
 			case 1:
//              textview_staircase.setText("Staircase Section ,"+sensorsAdmin.x);
 				if(initialcount<40&&sensorsAdmin.accStrength>0)
 				{
					accStrengthsum=accStrengthsum+sensorsAdmin.accStrength;
					initialcount=initialcount+1;
					if(initialcount==40)
					{
						userstepdetection.StepParaInit(accStrengthsum);					
					}
 				}
 				else
 				{
                    userdata.updateSensorData();
                    userdata.isPhoneStatic(10);

 					if(userstepdetection.StepDetection(sensorsAdmin.accStrength,time_init))
 					{
 						textview_wifisignal.setText("waiting for wifi update");
 						mapprogressbar.setProgress(0);
						userdata.GetUserWifiVector();
// 						while(!UserData.wifiListUpdate()){
//							userdata.GetUserWifiVector();
//						}
						mapprogressbar.setProgress(50);
 						stepcount=stepcount+1;
                        userdata.pdrStatisticData();

 						if(stepcount>1) {
// 						    if(userdata.isPhoneShaking()){
// 						          stepcount -=1;
//                            }
//                          else{
 						        userstepdetection.steplengthestimate(userdata.meanVar_acc_Strength[1],(float)(1));
 						        userdata.getPdrDirection(stepcount,sensorsAdmin.state[2]);
 						        userdata.getWalkDis();

                                particlefilter.aggregateParticles();
                                particlefilter.getPosition();
                                LocationX=particlefilter.resultX;
                                LocationY=particlefilter.resultY;

                                particlefilter.updateParticles(userdata.pdrDirection+155,2.5 * UserStepDetection.steplength);
                                textview_position.setText("Position:("+LocationX + ","+LocationY+")"+","
                                        +Map.availableArea.size()+","+Map.availableArea.size()/160);
                                textview_wifisignal.setText("particle updated");
                                mapprogressbar.setProgress(0);
//                            }

                        }



                        //清除缓存
                        userdata.pdrBuffClear();

 					}

 					if(stepcount > 1) {
                        textview_step.setText("Steps:" + stepcount + ";"
                                + df.format(userdata.meanVar_acc_Strength[1]) + "," + df.format(userdata.meanVar_acc_z[1]) + "," + df.format(userdata.meanVar_acc_xy[1]) + ";"
                                + df.format(userdata.meanVar_acc_z[0]) + "," + df.format(userdata.meanVar_acc_xy[0]) + ";"
                                + df.format(userdata.meanVar_ori_x[1]) + "," + df.format(userdata.meanVar_ori_y[1]) + "," + df.format(userdata.meanVar_ori_z[1]));
                        textview_direction.setText("Direction:" + userdata.walkStateType + ";"
                                + df.format(UserStepDetection.steplength) + ";" + df.format(userdata.walkDis) + "," + df.format(userdata.walkVecDis) + ";"
                                + userdata.getUserPdrDirection((float) userdata.pdrDirection) + ";" + (float) userdata.pdrDirection);
                        textview_floor.setText("Current Level:" + floor.floorID + ";" + df.format(userdata.minMax_acc_Strength[0]) + ","
                                + df.format(userdata.minMax_acc_Strength[1]) + "," + df.format(userstepdetection.accelerometer_averagre) + ";");
                    }

                    userstepdetection.stepflag=false;
 				}
 				break;
            case 2:
                String textToUpdate_baro = String.format(Locale.US, "%03f", sensorsAdmin.baroValue);
                textview_floor.setText("Current Level:"+floor.floorID+","+textToUpdate_baro);
                baroV. add(sensorsAdmin.baroValue);
//                System.out.println("Size:"+baroV.size());
                baroDataTsTrue.add(System.currentTimeMillis());
                baroprocessor.levelDetection(baroV.size(), climb, baroV, baroFilter, baroDataTsTrue);
                if(BaroProcessor.floorChangeFlag){
                    //清除缓存
                    baroV.clear();
                    climb.clear();
                    arrayListClear(baroFilter);
                    baroprocessor = new BaroProcessor();
                    BaroProcessor.floorChangeFlag = false;
                    Floor.floorID= BaroProcessor.floorLevel;
                    Toast.makeText(MapActivity.this,"当前进入第"+ Floor.floorID+"层",Toast.LENGTH_LONG).show();
					System.out.println("楼层改变");

                    //重新建图
//                    map=new Map();
//                    PublicData.getrout(Floor.floorID);
//                    map.pathNumber=PublicData.rout.length/4;
//                    if(map.allPathsOk(Floor.floorID))
//                    {
//                        map.createMap(Floor.floorID);
//                        createmapflag=true;
//                        Toast.makeText(MapActivity.this, "map has been created", Toast.LENGTH_SHORT).show();
//                    }
//                    else
//                    {
//                        Toast.makeText(MapActivity.this, "Only "+map.file_count+" paths!please finish building map",
//                                Toast.LENGTH_SHORT).show();
//                    }
                }
                break;
            case 3:
                Toast.makeText(MapActivity.this, "map is sucessfully created", Toast.LENGTH_SHORT).show();
                break;
 			default:
 				break;
 			}
 		}
 	};

    //清除二维数组缓存
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
		sensorsAdmin.registerSensor(MapActivity.this);
	}
	
	@Override
	protected void onDestroy(){
//        System.out.println("MapActivity-Destroy");
         stepTaskStop();
//        floorLevel.cancel();
		super.onDestroy();
		sensorsAdmin.unregister();
	}

	/**
	 * 防止在旋转时，重启Activity的onCreate方法
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// 横屏的操作
			System.out.print("横屏");
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			// 竖屏的操作
			System.out.print("竖屏");
		}
	}

	public void permissionRequest(){
        //循环申请字符串数组里面的权限，在小米中是直接弹出一个权限框等待用户确认，确认一次既将上面数组里面的权限全部申请
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {
//            if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
//            {
//                ActivityCompat.requestPermissions(MapActivity.this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
//            }
            ArrayList<String> permisssions = new ArrayList<>();
            for(int i=0;i<PERMISSIONS_STORAGE.length;i++){
                if(ActivityCompat.checkSelfPermission(MapActivity.this,PERMISSIONS_STORAGE[i]) != PackageManager.PERMISSION_GRANTED){
                    permisssions.add(PERMISSIONS_STORAGE[i]);
                }
            }

            if(permisssions.size()>0){
                ActivityCompat.requestPermissions(MapActivity.this, permisssions.toArray(new String[permisssions.size()]),
                        REQUEST_PERMISSION_CODE);
            }
        }


    }

    void stepTaskStop(){
        if (step != null) {
            step.cancel();
            step = null;
        }
        if (executeNewStep != null) {
            executeNewStep.cancel();
            executeNewStep = null;
        }

    }

}
