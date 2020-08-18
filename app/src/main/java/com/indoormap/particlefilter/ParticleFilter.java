package com.indoormap.particlefilter;

import com.indoormap.map.Map;
import com.indoormap.map.PublicData;
import com.indoormap.tools.availablePoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;


public class ParticleFilter  {

    public  ArrayList<Particle> particles = new ArrayList<Particle>();
    private Random generator=new Random();
    private double sigma = 0.5;
    private final static int particleRatio = 160;       //地图上每160个可以到达的点生成1个粒子
    private final static double shiftRatio = 0.6;       //置信度最高的60%的粒子仅进行移动
    private final static double predictRatio = 0.2;     //20%的粒子根据PDR数据预测位置
    private final static double shiftDirGaussScale = 20;    //移动粒子时，方向的高斯噪声尺度为±10°
    private final static double shiftDisGaussScale = 10;    //移动粒子时，距离的高斯噪声尺度为5分米（50厘米）
    private final static double predictDirGaussScale = 40;  //预测粒子时，方向的高斯噪声尺度为±20°
    private final static double predictDisGaussScale = 20;  //预测粒子时，距离的高斯噪声尺度为10分米（100厘米）
    private final static int muterange = 5;               //粒子聚集时高权重粒子的高斯范围
    public int resultX;         //定位结果
    public int resultY;

    public static ArrayList<Particle> shiftParticlesList;
    public static ArrayList<Particle> predictParticlesList;
    public static ArrayList<Particle> randomParticlesList;

    public static int particleNum = 0;      //维护粒子的数量


    public ParticleFilter(){
    }

    public ArrayList<Particle> getParticles(){
        return this.particles;
    }

    //初始化粒子
    public void initParticles()
    {
        this.particles.clear();
        int x = 0;
        int y = 0;
        int randomIndex = 0;
        particleNum = Map.availableArea.size()/particleRatio;
        for(int i=0;i<particleNum;i++)
        {
            randomIndex = generator.nextInt(Map.availableArea.size());
            availablePoint temp = Map.availableArea.get(randomIndex);
            x = temp.x;
            y = temp.y;
            particles.add(new Particle(x,y));
        }
    }

    //粒子聚集
    public void aggregateParticles(){
        ArrayList<Particle> nextDistribution = new ArrayList<Particle>();

        Collections.sort(this.particles,confidenceComparator);

        normalizeConfidence();

        int x = 0;
        int y = 0;
        for(Particle part: this.particles){
            int cloneNum = (int)Math.floor(part.normolizedConfidence* particleNum);
            for(int i = 0 ; i < cloneNum ; i++){
                x = part.x + (int)generator.nextGaussian()*muterange;
                y = part.y + (int)generator.nextGaussian()*muterange;
                if(Map.assertCoordinates(x,y))
                    nextDistribution.add(new Particle(x,y));
                else
                    nextDistribution.add(new Particle(part.x,part.y));
            }
        }
        int replenishNum = this.particles.size() - nextDistribution.size();
        int randomIndex;
        for(int i = 0 ; i < replenishNum ; i ++){
            randomIndex = generator.nextInt(Map.availableArea.size());
            availablePoint temp = Map.availableArea.get(randomIndex);
            x = temp.x;
            y = temp.y;
            nextDistribution.add(new Particle(x,y));
        }
        this.particles = nextDistribution;
    }

    //得出估计位置
    public void getPosition(){
        Collections.sort(this.particles,confidenceComparator);

        ArrayList<Particle> gatheredParticles = gatherParticle() ;

        int particleNum = gatheredParticles.size();
        int sumX = 0;
        int sumY = 0;
        Particle part = null;
        for(int i = 0 ; i < particleNum ; i++){
            part = this.particles.get(i);
            sumX += part.x;
            sumY += part.y;
        }
        this.resultX = (int)(sumX/particleNum);
        this.resultY = (int)(sumY/particleNum);
    }

    private  ArrayList<Particle> gatherParticle(){
        int highestConfidenceNum = (int)(PublicData.highWeightRate * particleNum);
        ArrayList<Particle> gatheredParticles = new ArrayList<>(this.particles.subList(0,highestConfidenceNum));
        int[] distances = new int[gatheredParticles.size()];
        int sumX = 0;
        int sumY = 0;
        Particle part = null;
        for(int i = 0 ; i < gatheredParticles.size() ; i++){
            part = gatheredParticles.get(i);
            sumX += part.x;
            sumY += part.y;
        }
        int averageX = sumX/gatheredParticles.size();
        int averageY = sumY/gatheredParticles.size();
        int sumDistance = 0;
        for(int i = 0 ; i < gatheredParticles.size() ; i ++){
            part = gatheredParticles.get(i);
            distances[i] = (part.x - averageX) * (part.x - averageX) + (part.y - averageY) * (part.y - averageY);
            sumDistance += distances[i];
        }

        int averageDistance = sumDistance / distances.length;

        int boundary = averageDistance * 2;
        for(int i = distances.length - 1 ; i >= 0  ; i --){
            if(distances[i] > boundary)
                gatheredParticles.remove(i);
        }

        return gatheredParticles;
    }

    //归一化置信度，用于粒子聚集
    private void normalizeConfidence(){
        double totalCondifence = 0;
        for(Particle part: this.particles)
        {
            totalCondifence = totalCondifence + part.confidence;
        }
        for(Particle part: this.particles)
        {
            part.normolizedConfidence = part.confidence/totalCondifence;
        }
    }

    public void updateParticles(double direction, double distance)
    {

        int shiftNum = (int)(this.shiftRatio * this.particles.size());     //计算移动粒子的数量
        int predictNum = (int)(this.predictRatio * this.particles.size());  //计算预测粒子的数量
        this.shiftParticlesList = new ArrayList<>(this.particles.subList(0, shiftNum));
        this.predictParticlesList =new ArrayList<>(this.particles.subList(shiftNum + 1, shiftNum + predictNum));
        this.randomParticlesList = new ArrayList<>(this.particles.subList(shiftNum + predictNum + 1, this.particles.size()));

        this.shiftParticles(this.shiftParticlesList, direction, distance);
        this.predictParticles(this.predictParticlesList, direction, distance);
        this.randomParticles(this.randomParticlesList);
    }

    //移动粒子
    private void shiftParticles(ArrayList<Particle> shiftParticlesList, double direction, double distance)
    {
        double gaussianDirection;
        double gaussianDistance;
        double gaussianFactor;
        double resRadian;
        int i;

        for(Particle sParticle: shiftParticlesList)
        {
            for(i = 0;i < 3;i++)        //尝试三次
            {
                gaussianFactor = generator.nextGaussian();
                gaussianDirection = direction + shiftDirGaussScale * gaussianFactor -  shiftDirGaussScale / 2;
                gaussianFactor = generator.nextGaussian();
                gaussianDistance = distance + shiftDisGaussScale * gaussianFactor - shiftDisGaussScale / 2;
                resRadian = Math.toRadians(gaussianDirection);
                sParticle.x += Math.cos(resRadian) * gaussianDistance;
                sParticle.y += Math.sin(resRadian) * gaussianDistance;
                if(Map.assertCoordinates(sParticle.x, sParticle.y))
                {
                    sParticle.updateConfidence();
                    break;
                }
            }
            if(i > 2)randomParticlesList.add(sParticle);   //三次移动的坐标都非法，加入随机散布列表
        }
    }

    //根据PDR数据生成预测粒子
    private void predictParticles(ArrayList<Particle> predictParticlesList, double direction, double distance)
    {
        double gaussianDirection;
        double gaussianDistance;
        double gaussianFactor;
        double resRadian;
        int i;

        for(Particle pParticle: predictParticlesList)
        {
            for(i = 0;i < 3;i++)        //尝试三次
            {
                gaussianFactor = generator.nextGaussian();
                gaussianDirection = direction + predictDirGaussScale * gaussianFactor -  predictDirGaussScale / 2;
                gaussianFactor = generator.nextGaussian();
                gaussianDistance = distance + predictDisGaussScale * gaussianFactor - predictDisGaussScale / 2;
                resRadian = Math.toRadians(gaussianDirection);
                pParticle.x = this.resultX + (int)(Math.round(Math.cos(resRadian) * gaussianDistance));
                pParticle.y = this.resultY + (int)(Math.round(Math.sin(resRadian) * gaussianDistance));
                if(Map.assertCoordinates(pParticle.x, pParticle.y))
                {
                    pParticle.confidence = -1;
                    pParticle.updateConfidence();
                    break;
                }
            }
            if(i == 3)this.randomParticlesList.add(pParticle);    //三次预测的坐标都非法，加入随机散布列表
        }
    }
    //全图随机散布粒子
    public void randomParticles(ArrayList<Particle> randomParticlesList)
    {
        int randomIndex = 0;

        for(Particle rParticle: randomParticlesList)
        {
            randomIndex = generator.nextInt(Map.availableArea.size());
            availablePoint temp = Map.availableArea.get(randomIndex);
            rParticle.x = temp.x;
            rParticle.y = temp.y;
            rParticle.confidence = -1;
            rParticle.updateConfidence();
        }
    }


    //Particle降序
    private Comparator<Particle> confidenceComparator = new Comparator<Particle>() {
        @Override
        public int compare(Particle pa, Particle pb) {
            double ca = pa.confidence;
            double cb = pb.confidence;
            if(ca > cb)
                return -1;
            if(ca < cb)
                return 1;
            return 0;
        }
    };

}