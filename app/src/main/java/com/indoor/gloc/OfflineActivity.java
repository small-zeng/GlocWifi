package com.indoor.gloc;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.indoor.R;
import com.indoor.gloc.MapActivity.ButtonListener1;
import com.indoor.gloc.MapActivity.ButtonListener2;
import com.indoor.gloc.MapActivity.ButtonListener3;
import com.indoor.gloc.MapActivity.ButtonListener4;
import com.indoor.gloc.MapActivity.ButtonListener5;
import com.indoor.map.Floor;
import com.indoor.map.Map;
import com.indoor.map.PublicData;
import com.indoor.particlefilter.Particle_Filter;
import com.indoor.tools.OfflineUserData;

public class OfflineActivity extends Activity{
	//ImageButton
	private ImageButton createmap;
	private ImageButton localize;
	private ImageButton exit;
	private TextView textview_step=null;
	private TextView textview_direction=null;
	private Timer step = new Timer() ;

	public OfflineUserData offlineuserdata=new OfflineUserData();
	public Map map=new Map();	
	public Particle_Filter particlefilter=new Particle_Filter();
    public static int stepcount=0;
	public static int LocationX;
	public static int LocationY;
	public boolean createmapflag=false;//确认地图创建完毕
	int floorID=5;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		textview_step = (TextView) findViewById(R.id.step);
		textview_direction = (TextView) findViewById(R.id.direction);
		localize=(ImageButton)findViewById(R.id.localize);
		localize.setOnClickListener(new ButtonListener2());
		createmap=(ImageButton)findViewById(R.id.createmap);
		createmap.setOnClickListener(new ButtonListener3());
		exit=(ImageButton)findViewById(R.id.exit);
		exit.setOnClickListener(new ButtonListener5());
	}
	
	
	class ButtonListener2 implements OnClickListener {
		@Override
		public void onClick(View v)
		{
			if(createmapflag=true)
			{
				offlineuserdata.readuserfile();
				particlefilter.init_particles(PublicData.MapWidth, PublicData.MapHeight);
				TimerBegin();
			}
		}
	}
		
	class ButtonListener3 implements OnClickListener{
		@Override
		public void onClick(View v)
		{
			if(map.allPathsOk(floorID))
			{
				map.createmap(Floor.floorID);
//				map.saveFingerprintMapToStorage(PublicData.MapWidth, PublicData.MapHeight);
//				map.saveRelativeMapToStorage(PublicData.MapWidth,PublicData.MapHeight);
				createmapflag=true;
				Toast.makeText(OfflineActivity.this, "map has been created", Toast.LENGTH_SHORT).show();
			}
			else
			{
				Toast.makeText(OfflineActivity.this, "Only "+map.file_count+" paths!please finish building map", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	
	class ButtonListener5 implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			System.exit(0);
		}
	}
		
    void TimerBegin() {
 		step.scheduleAtFixedRate(executeNewStep,1000,1000 );
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
 	
 	final Handler handler = new Handler()
 	{
 		public void handleMessage(Message msg){
 			switch(msg.what){
 			case 1:
 				stepcount=stepcount+1;
 				textview_step.setText("Step:"+stepcount);
 				if(stepcount-1<PublicData.relativelength/10){
 					offlineuserdata.GetUserWifiVector(stepcount);
 					offlineuserdata.BuffUserWifiVector(stepcount);
 				}
 				else
 				{
 					offlineuserdata.GetUserWifiVector(stepcount);
 					offlineuserdata.GetUserRelativeWifiVector(stepcount);
 					//比较完之后，拿新的数据替代掉旧的数据
 					offlineuserdata.BuffUserWifiVector(stepcount);
 					textview_direction.setText("direction:"+offlineuserdata.getUserStringDirection(stepcount));
 					particlefilter.update_particles(offlineuserdata.getUserDirection(stepcount),offlineuserdata.steplength,offlineuserdata.relativeUserWifiMacList,offlineuserdata.relativeUserWifiBinList);
 					//得出行人的位置
 					particlefilter.result_particles();
 					particlefilter.exportParticleTraces(PublicData.MapWidth, PublicData.MapHeight);
 					LocationX=particlefilter.positionx;
 					LocationY=particlefilter.positiony;
 					System.out.println("X:"+2*LocationX);
 					System.out.println("Y:"+2*LocationY);
 					offlineuserdata.exportuserlocation(stepcount, LocationX, LocationY);
 					if(stepcount%PublicData.resamplingfreq==0)
 					{
 						particlefilter.resample(offlineuserdata.steplength);
 					}
 				}	
 				break;
 			default:
 				break;
 			}
 		}
 	};

}
	
	
	
