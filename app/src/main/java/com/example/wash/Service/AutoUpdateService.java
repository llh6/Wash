package com.example.wash.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import androidx.annotation.Nullable;


public class AutoUpdateService extends Service {
    public static final int REFRESH = 1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private MyThread myThread;
    Messenger mMessenger;
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.myThread = new MyThread();
        this.myThread.start();
        mMessenger = (Messenger) intent.getExtras().get("messenger");
        return super.onStartCommand(intent, flags, startId);
    }

    public class MyThread extends Thread {
        @Override
        public void run() {
            while (true) {
                System.out.println("发送请求");

                try {
                    // 每个30秒向服务器发送一次请求
                    Thread.sleep(30*1000);
                    Message message = new Message();
                    message.what = REFRESH;
                    try {
                        mMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
