package com.indoormap.tools;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.indoormap.R;

public class MyToast extends Toast {

    private static Toast resToast;

    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context The context to use.  Usually your {@link Application}
     *                or {@link Activity} object.
     */
    public MyToast(Context context) {
        super(context);
    }

    public static Toast  showToast(Context context,CharSequence text,int duration){
        //获取系统的LayoutInflater
        LayoutInflater inflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //加载布局文件
        View view=inflater.inflate(R.layout.custom_toast, null);
        //实例化控件
        TextView textView=(TextView) view.findViewById(R.id.textView1);
//        //设置控件要显示的东西
        textView.setText(text);
        //实例化Toast
        resToast=new Toast(context);
        resToast.setView(view);     //设置要显示的view
        resToast.setGravity(Gravity.CENTER, 0,0); //设置在屏幕的什么位置显示
        resToast.setDuration(duration); //显示的时间长短
        resToast.show();  //show
        return resToast;
    }

    public static Toast  showToastDraw(Context context,int resId,CharSequence text,int duration){
        //获取系统的LayoutInflater
        LayoutInflater inflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //加载布局文件
        View view=inflater.inflate(R.layout.custom_toast, null);
        //实例化控件
        ImageView imageView=(ImageView) view.findViewById(R.id.imageView1);
        TextView textView=(TextView) view.findViewById(R.id.textView1);
//        //设置控件要显示的东西
        imageView.setImageResource(resId);
         textView.setText(text);
        //实例化Toast
        resToast=new Toast(context);
        resToast.setView(view);     //设置要显示的view
        resToast.setGravity(Gravity.CENTER, 0,0); //设置在屏幕的什么位置显示
        resToast.setDuration(duration); //显示的时间长短
        resToast.show();  //show
        return resToast;
    }






}
