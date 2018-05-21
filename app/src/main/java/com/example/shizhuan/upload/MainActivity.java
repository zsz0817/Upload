package com.example.shizhuan.upload;

import android.animation.Animator;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.kcode.lib.UpdateWrapper;
import com.kcode.lib.bean.VersionModel;
import com.kcode.lib.net.CheckUpdateTask;
import com.thinkcool.circletextimageview.CircleTextImageView;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,ServiceConnection{

    private PickerScrollView pickerscrlllview; // 滚动选择器
    private List<Pickers> list; // 滚动选择器数据

    private Button bt_yes; // 确定按钮
    private RelativeLayout picker_rel; // 选择器布局

    //声明mlocationClient对象
    public AMapLocationClient mlocationClient;
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;
    private CircleTextImageView start,stop;
    private TextView tv_result;
    Map<String,Map<String,Object>> param = new HashMap<>();
    Map<String,Object> map1,map2;

    private final int MSG_HELLO = 0;

    private Handler mHandler;
    private int linenumber = 1;
    private Intent intent;
    private LocationService locationService;
    private String result;
    private boolean isStop = true;

    private PowerManager.WakeLock wakeLock;

    MyApplication application = MyApplication.getInstance();
    boolean isBound = application.getisBound();



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        checkUpdate(0,CustomsUpdateActivity.class);//检查更新

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        new CustomThread().start();

//        mlocationClient = new AMapLocationClient(MainActivity.this);
//        //初始化定位参数
//        mLocationOption = new AMapLocationClientOption();
//        //设置定位监听
//        mlocationClient.setLocationListener(this);
        //检测系统是否打开开启了地理定位权限
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String []{android.Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
//        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//        //设置定位间隔,单位毫秒,默认为2000ms
//        mLocationOption.setInterval(1000);
//
//        mLocationOption.setNeedAddress(true);
//        mLocationOption.setLocationCacheEnable(false);
//        mLocationOption.setGpsFirst(true);
//        mLocationOption.setSensorEnable(true);
//        mLocationOption.setWifiScan(true);
//
//        //设置定位参数
//        mlocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除


        intent=new Intent(MainActivity.this,LocationService.class);

        start = (CircleTextImageView) findViewById(R.id.start);
        start.setOnClickListener(this);
        stop = (CircleTextImageView) findViewById(R.id.stop);
        stop.setOnClickListener(this);
        tv_result = (TextView)findViewById(R.id.tv_result);

        picker_rel = (RelativeLayout) findViewById(R.id.picker_rel);
        picker_rel.setVisibility(View.VISIBLE);
        pickerscrlllview = (PickerScrollView) findViewById(R.id.pickerscrlllview);
        bt_yes = (Button) findViewById(R.id.picker_yes);
        bt_yes.setOnClickListener(this);
//        if (isStop){
//            picker_rel.setVisibility(View.VISIBLE);
//            start.setVisibility(View.VISIBLE);
//            stop.setVisibility(View.GONE);
//        }else {
//            picker_rel.setVisibility(View.GONE);
//            start.setVisibility(View.GONE);
//            stop.setVisibility(View.VISIBLE);
//        }

        if (isServiceRunning("com.example.shizhuan.upload.LocationService",this)){
            if (application.getisBound()) {
                unbindService(this);// 解绑服务
                application.setisBound(false);
            }
            stopService(intent);
        }

        initData();
        pickerscrlllview.setOnSelectListener(new PickerScrollView.onSelectListener() {
            @Override
            public void onSelect(Pickers pickers) {
                linenumber = pickers.getShowId()+1;
            }
        });

    }

    /**
     * 初始化数据
     */
    private void initData() {
        list = new ArrayList<Pickers>();
        for (int i = 0; i < Constants.Lines.length; i++) {
            list.add(new Pickers(Constants.Lines[i], i));
        }
        // 设置数据，默认选择第一条
        pickerscrlllview.setData(list);
        pickerscrlllview.setSelected(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start:
                isStop = false;
                bindService(intent, this, Context.BIND_AUTO_CREATE);
                application.setisBound(true);
                stop.setVisibility(View.VISIBLE);
                Animator animator = ViewAnimationUtils.createCircularReveal(stop, start.getWidth() / 2, start.getHeight() / 2, 0,
                        start.getWidth());
                animator.setDuration(1000);
                animator.setInterpolator(new AccelerateInterpolator());
                animator.start();
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        start.setVisibility(View.GONE);

                        intent.putExtra("linenumber",linenumber);
                        startService(intent);
//                        //启动定位
//                        mlocationClient.startLocation();
                        listenResult();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                break;
            case R.id.stop:
                isStop = true;
//                application.setStop(true);
                start.setVisibility(View.VISIBLE);
                if (application.getisBound()) {
                    unbindService(this);// 解绑服务
                    application.setisBound(false);
                }

                animator = ViewAnimationUtils.createCircularReveal(start, stop.getWidth()/2, stop.getHeight()/2, 0,
                        stop.getWidth());
                animator.setDuration(1000);
                animator.setInterpolator(new AccelerateInterpolator());
                animator.start();
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        stop.setVisibility(View.INVISIBLE);


//                        intent.putExtra("linenumber",linenumber);
//                        intent.putExtra("statue",true);
                        stopService(intent);
                        //停止定位
//                        mlocationClient.stopLocation();
                        tv_result.setText("定位停止");
                        picker_rel.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                break;
            case R.id.picker_yes:
                picker_rel.setVisibility(View.GONE);
                break;
        }
    }

//    @Override
//    public void onLocationChanged(AMapLocation aMapLocation) {
//        if (aMapLocation != null) {
//            StringBuffer sb = new StringBuffer();
//            if (aMapLocation.getErrorCode() == 0) {
//                //定位成功回调信息，设置相关消息
//                sb.append("定位成功" + "\n");
//                sb.append("当前路线为:  " + Constants.Lines[linenumber-1] + "\n");
//                sb.append("定位类型: " + aMapLocation.getLocationType() + "\n");
//                sb.append("经    度    : " + aMapLocation.getLongitude() + "\n");
//                sb.append("纬    度    : " + aMapLocation.getLatitude() + "\n");
//                sb.append("精    度    : " + aMapLocation.getAccuracy() + "米" + "\n");
//                sb.append("提供者    : " + aMapLocation.getProvider() + "\n");
//
//                sb.append("速    度    : " + aMapLocation.getSpeed() + "米/秒" + "\n");
//                sb.append("角    度    : " + aMapLocation.getBearing() + "\n");
//                // 获取当前提供定位服务的卫星个数
//                sb.append("星    数    : " + aMapLocation.getSatellites() + "\n");
//                sb.append("国    家    : " + aMapLocation.getCountry() + "\n");
//                sb.append("省            : " + aMapLocation.getProvince() + "\n");
//                sb.append("市            : " + aMapLocation.getCity() + "\n");
//                sb.append("城市编码 : " + aMapLocation.getCityCode() + "\n");
//                sb.append("区            : " + aMapLocation.getDistrict() + "\n");
//                sb.append("区域 码   : " + aMapLocation.getAdCode() + "\n");
//                sb.append("地    址    : " + aMapLocation.getAddress() + "\n");
//                sb.append("兴趣点    : " + aMapLocation.getPoiName() + "\n");
//                //定位完成的时间
//                sb.append("定位时间: " + Utils.formatUTC(aMapLocation.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");
//                aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
//                aMapLocation.getLatitude();//获取纬度
//                aMapLocation.getLongitude();//获取经度
//                aMapLocation.getAccuracy();//获取精度信息
//                try {
//                    SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
//                    SimpleDateFormat df2 = new SimpleDateFormat("HH:mm:ss");
//                    Date date = new Date(aMapLocation.getTime());
//                    map1 = new HashMap<>();
//                    map2 = new HashMap<>();
//                    map1.put("TRACDE","BC00002");
//                    map1.put("TRADAT",df1.format(date));
//                    map1.put("TRATIM",df2.format(date));
//                    map1.put("USRNAM","zhou");
//                    map2.put("line",linenumber);
//                    map2.put("toc","1");
//                    map2.put("longitude",aMapLocation.getLongitude());
//                    map2.put("latitude",aMapLocation.getLatitude());
//                    param.put("head",map1);
//                    param.put("body",map2);
//                    net.sf.json.JSONArray jsonArray = net.sf.json.JSONArray.fromObject(param);
//                    String tmp = jsonArray.toString().substring(1,jsonArray.toString().length()-1);
//                    final MyApplication application = (MyApplication) getApplication();
//                    application.seturl(Constants.url_uploadLoaction + tmp);
//                    mHandler.obtainMessage(MSG_HELLO, Constants.url_uploadLoaction+tmp).sendToTarget();
//                }catch (Exception e){
//                    e.printStackTrace();
//                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
//                //定位失败
//                sb.append("定位失败" + "\n");
//                sb.append("错误码:" + aMapLocation.getErrorCode() + "\n");
//                sb.append("错误信息:" + aMapLocation.getErrorInfo() + "\n");
//                sb.append("错误描述:" + aMapLocation.getLocationDetail() + "\n");
//            }
//            //解析定位结果，
//            String result = sb.toString();
//            tv_result.setText(result);
//        }
//    }

//    class CustomThread extends Thread {
//        @Override
//        public void run() {
//            //建立消息循环的步骤
//            Looper.prepare();//1、初始化Looper
//            mHandler = new Handler(){//2、绑定handler到CustomThread实例的Looper对象
//                public void handleMessage (Message msg) {//3、定义处理消息的方法
//                    switch(msg.what) {
//                        case MSG_HELLO:
//                            try {
//                                String retmsg = OkHttpClientManager.getAsString(msg.obj.toString());
//                                Toast.makeText(MainActivity.this,"上传结果"+retmsg,Toast.LENGTH_SHORT).show();
//                            }catch (Exception e){
//                                Toast.makeText(MainActivity.this,"上传失败"+e.getMessage(),Toast.LENGTH_SHORT).show();
//                            }
//
//                            break;
//                    }
//                }
//            };
//            Looper.loop();//4、启动消息循环
//        }
//    }

    /**
     * 检查更新
     * @param time
     * @param cls
     */
    private void checkUpdate(final long time, final Class<? extends FragmentActivity> cls) {

        UpdateWrapper.Builder builder = new UpdateWrapper.Builder(getApplicationContext())
                .setTime(time)
                .setNotificationIcon(R.mipmap.ic_launcher_round)
                .setUrl("http://111.230.148.118:8080/update/upload.json")
                .setIsShowToast(false)
                .setCallback(new CheckUpdateTask.Callback() {
                    @Override
                    public void callBack(VersionModel versionModel) {

                    }
                });

        if (cls != null) {
            builder.setCustomsActivity(cls);
        }

        builder.build().start();

    }

    /**
     * 判断服务是否正在运行
     *
     * @param serviceName 服务类的全路径名称 例如： com.jaychan.demo.service.PushService
     * @param context 上下文对象
     * @return
     */
    public static boolean isServiceRunning(String serviceName, Context context) {
        //活动管理器
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = am.getRunningServices(100); //获取运行的服务,参数表示最多返回的数量

        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServices) {
            String className = runningServiceInfo.service.getClassName();
            if (className.equals(serviceName)) {
                return true; //判断服务是否运行
            }
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
        stopService(intent);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        locationService = ((LocationService.Binder)iBinder).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    /**
     * 监听結果，每秒钟获取定位，更新UI
     */
    public void listenResult(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                while(!isStop){
                    result = locationService.getDate();
                    try {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_result.setText(result);
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }
}
