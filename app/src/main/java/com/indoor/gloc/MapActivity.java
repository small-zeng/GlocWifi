package com.indoor.gloc;

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
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.indoor.R;
import com.indoor.map.Floor;
import com.indoor.map.Map;
import com.indoor.map.PublicData;
import com.indoor.mapshow.MySurfaceView;
import com.indoor.particlefilter.Particle_Filter;
import com.indoor.tools.BaroProcessor;
import com.indoor.tools.NaviInstruction;
import com.indoor.tools.Point;
import com.indoor.tools.RoutePlanning;
import com.indoor.tools.SensorsAdmin;
import com.indoor.tools.UserData;
import com.indoor.tools.UserStepDetection;
import com.indoor.tools.WifiAdmin;

public class MapActivity extends Activity {
	
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
	private AutoCompleteTextView startEditText=null;
	private AutoCompleteTextView endEditText=null;
    private Timer step = new Timer() ;
    private Timer floorLevel = new Timer() ;
	public SensorsAdmin sensorsAdmin= new SensorsAdmin(MapActivity.this);
	public UserData userdata=new UserData();
	public UserStepDetection userstepdetection=new UserStepDetection();
	public Map map=new Map();
	public Floor floor=new Floor();
	public Particle_Filter particlefilter=new Particle_Filter(); 
	public static int LocationX;
	public static int LocationY;
	public static int direction;
	int initialcount=0;
	float accStrengthsum=0;
    public float time_init;	
    public static int stepcount;

    public boolean createmapflag=false;

    //气压计检测楼层
    private ArrayList<Long> baroDataTsTrue = new ArrayList<Long>();
    ArrayList<Integer> climb = new ArrayList<Integer>();
    ArrayList<ArrayList<Integer>> level = new ArrayList<ArrayList<Integer>>();
    ArrayList<Float>   baroV = new ArrayList<Float>();
    ArrayList<ArrayList<Float>> baroFilter = new ArrayList<ArrayList<Float>>();
    BaroProcessor baroprocessor = new BaroProcessor();

    //导航方向相关
    private NaviInstruction navDir, navUp, navDown;


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
        arrayListClear(MySurfaceView.pointList);

		//动态权限申请
		permissionRequest();

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
				particlefilter.init_particles(PublicData.MapWidth, PublicData.MapHeight);
//			    particlefilter.test_particles();
				//获取stepdetection的基准时间
				time_init=userstepdetection.get_time_init();	
				TimerBegin();
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
            map.pathNumber=PublicData.rout.length/4;
			if(map.allPathsOk(Floor.floorID))
			{
				map.createmap(Floor.floorID);
				createmapflag=true;
				Toast.makeText(MapActivity.this, "map has been created", Toast.LENGTH_SHORT).show();
			}
			else
			{
				Toast.makeText(MapActivity.this, "Only "+map.file_count+" paths!please finish building map", Toast.LENGTH_SHORT).show();
			}
		}
	}
	//Information
	class ButtonListener4 implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			Intent ii=new Intent(MapActivity.this,OfflineActivity.class);
			startActivity(ii);
			stepcount=stepcount-1;
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
		    String start,end;
		    start=startEditText.getText().toString();
            end=endEditText.getText().toString();
			RoutePlanning routePlanning =new RoutePlanning(MapActivity.this,start,end);
			//起点和终点坐标输入正确才计算最短路径
			if(routePlanning.inputValid){
                Toast.makeText(MapActivity.this,"开始路径规划......",Toast.LENGTH_SHORT).show();
                routePlanning.test(routePlanning.startPoint,routePlanning.endPoint);
                Toast.makeText(MapActivity.this,"完成路径规划......",Toast.LENGTH_SHORT).show();
            }
            //在图中显示规划的路径
            arrayListClear(MySurfaceView.pointList);
			int index=0;
            for(Point point : routePlanning.shortestPath){
                MySurfaceView.pointList.add(new ArrayList<Integer>());
                MySurfaceView.pointList.get(index).add(point.x);
                MySurfaceView.pointList.get(index).add(point.y);
                index++;
            }

			Toast.makeText(MapActivity.this,"Start Navigation...",Toast.LENGTH_SHORT).show();

		}
	}



	void TimerBegin(){
     	step.scheduleAtFixedRate(executeNewStep,100,100 );
        floorLevel.scheduleAtFixedRate(executeNewBaro,100,40 );
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
 				if(initialcount<20&&sensorsAdmin.accStrength>0)
 				{
					accStrengthsum=accStrengthsum+sensorsAdmin.accStrength;
					initialcount=initialcount+1;
					if(initialcount==20)
					{
						userstepdetection.StepParaInit(accStrengthsum);					
					}
 				}
 				else
 				{
 					if(userstepdetection.StepDetection(sensorsAdmin.accStrength,time_init))
 					{
 						stepcount=stepcount+1;
 						textview_step.setText("Steps:"+stepcount);
 						userstepdetection.steplengthestimate();
 						if(stepcount-1<PublicData.relativelength/10){
 							userdata.GetUserWifiVector();
 							userdata.BuffUserWifiVector(stepcount);
 							textview_direction.setText("direction:"+userdata.getUserStringDirection(sensorsAdmin.x));
 							direction=userdata.getUserDirection(sensorsAdmin.x);
 							userdata.exportuserdata(stepcount, direction, UserStepDetection.steplength,MapActivity.this);
 						}			
 						else
 						{
 							userdata.GetUserWifiVector();
 							userdata.GetUserRelativeWifiVector(stepcount);
 							//导出数据		
 							direction=userdata.getUserDirection(sensorsAdmin.x);
// 							userdata.exportuserdata(stepcount, direction, UserStepDetection.steplength,MapActivity.this);
 							//比较完之后，拿新的数据替代掉旧的数据
 							userdata.BuffUserWifiVector(stepcount);
 							textview_direction.setText("Direction:"+userdata.getUserStringDirection(sensorsAdmin.x));
 							particlefilter.update_particles(direction,UserStepDetection.steplength,userdata.relativeUserWifiMacList,userdata.relativeUserWifiBinList);
 							//得出行人的位置
 							particlefilter.result_particles();
// 							particlefilter.result2_particles(LocationX,LocationY);
// 		 					particlefilter.exportParticleTraces(PublicData.MapWidth, PublicData.MapHeight);
 							LocationX=particlefilter.positionx;
 							LocationY=particlefilter.positiony;
							textview_position.setText("Position:("+LocationX + ","+LocationY+")"+","+UserStepDetection.steplength+","+sensorsAdmin.x+","+direction);
// 							floor.floorchange(LocationX,LocationY,userdata.getUserDirection(sensorsAdmin.x));
// 							if(floor.reloadflag)
// 							{
// 								map.createmap(floor.floorID);
// 							    Toast.makeText(MapActivity.this, "reload map of floor"+floor.floorID,Toast.LENGTH_SHORT).show();
// 							}
 							textview_floor.setText("Current Level:"+floor.floorID);
// 							userdata.exportuserlocation(stepcount, LocationX, LocationY);
 							if(stepcount%PublicData.resamplingfreq==0)
 							{
 								particlefilter.resample(UserStepDetection.steplength);
// 								particlefilter.resample2(UserStepDetection.steplength);
 							}						
 						}					
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
                    map=new Map();
                    PublicData.getrout(Floor.floorID);
                    map.pathNumber=PublicData.rout.length/4;
                    if(map.allPathsOk(Floor.floorID))
                    {
                        map.createmap(Floor.floorID);
                        createmapflag=true;
                        Toast.makeText(MapActivity.this, "map has been created", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(MapActivity.this, "Only "+map.file_count+" paths!please finish building map", Toast.LENGTH_SHORT).show();
                    }
                }

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
                ActivityCompat.requestPermissions(MapActivity.this, permisssions.toArray(new String[permisssions.size()]), REQUEST_PERMISSION_CODE);
            }
        }


    }

}
