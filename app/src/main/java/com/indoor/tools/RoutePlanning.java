package com.indoor.tools;

import android.content.Context;
import android.widget.Toast;

import com.indoor.gloc.MapActivity;
import com.indoor.map.Floor;
import com.indoor.map.PublicData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;


public class RoutePlanning {
    //起点和终点
    public    Point startPoint =new Point(10,28);
    public    Point endPoint =new Point(105,5);

    //点列和路径列
    private  ArrayList<Point>  pointList = new ArrayList<Point>();
    private  ArrayList<ArrayList<Point>>  pathList = new ArrayList<ArrayList<Point>>();

    //顶点数量
    private  int pointNum=0;

    //邻接矩阵
    private int[][] adjacentMatrix;//计算产生的邻接矩阵
    private static int[][] network={{0,1,0,2,0,0},//用于仿真测试的邻接矩阵
                                     {1,0,2,4,3,0},
                                     {0,2,0,0,1,4},
                                     {2,4,0,0,6,0},
                                     {0,3,1,6,0,2},
                                     {0,0,4,0,2,0}};
    //最短路径
    public  ArrayList<Integer> shortestPathNumber =new ArrayList<>();
    public  ArrayList<Point> shortestPath = new ArrayList<>();

    //输入是否有效标志位
    public boolean inputValid=true;

    //路径数据初始化,加载进内存
    public RoutePlanning(Context context,String start,String end){
        PublicData.getrout(Floor.floorID);
        String [] str1=start.split(",");
        String [] str2=end.split(",");
        if(str1.length==2 && str2.length==2){
            startPoint.x=Integer.parseInt(str1[0]);
            startPoint.y=Integer.parseInt(str1[1]);
            endPoint.x=Integer.parseInt(str2[0]);
            endPoint.y=Integer.parseInt(str2[1]);
            //j将路径数据加载进内存
            for(int i=0;i<PublicData.rout.length/4;i++){
                int boundary1=PublicData.rout[4*i];
                int boundary2=PublicData.rout[4*i+1];
                int boundary3=PublicData.rout[4*i+2];
                int boundary4=PublicData.rout[4*i+3];
                pointList.add(new Point(boundary1,boundary2));
                pointList.add(new Point(boundary3,boundary4));
                pathList.add(new ArrayList<Point>());
                pathList.get(pathList.size()-1).add(new Point(boundary1,boundary2));
                pathList.get(pathList.size()-1).add(new Point(boundary3,boundary4));
            }
            boolean inputCheck_start = false;
            boolean inputCheck_end = false;
            System.out.println("开始测试..."+start+"->"+end);
            //判断输入的起点和终点是否有意义（是否在地图路径中）,若不正确会退出
            for( ArrayList<Point> path : pathList){
                if (pointIsOnPath(path,startPoint)){
                    inputCheck_start = true;
                }
                if (pointIsOnPath(path,endPoint)){
                    inputCheck_end = true;
                }
            }
            System.out.println(inputCheck_start+","+inputCheck_end);
            if (inputCheck_start && inputCheck_end){
                System.out.println("输入点有意义");
            }
            else {
                System.out.println("输入点无意义，请正确输入");
                Toast.makeText(context,"输入点不在地图路径中，请正确输入!!!",Toast.LENGTH_SHORT).show();
                inputValid = false;
            }

            //判断输入的起点和终点是相同
            if(startPoint.equals(endPoint)){
                Toast.makeText(context,"输入起点和终点相同!!!",Toast.LENGTH_SHORT).show();
                inputValid=false;
            }

        }
        else {
            Toast.makeText(context,"坐标格式输入不正确，请按提示输入!!!",Toast.LENGTH_SHORT).show();
            inputValid=false;

        }

    }


/*
    public static void main(String[] args) {
        RoutePlanning routePlanning =new RoutePlanning();
        File file = new File("src/zj_floor1.txt");
        routePlanning.txt2String(file);
        for(int i=0;i<pointList.size();i++){
            System.out.print(pointList.get(i).x+","+pointList.get(i).y+"---");
        }
//      routePlanning.Dijkstra(network,1,6);//用于仿真测试
        routePlanning.test(startPoint,endPoint);

    }

    //读取txt文件内容（加载路径数据）
    public  String txt2String(File file){
        StringBuilder result = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
            String str = null;
            while((str = br.readLine())!=null){//使用readLine方法，一次读一行
                result.append(System.lineSeparator()+str);
                byte [] bytes =str.getBytes();
                String [] str1=str.split(",");
                int [] path =new int[4];

                if(bytes[0]!='/'){
                    for(int i = 0;i <4;i++) {
                        path[i] = Integer.parseInt(str1[i]);
                    }
                pointList.add(new Point(path[0],path[1]));
                pointList.add(new Point(path[2],path[3]));
                pathList.add(new ArrayList<Point>());
                pathList.get(pathList.size()-1).add(new Point(path[0],path[1]));
                pathList.get(pathList.size()-1).add(new Point(path[2],path[3]));
                }
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return result.toString();
    }
*/
    //迪杰斯特拉算法算s-d的最短路径，并返回该路径和代价
    public ArrayList<Integer> Dijkstra(int [][]network,int s,int d){
        System.out.println("Start Dijstra Path……");
        int n=network.length;  //邻接矩阵维度，即节点个数
        int fmax=999;
        int [][]w=new int[n][n];//邻接矩阵转化成维度矩阵，即0→max;初始值均为0
        int [] book=new int[n];//是否已经是最小的标记列表
        int [] dis=new int[n];//s到其他节点的最小距离
        Arrays.fill(dis, fmax);//dis初始化全部赋值为fmax
        book[s-1]=1;//节点编号从1开始，列表序号从0开始
        int [] midpath=new int[n];//上一跳列表
        Arrays.fill(midpath, -1);//midpath初始化全部赋值为-1
        ArrayList<Integer> path = new ArrayList<Integer>();// s-d的最短路径

        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++){
                if(network[i][j]!=0){
                    w[i][j]=network[i][j];//0→max
                }
                else {
                    w[i][j]=fmax;
                }
                if(i==s-1 && network[i][j]!=0){ //直连的节点最小距离就是network[i][j]
                    dis[j]=network[i][j];
                }
            }
        }
//        System.out.println(Arrays.toString(dis));

        //n-1次遍历，除了s节点
        for(int i=0;i<n-1;i++){
            int min=fmax;
            int u=0;
            for(int j=0;j<n;j++){
                if( book[j]==0 && dis[j]<min){ //如果未遍历且距离最小
                    min=dis[j];
                    u=j;
                }
            }
            book[u]=1;
            for(int v=0;v<n;v++){
                if(dis[v]>dis[u]+w[u][v]){
                    dis[v]=dis[u]+w[u][v];
                    midpath[v]=u+1; //上一跳更新
                }
            }
        }

        int j=d-1;//j是序号
        path.add(d);//因为存储的是上一跳，所以先加入目的节点d，最后倒置
        while(midpath[j]!=-1){
            path.add(midpath[j]);
            j=midpath[j]-1;
        }

        path.add(s);
        Collections.reverse(path); //倒置列表
        System.out.println(path);
//        System.out.println(Arrays.toString(midpath));
        System.out.println(Arrays.toString(dis));

        return path;
    }

    //判断点是否在线段上
    public boolean pointIsOnPath(ArrayList<Point> path,Point point){
        int oa_x = path.get(0).x - point.x;
        int oa_y = path.get(0).y - point.y;
        int ob_x = path.get(1).x - point.x;
        int ob_y = path.get(1).y - point.y;
        if  ((oa_x * ob_y - oa_y * ob_x)==0) { //在直线上
            if ((point.x - path.get(0).x) *(point.x - path.get(1).x) <= 0 && (point.y - path.get(0).y) * (point.y - path.get(1).y) <= 0) {//在线段上
                return true;
            }
        }
        return false;
    }

    //计算两条线段交点
   public Point calulateCorssLines(ArrayList<Point> path1, ArrayList<Point> path2){
       /**
        * 求两条直线直接的交点
        * :param line0_pos0: 第一条直接的第一个点的坐标
        * :param line0_pos1: 第一条直接的第二个点的坐标
        * :param line1_pos0: 第二条直接的第一个点的坐标
        * :param line1_pos1: 第二条直接的第二个点的坐标
        *
        * x = (b0*c1 – b1*c0)/D
        * y = (a1*c0 – a0*c1)/D
        * D = a0*b1 – a1*b0， (D为0时，表示两直线重合)
        */
       //四个端点
       Point line0_pos0 = new Point(path1.get(0).x,path1.get(0).y);
       Point line0_pos1 = new Point(path1.get(1).x,path1.get(1).y);
       Point line1_pos0 = new Point(path2.get(0).x,path2.get(0).y);
       Point line1_pos1 = new Point(path2.get(1).x,path2.get(1).y);

       //相关向量
       int line0_a =line0_pos0.y- line0_pos1.y;
       int line0_b = line0_pos1.x - line0_pos0.x;
       int line0_c = line0_pos0.x *line0_pos1.y - line0_pos1.x * line0_pos0.y;
       int line1_a =line1_pos0.y - line1_pos1.y;
       int line1_b = line1_pos1.x - line1_pos0.x;
       int line1_c = line1_pos0.x *line1_pos1.y - line1_pos1.x * line1_pos0.y;
//       System.out.println(line0_pos0.toString()+line0_pos1.toString()+line1_pos0.toString()+line1_pos1.toString());
       int d = line0_a * line1_b - line1_a * line0_b;
       if(d==0){
           //重合或者平行的边线没有交点
           System.out.println("没有交点");
           return null;
       }
       double x = (line0_b * line1_c - line1_b * line0_c) * 1.0 / d;
       double y = (line0_c * line1_a - line1_c * line0_a) * 1.0 / d;

       if ((x - line0_pos0.x) * (x - line0_pos1.x) <= 0 && (x - line1_pos0.x) * (x - line1_pos1.x) <= 0
       &&(y - line0_pos0.y) * (y - line0_pos1.y) <= 0 && (y - line1_pos0.y) * (y - line1_pos1.y) <= 0){
            //判断交点是否在两条线段上
//            System.out.println(x+","+y);
            return new Point((int)x,(int)y);
       }
       else {
           //交点不在两条线段上除外
           System.out.println("交点不在两条线段上");
           return null;
       }

   }

   //原路径的点排序(按照x或y从小到大,type=1按照y排序，type=0按照x排序)
    public ArrayList<Point> sort(ArrayList<Point>pathPoint,int type ){
        int  n = pathPoint.size();
        //遍历所有数组元素
        for(int i=0;i<n;i++){
            for(int j=0;j<n-i-1;j++){
                int temp_x,temp_y;
                temp_x=pathPoint.get(j).x;
                temp_y=pathPoint.get(j).y;
                if(type==0){
                    if(pathPoint.get(j).x > pathPoint.get(j+1).x){
                        pathPoint.get(j).x=pathPoint.get(j+1).x;
                        pathPoint.get(j).y=pathPoint.get(j+1).y;
                        pathPoint.get(j+1).x=temp_x;
                        pathPoint.get(j+1).y=temp_y;
                    }

                }
                else if(type==1){
                    if(pathPoint.get(j).y > pathPoint.get(j+1).y){
                        pathPoint.get(j).x=pathPoint.get(j+1).x;
                        pathPoint.get(j).y=pathPoint.get(j+1).y;
                        pathPoint.get(j+1).x=temp_x;
                        pathPoint.get(j+1).y=temp_y;
                    }
                }

            }
        }

        return pathPoint;

    }

    //路径规划测试
    public void test(Point start,Point end){
        System.out.println(pointList.size());
        //对于起点和终点，判断pointList是否需要增加新的顶点，若是，则增加
        if(!pointList.contains(new Point(start))){
            pointList.add(new Point(start));
        }
        if(!pointList.contains(new Point(end))){
            pointList.add(new Point(end));
        }
//        for(int i=0;i<pointList.size();i++){
//            System.out.print(pointList.get(i).x+","+pointList.get(i).y+"---");
//        }
//        System.out.println();
//        System.out.println(pointList.size());

        //除去原路径中起点和终点的重复计入
        HashSet hSet  =   new  HashSet((List)pointList);
        pointList.clear();
        pointList.addAll(hSet);
        System.out.println(pointList.size());

        //计算线段交点，若是新出现的顶点，添加在点列中
        for(int i=0;i<pathList.size()-1;i++){
            for(int j=i+1;j<pathList.size();j++){
                System.out.println(i+","+j);
                Point intersectPoint = calulateCorssLines(pathList.get(i), pathList.get(j));
//                System.out.println(intersectPoint.toString());
                if (intersectPoint!=null) {
                    System.out.println(intersectPoint.toString());
                    if(pointList.contains(intersectPoint)){
                        System.out.println("不是新的顶点");
                    }
                    else {
                        System.out.println("找到新的顶点");
                        pointList.add(intersectPoint);
                    }
                }

            }
        }

        pointNum = pointList.size();
        System.out.println("图中顶点数为:" + pointNum);
        adjacentMatrix=new int[pointNum][pointNum];//邻接矩阵分配空间
        //每条路径的点列排序并生成邻接矩阵
        for(ArrayList<Point> path :pathList){
            ArrayList<Point> pathPoint =new  ArrayList<Point>();
            for(Point point : pointList) {
                if(pointIsOnPath(path, point)) {
                    pathPoint.add(new Point(point));
                }
            }

            ArrayList<Point> pathPoint1 =new  ArrayList<Point>();
            if(path.get(0).x==path.get(1).x){
                pathPoint1=sort(pathPoint,1);
            }
            else if(path.get(0).y==path.get(1).y){
                pathPoint1=sort(pathPoint,0);
            }
            System.out.println("排序后：");
            for(int i=0;i<pathPoint.size();i++){
                System.out.print(pathPoint1.get(i).toString());
            }
            System.out.println();
            for(int i=0;i<pathPoint1.size()-1;i++) {
                int index1 = pointList.indexOf(pathPoint1.get(i));
                int index2 = pointList.indexOf(pathPoint1.get(i+1));
                System.out.println("index="+index1+","+index2);
                int distance=(int)Math.sqrt(Math.pow(pathPoint1.get(i).x - pathPoint1.get(i+1).x, 2) + Math.pow(pathPoint1.get(i).y - pathPoint1.get(i+1).y, 2));
                adjacentMatrix[index1][index2] = distance;
                adjacentMatrix[index2][index1] = distance;
            }

        }

        //打印
        for(int i=0;i<pointList.size();i++){
            System.out.print(pointList.get(i).x+","+pointList.get(i).y+"---");
        }
        System.out.println();
        for(int i=0;i<pathList.size();i++){
            System.out.print(pathList.get(i).get(0).x+","+pathList.get(i).get(0).y+";");
            System.out.print(pathList.get(i).get(1).x+","+pathList.get(i).get(1).y+"---");
        }
        System.out.println();
        for(int i=0;i<adjacentMatrix.length;i++){
            System.out.println(Arrays.toString(adjacentMatrix[i]));//调用Arrays.toString将数组a的值转换为字符串并 显示出来
        }

        //计算最短路径
        int index1 = pointList.indexOf(start);
        int index2 = pointList.indexOf(end);
        shortestPathNumber=Dijkstra(adjacentMatrix,index1+1,index2+1);
        System.out.print("最短路径为：");
        for(int i : shortestPathNumber){
            System.out.print((pointList.get(i-1))+"->");
            shortestPath.add(new Point(pointList.get(i-1)));
        }
        System.out.println();
        System.out.println("结束测试..."+start.toString()+"->"+end.toString());
    }

}
