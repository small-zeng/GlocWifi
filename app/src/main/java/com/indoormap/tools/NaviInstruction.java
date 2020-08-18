package com.indoormap.tools;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.View;
import android.widget.ImageView;

import com.indoormap.gloc.MapActivity;

public class NaviInstruction extends MapActivity {
	public ImageView img;
	public Bitmap bitmap0, bitmap1;
	Matrix matrixDirection = new Matrix();
	int turnThreD =  80;

	public NaviInstruction()
	{

	}

	public void updateDir(int instrucTurn, int[] rotateDegree){
		if (instrucTurn ==  turnThreD)
		{
			rotateDegree[1] = -rotateDegree[0] + 90;
		}
		else if(instrucTurn == - turnThreD)
		{
			rotateDegree[1] = -rotateDegree[0] - 90;
		}
		else
		{
			rotateDegree[1] = -rotateDegree[0];
		}
            
		rotateDegree[0] += rotateDegree[1];
		
		if(rotateDegree[1] !=0)
		{
			matrixDirection.setRotate(rotateDegree[1]);
			// System.out.println("rotate: " + rotateDegree[1]);
			bitmap0 = Bitmap.createBitmap(bitmap0, 0, 0, bitmap0.getWidth(), bitmap0.getHeight(), matrixDirection, true);
			display();
		}
	}
	public void display()
	{
		img.setImageBitmap(bitmap0);
		img.setVisibility(View.VISIBLE);
	}

	public void disappear()
	{
		img.clearAnimation();
		img.setVisibility(View.INVISIBLE);
	}

/*	

	public void flicker()
	{
		final Animation animation = new AlphaAnimation(1,0); // Change alpha from fully visible to invisible
		animation.setDuration(500);  // duration - half a second
		animation.setInterpolator(new LinearInterpolator());  // do not alter animation rate
		animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
		animation.setRepeatMode(Animation.REVERSE); 
		img.setAnimation(animation);
	}
	
	*/
	public void updateArrival(){
		img.setImageBitmap(bitmap1);
	}


}
