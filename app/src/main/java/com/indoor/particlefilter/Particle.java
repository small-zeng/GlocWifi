package com.indoor.particlefilter;

import java.util.ArrayList;
import java.util.Vector;

public class Particle {

	private double weight;
	private double length;
	private int direction;
	private Vector<Float> magnetics = new Vector<Float>();
	private int xPos;
	private int yPos;
	private int num;
	private int hitWall;
	
	private ArrayList<Integer> rightBIN = new ArrayList<Integer> ();
	private ArrayList<Integer> leftBIN = new ArrayList<Integer> ();
	private ArrayList<Integer> topBIN= new ArrayList<Integer> ();
	private ArrayList<Integer> bottomBIN = new ArrayList<Integer> ();
	
	private ArrayList<String> rightMAC = new ArrayList<String> ();
	private ArrayList<String> leftMAC = new ArrayList<String> ();
	private ArrayList<String> topMAC = new ArrayList<String> ();
	private ArrayList<String> bottomMAC = new ArrayList<String> ();
	
	private double wLeft; 
	private double wRight; 
	private double wTop; 
	private double wBottom; 
	private double variance; 
	
	public Particle()
	{
		
	}
	
	public Particle(double l,int dir,int x, int y,int num, int hitwall,double variance,
					ArrayList<Integer> leftBIN, ArrayList<Integer> rightBIN, ArrayList<Integer> bottomBIN, ArrayList<Integer> topBIN,
					ArrayList<String> leftMAC, ArrayList<String> rightMAC, ArrayList<String> bottomMAC, ArrayList<String> topMAC,
					double wLeft, double wRight , double wBottom, double wTop)
	{
		this.variance=variance;
		this.wLeft=wLeft;
		this.wRight=wRight;
		this.wTop=wTop;
		this.wBottom=wBottom;
		this.length=l;
		this.direction=0;
		this.leftBIN=leftBIN;
		this.rightBIN=rightBIN;
		this.topBIN=topBIN;
		this.bottomBIN=bottomBIN;
		this.leftMAC=leftMAC;
		this.rightMAC=rightMAC;
		this.topMAC=topMAC;
		this.bottomMAC=bottomMAC;
		this.xPos=x;
		this.yPos=y;
		this.num=num;
		this.hitWall=hitwall;
	}
	
	/** GETTER **************************************************** */
	public int getNum()
	{
		return this.num;
	}
	
	public double getWeight()
	{
		return this.weight;
	}
	
	public double getLength()
	{
		return this.length;
	}

	public int getDirection()
	{
		return this.direction;
	}
	
	public double getPositionX()
	{
		return this.xPos;
	}
	
	public double getPositionY()
	{
		return this.yPos;
	}
	
	public int getHitWall()
	{
		return this.hitWall;
	}
	
	public double getVariance()
	{
		return this.variance;
	}
	
	public double getLeftWeight()
	{
		return this.wLeft;
	}
	
	public double getRightWeight()
	{
		return this.wRight;
	}
	
	public double getTopWeight()
	{
		return this.wTop;
	}
	
	public double getBottomWeight()
	{
		return this.wBottom;
	}
	
	public ArrayList<String> getLeftMACList()
	{
		return this.leftMAC;
	}
	
	public ArrayList<String> getRightMACList()
	{
		return this.rightMAC;
	}
	
	public ArrayList<String> getTopMACList()
	{
		return this.topMAC;
	}
	
	public ArrayList<String> getBottomMACList()
	{
		return this.bottomMAC;
	}
	
	public ArrayList<Integer> getLeftBINList()
	{
		return this.leftBIN;
	}
	
	public ArrayList<Integer> getRightBINList()
	{
		return this.rightBIN;
	}
	
	public ArrayList<Integer> getTopBINList()
	{
		return this.topBIN;
	}
	
	public ArrayList<Integer> getBottomBINList()
	{
		return this.bottomBIN;
	}
		
	/** SETTER **************************************************** */
	
	public void setNum(int numero)
	{
		this.num=numero;
	}
	
	public void setWeight(double wght)
	{
		this.weight=wght;
	}
	
	public void setLength(double lgt)
	{
		this.length=lgt;
	}
	
	public void setDirection(int dir)
	{
		this.direction=dir;
	}
	
	public void setPositionX(int xpos)
	{
		this.xPos=xpos;
	}
	
	public void setPositionY(int ypos)
	{
		this.yPos=ypos;
	}
	
	public void setMagnetics(Vector<Float> mag)
	{
		this.magnetics=mag;
	}
	
	public void setHitWall(int hitwall)
	{
		this.hitWall=hitwall;
	}
	
	public void setLeftWeight(double wght)
	{
		this.wLeft=wght;
	}
	
	public void setRightWeight(double wght)
	{
		this.wRight=wght;
	}
	
	public void setTopWeight(double wght)
	{
		this.wTop=wght;
	}
	
	public void setBottomWeight(double wght)
	{
		this.wBottom=wght;
	}
	
	public void setVariance(double var)
	{
		this.variance=var;
	}
		
	public void setLeftBINList(ArrayList<Integer> list)
	{
		this.leftBIN=list;
	}
	
	public void setRightBINList(ArrayList<Integer> list)
	{
		this.rightBIN=list;
	}
	
	public void setTopBINList(ArrayList<Integer> list)
	{
		this.topBIN=list;
	}
	
	public void setBottomBINList(ArrayList<Integer> list)
	{
		this.bottomBIN=list;
	}
	
	public void setLeftMACList(ArrayList<String> list)
	{
		this.leftMAC=list;
	}
	
	public void setRightMACList(ArrayList<String> list)
	{
		this.rightMAC=list;
	}
	
	public void setTopMACList(ArrayList<String> list)
	{
		this.topMAC=list;
	}
	
	public void setBottomMACList(ArrayList<String> list)
	{
		this.bottomMAC=list;
	}
	
	
	/** ADDER **************************************************** */	
	
	public void normalizeWeight () 
	{
		double sum = 0 ;	
		sum = this.wLeft + this.wRight + this.wTop + this.wBottom;	
		this.wLeft = this.wLeft/sum;
		this.wRight = this.wRight/sum;
		this.wTop = this.wTop/sum;
		this.wBottom = this.wBottom/sum;
	}	
	
	
}
