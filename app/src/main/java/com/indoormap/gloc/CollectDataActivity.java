package com.indoormap.gloc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.indoormap.BuildConfig;
import com.indoormap.R;
import com.indoormap.ble.DeviceListActivity;
import com.indoormap.ble.UartService;
import com.indoormap.map.PublicData;
import com.indoormap.mapshow.DataCollectSurfaceView;
import com.indoormap.recyclerview.MyRecyclerViewAdapter;
import com.indoormap.tools.AP;
import com.indoormap.tools.AccProcessor;
import com.indoormap.tools.ContentUriUtil;
import com.indoormap.tools.DeleteFileUtil;
import com.indoormap.tools.MyToast;
import com.indoormap.tools.UriTofilePath;
import com.indoormap.tools.UserStepDetection;
import com.indoormap.tools.availablePoint;
import com.indoormap.tools.SensorsAdmin;
import com.indoormap.tools.UserData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CollectDataActivity extends Activity implements OnClickListener,RadioGroup.OnCheckedChangeListener{

    //选择文件相关
    private static final int SELECT_FILE = 100;
    String filepath= "/storage/emulated/0/Gloc/uwbLocMagPoints.txt";

    //矩形边界
    public static ArrayList<ArrayList<Integer>>boundaryPointList =new ArrayList<>();
    public static  ArrayList<availablePoint> uwbPointList = new ArrayList<>();


    //BLE蓝牙相关
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static  final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private Button btnConnectDisconnect;

    //从蓝牙获取UWB数据
    private String uwbData = "";
    public static int uwbLoc_x = 0,uwbLoc_y=0;
    private boolean uwbBuildMapFlag = false;
    private Toast mToast = null;

	private ListView listview;
    private RecyclerView recyclerview;
    private MyRecyclerViewAdapter adapter;
	private EditText pathname,pathStart,pathEnd,uwbPosition,angle;
    private TextView mStepTextView;
    private TextView initialmagTextView;
    private TextView transformedmagTextView;
    private TextView magangleTextView;
    private TextView uwbLocTextView;
	private Spinner RecordButton;
	private Button uwbSinglePointButton,uwbMultiplePointsButton,tickButton,selectFileButton;
	private Chronometer chrono;
	private WifiManager mywifiManager ;
	private long elapsedTime;
	private StringBuilder sb;
	private int nb_recorded=0;
	Timer wifiTimer=new Timer();
	private Long systime;
	List<ScanResult> results;
	ArrayList<String> APMessage = new ArrayList<String>();
	ArrayList<String>list_bssid=new ArrayList<String>();

	//文件记录标志
	public static boolean  showMapFlag = false;

	//记录传感器数据，用于计步
    public SensorsAdmin sensorsAdmin;
    Timer step = new Timer() ;
    int initialcount=0;
    float accStrengthsum=0;
    public float time_init;
    public static int stepcount;
    public UserStepDetection userstepdetection=new UserStepDetection();

    // acc
    ArrayList<Integer> steps ;
    ArrayList<ArrayList<Float>> accData ;
    ArrayList<ArrayList<Float>> accFilterData;
    AccProcessor accprocessor ;
    float accMag;
    private int nSampleTotal = 0, nSampleTrue = 0, downSamplingRate= 5;
    private TimerTask stepTask = null;
    private int lastStep = 0,newStep = 0;

    //采集数据有效性标志位
    boolean pathInputValid=false;
    boolean PointInputValid=false;

    //便于记录方向
    public UserData userdata=new UserData();
    String systemPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;

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
	protected void onCreate(Bundle savedInstanceState)  {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collect);
        pathStart=(EditText)findViewById(R.id.pathstart);
//        pathStart.setText("0,0");
        pathEnd=(EditText)findViewById(R.id.pathend);
//        pathEnd.setText("0,0");
        uwbPosition=(EditText)findViewById(R.id.uwbposition);
        uwbPosition.setText("0,0");
        mStepTextView = (TextView) findViewById(R.id.stepcounter) ;
        mStepTextView.setText("  step:0");
        uwbLocTextView = (TextView) findViewById(R.id.uwbLoc);
        angle = (EditText) findViewById(R.id.angle);
		RecordButton = findViewById(R.id.record);
		RecordButton.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String choosed = RecordButton.getItemAtPosition(position).toString();
                switch (choosed){
                    case "all map" : DataCollectSurfaceView.speed = -1;break;
                    case "static map" : DataCollectSurfaceView.speed = 0;break;
                    case "read map" :uwbPointList.clear();readUwbFile(filepath);DataCollectSurfaceView.speed = -1;
                                     MyToast.showToast(CollectDataActivity.this,"成功读取文件"+filepath+"的地图", Toast.LENGTH_SHORT);break;
                    case "slow 1" : DataCollectSurfaceView.speed = 1; DataCollectSurfaceView.direction = 1;break;
                    case "slow 2" : DataCollectSurfaceView.speed = 1; DataCollectSurfaceView.direction = 2;break;
                    case "slow 3" : DataCollectSurfaceView.speed = 1; DataCollectSurfaceView.direction = 3;break;
                    case "slow 4" : DataCollectSurfaceView.speed = 1; DataCollectSurfaceView.direction = 4;break;
                    case "normal 1" : DataCollectSurfaceView.speed = 2; DataCollectSurfaceView.direction = 1;break;
                    case "normal 2" : DataCollectSurfaceView.speed = 2; DataCollectSurfaceView.direction = 2;break;
                    case "normal 3" : DataCollectSurfaceView.speed = 2; DataCollectSurfaceView.direction = 3;break;
                    case "normal 4" : DataCollectSurfaceView.speed = 2; DataCollectSurfaceView.direction = 4;break;
                    case "fast 1" : DataCollectSurfaceView.speed = 3; DataCollectSurfaceView.direction = 1;break;
                    case "fast 2" : DataCollectSurfaceView.speed = 3; DataCollectSurfaceView.direction = 2;break;
                    case "fast 3" : DataCollectSurfaceView.speed = 3; DataCollectSurfaceView.direction = 3;break;
                    case "fast 4" : DataCollectSurfaceView.speed = 3; DataCollectSurfaceView.direction = 4;break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                DataCollectSurfaceView.speed = -1;
            }
        });
        uwbSinglePointButton = (Button)findViewById(R.id.uwbSinglePoint);
        uwbSinglePointButton.setOnClickListener(this);
        uwbMultiplePointsButton = (Button) findViewById(R.id.uwbMultiplePoints);
        uwbMultiplePointsButton.setOnClickListener(this);
        tickButton = (Button)findViewById(R.id.tick) ;
        tickButton.setOnClickListener(this);
        selectFileButton = (Button) findViewById(R.id.selectfile);
        selectFileButton.setOnClickListener(this);
        initialmagTextView = (TextView) findViewById(R.id.initialmag);
        transformedmagTextView = (TextView) findViewById(R.id.transformedmag);
        magangleTextView = (TextView) findViewById(R.id.magangle);
//		listview=(ListView)findViewById(R.id.listView);
        recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        //添加分割线
        recyclerview.addItemDecoration(new DividerItemDecoration(CollectDataActivity.this, DividerItemDecoration.VERTICAL));
        recyclerview.addItemDecoration(new DividerItemDecoration(CollectDataActivity.this, DividerItemDecoration.VERTICAL));

        sensorsAdmin= new SensorsAdmin( CollectDataActivity.this,initialmagTextView,transformedmagTextView,magangleTextView,tickButton,uwbPosition);
		mywifiManager=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//		mywifiManager.startScan();
		results = mywifiManager.getScanResults();

		//建立文件夹Gloc
        File myDir=new File(Environment.getExternalStorageDirectory()+File.separator+"Gloc"+ File.separator);
        Boolean success=true;
        if(!myDir.exists())
        {
            success = myDir.mkdir();
        }
        //计时时钟
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
        stepTimerBegin();

		//开启BLE蓝牙
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnConnectDisconnect = (Button) findViewById(R.id.searchBtn);
        btnConnectDisconnect.setOnClickListener(this);
        service_init();

        sensorsAdmin.locImuUpdateStart();
        permissionRequest();

        //矩形边界斜对着的两个顶点
        boundaryPointList.add(new ArrayList<Integer>());
        boundaryPointList.add(new ArrayList<Integer>());

        //读取修正角度
        UserData.getCalibrationAngle();
        angle.setText(String.valueOf(UserData.calibrationAngle));

        //开启姿态解算
        sensorsAdmin.locImuUpdateStart();

	}



    public void openSystemFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // 所有类型
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivityForResult(Intent.createChooser(intent, "请选择文件"), SELECT_FILE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(CollectDataActivity.this, "请安装文件管理器", Toast.LENGTH_SHORT).show();
        }
    }

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
        case R.id.searchBtn:
            System.out.println("Click_SearchButton");
            if (!mBtAdapter.isEnabled()) {
                Log.i(TAG, "onClick - BT not enabled yet");
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            }
            else {
                if (mState == UART_PROFILE_DISCONNECTED){
                    //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
                    Intent newIntent = new Intent(CollectDataActivity.this, DeviceListActivity.class);
                    startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                } else {
                    //Disconnect button pressed
                    if (mDevice!=null)
                    {
                        mService.disconnect();

                    }
                }
            }
            break;
        case  R.id.uwbMultiplePoints:
            if(uwbMultiplePointsButton.getText().equals("连续")){
                MyToast.showToast(CollectDataActivity.this,"UWB连续建图---开始", Toast.LENGTH_SHORT);
                uwbBuildMapFlag = true;
                uwbMultiplePointsButton.setText("停止");
                //设置基准时间并开始计时
                chrono.setBase(SystemClock.elapsedRealtime());
                chrono.start();
            }
            else {
                MyToast.showToast(CollectDataActivity.this,"UWB连续建图---停止", Toast.LENGTH_SHORT);
                uwbBuildMapFlag = false;
                uwbMultiplePointsButton.setText("连续");
                //停止计时
                chrono.stop();
            }

            break;
        case R.id.uwbSinglePoint:
            //检查输入UWB坐标是否正确
            String[] str=uwbPosition.getText().toString().split(",");
            if(str.length==2) {
                availablePoint uwbavailablePoint = new availablePoint(Integer.parseInt(str[0]), Integer.parseInt(str[1]));
                if (uwbavailablePoint.x>0 && uwbavailablePoint.x<PublicData.MapWidth &&uwbavailablePoint.y>0 && uwbavailablePoint.y<PublicData.MapHeight) {
                    PointInputValid=true;
                }
                else {
                    PointInputValid=false;
                }
            }
            else {
                PointInputValid=false;
            }
            //记入文件
            if(PointInputValid){
//                APshow();
                savewifiToStorage_Point(uwbPosition.getText().toString(),writeAp());
                MyToast.showToast(CollectDataActivity.this,"静止建图---成功记录位置("+uwbPosition.getText().toString()+")的 WiFi样本", Toast.LENGTH_SHORT);
            }
            else{
                MyToast.showToast(CollectDataActivity.this, "静止建图---输入坐标无效，请重新输入!!!", Toast.LENGTH_SHORT);
            }
            break;
        case R.id.selectfile:
            MyToast.showToast(CollectDataActivity.this,"请选择地图文件", Toast.LENGTH_SHORT);
            openSystemFile();
            break;
        case R.id.tick:
           /* if(!sensorsAdmin.isMagRecordFlag()){
                String dir = systemPath + "Gloc" + File.separator + "tick.txt";
                DeleteFileUtil.deleteFile(dir);
                File mywifiFile=new File(Environment.getExternalStorageDirectory()+File.separator+"Gloc"+ File.separator,"tick"+".txt");
                sensorsAdmin.setMagRecordFlag(true);
                tickButton.setText("stop");
                Toast.makeText(CollectDataActivity.this, "开始记录地磁数据到文件:tick.txt", Toast.LENGTH_SHORT).show();
            }
            else {
                sensorsAdmin.setMagRecordFlag(false);
                tickButton.setText("tick");
                Toast.makeText(CollectDataActivity.this, "停止记录地磁数据到文件:tick.txt", Toast.LENGTH_SHORT).show();
            }*/

            if(!sensorsAdmin.isMagRecordFlag()){
                tickButton.setText("stop");
                sensorsAdmin.setMagRecordFlag(true);
                Toast.makeText(CollectDataActivity.this, "开始记录地磁数据到文件:tick.txt", Toast.LENGTH_SHORT).show();
            }




            break;
		default:
			chrono.setBase(SystemClock.elapsedRealtime());
			break;
		}
	}


    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        btnConnectDisconnect.setBackgroundResource(R.drawable.bluetooth);
                        showMessage("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
                        mState = UART_PROFILE_CONNECTED;
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        btnConnectDisconnect.setBackgroundResource(R.drawable.bluetooth_red);
                        showMessage("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();
                    }
                });
            }


            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            System.out.println("BLE数据："+text);

                            if(text.contains("Tag")){
                                uwbData = "";
                                uwbData = uwbData +text;
                            }
                            else {
                                uwbData = uwbData +text;
                                if(uwbData.contains("Tag") && uwbData.contains("END")){
                                    System.out.println("得到数据："+uwbData);
                                    String[] str1 = uwbData.split(",");
                                    if(Integer.parseInt(str1[6])>0) {
                                        uwbLocTextView.setText("uwbLoc:" + str1[3] + "," + str1[4] + ";" + str1[6]);
                                        uwbLoc_x = Integer.parseInt(str1[3])/100;
                                        uwbLoc_y = Integer.parseInt(str1[4])/100;

                                        if(uwbBuildMapFlag) {
                                            if (mState == UART_PROFILE_CONNECTED) {
                                                System.out.println("uwbtimestamp="+System.currentTimeMillis());
                                                String availablePoint = "" + uwbLoc_x + "," + uwbLoc_y;
                                                uwbPointList.add(new availablePoint(uwbLoc_x, uwbLoc_y, UserData.walkState, userdata.getUserFourDirection(UserData.pdrDirection)));
                                                saveMagToStorage_uwbLocPoints(availablePoint, SensorsAdmin.magres);
                                            } else {
                                                MyToast.showToast(CollectDataActivity.this,"请连接蓝牙!!!", Toast.LENGTH_SHORT);
                                            }
                                        }




                                    }
                                }
                            }


                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }


        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    //Timer and Tasks
	void wifiTimerBegin(){
		wifiTimer.scheduleAtFixedRate(task1,0,100);
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
        if(step != null && stepTask != null ) {
            step.scheduleAtFixedRate(stepTask, 300, 50);
        }
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
                boolean wifiFlag = mywifiManager.startScan();
                System.out.println("扫描返回标志："+wifiFlag);
                UserData.calibrationAngle = Float.parseFloat(angle.getText().toString());
                if(UserData.calibrationAngle!=UserData.calibrationAngleOld){
                    UserData.calibrationAngleOld = UserData.calibrationAngle;
                    UserData.storageCalibrationAngle();
                    MyToast.showToast(CollectDataActivity.this,"方向修正成功", Toast.LENGTH_SHORT);
                }

                //检查输入起点和终点是否正确
                String[] str1=pathStart.getText().toString().split(",");
                String[] str2=pathEnd.getText().toString().split(",");
                if(str1.length==2 && str2.length==2) {
                    availablePoint start = new availablePoint(Integer.parseInt(str1[0]), Integer.parseInt(str1[1]));
                    availablePoint end = new availablePoint(Integer.parseInt(str2[0]), Integer.parseInt(str2[1]));
                    if (start.x > 0 && start.x < PublicData.MapWidth && start.y > 0 && start.y < PublicData.MapHeight &&
                            end.x > 0 && end.x < PublicData.MapWidth && end.y > 0 && end.y < PublicData.MapHeight) {
                        boundaryPointList.get(0).clear();
                        boundaryPointList.get(0).add(start.x);
                        boundaryPointList.get(0).add(start.y);
                        boundaryPointList.get(1).clear();
                        boundaryPointList.get(1).add(end.x);
                        boundaryPointList.get(1).add(end.y);

                    }
                }

                break;
            case 2:
                System.out.println("进入中断");
                if(initialcount<40&&sensorsAdmin.accStrength>0)
                {
                    accStrengthsum=accStrengthsum+sensorsAdmin.accStrength;
                    initialcount=initialcount+1;
                    if(initialcount==40)
                    {
                        userstepdetection.StepParaInit(accStrengthsum);
                    }
                }
                else{
                    userdata.updateSensorData();
                    userdata.isPhoneStatic(10);
                    if(userstepdetection.StepDetection(sensorsAdmin.accStrength,time_init))
                    {
                        System.out.println("计步");
                        stepcount=stepcount+1;
                        userdata.pdrStatisticData();

                        if(stepcount>1) {
                            if(userdata.isPhoneShaking()){
                                stepcount=stepcount - 1;
                            }
                            else {
                                userstepdetection.steplengthestimate(userdata.meanVar_acc_Strength[1], (float) (1));
                                userdata.getPdrDirection(stepcount, sensorsAdmin.state[2]);
                                userdata.getWalkDis();
                            }
                        }
                        //清除缓存
                        userdata.pdrBuffClear();
                    }

                    if(stepcount>1){
                        mStepTextView.setText(stepcount+","+UserData.walkState
                                +","+userdata.getUserFourDirection(userdata.pdrDirection)+","+(int)userdata.pdrDirection);
                    }

                    userstepdetection.stepflag=false;


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
        systime=System.currentTimeMillis();
		list_bssid.clear();
		String sTime = systime.toString();
		int counter=0;

        List<AP> ApList = new ArrayList<AP>();
        for (ScanResult result : results)
        {
            ApList.add(new AP(result.BSSID,result.SSID,result.level));
        }
        //wifi排序
        System.out.println("按RSS降序：");
        Collections.sort(ApList, new SortByRss());
        for (AP ap : ApList) {
//            System.out.println(ap.toString());
            counter++;
            sb.append(sTime +";");
            sb.append(counter +";");
            sb.append(ap.getWifiMac()+";");
            sb.append(ap.getWifiSSID()+";");
            sb.append(ap.getWifiLevel()+";");
            sb.append("\n");
        }

		counter = 0;	    	
		return sb;
	}

    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
            if (success) {
                System.out.println("扫描成功");
                APshow();
                if(uwbBuildMapFlag) {
                    if (mState == UART_PROFILE_CONNECTED) {
                            String availablePoint = "" + uwbLoc_x + "," + uwbLoc_y;
//                            uwbPointList.add(new availablePoint(uwbLoc_x, uwbLoc_y, UserData.walkState, userdata.getUserFourDirection(UserData.pdrDirection)));
//                            savewifiToStorage_uwbLocPoints(availablePoint, writeAp());
                    } else {
                        MyToast.showToast(CollectDataActivity.this,"请连接蓝牙!!!", Toast.LENGTH_SHORT);
                    }
                }
            } else {
                // scan failure handling
                System.out.println("扫描失败");
            }
        }
    };

    private void APshow()
	{
//		boolean wifiFlag = mywifiManager.startScan();
//        System.out.println("扫描返回标志："+wifiFlag);
        results = mywifiManager.getScanResults();
        APMessage.clear();
		list_bssid.clear();

        List<AP> ApList = new ArrayList<AP>();
		for (ScanResult result : results)
		{
//			Log.i("WiFi---", "wifi结果: " + result.SSID + "  RSSI:" + result.level  );
            ApList.add(new AP(result.BSSID,result.SSID,result.level));
		}
        //wifi排序
        System.out.println("按RSS降序：");
        Collections.sort(ApList, new SortByRss());
        for (AP ap : ApList) {
            System.out.println(ap.toString());
            APMessage.add("AP :"+ap.getWifiSSID()+ "/"+ap.getWifiMac()+ "/" +ap.getWifiLevel());
            list_bssid.add(ap.getWifiMac());
        }

        //设置RecyclerView适配器
        adapter=new MyRecyclerViewAdapter(CollectDataActivity.this,APMessage);
        recyclerview.setAdapter(adapter);
//        System.out.println("分割线："+recyclerview.getItemDecorationCount());

        //LayoutManager
        recyclerview.setLayoutManager(new LinearLayoutManager(CollectDataActivity.this,LinearLayoutManager.VERTICAL,false));

	}

	private void readUwbFile(String filepath)  {
        String dir = filepath;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dir)));
            String line;
            String[] str = null;
            String[] str1 = null;
            int P_x = 0;
            int P_y = 0;
            while ((line = br.readLine()) != null) {
                System.out.println("come");
                str = line.split(";");
                if (str[0].equals("mag")) {
                    str1 = str[1].split(",");
                    P_x = Integer.parseInt(str1[0]);
                    P_y = Integer.parseInt(str1[1]);
                    uwbPointList.add(new availablePoint(P_x,P_y,2,1));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void savewifiToStorage_uwbLocPoints(String Point,StringBuilder res)
    {
        File mywifiFile=new File(Environment.getExternalStorageDirectory()+File.separator+"Gloc"+ File.separator,"uwbLocPoints"+".txt");
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
                String resultWifi="UWB;" + Point+","+UserData.walkState+","
                        + userdata.getUserFourDirection(UserData.pdrDirection)+"\n"+ res+"";
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

    private void saveMagToStorage_uwbLocPoints(String Point,String res)
    {
        File mywifiFile=new File(Environment.getExternalStorageDirectory()+File.separator+"Gloc"+ File.separator,"uwbLocMagPoints"+".txt");
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
                String resultWifi="mag;" + Point+";"+res+"\n";
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

    private void savewifiToStorage_Point(String Point,StringBuilder res)
    {
        File mywifiFile=new File(Environment.getExternalStorageDirectory()+File.separator+"Gloc"+ File.separator,"UWBStaticPoints"+".txt");
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
                String resultWifi="UWB;" +Point+","+0+","+"0"+","+1+"\n"+res+"";
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


    //按RSS降序
    class SortByRss implements Comparator {
        public int compare(Object o1, Object o2) {
            AP s1 = (AP) o1;
            AP s2 = (AP) o2;
            if (s1.getWifiLevel() < s2.getWifiLevel())
                return  1;
            else if (s1.getWifiLevel() > s2.getWifiLevel())
                return -1;
            else
                return  0;
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
        unregisterReceiver(wifiScanReceiver);
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorsAdmin.registerSensor(CollectDataActivity.this);
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mywifiManager.startScan();

        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onDestroy(){
        stepTaskStop();
        super.onDestroy();
        sensorsAdmin.unregister();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_FILE:
                if(resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    filepath = UriTofilePath.getFilePathByUri(this, uri);
                    System.out.println("文件URL：" + uri);
                    System.out.println("文件路径：" + filepath);
                    Toast.makeText(this, "文件路径：" + filepath, Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
//                    ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                    mService.connect(deviceAddress);


                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }


    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.popup_title)
                    .setMessage(R.string.popup_message)
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.popup_no, null)
                    .show();
        }
    }

    public void permissionRequest(){
        //循环申请字符串数组里面的权限，在小米中是直接弹出一个权限框等待用户确认，确认一次既将上面数组里面的权限全部申请
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {
            ArrayList<String> permisssions = new ArrayList<>();
            for(int i=0;i<PERMISSIONS_STORAGE.length;i++){
                if(ActivityCompat.checkSelfPermission(this,PERMISSIONS_STORAGE[i]) != PackageManager.PERMISSION_GRANTED){
                    permisssions.add(PERMISSIONS_STORAGE[i]);
                }
            }

            if(permisssions.size()>0){
                ActivityCompat.requestPermissions(this, permisssions.toArray(new String[permisssions.size()]),
                        REQUEST_PERMISSION_CODE);
            }
        }


    }

}

