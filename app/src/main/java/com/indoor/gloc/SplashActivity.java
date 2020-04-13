package com.indoor.gloc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.indoor.R;


public class SplashActivity extends Activity {
	
	private static final long SPLASHTIME = 500;
	private static final int STOPSPLASH = 0;
	
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
                        final Intent intent = new Intent(SplashActivity.this, MapActivity.class);
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
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash, menu);
		return true;
	}

}

