package com.indoormap.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.indoormap.R;

import java.util.ArrayList;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {

    private  Context context;
    private  ArrayList<String> datas=new ArrayList<>();

    public MyRecyclerViewAdapter(Context context, ArrayList<String> datas) {
        this.context = context;
        this.datas =datas;

    }


    @Override
    public MyViewHolder onCreateViewHolder( ViewGroup viewGroup, int i) {
        View  itemView = View.inflate(context, R.layout.item_recyclerview,null);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder( MyViewHolder myViewHolder, int i) {
        //根据位置得到对应的数据
        String data =datas.get(i);
//        System.out.println("item："+i+";"+data+myViewHolder.text);
        myViewHolder.text.setText(data);
    }

    @Override
    public int getItemCount() {
        return this.datas.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView text;
        public MyViewHolder( View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.textAP);
        }
    }
}
