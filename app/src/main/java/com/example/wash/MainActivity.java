package com.example.wash;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.wash.Service.AutoUpdateService;
import com.example.wash.adapter.myFragmentAdapter;
import com.example.wash.entity.Wash;
import com.example.wash.ui.fragment_Dry;
import com.example.wash.ui.fragment_Setting;
import com.example.wash.ui.fragment_Wash;
import com.example.wash.utils.Util;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

//通信包

public class MainActivity extends AppCompatActivity implements View.OnClickListener, fragment_Wash.CallBackValue {
    //通信变量
    private Broadcast broadCast;
    String busyState = "03", idleState = "04", State = "";

    //UI变量
    private Fragment Wash, Dry, Setting, currentFragment;
    private TextView tab_1, tab_2, tab_3;
    private ViewPager myViewPager;
    private List<Fragment> myFragmentList;
    private myFragmentAdapter adapter;
    private LinearLayout mTab1, mTab2, mTab3;
    private ImageButton mImag1, mImag2, mImag3;
    private static String str = "";
    //
    public static String Loramessage = "";//接受的Lora信息
    public static int[] WashState;//接收Lora洗衣机状态信息 下标对应地址
    public static int wantWashaddress = -1; //想要启动的洗衣机地址
    public static boolean ifAllReady = false;//校园卡与洗衣机都验证成功
    public int sendcount = 1;
    //10.20
    public static Queue<Integer> queue = new LinkedList();
    Timer polling = new Timer();
    //10-29
    private static String flagall = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //创建通信对象
        broadCast = new Broadcast();
        EventBus.getDefault().register(this);
        initUI();
        initTab();

    }

    /*
      初始化UI
    */
    private void initUI() {
        mTab1 = findViewById(R.id.id_tab1);
        mTab2 = findViewById(R.id.id_tab2);
        mTab3 = findViewById(R.id.id_tab3);
        mImag1 = findViewById(R.id.id_tab_img1);
        mImag2 = findViewById(R.id.id_tab_img2);
        mImag3 = findViewById(R.id.id_tab_img3);

        mImag1.setImageResource(R.drawable.washing);

        myViewPager = findViewById(R.id.viewpager);

        mTab1.setOnClickListener(this);
        mTab2.setOnClickListener(this);
        mTab3.setOnClickListener(this);
        myViewPager.addOnPageChangeListener(new MyPagerChangeListener());
        //initUsbSerial();
    }

    private void initTab() {
        myFragmentList = new ArrayList<>();
        Wash = new fragment_Wash();
        Dry = new fragment_Dry();
        Setting = new fragment_Setting();
        myFragmentList.add(Wash);
        myFragmentList.add(Dry);
        myFragmentList.add(Setting);
        adapter = new myFragmentAdapter(getSupportFragmentManager(), myFragmentList);
        myViewPager.setAdapter(adapter);
        myViewPager.setCurrentItem(0);
    }

    private void resetImag() {
        //未选中图片变为灰色
        mImag1.setImageResource(R.drawable.washingse);
        mImag2.setImageResource(R.drawable.dryse);
        mImag3.setImageResource(R.drawable.settingse);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    //接受来自Mainactivity的消息，自动调用
    public void hanldeEvent(String str) {
        if (str.equals("OpenLora")){
            initUsbSerial();
        }
    }

    @Override
    public void onClick(View view) {
        resetImag();
        switch (view.getId()) {
            case R.id.id_tab1:
                mImag1.setImageResource(R.drawable.washing);
                myViewPager.setCurrentItem(0);
                break;
            case R.id.id_tab2:
                mImag2.setImageResource(R.drawable.dry);
                myViewPager.setCurrentItem(1);

                break;
            case R.id.id_tab3:
                mImag3.setImageResource(R.drawable.setting);
                myViewPager.setCurrentItem(2);
                break;
            default:
                break;
        }
    }

    private void select_Tab(int i) {
        switch (i) {
            case 0:
                mImag1.setImageResource(R.drawable.washing);
                break;
            case 1:
                mImag2.setImageResource(R.drawable.dry);
                break;
            case 2:
                mImag3.setImageResource(R.drawable.setting);
                break;
            default:
                break;
        }
        myViewPager.setCurrentItem(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String result = "";
        switch (requestCode) {
            case 1:
                try {
                    result = data.getStringExtra("data");
                } catch (Exception e) {

                }

                break;
            default:
                result = "未获取到相关数据。";
        }
        str = result;

        //Toast.makeText(MainActivity.this,result,Toast.LENGTH_LONG).show();
    }

    @Override
    public void SendMessageValue(String value) {
        Intent intent = new Intent(MainActivity.this, ChooseActivity.class);
        intent.putExtra("data", value);
        //startActivity(intent);
        startActivityForResult(intent, 1);
    }

    private class MyPagerChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {

            resetImag();
            select_Tab(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //EventBus.getDefault().post(str);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    //初始化USB
    public void initUsbSerial() {
        // 1.查找设备
        broadCast.usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        // System.out.println("aaaaaaaaaaaaaaaa");
        broadCast.drivers = UsbSerialProber.getDefaultProber().findAllDrivers(broadCast.usbManager);
        if (broadCast.drivers.size() <= 0) {
            Toast.makeText(this, "无串口设备", Toast.LENGTH_SHORT).show();
            return;
        }
        for (UsbSerialDriver driver : broadCast.drivers) {
            int id = driver.getDevice().getProductId();
            if (id == 8963) {
                broadCast.device_lora = driver.getDevice();
                broadCast.port_lora = driver.getPorts().get(0);
            } else if (id == 29987) {
                broadCast.device_card = driver.getDevice();
                broadCast.port_card = driver.getPorts().get(0);
            }
        }
        try {
            if (broadCast.usbManager.hasPermission(broadCast.device_lora) && broadCast.usbManager.hasPermission(broadCast.device_card)) {
                openLora();
                openCard();
            } else {
                Log.e("TAG", "没有权限");
                Broadcast.UsbPermissionActionReceiver mUsbPermissionActionReceiver = new Broadcast.UsbPermissionActionReceiver();
                Intent intent = new Intent(broadCast.ACTION_USB_PERMISSION);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
                IntentFilter intentFilter = new IntentFilter(broadCast.ACTION_USB_PERMISSION);
                registerReceiver(mUsbPermissionActionReceiver, intentFilter);
                broadCast.usbManager.requestPermission(broadCast.device_lora, pendingIntent);
                broadCast.usbManager.requestPermission(broadCast.device_card, pendingIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "差一个设备", Toast.LENGTH_SHORT).show();
        }

        //对指令进行处理
        Timer TotalExcu = new Timer();
        TotalExcu.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!queue.isEmpty()) {
                    int TargetData = queue.poll();
                    String TargetAddress = String.valueOf(queue.poll());
                    String area = TargetAddress.substring(0, 4), number = TargetAddress.substring(4, 7);
                    System.out.println("TarData " + TargetData + " TargetAdd " + TargetAddress);
                    for (int i = 0; i < 5; i++) {
                        if (area.equals("1701")) {
                            //发送数据到Lora，上限为5
                            Broadcast.sendMessage2Lora((byte) TargetData, (byte) 1, (byte) Integer.parseInt(number));
                            //System.out.println("我已经发送"+"1"+number);
                            //休眠200ms使得接收信息完整
                        } else if (area.equals("1702")) {
                            //发送数据到Lora，上限为5
                            Broadcast.sendMessage2Lora((byte) TargetData, (byte) 2, (byte) Integer.parseInt(number));
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //如收到的信息字节数匹配，则进行处理
                        if (Loramessage.length() == 14) {
                            System.out.println(Loramessage);
                            String head = Loramessage.substring(0, 2),
                                    data = Loramessage.substring(2, 4),
                                    address = Loramessage.substring(4, 6),
                                    chl = Loramessage.substring(6, 8),
                                    CRC = Loramessage.substring(8, 12),
                                    tail = Loramessage.substring(12, 14);
                            if (head.equals("28") && tail.equals("29")) {    //   CRC校验判断   &&CRC.equals(Broadcast.getCRC(Util.hexStr2Byte(data+address))
                                if (data.equals("02")) {
                                    //System.out.println(1111);
                                    //System.out.println("我已经发送给后台1701");
                                    Format f1 = new DecimalFormat("000");
                                    if (chl.equals("01"))
                                        ChooseActivity.post_statue("1701" + f1.format(Util.Hex2Int(address)), "Norm");
                                    else if (chl.equals("02"))
                                        ChooseActivity.post_statue("1702" + f1.format(Util.Hex2Int(address)), "Norm");

                                    if (TargetData == 3) {
                                        flagall = "1701" + f1.format(Util.Hex2Int(address));
                                    }

                                    //fragment_Wash.washerslist.get(Integer.valueOf(address).intValue()).setstatus("忙碌");
                                } else if (data.equals("01")) {
                                    Format f1 = new DecimalFormat("000");
                                    if (chl.equals("01"))
                                        ChooseActivity.post_statue("1701" + f1.format(Util.Hex2Int(address)), "Y");
                                    else if (chl.equals("02"))
                                        ChooseActivity.post_statue("1702" + f1.format(Util.Hex2Int(address)), "Y");
                                } else System.out.println("data有问题");
                                i = 5;
                            } else Loramessage = "";
                        } else {
                            //if(i==5) 判定为故障
                            System.out.println("长度不够 Len=" + Loramessage.length());
                            Loramessage = "";
                        }
                    }
                }
            }

        }, 0, 2000);
    }

    //接受Lora信息
    public void InitRead_Lora() {
        broadCast.usbIoManager_lora = new SerialInputOutputManager(broadCast.port_lora, new SerialInputOutputManager.Listener() {
            @Override
            public void onNewData(byte[] data) {
                String tempdata = Util.bytesToHex(data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(tempdata);
                        Loramessage += tempdata;
                    }
                });
            }

            @Override
            public void onRunError(Exception e) {
            }
        });
        Executors.newSingleThreadExecutor().submit(broadCast.usbIoManager_lora);
    }

    //接收校园卡返回的信息
    public void InitRead_Card() {
        broadCast.usbIoManager_card = new SerialInputOutputManager(broadCast.port_card, new SerialInputOutputManager.Listener() {
            @Override
            public void onNewData(byte[] data) {
                String s = Util.str2HexStr(new String(data));
                System.out.println(s);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (s != null) {
                            if (s.contains("030100"))  //030100
                            {
                                System.out.println("11111111111111111");
                                Toast.makeText(MainActivity.this, "开始支付", Toast.LENGTH_SHORT).show();
                                broadCast.send_card(broadCast.payMessage);
                            } else if (s.contains("06010000"))//支付成功
                            {

                                System.out.println("支付成功");
                                queue.add(3);
                                queue.add(wantWashaddress);
                                Timer t = new Timer();
                                t.schedule(new TimerTask() {
                                    @Override
                                    public void run() {

//                                            if(String.valueOf(wantWashaddress).equals(flagall)) {
//                                                System.out.println(wantWashaddress);
//                                                System.out.println("刷卡成功&&启动机器成功");
//                                                ifAllReady=true;
//                                                flagall="";
//                                                wantWashaddress=-1;
//                                                t.cancel();
//                                            }
                                        for (com.example.wash.entity.Wash wash : fragment_Wash.washerslist) {
                                            if (wash.getNum().equals(wantWashaddress) && wash.getstatus().equals("忙碌")) {
                                                System.out.println("刷卡成功&&启动机器成功");
                                                ifAllReady = true;
                                                wantWashaddress = -1;
                                                t.cancel();
                                            }
                                        }

                                    }

                                }, 0, 100);

                            }
                        }
                    }
                });
            }

            @Override
            public void onRunError(Exception e) {
            }
        });
        Executors.newSingleThreadExecutor().submit(broadCast.usbIoManager_card);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void openLora() {
        try {
            UsbDeviceConnection usbDeviceConnection = broadCast.usbManager.openDevice(broadCast.device_lora);
            broadCast.port_lora.open(usbDeviceConnection);
            broadCast.port_lora.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            //bt_connect2.setText("Close");
            Toast.makeText(MainActivity.this, "LORA", Toast.LENGTH_SHORT).show();
            System.out.println("Lora");
            InitRead_Lora();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UsbInterface anInterface = broadCast.device_lora.getInterface(0);

        if (anInterface == null) {
            //Toast.makeText(this, "初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }

        //轮询，开机使用一次
//        polling.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                //设置轮询启动的洗衣机数目 ：pollingnumber
//                for(com.example.wash.entity.Wash wash:fragment_Wash.washerslist){
//                    //Broadcast.sendMessage2Lora((byte)1, washnumber);
//                    //System.out.println(washnumber);
//                    queue.add(1);queue.add(Integer.valueOf(wash.getNum()));
//                    try {
//                        Thread.sleep(2000);//每次停顿时间
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    //System.out.println("4");
//                }
//            }
//        },100);

    }

    public void openCard() {
        try {
            UsbDeviceConnection usbDeviceConnection = broadCast.usbManager.openDevice(broadCast.device_card);
            broadCast.port_card.open(usbDeviceConnection);
            broadCast.port_card.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            Toast.makeText(MainActivity.this, "CARD", Toast.LENGTH_SHORT).show();
            System.out.println("CARD");
            InitRead_Card();
        } catch (IOException e) {
            e.printStackTrace();
        }
        UsbInterface anInterface = broadCast.device_card.getInterface(0);
        if (anInterface == null) {
            //Toast.makeText(this, "初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }
    }


}