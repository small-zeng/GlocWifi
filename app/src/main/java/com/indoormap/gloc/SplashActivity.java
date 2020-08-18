package com.indoormap.gloc;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.indoormap.R;

import java.util.ArrayList;


public class SplashActivity extends Activity {
	
	private static final long SPLASHTIME = 500;
	private static final int STOPSPLASH = 0;

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
	
//	/**
//     * Handler to close this activity and to start automatically {@link MainActivity}
//     * after <code>SPLASHTIME</code> seconds.
//     */

	private final transient Handler splashHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
        	if (msg.what == STOPSPLASH)
            {
                final Animation animation = AnimationUtils.loadAnimation(getBaseContext(), android.R.anim.slide_out_right);
                animation.setAnimationListener(new AnimationListener()
                {
                    @Override public void onAnimationEnd(Animation animation)
                    {
                        ((RelativeLayout)findViewById(R.id.Splash)).setVisibility(View.INVISIBLE);
//                        final Intent intent = new Intent(SplashActivity.this, MapActivity.class);
                        final Intent intent = new Intent(SplashActivity.this, CollectDataActivity.class);
//                        final Intent intent = new Intent(SplashActivity.this, InformationActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override public void onAnimationRepeat(Animation animation)
                    {
                    }

                    @Override public void onAnimationStart(Animation animation)
                    {
                    }
                });

                ((RelativeLayout)findViewById(R.id.Splash)).startAnimation(animation);
            }

            super.handleMessage(msg);
		 }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		final Message msg=new Message();
		msg.what=STOPSPLASH;
		splashHandler.sendMessageDelayed(msg, SPLASHTIME);

//        //动态权限申请
//        permissionRequest();
        System.out.println(2);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash, menu);
		return true;
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

