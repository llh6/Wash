package com.example.wash;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChooseActivity extends AppCompatActivity {
    private Fragment Wash_fragment;
    private Button btn_reserve,btn_dantuo,btn_biaozhun;
    private TextView txt_number,txt_status,txt_money,txt_time;
    private Toolbar toolbar;
    private ConstraintLayout biaozhun,dantuo;
    private int flag1=-1,flag2=-1;//判断洗衣模式
    private String[] sArray;
    private Handler mhandler;
    private String wash_type;
    //定时发送
    static boolean isEnd = false; //控制TimerTask的结束标识
    static int count = 1; //循环计数器
    private LoadingDialog ld;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        biaozhun=findViewById(R.id.constraintLayout2);
        ld=new LoadingDialog(this);
        dantuo=findViewById(R.id.constraintLayout4);
        toolbar=findViewById(R.id.toolbar);
        wash_type=new String("");
        txt_number=(TextView) findViewById(R.id.choose_number);
        txt_status=(TextView)findViewById(R.id.choose_status);
        btn_reserve=findViewById(R.id.choose_btn_reserve);
        btn_dantuo=findViewById(R.id.btn_dantuo);
        btn_biaozhun=findViewById(R.id.btn_biaozhun);
        Intent intent=getIntent();
        String result=intent.getStringExtra("data");
        sArray=result.split(",");
        if (sArray[0].equals("忙碌")){
            txt_status.setTextColor(this.getResources().getColor(R.color.red));
            txt_number.setTextColor(this.getResources().getColor(R.color.red));
            btn_reserve.setBackgroundColor(this.getResources().getColor(R.color.red));
            btn_reserve.setText("正在忙碌中");
        }
        txt_number.setText("#"+sArray[1]);
        txt_status.setText("#"+sArray[0]);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ChooseActivity.this,MainActivity.class);
                intent.putExtra("data","");
                setResult(1,intent);
                finish();
            }
        });
        mhandler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what==1001){
                    ld.setSuccessText("支付成功")
                            .setLoadSpeed(LoadingDialog.Speed.SPEED_TWO)
                            .setRepeatCount(2)
                    ;
                    ld.loadSuccess();




                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ld.close();
                            ChooseActivity.this.finish();
                        }
                    },4000);

                }
                else {
                    ld.setFailedText("支付失败")
                            .setLoadSpeed(LoadingDialog.Speed.SPEED_ONE)
                            .setRepeatCount(3)
                    ;
                    ld.loadFailed();


                }

            }

        };
    }

    public void btn_reserve(View view) {
        String wid=sArray[1].substring(0,3);
        if (sArray[0].equals("忙碌")){
            Toast.makeText(this,"当前洗衣机正在工作中",Toast.LENGTH_LONG).show();
            return;
        }
        if (wash_type.equals("")){
            Toast.makeText(this,"请先选择洗衣模式",Toast.LENGTH_LONG).show();
            return;
        }
        else if(wash_type.equals("biaozhun")){
            txt_money=findViewById(R.id.biaozhun_money);
            txt_time=findViewById(R.id.biaozhun_time);
            //change_statueN(wid,"biaozhun");
        }
        else if (wash_type.equals("dantuo")){
            txt_money=findViewById(R.id.dantuo_money);
            txt_time=findViewById(R.id.dantuo_time);
            //change_statueN(wid,"daotuo");
        }
        /*if(flag1==-1&&flag2==-1){
            Toast.makeText(this,"请先选择洗衣模式",Toast.LENGTH_LONG).show();
            return;
        }
        else if(flag1==1){

            txt_money=findViewById(R.id.biaozhun_money);
            txt_time=findViewById(R.id.biaozhun_time);
            //change_statueN(wid,flag1,flag2);
        }
        else if(flag2==1){
            txt_money=findViewById(R.id.dantuo_money);
            txt_time=findViewById(R.id.dantuo_time);
            //change_statueN(wid,flag1,flag2);
        }*/

        ld.setLoadingText("请进行支付").show();
//        String regEx="[^0-9]";
//        Pattern p= Pattern.compile(regEx);
//        Matcher m = p.matcher(txt_number.getText().toString());
//        int position = Integer.valueOf(m.replaceAll("").trim());
        //System.out.println(txt_number.getText());
        int position = 0;
        if (txt_number.getText().toString().substring(0,4).equals("#17A"))
        {
            position=1701000+Integer.parseInt(txt_number.getText().toString().substring(4,7));
        }else if(txt_number.getText().toString().substring(0,3).equals("#17B"))
        {
            position=1702000+Integer.parseInt(txt_number.getText().toString().substring(4,7));
        }

        System.out.println("成功修改position"+position);
        MainActivity.wantWashaddress=position;//将要发送的地址修改
        Message message=Message.obtain(mhandler);
        Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(MainActivity.ifAllReady){
                    System.out.println("支付成功并且启动机器");
                    message.what=1001;
                    mhandler.sendMessage(message);
                    MainActivity.ifAllReady=false;

                    timer.cancel();
                }
                else {
                    //30s后没反应则关闭Loding
                    count++;
                    if (count == 15) {
                        timer.cancel();
                        count = 1;
                        message.what = 1000;
                        mhandler.sendMessage(message);
                        //ld.close();
                        //ChooseActivity.this.finish();
                    }
                }
            }
        },0,1000);

    }

    public void btn_biaozhun(View view) {
        wash_type="biaozhun";
        flag1=1;flag2=-1;
        dantuo.setBackgroundColor(Color.WHITE);
        biaozhun.setBackgroundColor(Color.rgb(220,220,220));
    }
    public void btn_dantuo(View view) {
        wash_type="dantuo";
        flag2=1;
        flag1=-1;
        dantuo.setBackgroundColor(Color.rgb(220,220,220));
        biaozhun.setBackgroundColor(Color.WHITE);
    }
    public  void change_statueN(String wid,String type){
        if (type.equals("biaozhun")){
            if (wid.equals("17A"))
            {
                wid = String.valueOf(1701)+sArray[1].substring(3,6);
                post_statue(wid,"Norm");
            }else if(wid.equals("17B")){
                wid = String.valueOf(1702)+sArray[1].substring(3,6);
                post_statue(wid,"Norm");
            }else{
                post_statue(sArray[1],"Norm"); //???
            }
        }

        else if (type.equals("dantuo")){
            if (wid.equals("17A"))
            {
                wid = String.valueOf(1701)+sArray[1].substring(3,6);
                post_statue(wid,"Spin");
            }else if(wid.equals("17B")){
                wid = String.valueOf(1702)+sArray[1].substring(3,6);
                post_statue(wid,"Spin");
            }else{
                post_statue(sArray[1],"Spin");
            }
        }

       /* if (flag1==1&&flag2==-1){
            if (wid.equals("17A"))
            {
                wid = String.valueOf(1701)+sArray[1].substring(3,6);
                post_statue(wid,"Norm");
            }else if(wid.equals("17B")){
                wid = String.valueOf(1702)+sArray[1].substring(3,6);
                post_statue(wid,"Norm");
            }else{
                post_statue(sArray[1],"Spin");
            }
        }else if(flag1==-1&&flag2==1){
            if (wid.equals("17A"))
            {
                wid = String.valueOf(1701)+sArray[1].substring(3,6);
                post_statue(wid,"Spin");
            }else if(wid.equals("17B")){
                wid = String.valueOf(1702)+sArray[1].substring(3,6);
                post_statue(wid,"Spin");
            }else{
                post_statue(sArray[1],"Spin");
            }
        }*/
    }
//    public void change_statueY(String wid){
//        if (wid.equals("17A"))
//        {
//            wid = String.valueOf(1701)+sArray[1].substring(3,6);
//            post_statue(wid,"Y");
//        }else if(wid.equals("17B")){
//            wid = String.valueOf(1702)+sArray[1].substring(3,6);
//            post_statue(wid,"Y");
//        }
//    }
    public static void  post_statue(String wid, String statue){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://"+Activity_Setting.url+"/api/washer?pageNum=1&pageSize=1000&search=&widStart=&widEnd=&widTarget="+wid+"&widStatus="+statue)
                .get()
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("fail", "onFailure: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.finish();
    }
}