package com.indoormap.tools;


import com.indoormap.map.Floor;

import java.util.ArrayList;
// import java.util.Collections;


public class BaroProcessor {

	private int wsAverage = 100;
	private float alpha = (float) 0.25;
	private float threD = (float) 0.35;  // 这种情况下需要打一下补丁，0.04在这里是不合适的，要保证在一层楼的barolevel值的波动范围（教九实验）
	private float wsThred = (float) threD/2;
	private int level = 0;
	public static int floorLevel = Floor.floorID ;//初始楼层
    public static boolean floorChangeFlag = false;//楼层变化标志位
	private int wsMax = 14;
	private int wsMin = 5;  // 约束产生两次level变化的时间，这里约束上一段楼层的时间至少为3s, Make sense
	private int idN;
    public ArrayList<ArrayList<Integer>> levelChangeIn = new ArrayList<ArrayList<Integer>>();


	public BaroProcessor()
	{
	    idN = 0;
        levelChangeIn.add(new ArrayList<Integer>());
        levelChangeIn.get(0).add(0);
        levelChangeIn.get(0).add(0);
        levelChangeIn.get(0).add(0);
	}

	public void levelDetection(int nSampleIn, ArrayList<Integer> climbIn,
							   ArrayList<Float> baroValueIn, ArrayList<ArrayList<Float>> baroFilterIn, ArrayList<Long> Ts)
	{
		climbIn.add(0);
		baroFilterIn.add( new ArrayList<Float>());
		baroFilterIn.get(nSampleIn - 1).add(baroValueIn.get(nSampleIn - 1));
		baroFilterIn.get(nSampleIn - 1).add((float) 0);
		baroFilterIn.get(nSampleIn - 1).add((float)0);


		// smooth and low pass filter
		if (nSampleIn >= wsAverage)
		{

			// inverse case when it comes to a 0 value
			if(baroValueIn.get(nSampleIn - 1) == 0)
			{
				baroValueIn.set(nSampleIn - 1, baroValueIn.get(nSampleIn - 2));
			}
			float avg = (float) 0;
			for(int i = 0; i < wsAverage; i++)
			{
				avg += baroValueIn.get(nSampleIn - i - 1);
			}
			avg =(float) avg/wsAverage;
			baroFilterIn.get(nSampleIn - 1).set(0, avg);
		}

		if (nSampleIn >= 2)
		{
			// lpf
			baroFilterIn.get(nSampleIn - 1).set(1, (float) baroFilterIn.get(nSampleIn - 2).get(1) + alpha*(baroFilterIn.get(nSampleIn - 1).get(0) - baroFilterIn.get(nSampleIn - 2).get(1)));
		}
		else
		{
			baroFilterIn.get(nSampleIn - 1).set(1, baroFilterIn.get(nSampleIn -1).get(0));
		}


		// calculate the level in another method

		float gap_max = 0;
		int gap_sign = 0;
		int iPrevious = nSampleIn - 1;
		int gap_p = 0;

		while(  (Math.abs(Ts.get(nSampleIn - 1) - Ts.get(iPrevious))*0.001) <= wsMax && iPrevious >= wsAverage)
		{

			iPrevious --;
			float gap = baroFilterIn.get(nSampleIn - 1).get(1) - baroFilterIn.get(iPrevious).get(1);
			float gap_abs = Math.abs(gap);
			if(gap_abs >= gap_max)
			{
				gap_max = gap_abs;
				gap_sign = (int) Math.signum(gap);
				gap_p = iPrevious;
			}
		}


		if (gap_max >= threD)
		{

			if(Math.abs(Ts.get(nSampleIn - 1) - Ts.get(gap_p))/1000 < wsMin)
			{
				idN = nSampleIn - 1;
				return;
			}
			if (gap_p < idN)
			{
				return;
			}

			if(Math.abs(baroFilterIn.get(nSampleIn - 1).get(1) - baroFilterIn.get(gap_p).get(1)) > threD)
			{
				gap_p++;
			}

			baroFilterIn.get(nSampleIn - 1).set(2, (float) gap_p);
			for(int i = gap_p; i <= nSampleIn - 1; i ++)
			{
				climbIn.set(i, gap_sign);
			}

           /**
			// 约束两次上楼的时间差
			if(level > 0 && Math.abs(Ts.get(gap_p) - Ts.get(levelChangeIn.get(level).get(1)))/1000 < wsMin)
			{
//				System.out.println("here1");
				return;
			}
			// 约束两次上楼的阈值差
    		if(level > 0 && Math.abs(baroFilterIn.get(gap_p).get(1) - baroFilterIn.get(levelChangeIn.get(level).get(1)).get(1)) < wsThred/2)
			{
//				System.out.println("here2");
				return;
			}

			// 判断是否是连续下楼梯, 不能将连续下楼梯作为排除条件，这样会减弱准确度
			ArrayList<Float>  tmp_climb = new ArrayList<Float>();
			for(int tmp_i = levelChangeIn.get(level).get(1); tmp_i <= gap_p; tmp_i ++)
			{
				tmp_climb.add(baroFilterIn.get(tmp_i).get(1));
			}
			Collections.sort(tmp_climb);
			if(tmp_climb.get(tmp_climb.size() - 1) < threD)
			{
//				System.out.println("here3");
				return;
			}
            */
			if (climbIn.get(gap_p - 1) != climbIn.get(gap_p))
			{
				// 楼梯状态是能够检测出来了，但是分析不准确
				level ++;
                floorChangeFlag =true;
				if(gap_sign<0){
                    floorLevel++;
                }
                else{
                    floorLevel--;
                }
                levelChangeIn.add(new ArrayList<Integer>());
                levelChangeIn.get(level).add(floorLevel);
				levelChangeIn.get(level).add(gap_p);
				levelChangeIn.get(level).add((int)gap_sign);
				System.out.println("level: " + level + "gap_p: " + gap_p);
			}

		}

	}

}
