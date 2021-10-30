package com.example.wash.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wash.ChooseActivity;
import com.example.wash.R;
import com.example.wash.entity.Wash;

import java.util.ArrayList;
import java.util.List;

public class myRecyclerAdapter extends RecyclerView.Adapter<myRecyclerAdapter.MyViewHolder> {

    private List<Wash> wdata=new ArrayList<Wash>();
    private Context context;
    private OnItemClickListener mOnItemClickListener;

    public myRecyclerAdapter(List<Wash> wdata, Context context) {
        this.wdata = wdata;
        this.context = context;
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener){
        this.mOnItemClickListener=mOnItemClickListener;
    }
    
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.wash_item,parent,false);
        MyViewHolder myViewHolder=new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder,int position) {
        //System.out.println(position+" ");
        Wash wash=wdata.get(position);
        if (wash.getstatus().equals("空闲")){
            holder.myImag.setImageDrawable(context.getResources().getDrawable(R.drawable.wash));
            holder.mySandGlass.setVisibility(View.GONE);
            holder.myTime.setVisibility(View.GONE);
            holder.myNumber.setTextColor(context.getResources().getColor(R.color.black));
            holder.myStatus.setTextColor(context.getResources().getColor(R.color.black));
            new ChooseActivity().post_statue(wash.getWid(),"Y");
        }
        else  if (wash.getstatus().equals("忙碌")){
           holder.myImag.setImageDrawable(context.getResources().getDrawable(R.drawable.wash_ing));
            holder.myStatus.setTextColor(context.getResources().getColor(R.color.red));
            holder.myNumber.setTextColor(context.getResources().getColor(R.color.red));
            holder.mySandGlass.setImageDrawable(context.getResources().getDrawable(R.drawable.sandglass));
            holder.mySandGlass.setVisibility(View.VISIBLE);
            holder.myTime.setText("大约剩余"+wash.getRemainTime()+"分钟");
            holder.myTime.setVisibility(View.VISIBLE);
            holder.myTime.setTextColor(context.getResources().getColor(R.color.red));
        }

        holder.myNumber.setText(wash.getNum());
        holder.myStatus.setText(wash.getstatus());
        if (mOnItemClickListener!=null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(holder.itemView,position);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return wdata.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView myImag,mySandGlass;
        private TextView myLocation,myStatus,myNumber,myTime;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myImag=itemView.findViewById(R.id.item_imag);
            myLocation=itemView.findViewById(R.id.item_location);
            myStatus=itemView.findViewById(R.id.item_status);
            myNumber=itemView.findViewById(R.id.item_number);
            mySandGlass=itemView.findViewById(R.id.item_sandGlass);
            myTime=itemView.findViewById(R.id.item_time);

        }
    }



}
