package com.example.shizhuan.upload;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by ShiZhuan on 2018/5/15.
 */

public class LocationService extends Service {

    //声明mlocationClient对象
    public AMapLocationClient mlocationClient;
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;

    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private boolean isStop = false;
    public   int intTimer=300;
    public  String strIsLogin="1";

    Map<String,Map<String,Object>> param = new HashMap<>();
    Map<String,Object> map1,map2;

    private final int MSG_HELLO = 0;

    private Handler mHandler;
    private int linenumber = 1;

    private String data;

    private PowerManager.WakeLock wakeLock;


    @Override
    public void onCreate() {
        super.onCreate();
        init();
        new CustomThread().start();
        acquireWakeLock();//获取电源锁
    }

    private void init(){
        mlocationClient = new AMapLocationClient(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mlocationClient.setLocationListener(mLocationListener);
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(1000);

        mLocationOption.setNeedAddress(true);
        mLocationOption.setLocationCacheEnable(false);
        mLocationOption.setGpsFirst(true);
        mLocationOption.setSensorEnable(true);
        mLocationOption.setWifiScan(true);

        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        mlocationClient.startLocation();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
            .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher)) // 设置下拉列表中的图标(大图标)
            .setContentTitle("正在定位") // 设置下拉列表里的标题
            .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
            .setContentText("点击停止") // 设置上下文内容
            .setWhen(System.currentTimeMillis())
            .setTicker("点击停止")
            .setOngoing(false); // 设置该通知发生的时间
        Notification notification = builder.build(); // 获取构建好的Notification
//        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音

        startForeground(110, notification);// 开始前台服务

        if(intent!=null){
            linenumber = intent.getIntExtra("linenumber",1);
        }

        // 触发定时器
//        if (!isStop) {
//            startTimer();
//        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (mlocationClient!=null) {
            mlocationClient.stopLocation();
        }
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        releaseWakeLock();//释放电源锁
        super.onDestroy();
//        // 停止定时器
//        if (isStop) {
//            stopTimer();
//        }
    }

    private void startTimer() {
        isStop = true;//定时器启动后，修改标识，关闭定时器的开关
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    do {
                        if (strIsLogin=="1"){
                            mlocationClient.startLocation();
                        }
                    } while (isStop);
                }
            };
        }
        if (mTimer != null && mTimerTask != null) {
            mTimer.schedule(mTimerTask, 0);//执行定时器中的任务
        }
    }
    /**
     * 停止定时器，初始化定时器开关
     */
    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        isStop = false;//重新打开定时器开关
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void setData(String data){
        LocationService.this.data = data;
    }

    public String getDate(){
        return data;
    }

    public class Binder extends android.os.Binder{
        /**
         * 获取当前Service的实例
         * @return
         */
        public LocationService getService(){
            return LocationService.this;
        }
    }

    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                StringBuffer sb = new StringBuffer();
                if (aMapLocation.getErrorCode() == 0) {
                    //定位成功回调信息，设置相关消息
                    sb.append("定位成功" + "\n");
                    sb.append("当前路线为:  " + Constants.Lines[linenumber - 1] + "\n");
                    sb.append("定位类型: " + aMapLocation.getLocationType() + "\n");
                    sb.append("经    度    : " + aMapLocation.getLongitude() + "\n");
                    sb.append("纬    度    : " + aMapLocation.getLatitude() + "\n");
                    sb.append("精    度    : " + aMapLocation.getAccuracy() + "米" + "\n");
                    sb.append("提供者    : " + aMapLocation.getProvider() + "\n");

                    sb.append("速    度    : " + aMapLocation.getSpeed() + "米/秒" + "\n");
                    sb.append("角    度    : " + aMapLocation.getBearing() + "\n");
                    // 获取当前提供定位服务的卫星个数
                    sb.append("星    数    : " + aMapLocation.getSatellites() + "\n");
                    sb.append("国    家    : " + aMapLocation.getCountry() + "\n");
                    sb.append("省            : " + aMapLocation.getProvince() + "\n");
                    sb.append("市            : " + aMapLocation.getCity() + "\n");
                    sb.append("城市编码 : " + aMapLocation.getCityCode() + "\n");
                    sb.append("区            : " + aMapLocation.getDistrict() + "\n");
                    sb.append("区域 码   : " + aMapLocation.getAdCode() + "\n");
                    sb.append("地    址    : " + aMapLocation.getAddress() + "\n");
                    sb.append("兴趣点    : " + aMapLocation.getPoiName() + "\n");
                    //定位完成的时间
                    sb.append("定位时间: " + Utils.formatUTC(aMapLocation.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");
                    aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                    aMapLocation.getLatitude();//获取纬度
                    aMapLocation.getLongitude();//获取经度
                    aMapLocation.getAccuracy();//获取精度信息
                    try {
                        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
                        SimpleDateFormat df2 = new SimpleDateFormat("HH:mm:ss");
                        Date date = new Date(aMapLocation.getTime());
                        map1 = new HashMap<>();
                        map2 = new HashMap<>();
                        map1.put("TRACDE", "BC00002");
                        map1.put("TRADAT", df1.format(date));
                        map1.put("TRATIM", df2.format(date));
                        map1.put("USRNAM", "zhou");
                        map2.put("line", linenumber);
                        map2.put("toc", "1");
                        map2.put("longitude", aMapLocation.getLongitude());
                        map2.put("latitude", aMapLocation.getLatitude());
                        param.put("head", map1);
                        param.put("body", map2);
                        net.sf.json.JSONArray jsonArray = net.sf.json.JSONArray.fromObject(param);
                        String tmp = jsonArray.toString().substring(1, jsonArray.toString().length() - 1);
                        final MyApplication application = (MyApplication) getApplication();
                        application.seturl(Constants.url_uploadLoaction + tmp);
                        mHandler.obtainMessage(MSG_HELLO, Constants.url_uploadLoaction + tmp).sendToTarget();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    //定位失败
                    sb.append("定位失败" + "\n");
                    sb.append("错误码:" + aMapLocation.getErrorCode() + "\n");
                    sb.append("错误信息:" + aMapLocation.getErrorInfo() + "\n");
                    sb.append("错误描述:" + aMapLocation.getLocationDetail() + "\n");
                }
                data = sb.toString();
            }
        }
    };

    //获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    private void acquireWakeLock()
    {
        if (null == wakeLock)
        {
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK| PowerManager.ON_AFTER_RELEASE, "PostLocationService");
            if (null != wakeLock)
            {
                wakeLock.acquire();
            }
        }
    }

    //释放设备电源锁
    private void releaseWakeLock()
    {
        if (null != wakeLock)
        {
            wakeLock.release();
            wakeLock = null;
        }
    }


    class CustomThread extends Thread {
        @Override
        public void run() {
            //建立消息循环的步骤
            Looper.prepare();//1、初始化Looper
            mHandler = new Handler(){//2、绑定handler到CustomThread实例的Looper对象
                public void handleMessage (Message msg) {//3、定义处理消息的方法
                    switch(msg.what) {
                        case MSG_HELLO:
                            try {
                                String retmsg = OkHttpClientManager.getAsString(msg.obj.toString());
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            break;
                    }
                }
            };
            Looper.loop();//4、启动消息循环
        }
    }
}

