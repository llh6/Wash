package com.example.wash;


import static com.example.wash.ui.fragment_Wash.getAllWashers;
import static com.example.wash.ui.fragment_Wash.washerslist;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wash.entity.DataAll;
import com.example.wash.entity.Utility;
import com.example.wash.entity.Wash;
import com.example.wash.self_style.WheelView;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Activity_Setting extends AppCompatActivity {

    private String selectText = "";
    private ArrayList<String> numberlist1 = new ArrayList<>();
    private ArrayList<String> numberlist2 = new ArrayList<>();
    private ArrayList<String> numberlist3 = new ArrayList<>();
    public TextView numLimitation1 ;
    public TextView numLimitation2 ;
    public TextView numLimitation3 ;
    public EditText ip_address;
    public static int start_num = 0;
    public static int end_num = 0;
    public static String address = "全部";
    public static String url = "10.1.20.146";
    private String res = "404";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Button confirm = findViewById(R.id.confirm);
        Button cancel = findViewById(R.id.cancel);
        SharedPreferences pref = getSharedPreferences("data",MODE_PRIVATE);
        numLimitation1 = findViewById(R.id.num_limitation1);
        numLimitation2 = findViewById(R.id.num_limitation2);
        numLimitation3 = findViewById(R.id.num_limitation3);
        ip_address = findViewById(R.id.ip_address);
        numLimitation1.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "99")});
        numLimitation2.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "100")});
        numLimitation1.setText(String.valueOf(pref.getInt("start_num",1)));
        numLimitation2.setText(String.valueOf(pref.getInt("end_num",100)));
        numLimitation3.setText(pref.getString("address","全部"));
        ip_address.setText(pref.getString("ip_address","10.1.20.146"));
        initData();
        numLimitation1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(numLimitation1, numberlist1, 0);
            }
        });
        numLimitation2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(numLimitation2, numberlist2, 0);
            }
        });
        numLimitation3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(numLimitation3, numberlist3, 0);
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity_Setting.this,MainActivity.class);
                start_num = Integer.parseInt(numLimitation1.getText().toString());
                end_num = Integer.parseInt(numLimitation2.getText().toString());
                address = numLimitation3.getText().toString();
                url = ip_address.getText().toString();
                if (address.equals("南区宿舍")){
                    Toast.makeText(Activity_Setting.this, "南区宿舍还未建造完毕哦~", Toast.LENGTH_SHORT).show();
                }else {
                    ProgressDialog pd1 = new ProgressDialog(Activity_Setting.this);
                    pd1.setTitle("提示");
                    pd1.setMessage("ip验证中...");
                    pd1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pd1.show();

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            check_ip(url);
                        }
                    });
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                    }
                    pd1.cancel();
                    if (res.equals("成功"))
                    {
                        Toast.makeText(Activity_Setting.this, "ip连接成功", Toast.LENGTH_SHORT).show();
                        ProgressDialog pd2 = new ProgressDialog(Activity_Setting.this);
                        pd2.setTitle("提示");
                        pd2.setMessage("玩命加载中...");
                        pd2.setCancelable(true);
                        pd2.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pd2.show();
                        SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                        editor.putString("address",address);
                        editor.putInt("start_num",start_num);
                        editor.putInt("end_num",end_num);
                        editor.putString("ip_address",url);
                        editor.apply();
                        getRangeWashers(url,start_num,end_num,address);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(intent);
                                overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                                pd2.cancel();
                                finish();
                            }
                        },1000);
                    }else {
                        Toast.makeText(Activity_Setting.this, "请检查ip是否正确", Toast.LENGTH_SHORT).show();
                    }
                }



            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity_Setting.this,MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                finish();
            }
        });
    }

    //数字选择器
    private void initData() {
        numberlist1.clear();
        numberlist2.clear();
        for (int i = 1; i <= 99; i++) {
            numberlist1.add(String.format("%d", i));
        }
        for (int i = 1; i <= 100; i++) {
            numberlist2.add(String.format("%d", i));
        }
        for (int i = 1; i <= 4 ; i++) {
            if (i==1){
                numberlist3.add("全部");
            }
            if (i==2){
                numberlist3.add("17A");
            }
            if(i==3){
                numberlist3.add("17B");
            }
            if(i==4){
                numberlist3.add("南区宿舍");
            }
        }
    }


    private void showDialog(TextView textView, ArrayList<String> list, int selected){
        showChoiceDialog(list, textView, selected,
                new WheelView.OnWheelViewListener() {
                    @Override
                    public void onSelected(int selectedIndex, String item) {
                        selectText = item;
                    }
                });
    }

    private void showChoiceDialog(ArrayList<String> dataList,final TextView textView,int selected,
                                  WheelView.OnWheelViewListener listener){
        selectText = "";
        View outerView = LayoutInflater.from(this).inflate(R.layout.dialog_wheelview,null);
        final WheelView wheelView = outerView.findViewById(R.id.wheel_view);
        wheelView.setOffset(2);// 对话框中当前项上面和下面的项数
        wheelView.setItems(dataList);// 设置数据源
        wheelView.setSeletion(selected);// 默认选中第三项
        wheelView.setOnWheelViewListener(listener);

        // 显示对话框，点击确认后将所选项的值显示到EditText上
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(outerView)
                .setPositiveButton("确认",
                        (dialogInterface, i) -> {
                            textView.setText(selectText);
                            textView.setTextColor(this.getResources().getColor(R.color.black));
                        })
                .setNegativeButton("取消",null).create();
        alertDialog.show();
        int green = this.getResources().getColor(R.color.black);
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(green);
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(green);
    }

    public void OpenLora(View view) {
        EventBus.getDefault().post("OpenLora");
    }

    public class InputFilterMinMax implements InputFilter {
        private int min, max;

        public InputFilterMinMax(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(min, max, input))

                    return null;
            } catch (NumberFormatException nfe) {
            }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }
    public static void getRangeWashers(String url,int start_num, int end_num, String address) {
        String start = null;
        String end = null;
        Format f1 = new DecimalFormat("000");
        if(address.equals("全部")){
            washerslist.clear();
            getAllWashers(url);
            return;
        }
        else if (address.equals("17A")){
            washerslist.clear();
            start = String.valueOf(1701)+f1.format(start_num);
            end = String.valueOf(1701)+f1.format(end_num);
        }else if (address.equals("17B")){
            washerslist.clear();
            start = String.valueOf(1702)+f1.format(start_num);
            end = String.valueOf(1702)+f1.format(end_num);
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://"+url+"/api/washer?pageNum=1&pageSize=1000&search=&widStart="+start+"&widEnd="+end+"&widTarget=&widStatus=")
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
                String responseText = response.body().string();
                try {
                    DataAll dataAll = Utility.handleWashersResponse(responseText);
                    for (Wash wash : dataAll.washerList) {
                        washerslist.add(wash);
                        if (wash.check_statue()){
                            MainActivity.queue.add(2);
                            MainActivity.queue.add(Integer.parseInt(wash.getWid()));
                            wash.setstatus("Y");
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String check_ip(String ip_address){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://"+ip_address+"/api/washer?pageNum=1&pageSize=1000&search")
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
                String responseText = response.body().string();
                res = Utility.handleIP(responseText);
            }
        });
        return null;
    }

}

