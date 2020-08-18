package com.indoormap.particlefilter;

import com.indoormap.map.Map;
import com.indoormap.tools.UserData;

import java.util.ArrayList;

public class Particle {

    public int x;                   //粒子当前坐标
    public int y;
    public double confidence = -1;   //综合权重（粒子置信度）
    public double normolizedConfidence = -1;

    private final static double coJaccardIndex = 0.1;
    private final static double coLcsLen = 0.7;
    private final static double coRssiRes = 0.2;
    private final static double coConfidence = 0.8;

    public Particle(int x, int y)
    {
        this.x = x;
        this.y = y;
        this.confidence = -1;
        this.updateConfidence();
    }
    
    //更新粒子置信度
    public void updateConfidence()
    {
        ArrayList<String> userMacList = UserData.userWifiMacList;
        ArrayList<Integer> userRssiList = UserData.userWifiLevelList;
        ArrayList<String> mapMacList = Map.map[this.x][this.y].wifiMacList;
        ArrayList<Integer> mapRssiList = Map.map[this.x][this.y].wifiRssiList;
        int n = userMacList.size();     //手机当前位置wifi数量
        int m = mapMacList.size();      //粒子当前位置wifi数量
        int k = 0;      //手机当前位置与粒子所在位置wifi交集数量
        int i = -1;
        int j;

        //计算Rssi距离的同时建立索引
        int [] indexList = new int[n];
        double rssiRes = 0;
        int index;
        for(String macAddr: userMacList)
        {
            i++;    //统计循环次数，对当前列表进行索引
            index = mapMacList.indexOf(macAddr);        //建立wifi交集索引
            if(index < 0)continue;
            indexList[k] = index;
            k++;
            rssiRes += Math.abs(userRssiList.get(i) - mapRssiList.get(index));
        }
        if(k == 0) rssiRes = 10000;
        else
        {
            for(i = 0;i < m-k;i++)
                rssiRes += (double) 100 * m / k;
            for(i = 0;i < n-k;i++)
                rssiRes += (double) 100 * n / k;
        }

        //计算Jaccard Index
        double jaccardIndex = (double) k / (m + n - k); 

        //计算LCS

        int[] low = new int[k+1];       //维护长度为l的递增子序列的结尾最小值
        int lcsLen = 1;                 //起始子序列长度为1
        if(k == 0)
            lcsLen = 0;
        else{
            low[lcsLen] = indexList[0];     //放入第一个元素
            for(i = 1;i < k;i++)
            {
                if(indexList[i] > low[lcsLen])
                {
                    lcsLen++;
                    low[lcsLen] = indexList[i];
                    continue;
                }
                j = binarySearch(indexList, lcsLen, indexList[i]);
                low[j] = indexList[i];
            }
        }

        //归一化数据
        double lamda;
        double unitJaccardIndex = 0;
        lamda = -0.5;
        if(jaccardIndex > 0) unitJaccardIndex = (1 - Math.exp(jaccardIndex / lamda)) / (1 - Math.exp(1 / lamda));
        
        double unitLcsLen = 0;
        lamda = -0.6;
        if(lcsLen > 0) unitLcsLen = (1 - Math.exp(lcsLen / lamda)) / (1 - Math.exp(1 / lamda));

        double unitRssiRes = 0;
        lamda = 50;
        if(rssiRes > 0) unitRssiRes = 1 / (Math.sqrt(2 * Math.PI) * lamda) * Math.exp(-(rssiRes) / (2 * lamda * lamda));

        double newConfidence;
        newConfidence = coJaccardIndex * unitJaccardIndex + coLcsLen * unitLcsLen + coRssiRes * unitRssiRes;
        if(this.confidence > 0) this.confidence = coConfidence * newConfidence + (1 - coConfidence) * this.confidence;
        else this.confidence = newConfidence;
    }

    public static int binarySearch(int[] array, int len, int key)
    {
        int l = 1;
        int r = len;
        int mid;
        while (l < r)
        {
            mid = (l + r) >> 1;   
            if(key < array[mid])
                r = mid;
            else 
                l = mid+1; 
        }
        return r;
    }
}
