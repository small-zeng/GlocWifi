package com.indoormap.tools;

import java.util.ArrayList;

public class LCS {

    public static int longestCommonSubsequence( ArrayList<String>  text1,  ArrayList<String>  text2) {
        int m= text1.size(), n=text2.size();
        int[][] dp=new int[m+1][n+1];

        for(int i=0;i<m;i++){
            for(int j=0;j<n;j++){
                if(text1.get(i).equals(text2.get(j))){
                    dp[i+1][j+1] = dp[i][j]+1;
                }else{
                    dp[i+1][j+1] =Math.max(dp[i+1][j],dp[i][j+1]);
                }

            }
        }
        return dp[m][n];

    }



}
