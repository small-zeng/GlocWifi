package com.indoor.mapshow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;


import com.indoor.R;
import com.indoor.gloc.MapActivity;
import com.indoor.gloc.OfflineActivity;
import com.indoor.map.Floor;

import java.util.ArrayList;

public class MySurfaceView extends SurfaceView implements Callback, Runnable {

	private Thread th;
	private SurfaceHolder sfh;
	//声明一个画笔
	private Paint paint ,paint1;
	private Canvas canvas;
	private  Bitmap building;
	private  Bitmap stairs;
    private Bitmap redpoint;
    private Bitmap background;
    private Bitmap endpoint;
	private int screenW, screenH;
	private boolean flag=false;
	private int redpointX=420-8;
	private int redpointY=50-5;
	private int floorX=500;
	private int floorY=25;
	private int stairsX=20;
	private int stairsY=450;
	private float redpointX2=stairsX;
	private float redpointY2=stairsY;
	private float Width;
	private float Height;
	private int oldfloorID;
	private boolean reloadflag=false;
	public static boolean pathUpdateFlag = true;
    Matrix matrix = new Matrix();

    public static ArrayList<ArrayList<Integer>> pointList=new ArrayList<>();


	/*
	 * public MySurfaceView(Context context) { super(context); }//备注1（这里一定要引起注意，仔细看下文对备注1的解释 ）
	 */

	public MySurfaceView(Context context, AttributeSet attrs) {//备注1
		// 如果是new出来的此类实例肯定是没有问题，但是我们为了能在显示SurfaceView同时显示别的组件，所以把自定义的SurfaceView也当作组件注册在了main——xml中，
		// 所以这里需要注意，当在xml中注册的就必须加上这种初始化方法， 初始化的时候会调用这个方法
		// 当时这个问题困扰了一天的研究时间，最后在一个群友的帮助下才发现是这里出了问题
		// 那么第二个参数指的自定义的组件的一些属性,就像长宽一样，你可以给组件属性,就是通过这个来传递的
		super(context, attrs);
		paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
        matrix.postScale((float)0.5 ,(float)0.5);//放缩比
        reloadmap(Floor.floorID);
		redpoint= BitmapFactory.decodeResource(this.getResources(), R.drawable.red10);
	    stairs= BitmapFactory.decodeResource(this.getResources(), R.drawable.stairs);
        System.out.println("stairsW="+stairs.getWidth()+"---stairsH="+stairs.getHeight());
	    background=BitmapFactory.decodeResource(this.getResources(), R.drawable.background);
//        stairs=Bitmap.createBitmap(stairs,0,0,stairs.getWidth(),stairs.getHeight(),matrix,true);//放缩
        System.out.println("backgroundW="+background.getWidth()+"---backgroundH="+background.getHeight());
//        background=Bitmap.createBitmap(background,0,0,background.getWidth(),background.getHeight(),matrix,true);//放缩
        endpoint=BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_en);
        endpoint=Bitmap.createBitmap(endpoint,0,0,endpoint.getWidth(),endpoint.getHeight(),matrix,true);
		sfh = this.getHolder();
		sfh.addCallback(this);
		th = new Thread(this);
		//屏幕相关
		this.setKeepScreenOn(true);
		setFocusable(true);

		//路径画笔特征
        paint1 = new Paint();
        paint1.setColor(Color.GREEN);
        paint1.setStrokeWidth(5);

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		screenW = this.getWidth();
		screenH = this.getHeight();
		if(Floor.floorID == 6){
			Height=building.getHeight()/195f;
			Width=building.getWidth()/320f;
		}
		else if(Floor.floorID == 7){
			Height=building.getHeight()/140f;
			Width=building.getWidth()/170f;
		}

		flag = true;
		th.start(); 
	} 
	
	public void draw() {
		try {
			canvas = sfh.lockCanvas();
			if (canvas != null) {
				canvas.drawBitmap(background, 0, 0, paint);
				canvas.drawBitmap(building, floorX, floorY, paint);
			    canvas.drawBitmap(redpoint,redpointX,redpointY,paint);
				canvas.drawBitmap(stairs,stairsX,stairsY,paint);
			    canvas.drawBitmap(redpoint,redpointX2,redpointY2,paint);
			    if(pathUpdateFlag){
                    drawPath(pointList);
				}


			}
		}
		catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (canvas != null)
				sfh.unlockCanvasAndPost(canvas);
		}
	}

	public void drawPath(ArrayList<ArrayList<Integer>> pointList){
        float x1=0,y1=0,x2=0,y2=0;
        for(int i=0;i<pointList.size()-1;i++){
            for(int j=0;j<2;j++){
                x1=floorX+(int) (2*Width*pointList.get(i).get(0)-10);
                y1=floorY+(int) (2*Height*pointList.get(i).get(1)-5);
                x2=floorX+(int) (2*Width*pointList.get(i+1).get(0)-10);
                y2=floorY+(int) (2*Height*pointList.get(i+1).get(1)-5);
            }
            canvas.drawLine(x1,y1,x2,y2,paint1);
        }
        if(pointList.size()>=2){
            canvas.drawBitmap(endpoint, x2-20,y2-50, paint1);
        }


    }

	public void logic() {

		if(Floor.floorID!=oldfloorID&oldfloorID!=0)
		{
			reloadmap(Floor.floorID);
			reloadflag=true;
		}
		oldfloorID=Floor.floorID;
		redpointX=floorX+(int) (2*Width*MapActivity.LocationX-10);
		redpointY=floorY+(int) (2*Height*MapActivity.LocationY-5);
		//stairs逻辑
		if(MapActivity.LocationX<590/2&MapActivity.LocationY>0)
		{
			reloadflag=false;
			redpointX2=stairsX+2*MapActivity.LocationX/590f*105;
			redpointY2=stairsY+(5-Floor.floorID)*71+18-4;
			redpointY2=redpointY2+10;
		}
		if(MapActivity.LocationX>590/2&MapActivity.LocationY<630/2)
		{
			if(MapActivity.LocationY==126/2||MapActivity.LocationY==186/2)
			{
				reloadflag=false;
				if(MapActivity.direction==1)
				{
					redpointX2=stairsX+145-(630-2*MapActivity.LocationX);	
					redpointY2=stairsY+(5-Floor.floorID)*71-18-4+(630-2*MapActivity.LocationX)-7;
					redpointY2=redpointY2+10;
				}
				if(MapActivity.direction==2)
				{
					redpointX2=stairsX+100+(2*MapActivity.LocationX-590);
					redpointY2=stairsY+(5-Floor.floorID)*71+18-4-(2*MapActivity.LocationX-590);
					redpointY2=redpointY2+10;
				}
			}
			if(MapActivity.LocationY==144/2||MapActivity.LocationY==155/2||MapActivity.LocationY==166/2)
			{
				if(reloadflag==false)
				{
					if(MapActivity.direction==1)
					{
						redpointX2=stairsX+145-(630-2*MapActivity.LocationX);	
						redpointY2=stairsY+(5-Floor.floorID)*71-18-(630-2*MapActivity.LocationX)+3;
						redpointY2=redpointY2+10;
					}
					if(MapActivity.direction==2)
					{
						redpointX2=stairsX+100+(2*MapActivity.LocationX-590);
						redpointY2=stairsY+(5-Floor.floorID)*71+18+(2*MapActivity.LocationX-590)-3;
						redpointY2=redpointY2+10;
					}
				}
				
				if(reloadflag==true)
				{
					if(MapActivity.direction==1)
					{
						redpointX2=stairsX+145-(630-2*MapActivity.LocationX);	
						redpointY2=stairsY+(5-(Floor.floorID-1))*71-18-(630-2*MapActivity.LocationX)+3;
						redpointY2=redpointY2+10;
					}
					if(MapActivity.direction==2)
					{
						redpointX2=stairsX+100+(2*MapActivity.LocationX-590);
						redpointY2=stairsY+(5-(Floor.floorID+1))*71+18+(2*MapActivity.LocationX-590)-3;
						redpointY2=redpointY2+10;
					}
				}
			}
		}
		if(MapActivity.LocationX>630/2)
		{
			reloadflag=false;
			redpointX2=stairsX+145+(2*MapActivity.LocationX-630)/140f*105;
			redpointY2=stairsY+(5-Floor.floorID)*71-18-4;
			redpointY2=redpointY2+10;
		}
	}
	
	private void reloadmap(int floorID)
	{
		switch (floorID)//TODO
		{
			case 1:
				building = BitmapFactory.decodeResource(this.getResources(), R.drawable.floor1s);
				break;
			case 2:
				building = BitmapFactory.decodeResource(this.getResources(), R.drawable.floor2s);
				break;
			case 3:
				building = BitmapFactory.decodeResource(this.getResources(), R.drawable.floor3s);
				break;
			case 4:
				building = BitmapFactory.decodeResource(this.getResources(), R.drawable.floor4s);
				break;
			case 5:
				building = BitmapFactory.decodeResource(this.getResources(), R.drawable.floor5s);
				break;
			case 6:
				building = BitmapFactory.decodeResource(this.getResources(), R.drawable.zj_floor1);
//              System.out.println("buildingW="+building.getWidth()+"---buildingH="+building.getHeight());
				break;
            case 7:
                building = BitmapFactory.decodeResource(this.getResources(), R.drawable.zj_floor2);
//              System.out.println("buildingW="+building.getWidth()+"---buildingH="+building.getHeight());
                break;
		}
	}


	
	@Override
	public boolean onKeyDown(int key, KeyEvent event) {
		return super.onKeyDown(key, event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (flag) {
			logic();
			draw();
			try {
				Thread.sleep(500);
			} catch (Exception ex) {
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		flag = false;
	}

}
