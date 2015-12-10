package lang.com.applocker.service;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import lang.com.applocker.EnterPwdActivity;
import lang.com.applocker.utils.AppLockerData;
import lang.com.applocker.utils.IService;

/**
 * Created by duanlang on 12/3/15.
 * <p>
 * 该服务被唤起时，会启动一个线程进入死循环不断的获取activityTask的栈顶元素，获取packagename，
 * 经过忽略判断后决定是否跳转到解锁界面（如要退出死循环可调整flag值）
 * <p>
 * 另在服务启动时会注册一个广播接收器，在接收到锁屏广播ACTION_SCREEN_OFF时清空忽略LIST
 * 保证所有标志上锁的app在下次启动时需要重新解锁
 */
public class WatchDogService extends Service {

    private final String LOG_TAG = "WatchDogService";

    private List<String> lockPacknames;
    private List<String> tempStopProtectPacknames;

    private AppLockerData appLockerData;
    private MyObserver observer;

    private LockScreenReceiver lockScreenReceiver;

    private boolean flag = true;        //判断WatchDogService对ActivityTask的栈顶监视是否运行

    private final int SERVICE_SLEEP_TIME = 500;       //此处调整服务线程循环检测的间隔时间

    private MyBinder binder;

    private Intent pwdIntent;
    private ActivityManager activityManager;

    private Context mContext;

    private String tempPackageName = "";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        binder = new MyBinder();
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "WatchDogService onCreate");

        mContext = getApplicationContext();

        //注册内容观察者
        Uri uri = Uri.parse("content://lang.com.applocker.applock/");
        observer = new MyObserver(new Handler());
        getContentResolver().registerContentObserver(uri, true, observer);

        regReceiver();

        appLockerData = new AppLockerData(this);
        lockPacknames = appLockerData.getAll();
        tempStopProtectPacknames = new ArrayList<String>();

        pwdIntent = new Intent(this, EnterPwdActivity.class);
        pwdIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        new Thread() {
            @Override
            public void run() {
                while (flag) {
                    String topPackageName = getTaskTopAppName();

                    if (topPackageName != null && !tempPackageName.equals(topPackageName)) {
                        removeTempStopProtect(tempPackageName);
                        Log.i(LOG_TAG, "tempPackageName  = " + tempPackageName);
                        tempPackageName = topPackageName;
                        Log.i(LOG_TAG, "tempPackageName  = " + tempPackageName);
                    }

                    if (tempStopProtectPacknames.contains(topPackageName)) {
//                        Log.i(LOG_TAG, "tempStopProtectPacknames contains packagename");
                        continue;
                    }

                    pwdIntent.putExtra("packagename", topPackageName);
                    if (lockPacknames.contains(topPackageName)) {
                        startActivity(pwdIntent);
                    }

                    try {
                        sleep(SERVICE_SLEEP_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }

    private void regReceiver() {
        //注册锁屏
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        lockScreenReceiver = new LockScreenReceiver();
        registerReceiver(lockScreenReceiver, filter);
    }

    private String getTaskTopAppName() {

        String topPackageName = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            UsageStatsManager mUsageStatsManager = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);

            long currentTime = System.currentTimeMillis();
            // get usage stats for the last 10 seconds
            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000 * 10, currentTime);
            // search for app with most recent last used time
            if (stats != null) {
//                Log.i(LOG_TAG, "stats  = " + stats);
                long lastUsedAppTime = 0;
                for (UsageStats usageStats : stats) {
                    if (usageStats.getLastTimeUsed() > lastUsedAppTime) {
                        topPackageName = usageStats.getPackageName();
                        lastUsedAppTime = usageStats.getLastTimeUsed();
                    }
                }
            }
//            if (topPackageName != null)
//                Log.i(LOG_TAG, "topPackageName  = " + topPackageName);
//            else
//                Log.i(LOG_TAG, "topPackageName  = null");

        }

        return topPackageName;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(observer);
        observer = null;
    }

    private class MyBinder extends Binder implements IService {
        @Override
        public void callTempStopProtect(String packname) {
            tempStopProtect(packname);
        }
    }

    public void tempStopProtect(String packagename) {
        Log.i(LOG_TAG, "tempStopProtect packagename = " + packagename);
        tempStopProtectPacknames.add(packagename);
    }

    public void removeTempStopProtect(String packagename) {
        Log.i(LOG_TAG, "tempStopProtect packagename = " + packagename);
        if (tempStopProtectPacknames.contains(packagename))
            tempStopProtectPacknames.remove(packagename);
    }

    private class MyObserver extends ContentObserver {

        public MyObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            lockPacknames = appLockerData.getAll();
            super.onChange(selfChange);
        }
    }

    //锁屏时清空临时停止保护列表
    private class LockScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            tempStopProtectPacknames.clear();
        }
    }

}
