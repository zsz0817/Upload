package com.example.shizhuan.upload;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;


/**
 * Created by ShiZhuan on 2018/4/25.
 */

public class MyApplication extends Application {
    private String url;

    private boolean isBound = false;

    public static final String TAG = MyApplication.class.getSimpleName();

    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
//        startAlarm();
        mInstance = this;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public void seturl(String url){
        this.url = url;
    }

    public String geturl(){
        return url;
    }

    public void setisBound(boolean var){
        this.isBound = var;
    }

    public boolean getisBound(){
        return isBound;
    }

//    public void startAlarm(){
//        /**
//         首先获得系统服务
//         */
//        AlarmManager am = (AlarmManager)
//                getSystemService(Context.ALARM_SERVICE);
//
//        /** 设置闹钟的意图，我这里是去调用一个服务，该服务功能就是获取位置并且上传*/
//        Intent intent = new Intent(this, LocationService.class);
//        PendingIntent pendSender = PendingIntent.getService(this, 0, intent, 0);
//        am.cancel(pendSender);
//
//        /**AlarmManager.RTC_WAKEUP 这个参数表示系统会唤醒进程；我设置的间隔时间是10分钟 */
//        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5000, pendSender);
//    }
}
