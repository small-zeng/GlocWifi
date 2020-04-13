package com.indoor.particlefilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import android.os.Environment;
import android.text.format.Time;

import com.indoor.map.Floor;
import com.indoor.map.Map;
import com.indoor.map.PublicData;

public class Particle_Filter  {	

	private  ArrayList<Particle> particles = new ArrayList<Particle>();
	private ArrayList<Double>  particlesVarianceList = new ArrayList<Double>();
	private ArrayList<Integer> varianceNumList = new ArrayList<Integer>();
	//粒子的数目
	private int nbParticles=0;
	private Random generator=new Random();
	private double sigma=0.5;
	private double num;
	private double num2;
	private double disLeft=0;
	private double disRight=0;
	private double disBottom=0;
	private double disTop=0;
	//variance最高的粒子的x,y值作为数值
	public int positionx;
	public int positiony;
	Time timestamp = new Time();
	
	public Particle_Filter(){
	}
	
	private void addParticle(Particle part){
		this.particles.add(part);
	}
	
	//初始化粒子
	public void init_particles(int widthmax,int lengthmax)
	{
		this.particles.clear();
		PublicData.GetPartNumber();
		while(nbParticles<PublicData.N)
		{
			for(int wid=0;wid<widthmax;wid++)
			{
				for(int len=0;len<lengthmax;len++)
				{
					if((len+wid)%1==0)
					{
						if(Map.binaryMap[wid][len]==1&nbParticles<PublicData.N)
						{
							num = generator.nextGaussian();
							num2 = ((num*sigma) + PublicData.initialpartlength)*1;
							Particle p = new Particle(num2, 0, wid, len, nbParticles, 0, calculateVariance(0.25,0.25,0.25,0.25),
									getBINNeighborList(wid,len,1), getBINNeighborList(wid,len,2), 
									getBINNeighborList(wid,len,3), getBINNeighborList(wid,len,4),
									getMACNeighborList(wid,len,1), getMACNeighborList(wid,len,2), 
									getMACNeighborList(wid,len,3), getMACNeighborList(wid,len,4),
									0.25, 0.25 , 0.25, 0.25);
							addParticle(p);
							nbParticles=nbParticles+1;
						}
					}
				}
			}
		}
		PublicData.ResetPartNumber();
	}
	
	//just for test
	public void test_particles()
	{
		int wid;
		int len;
		num = generator.nextGaussian();
		num2 = ((num*sigma) + PublicData.initialpartlength)*1;
		this.particles.clear();
		wid=727/2;
	    len=191/2;
		Particle p1 = new Particle(num2, 0, wid, len, nbParticles, 0, calculateVariance(0.25,0.25,0.25,0.25),
				getBINNeighborList(wid,len,1), getBINNeighborList(wid,len,2), 
				getBINNeighborList(wid,len,3), getBINNeighborList(wid,len,4),
				getMACNeighborList(wid,len,1), getMACNeighborList(wid,len,2), 
				getMACNeighborList(wid,len,3), getMACNeighborList(wid,len,4),
				0.25, 0.25 , 0.25, 0.25);
		addParticle(p1);
		nbParticles=nbParticles+1;
	}
	
	public void print_particles()
	{
		for(Particle part:this.particles)
		{
			String res="";
			res=res+part.getNum()+";"+2*part.getPositionX()+";"+2*part.getPositionY()+";"+part.getLeftWeight()+";"+part.getRightWeight()+";"+part.getBottomWeight()+";"+part.getTopWeight()+";"+part.getVariance();
			System.out.println(res);
		}
	}
	
 	private ArrayList<Integer> getBINNeighborList(int x, int y,int dir)
 	{
 		return fromStringToIntegerList(Map.relativeWifiMap[x][y][2*dir]);
 	}
	
 	private ArrayList<String> getMACNeighborList (int x, int y,int dir)
 	{
// 		System.out.println("dir:"+dir+";"+Map.relativeWifiMap[x][y][2*dir-1]);
 		return Map.relativeWifiMap[x][y][2*dir-1];
 	}
 	
	//更新粒子信息
 	public void update_particles(int dir,int steplength,ArrayList<String>  relativeUserWifiMacList,ArrayList<Integer>  relativeUserWifiBinList)
 	{
 		for(Particle part:this.particles)
 		{
 			part.setDirection(dir);
 			part.setLength(calculatepartlength(steplength));
 			change_particle(part);
 			calculateXorDistance(part,relativeUserWifiMacList,relativeUserWifiBinList);
 			updatepartweight(part);
 			updatepartVariance(part);
 		}
 	}
 	
 	private double calculatepartlength(int steplength)
 	{
 	    double partlength;
 		Random generator=new Random();
		double num ;
		double mean =steplength;
		//TODO check if the steplength is OK
		num = generator.nextGaussian();
		partlength= ((num*sigma) + mean)*1;			
		return partlength;
 	}
 	
 	//选取权重最大的粒子的相关信息作为粒子的相关信息
 	public void result_particles()
 	{
 		Particle part=getHighestVarianceParticle();
 		positionx=(int) part.getPositionX();
 		positiony=(int) part.getPositionY();
 	}
 	
	private Particle getHighestVarianceParticle()
	{
		Collections.sort(this.particles,varianceComparator);
		return particles.get(0);
	}
	
 	public void result2_particles(int LocationX,int LocationY)
 	{
 		Particle part=getMostSuitableParticle(LocationX,LocationY);
 		positionx=(int) part.getPositionX();
 		positiony=(int) part.getPositionY();
 	}
 	
	//TODO check if this is userful
	/**
	 * @param LocationX
	 * @param LocationY
	 * @return
	 */
	private Particle getMostSuitableParticle(int LocationX,int LocationY)
	{
		int index=0;
		double compare=1000000;
		double value=0;
		Collections.sort(this.particles,varianceComparator);
		for(int i=0;i<20;i++)
		{
			Particle part=this.particles.get(i);
			value=Math.sqrt((part.getPositionX()-LocationX)*(part.getPositionX()-LocationX)+(part.getPositionY()-LocationY)*(part.getPositionY()-LocationY));
			if(value<compare)
			{
				compare=value;
				index=i;
			}
		}
		return this.particles.get(index);
	}
	

 		
 	//粒子重布
 	public boolean resample(int stepLength)
 	 {
 		int number=0;
		double mean =stepLength;
		double sigma = 0.25;
		double num ;
		double num2;
		int nb=0;
		Random generator = new Random();
		ArrayList<Particle> nextParticleDistribution = new ArrayList<Particle>();
		ArrayList<Particle> particleDistribution = new ArrayList<Particle>();
		particleDistribution = this.particles;	
	    sortedByVarianceListParticle(particleDistribution);
	    normalizeVariance();
		PublicData.GetPartNumber();
	    for(Particle part: particleDistribution)
	    {
	    	number=(int) Math.floor(part.getVariance()*PublicData.N);
	    	if(number>0)
	    	{
	    		for(int i=0;i<number;i++)
	    		{	    	
	    			num = generator.nextGaussian();
	    			num2 = ((num*sigma) + mean)*1;
	    			Particle father=part;
	    			Particle son = new Particle
	    					(		num2, 
	    							0,    
	    							(int) father.getPositionX(),   
	    							(int) father.getPositionY(),  
	    							nb,   	
	    							0,		
	    							0,		
	    							father.getLeftBINList(), 
	    							father.getRightBINList(), 
	    							father.getBottomBINList(),
	    							father.getTopBINList(), 
	    							father.getLeftMACList(), 
	    							father.getRightMACList(), 
	    							father.getBottomMACList(),
	    							father.getTopMACList(), 
	    							0.25, 
	    							0.25, 
	    							0.25,
	    							0.25
	    							);
	    			nextParticleDistribution.add(son);
	    			nb++;
	    		}
	    	}
	    }
	    if(nextParticleDistribution.size()>PublicData.N*0.1)  //TODO 这边确保有足够的粒子
	    {
	    	this.particles = new ArrayList<Particle>(nextParticleDistribution);	
	    }
		PublicData.ResetPartNumber();
		return true;
 	}
	
 	//add the new kind of resample
 	public boolean resample2(int stepLength)
 	{
 		double mean =stepLength;
 		double sigma = 2.5;
 		double num ;
 		double num2;
 		double random = 0;
 		int nb=0;
 		Random generator = new Random();
 		Random generator1 = new Random();
 		ArrayList<Particle> nextParticleDistribution = new ArrayList<Particle>();
 		ArrayList<Particle> particleDistribution = new ArrayList<Particle>();
 		particleDistribution = this.particles;	
 		sortedByVarianceListParticle(particleDistribution);
 		normalizeVariance();
 		PublicData.GetPartNumber();
 		particlesVarianceList = getCumulativeVariance ();
 		while(nb<PublicData.N)
 		{
 			random = generator1.nextDouble();
 			num = generator.nextGaussian();
 			num2 = ((num*sigma) + mean)*1;
 			for(int i=0; i<PublicData.N ;i++)
 			{
 				if(  random > 0   && random < particlesVarianceList.get(0)  && nb<PublicData.N )
 				{
 					Particle father = particles.get(varianceNumList.get(0));
 					Particle son = new Particle
 							(		num2,
 									0,    
 									(int) father.getPositionX(), 
 									(int) father.getPositionY(), 
 									nb,  
 									0,		
 									0,		
 									father.getLeftBINList(), 
 									father.getRightBINList(), 
 									father.getBottomBINList(),
 									father.getTopBINList(), 
 									father.getLeftMACList(), 
 									father.getRightMACList(), 
 									father.getBottomMACList(),
 									father.getTopMACList(), 
 									0.25, 
 									0.25, 
 									0.25,
 									0.25
 									);					
 					nextParticleDistribution.add(son);
 					nb++;
 					break;
 				}
 				else if( random > particlesVarianceList.get(i) && random < particlesVarianceList.get(i+1) && nb<PublicData.N)
 				{
 					Particle father = particles.get(varianceNumList.get(i));			
 					Particle son = new Particle
 							(		num2, 
 									0,   
 									(int) father.getPositionX(),
 									(int) father.getPositionY(), 
 									nb,   
 									0,		
 									0,		
 									father.getLeftBINList(), 
 									father.getRightBINList(), 
 									father.getTopBINList(), 
 									father.getBottomBINList(),
 									father.getLeftMACList(), 
 									father.getRightMACList(), 
 									father.getTopMACList(), 
 									father.getBottomMACList(),
 									0.25, 
 									0.25, 
 									0.25,
 									0.25
 									);
 					nextParticleDistribution.add(son);
 					nb++;
 					break;
 				}				
 			}
 		}
 		this.particles = new ArrayList<Particle>(nextParticleDistribution);
		PublicData.ResetPartNumber();
 		return true;
 	}
 	
 	
 	private void change_particle(Particle part)
 	{
 		int newPositionX=0;
 		int newPositionY=0;

 		//TODO 走楼梯的时候，速度减半
 		if((part.getDirection()==3||part.getDirection()==4)&(part.getPositionX()==79||part.getPositionX()==66))
 		{
 			if(part.getPositionY()>25&part.getPositionY()<=70)//TODO,边界值需要修改
 			{
 				part.setLength(part.getLength()*0.55);
 			}
 		}
        /**
 		if(part.getDirection()==2||part.getPositionY()==126/2||part.getPositionY()==186/2)
 		{
 			if(part.getPositionX()>590/2&part.getPositionX()<630/2)//TODO,边界值需要修改
 			{
 				part.setLength(part.getLength()*2/3);
 			}
 		}
 		//TODO 走5楼横道的时候速度加1.5倍
 		if(part.getPositionY()==94/2)
 		{
 		   part.setLength(part.getLength()*3/2);
 		}
 		//TODO 走5楼竖道返回的时候
 		if(part.getPositionX()==727/2&part.getDirection()==4&Floor.floorID==5)
 		{
 		   part.setLength(part.getLength()*2/3);
 		}
 		
 		if(part.getPositionX()==727/2&part.getDirection()==4&Floor.floorID==2)
 		{
 		   part.setLength(part.getLength()*4/5);
 		}
 		*/
 		switch(part.getDirection())
 		{
 		case 1:
 			newPositionX=(int) (part.getPositionX()-part.getLength());//左移
 			newPositionY=(int) part.getPositionY();
 			break;
 		case 2:
 			newPositionX=(int) (part.getPositionX()+part.getLength());//右移
 			newPositionY=(int) part.getPositionY();
 			break;
 		case 3:
 			newPositionX=(int) part.getPositionX();
 			newPositionY= (int) (part.getPositionY()+part.getLength());//下移
 			break;
 		case 4:
 			newPositionX=(int) part.getPositionX();
 			newPositionY= (int) (part.getPositionY()-part.getLength());//上移
 			break;
 		}


 		//判断粒子是否出界
 		if(newPositionX<0||newPositionX>PublicData.MapWidth-1||newPositionY<0||newPositionY>PublicData.MapHeight-1) //注意这边的边界问题，挺夸张的
 		{
//			System.out.println("出界");
			updateListsParticleOutside(part);
			part.setHitWall(1);	
 		}	
 		else
 		{
//			System.out.println("未出界");
 			//判断粒子是否在路径上
 			if(Map.binaryMap[newPositionX][newPositionY]==1)
 			{
//				System.out.println("满足");
 				part.setPositionX(newPositionX);
				part.setPositionY(newPositionY);
//				System.out.println("坐标："+newPositionX+","+newPositionX);
				updateListsParticleOnPath(part, newPositionX,newPositionY);
 			}


 			//判断在交叉口上的粒子是否需要挪动
 			else
 			{
				/**
 				//TODO enlarge intersection
 				if(Floor.floorID==2)
 				{
 	 				enlargeintersection_2(part);
 				}
 				if(Floor.floorID==3||Floor.floorID==4)
 				{
 					enlargeintersection_34(part);
 				}
 				if(Floor.floorID==5)
 				{
 					enlargeintersection_5(part);
 				}
			 	*/

				if(Floor.floorID==6){
					enlargeintersection_6(part);
				}

                if(Floor.floorID==7){
                    enlargeintersection_7(part);
                }

 			}

 		}
 	}
 	
	private void updateListsParticleOnPath(Particle part, int xpart, int ypart)
	{
		part.setLeftBINList(getBINNeighborList(xpart,ypart,1));
		part.setRightBINList(getBINNeighborList(xpart,ypart,2));
		part.setBottomBINList(getBINNeighborList(xpart,ypart,3));
		part.setTopBINList(getBINNeighborList(xpart,ypart,4));
		
		part.setLeftMACList(getMACNeighborList(xpart,ypart,1));
		part.setRightMACList(getMACNeighborList(xpart,ypart,2));
		part.setBottomMACList(getMACNeighborList(xpart,ypart,3));	
		part.setTopMACList(getMACNeighborList(xpart,ypart,4));
	}
 	
 	
	private void updateListsParticleOutside(Particle part)
	{
		ArrayList<Integer> listOf2 = new ArrayList<Integer> ();
		ArrayList<String> listOfNull = new ArrayList<String> ();
		for(int i=0 ; i< 10; i++)
		{
			listOf2.add(2);
			listOfNull.add("null");
		}	
		part.setLeftBINList(listOf2);
		part.setRightBINList(listOf2);
		part.setTopBINList(listOf2);
		part.setBottomBINList(listOf2);	
		part.setLeftMACList(listOfNull);
		part.setRightMACList(listOfNull);
		part.setTopMACList(listOfNull);
		part.setBottomMACList(listOfNull);		
	}
	
	private void calculateXorDistance(Particle part,ArrayList<String>  relativeUserWifiMacList,ArrayList<Integer>  relativeUserWifiBinList)
	{
		for(int dir=1;dir<5;dir++)
		{
			ArrayList<String> intersectionMacList = new ArrayList<String> ();
			int sum=0;
			int index1=0;
			int index2=0;
			int unionnum=0;
			int intersectnum=0;
			double distance=0;
			ArrayList<String> compareMacList = new ArrayList<String> ();
			ArrayList<Integer> compareBinList= new ArrayList<Integer> ();
			
			switch(dir)
			{
			case 1:
				compareMacList=part.getLeftMACList();
				compareBinList=part.getLeftBINList();
//			    System.out.println("LeftMACList:"+part.getLeftMACList());
				break;
			case 2:
				compareMacList=part.getRightMACList();
				compareBinList=part.getRightBINList();
//				System.out.println("RightMACList:"+part.getRightMACList());
				break;
			case 3:
				compareMacList=part.getBottomMACList();
				compareBinList=part.getBottomBINList();
//				System.out.println("BottomMACList:"+part.getBottomMACList());
				break;
			case 4:
				compareMacList=part.getTopMACList();
				compareBinList=part.getTopBINList();
//				System.out.println("TopMACList:"+part.getTopMACList());
				break;
			}
			intersectionMacList = intersectionOfArrayList(relativeUserWifiMacList,compareMacList);
			intersectnum=intersectionMacList.size();
			unionnum = getUnionAp(relativeUserWifiMacList,compareMacList);
			if(intersectnum>0){
				for(String mac : intersectionMacList)	
				{
					index1 = relativeUserWifiMacList.indexOf(mac);
					index2 = compareMacList.indexOf(mac);
					if(relativeUserWifiBinList.get(index1)!=compareBinList.get(index2)) 
					{
						sum=sum+10;
					}
				}
				distance=(sum+1)*getRatio(unionnum, intersectnum,2);
			}
			else
			{
				distance=10000;
			}
			
			switch(dir)
			{
			case 1:
				disLeft=distance;
//				System.out.println("disLeft:"+disLeft);
				break;
			case 2:
				disRight=distance;
//				System.out.println("disRight:"+disRight);
				break;
			case 3:
				disBottom=distance;
//				System.out.println("disBottom:"+disBottom);
				break;
			case 4:
				disTop=distance;
//				System.out.println("disTop:"+disTop);
			}
		}
	}
	
	private void updatepartweight(Particle part)
	{
		double value=0;
		//disLeft disRight disTop disBottom均是10000
		if(disLeft>0 && disRight>0 && disTop>0 && disBottom>0)
		{	
			value = updateWeight(disLeft,50);
			part.setLeftWeight(part.getLeftWeight()*value);
			value = updateWeight(disRight,50);
			part.setRightWeight(part.getRightWeight()*value);
			value = updateWeight(disTop,50);
			part.setTopWeight(part.getTopWeight()*value);
			value = updateWeight(disBottom,50);
			part.setBottomWeight(part.getBottomWeight()*value);
		}
	}
	
	private void updatepartVariance(Particle part)
	{
		double newVariance=0;
		part.normalizeWeight();
		newVariance=calculateVariance(part.getLeftWeight()*100,part.getRightWeight()*100,part.getBottomWeight()*100,part.getTopWeight()*100);
		if(part.getHitWall()==1) 
		{
			newVariance=newVariance/1000;
		}
		else 
		{ 
			newVariance=newVariance*1000;
		}
		part.setVariance(newVariance);
		//every part’s variance is 0
	}
	
	private void normalizeVariance()
	{
		double totalVariance = getTotalVariance();
		for(Particle part: this.particles)
		{
			part.setVariance( part.getVariance() / totalVariance);		
		}
	}

	private double getTotalVariance()
	{
		double sum = 0;
		for(Particle part: this.particles)
		{
			sum = sum + part.getVariance();
		}		
		return sum;		
	}
	
	private double getRatio(int union, int intersect, int pow)
	{
		double ratio = 0;
		
		if(intersect>0)
		{
			ratio = Math.pow( (union/intersect) , pow);
		}
		else
		{
			ratio = Math.pow( union , pow);
		}						
		return ratio;
	}
	
	private double updateWeight(double dist, double c)
	{
		double w = 0;
		
		if(dist>0)
		{
			w = Math.exp(   -(dist) / (2*c*c)   );
		}
		return w ;	
	}
	
 	private double calculateVariance(double left, double right, double top, double bottom)
 	{
 		double var = 0;
 		double mean = 0;
 		mean = ( left + right + top + bottom ) / 4 ;	
 		var = ((left-mean)*(left-mean)) + ((right-mean)*(right-mean)) + ((top-mean)*(top-mean)) + ((bottom-mean)*(bottom-mean))  ;
 		return var/4;
 	}
 	 	 	
	private int getUnionAp(ArrayList<String> lst1, ArrayList<String> lst2)
	{
		int unionnum = 0;
		for(String item : lst1)	
		{
			if(!lst2.contains(item))
			{
				unionnum++;
			}
		}			
		unionnum = unionnum + lst2.size();
		return unionnum;
	}
	
	private ArrayList<Particle> sortedByVarianceListParticle(ArrayList<Particle> lst)
	{
		Collections.sort(lst,varianceComparator);
		return lst;
	}
	
	private Comparator<Particle> varianceComparator = new Comparator<Particle> ()
	{
		@Override
		public int compare(Particle part0, Particle part1) {

			double s0 = part0.getVariance();
			double s1 = part1.getVariance();
			if( s0 > s1) return -1;
			else if( s0 < s1) return +1;
			else return 0;	
		}
	};
	
	public ArrayList<Double> getCumulativeVariance()
	{
		double Cumul= 0;
		ArrayList<Double> cumulativeList = new ArrayList<Double>();
		int line=0;
		for(Particle part: this.particles)
		{	
			Cumul=Cumul+part.getVariance();
			cumulativeList.add(Cumul);
			varianceNumList.add(part.getNum());
			line++;
		}
		return cumulativeList;
	}
	
 	private ArrayList<String> intersectionOfArrayList(ArrayList<String> lst1 , ArrayList<String> lst2)
	{
		ArrayList<String> lst = new ArrayList<String> ();
		
		for(String str : lst1)	
		{
			if(lst2.contains(str))
			{
				lst.add(str);
			}
		}
		return lst;
	}
	
	private ArrayList<Integer> fromStringToIntegerList(ArrayList<String> stringList)
	{
		ArrayList<Integer> integerList = new ArrayList<Integer>();
		if(stringList.size()>0)
		{
			for(String item : stringList)
			{
				integerList.add( Integer.parseInt(item) );
			}
		}
		return integerList;
	}

	public void exportParticleTraces(int widthmax,int lengthmax)
	{
		String res=" ";
		for(int wid=0;wid<widthmax;wid++)
		{
			for(int len=0;len<lengthmax;len++)
			{
				if(Map.binaryMap[wid][len]==1)
					{
						res= res + ";"+wid+";"+len+";"+getCountParticle(wid, len)+";";
					}
			}
		}
		exportParticleTraces(res);
	}
	
	private int getCountParticle(int x, int y)
	{
		int count=0;
		for(Particle part: this.particles)
		{
			if( part.getPositionX()==x & part.getPositionY()==y )
			{
				count++;
			}
		}	
		return count;
	}
	
	private void exportParticleTraces(String res) 
	{
		File myOutputFile = new File(Environment.getExternalStorageDirectory() + File.separator + "Gloc","particles_traces"+".txt");
		File myDir = new File(Environment.getExternalStorageDirectory() + File.separator + "Gloc");
		Boolean success = true;
		if(!myDir.exists()) 
		{
			success = myDir.mkdir();
		}
		if(success)
		{

			try
			{
				timestamp.setToNow();
				String sTime = timestamp.format("%Y-%m-%d %H:%M:%S");
				String resultWifi= sTime+";"+res+"\n";			
				FileOutputStream outlocation = new FileOutputStream(myOutputFile,true);
				outlocation.write(resultWifi.getBytes());
				outlocation.close();
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
	
	
	private void enlargeintersection_2(Particle part)
	{
		switch(part.getDirection())
		{
		case 1:
			//path25-path38
			if(part.getPositionY()>160/2&part.getPositionY()<200/2&(part.getPositionX()==727/2))
			{
				part.setPositionY(191/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			//path38-path36
			if((part.getPositionY()>177/2&part.getPositionY()<196/2)&(part.getPositionX()>706/2&part.getPositionX()<718/2))
			{
				part.setPositionX(710/2);
				part.setPositionY(177/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			//path36-path34
			if((part.getPositionY()>164/2&part.getPositionY()<180/2)&(part.getPositionX()>690/2&part.getPositionX()<702/2))
			{
				part.setPositionX(695/2);
				part.setPositionY(166/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			//path34-path21
			if((part.getPositionY()>152/2&part.getPositionY()<168/2)&(part.getPositionX()>682/2&part.getPositionX()<690/2))
			{
				part.setPositionX(685/2);
				part.setPositionY(155/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			else
			{
			 part.setHitWall(1);
			}
			break;
		case 2:
			//path30-path29
			if(part.getPositionY()>300/2&part.getPositionY()<324/2&(part.getPositionX()==695/2))
			{
				part.setPositionY(324/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			else
			{ 
				part.setHitWall(1);
			}
			break;
		case 3:
			//path29-path25 
			if(part.getPositionX()>700/2&part.getPositionX()<727/2&(part.getPositionY()==324/2))
			{
				part.setPositionX(727/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			else
			{ 
				part.setHitWall(1);
			}
			break;
		case 4:
			//path29-path25 
			if(part.getPositionX()>700/2&part.getPositionX()<727/2&(part.getPositionY()==324/2))
			{
				part.setPositionX(727/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			else
			{ 
				part.setHitWall(1);
			}
			break;
		}
	}
	
	private void enlargeintersection_34(Particle part)
	{
		switch(part.getDirection())
		{
		case 1:
			//path13-path20
			if(part.getPositionY()>126/2&part.getPositionY()<144/2&(part.getPositionX()==640/2))
			{
				part.setPositionY(144/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			//path13-path21
			if(part.getPositionY()>144/2&part.getPositionY()<166/2&(part.getPositionX()==640/2))
			{
				part.setPositionY(155/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			//path13-path22
			if(part.getPositionY()>166/2&part.getPositionY()<186/2&(part.getPositionX()==640/2))
			{
				part.setPositionY(186/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			else
			{ 
				part.setHitWall(1);
			}
			break;
		case 2:
			//path12-path15
			if(part.getPositionY()>126/2&part.getPositionY()<144/2&(part.getPositionX()==580/2))
			{
				part.setPositionY(126/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			//path12-path19
			if(part.getPositionY()>166/2&part.getPositionY()<186/2&(part.getPositionX()==653/2))
			{
				part.setPositionY(186/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			else
			{ 
				part.setHitWall(1);
			}
			break;
		case 3:
			//path15-path13
			if(part.getPositionX()>620/2&part.getPositionX()<640/2&(part.getPositionY()==126/2))
			{
				part.setPositionX(640/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			//path16 path17 path18-path12
			if(part.getPositionX()>580/2&part.getPositionX()<600/2&(part.getPositionY()==144/2||part.getPositionY()==155/2||part.getPositionY()==166/2))
			{
				part.setPositionX(580/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			else
			{ 
				part.setHitWall(1);
			}
			break;
		case 4:
			//path19-path13
			if(part.getPositionX()>620/2&part.getPositionX()<640/2&(part.getPositionY()==186/2))
			{
				part.setPositionX(640/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			//path16 path17 path18-path12
			if(part.getPositionX()>580/2&part.getPositionX()<600/2&(part.getPositionY()==144/2||part.getPositionY()==155/2||part.getPositionY()==166/2))
			{
				part.setPositionX(580/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			else
			{ 
				part.setHitWall(1);
			}
			break;
		}
	}
	
	private void enlargeintersection_5(Particle part)
	{
		switch(part.getDirection())
		{
		case 1:
			//path25-path24
			if(part.getPositionY()>112/2&part.getPositionY()<240/2&(part.getPositionX()==727/2))
			{
				part.setPositionY(191/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			//path24-path35
			if(part.getPositionY()>177/2&part.getPositionY()<194/2&(part.getPositionX()>672/2&part.getPositionX()<694/2))
			{
				part.setPositionX(685/2);
				part.setPositionY(166/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			//path34-path33
			if(part.getPositionY()>152/2&part.getPositionY()<170/2&(part.getPositionX()>660/2&part.getPositionX()<678/2))
			{
				part.setPositionX(670/2);
				part.setPositionY(155/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			//path32-path31
			if(part.getPositionY()>140/2&part.getPositionY()<156/2&(part.getPositionX()>645/2&part.getPositionX()<660/2))
			{
				part.setPositionX(650/2);
				part.setPositionY(144/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			//path30-path15
			if(part.getPositionY()>120/2&part.getPositionY()<150/2&(part.getPositionX()>620/2&part.getPositionX()<646/2))
			{
				part.setPositionX(640/2);
				part.setPositionY(126/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			//path10-path1
			if(part.getPositionY()>92/2&part.getPositionY()<106/2&(part.getPositionX()==530/2))
			{
				part.setPositionY(94/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			else
			{ 
				part.setHitWall(1);
				
			}
			break;
		case 2:
			//path12-path15
			if(part.getPositionY()>94/2&part.getPositionY()<150/2&(part.getPositionX()==580/2||part.getPositionX()==530/2))
			{
				part.setPositionY(126/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			//path29-path30
			if((part.getPositionY()>122/2&part.getPositionY()<144/2)&(part.getPositionX()>636/2&part.getPositionX()<650/2))
			{
				part.setPositionX(640/2);
				part.setPositionY(144/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			//path31-path32
			if((part.getPositionY()>140/2&part.getPositionY()<158/2)&(part.getPositionX()>645/2&part.getPositionX()<655/2))
			{
				part.setPositionX(650/2);
				part.setPositionY(155/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			//path33-path34
			if((part.getPositionY()>151/2&part.getPositionY()<168/2)&(part.getPositionX()>660/2&part.getPositionX()<680/2))
			{
				part.setPositionX(670/2);
				part.setPositionY(166/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			//path35-path24
			if((part.getPositionY()>164/2&part.getPositionY()<191/2)&(part.getPositionX()>675/2&part.getPositionX()<690/2))
			{
				part.setPositionX(685/2);
				part.setPositionY(191/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}	
			else
			{ 
				part.setHitWall(1);
			}
			break;
		case 3:
			//path24-path25
			if(part.getPositionX()>700/2&part.getPositionX()<728/2&part.getPositionY()==191/2)
			{
				part.setPositionX(727/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}	
			
			//path1-path10
			if(part.getPositionX()>500/2&part.getPositionX()<560/2&part.getPositionY()==94/2)
			{
				part.setPositionX(530/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			else
			{ 
				part.setHitWall(1);
			}
			break;
		case 4:
			//path15-path10
			if(part.getPositionX()>530/2&part.getPositionX()<545/2&(part.getPositionY()==126/2))
			{
				part.setPositionX(530/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			//path1-path8
			if(part.getPositionX()>260/2&part.getPositionX()<300/2&part.getPositionY()==94/2)
			{
				part.setPositionX(287/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			//path16 path17 path18-path12
			if(part.getPositionX()>570/2&part.getPositionX()<600/2&(part.getPositionY()==144/2||part.getPositionY()==155/2||part.getPositionY()==166/2))
			{
				part.setPositionX(580/2);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
			else
			{ 
				part.setHitWall(1);
			}
			break;
		}
	}

	private void enlargeintersection_6(Particle part){
		switch(part.getDirection())
		{
		case 1:
			//path2-path1,path3
			if(part.getPositionY()>15&part.getPositionY()<55&(part.getPositionX()==56))
			{
			    if(part.getPositionY()<35){
                    part.setPositionY(28);
                }
                else{
                    part.setPositionY(38);
                }
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
            //path4-path1
            if(part.getPositionY()>25&part.getPositionY()<55&(part.getPositionX()==105))
            {
                part.setPositionY(38);
                updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
            }

            //path5-path1
            if(part.getPositionY()>25&part.getPositionY()<45&(part.getPositionX()==79))
            {
                part.setPositionY(38);
                updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
            }

            //path5-path6
            if(part.getPositionY()>55&part.getPositionY()<=70&(part.getPositionX()==79))
            {
                part.setPositionX(66);
                updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
            }

//            //path6-path1(第7层)
//            if(part.getPositionY()>15&part.getPositionY()<45&(part.getPositionX()==66))
//            {
//                part.setPositionY(28);
//                updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
//            }
			break;
		case 2:
			//path2-path1,path3
			if(part.getPositionY()>15&part.getPositionY()<55&(part.getPositionX()==56))
			{
                if(part.getPositionY()<35){
                    part.setPositionY(28);
                }
                else{
                    part.setPositionY(38);
                }
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
            //path4-path1
            if(part.getPositionY()>25&part.getPositionY()<55&(part.getPositionX()==105))
            {
                part.setPositionY(38);
                updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
            }

            //path6-path5
            if(part.getPositionY()>55&part.getPositionY()<=70&(part.getPositionX()==66))
            {
                part.setPositionX(79);
                updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
            }
            //path5-path1
            if(part.getPositionY()>25&part.getPositionY()<45&(part.getPositionX()==79))
            {
                part.setPositionY(38);
                updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
            }
			break;

		case 3:
			//path1,path3-path2
			if(part.getPositionX()>40&part.getPositionX()<65&(part.getPositionY()==38|| part.getPositionY()==28))
			{
				part.setPositionX(56);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}

            //path1-path4
            if(part.getPositionX()>90&part.getPositionX()<120&part.getPositionY()==38)
            {
                part.setPositionX(105);
                updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
            }

            //path1-path5(楼梯)
            if(part.getPositionX()>70&part.getPositionX()<110&part.getPositionY()==38)
            {
                part.setPositionX(79);
                updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
            }

			break;

		case 4:
			//path1,path3-path2
			if(part.getPositionX()>35&part.getPositionX()<65&(part.getPositionY()==38|| part.getPositionY()==28))
			{
				part.setPositionX(56);
				updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
			}
            //path1-path4
            if(part.getPositionX()>90&part.getPositionX()<120&part.getPositionY()==38)
            {
                part.setPositionX(105);
                updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
            }
			break;
		}

	}

    private void enlargeintersection_7(Particle part) {
        switch (part.getDirection()) {
        case 1:
            //path4-path1(第6层)
            if(part.getPositionY()>25&part.getPositionY()<55&(part.getPositionX()==79))
            {
                part.setPositionY(38);
                updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
            }
            //path3-path1
            if(part.getPositionY()>15&part.getPositionY()<45&(part.getPositionX()==66))
            {
                part.setPositionY(28);
                updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
            }

            //path4-path3
            if(part.getPositionY()>55&part.getPositionY()<=70&(part.getPositionX()==79))
            {
                part.setPositionX(66);
                updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
            }
            break;
        case 2:
            //path3-path4
            if(part.getPositionY()>55&part.getPositionY()<=70&(part.getPositionX()==66))
            {
                part.setPositionX(79);
                updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
            }

            //path4-path1(第6层)
            if(part.getPositionY()>25&part.getPositionY()<55&(part.getPositionX()==79))
            {
                part.setPositionY(38);
                updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
            }
            break;
        case 3:
            //path1-path3
            if(part.getPositionX()>50&part.getPositionX()<=80&part.getPositionY()==28)
            {
                part.setPositionX(66);
                updateListsParticleOnPath(part, (int)part.getPositionX(),(int)part.getPositionY());
            }

            break;
        case 4:


            break;
        }
    }
}