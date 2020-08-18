package com.indoormap.mapshow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.indoormap.R;
import com.indoormap.gloc.CollectDataActivity;

import java.util.ArrayList;
import com.indoormap.tools.availablePoint;

public class DataCollectSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable{

    private Thread th;
    private SurfaceHolder sfh;
    //声明一个画笔
    private Paint paint ,paint1;
    private Canvas canvas;
    private  Bitmap stairs;
    private Bitmap redpoint;
    private Bitmap bluepoint;
    private Bitmap greenpoint;
    private Bitmap background;
    private Bitmap endpoint;
    private int screenW, screenH;
    private boolean flag=false;
    private int redpointX=420-8;
    private int redpointY=50-5;
    private int floorX=550;
    private int floorY=25;
    private float Width;
    private float Height;
    private int oldfloorID;
    private boolean reloadflag=false;
    public static boolean pathUpdateFlag = true;
    public int x1,y1,x2,y2;
    public static int speed = -1;
    public static int direction = 1;
    Matrix matrix = new Matrix();

    public DataCollectSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        matrix.postScale((float)0.75 ,(float)0.5);//放缩比
//        reloadmap(Floor.floorID);
        redpoint= BitmapFactory.decodeResource(this.getResources(), R.drawable.red10);
        bluepoint = BitmapFactory.decodeResource(this.getResources(),R.drawable.blue);
        greenpoint = BitmapFactory.decodeResource(this.getResources(),R.drawable.green);
        greenpoint = Bitmap.createBitmap(greenpoint,0,0,greenpoint.getWidth(),greenpoint.getHeight(),matrix,true);
        bluepoint = Bitmap.createBitmap(bluepoint,0,0,bluepoint.getWidth(),bluepoint.getHeight(),matrix,true);
        redpoint=Bitmap.createBitmap(redpoint,0,0,redpoint.getWidth(),redpoint.getHeight(),matrix,true);//放缩
        background=BitmapFactory.decodeResource(this.getResources(), R.drawable.back);
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
    public void run() {
        while (flag) {
            logic();
            draw();
            try {
                Thread.sleep(500);
            } catch (Exception ex) {
            }
        }
    }

    public void logic(){
        Width = 1000f/(x2-x1);
        Height = 600f/(y2-y1);
        System.out.println("x1,y1="+x1+","+y1);
        System.out.println("x2,y2="+x2+","+y2);
        System.out.println("Width,Height="+Width+","+Height);
    }

    public void draw(){
        try {
            canvas = sfh.lockCanvas();
            System.out.println("画图");
            if (canvas != null) {
                canvas.drawBitmap(background, 0, 0, paint);
                drawBoundry(CollectDataActivity.boundaryPointList);

                canvas.drawBitmap(bluepoint,40,650,paint);
                canvas.drawBitmap(bluepoint,1040,50,paint);

                int x,y;
                if(speed == -1 ){
                    for(int i=0;i<CollectDataActivity.uwbPointList.size();i++){
                        x = CollectDataActivity.uwbPointList.get(i).x;
                        y = CollectDataActivity.uwbPointList.get(i).y;
                        float red_x,red_y;
                        red_x=40+ Width*(x-x1);
                        red_y=650 - Height*(y-y1);
                        System.out.println("red:"+red_x+","+red_y);
                        canvas.drawBitmap(redpoint, red_x, red_y, paint);
                    }
                }else{
                    for(int i=0;i<CollectDataActivity.uwbPointList.size();i++){
                        availablePoint point = CollectDataActivity.uwbPointList.get(i);
                        if((point.speed == speed && speed == 0)||(point.speed == speed && point.direction == direction)){
                            x = CollectDataActivity.uwbPointList.get(i).x;
                            y = CollectDataActivity.uwbPointList.get(i).y;
                            float red_x,red_y;
                            red_x=40+ Width*(x-x1);
                            red_y=650 - Height*(y-y1);
                            System.out.println("red:"+red_x+","+red_y);
                            canvas.drawBitmap(redpoint, red_x, red_y, paint);
                        }
                    }

                }
            }
        }catch (Exception e) {
            // TODO: handle exception
        } finally {
            if (canvas != null)
                sfh.unlockCanvasAndPost(canvas);
        }



    }

    public void drawBoundry(ArrayList<ArrayList<Integer>> pointList){

       if(pointList.size() == 2){
           x1 = pointList.get(0).get(0);
           y1 = pointList.get(0).get(1);
           x2 = pointList.get(1).get(0);
           y2 = pointList.get(1).get(1);
           canvas.drawLine(40,50,1040,50,paint1);
           canvas.drawLine(1040,50,1040,650,paint1);
           canvas.drawLine(1040,650,40,650,paint1);
           canvas.drawLine(40,650,40,50,paint1);
       }


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenW = this.getWidth();
        screenH = this.getHeight();

        System.out.println("surfaceCreated");
        th = new Thread(this);
        th.start();
        flag = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        flag = false;
    }

    @Override
    public boolean onKeyDown(int key, KeyEvent event) {
        return super.onKeyDown(key, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }


}
