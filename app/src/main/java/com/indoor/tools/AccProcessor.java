package com.indoor.tools;

import java.util.ArrayList;
import java.util.Collections;

public class AccProcessor {
             
	private int wsMedianSmooth = 11;  // must be odd
	private int wsAverageSmooth = 10; // generally here is 12
	private float alpha =(float) 0.25; // 1Hz/3Hz, 3Hz is the step frequency, 2*1 > 3, Shanoon Lema
	private int wsPeak = 11;           // equals to 11
	
	public AccProcessor()
	{
	        // it seems not necessary
  
	}

	public void stepDetection(int nSampleIn, ArrayList<Integer> stepsResultIn,
			ArrayList<ArrayList<Float>> accValueIn, ArrayList<ArrayList<Float>> accFilterValueIn )
	{
		stepsResultIn.add(0);
		boolean ifDetected = false;
		// every time goes into this session, the accFilterValueIn will be added an array
		accFilterValueIn.add(new ArrayList<Float>());
		accFilterValueIn.get(nSampleIn-1).add(accValueIn.get(nSampleIn-1).get(3));
		accFilterValueIn.get(nSampleIn-1).add((float) 0 );
		accFilterValueIn.get(nSampleIn-1).add((float) 0 );
		// from the above definition, we can make sure that the accValueIn and the accFilterValueIn are the same size
		
        
		// Median-Average Filter
		
		// Median Filter
		if (nSampleIn >= wsMedianSmooth )
		{
			ArrayList<Float> tmp_smooth0 = new ArrayList<Float>();
			for (int i = 0; i< wsMedianSmooth; i++  )
			{
				tmp_smooth0.add( accValueIn.get(nSampleIn - i - 1).get(3));
			}
			Collections.sort(tmp_smooth0);
			accFilterValueIn.get(nSampleIn - ( wsMedianSmooth-1)/2 - 1).set(0,tmp_smooth0.get((wsMedianSmooth-1)/2));
			
		}
		
		
		//  Average Filter
		//   (t-w, t+w) calculate the average value
		if (nSampleIn >= wsAverageSmooth + 2)
		{
			int presentT = nSampleIn - wsAverageSmooth;
			int lenAfterMedianSmooth = accFilterValueIn.size();
			
			float  sum1 = 0, average1 = 0;
			int j;
			for (j = 0; j< wsAverageSmooth; j++)
			{
				sum1 += accFilterValueIn.get(lenAfterMedianSmooth - j - 1).get(0); 
			}
			average1 = (float) sum1/j;
			
			float middle = accFilterValueIn.get(lenAfterMedianSmooth - j - 1).get(0);
			
			j++;
			float sum2 = 0, average2 = 0; 
			while (j < Math.min(lenAfterMedianSmooth, 2*wsAverageSmooth + 1))
			{
				sum2 += accFilterValueIn.get(lenAfterMedianSmooth - j - 1).get(0);
				j++;
			}
			average2 =(float) sum2/(j - wsAverageSmooth - 1);
			
			// calculate the T value, Median and Average Filter fusion
		    accFilterValueIn.get(presentT - 1).set(1, (float) (2*middle - average1 - average2));

			// 截尾，计算的不再是当前nSampleIn对应的值，而是前wsAverageSmooth个序列号位置的值， 将其作为本模块计算的结果输出位置
			// 也就是说最后几个数据将被丢掉，设置为0
		    
		    // lower pass Filter
		    accFilterValueIn.get(presentT - 1).set(2, accFilterValueIn.get(presentT - 2).get(2) + alpha*(accFilterValueIn.get(presentT - 1).get(1) - accFilterValueIn.get(presentT - 2).get(2)));
		    
		    // steps detection
		    if (presentT >= 2*wsPeak + 1 && accFilterValueIn.get(presentT - wsPeak - 1).get(2) > (float) 0.052 )
		    {
		    	ArrayList<Float> peakDetectionArray = new ArrayList<Float>();
		    	for (int tmp_i = 0; tmp_i < 2*wsPeak + 1 ; tmp_i++)
		    	{
		    		peakDetectionArray.add(accFilterValueIn.get(presentT - tmp_i - 1).get(2));
		    	}
		    	Collections.sort(peakDetectionArray);
		    	if(peakDetectionArray.get(peakDetectionArray.size() - 1) == accFilterValueIn.get(presentT - wsPeak - 1).get(2))
		    	{
		    		ifDetected = true;
		    	}
		    }
		    if(ifDetected)
		    {
		    	stepsResultIn.set(presentT - wsPeak - 1, stepsResultIn.get( presentT - wsPeak -2) + 1);
		    }
		    else if(presentT >= wsPeak + 2 ){    // 注意这里面的链表运算，一定要保证链表内的指数不能小于0
		    	stepsResultIn.set(presentT - wsPeak - 1, stepsResultIn.get( presentT - wsPeak -2));
		    }
		}
		else{
			accFilterValueIn.get(nSampleIn-1).set(1, accFilterValueIn.get(nSampleIn - 1).get(0));
		}
	    
	    
	}
}
