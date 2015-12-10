package lang.com.applocker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import lang.com.applocker.service.WatchDogService;
import lang.com.applocker.utils.AppInfo;
import lang.com.applocker.utils.AppInfoProvide;
import lang.com.applocker.utils.AppLockerData;


/**
 * Created by duanlang on 12/3/15.
 * <p/>
 */

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private final String LOG_TAG = "duanlang";

    private ListView app_listview;
    private View app_listview_loading;

    private Intent intentService;

    private AppInfoProvide appInfoProvide;
    private AppLockerData appLockerData;

    private List<String> lockedPacknames;

    private List<AppInfo> appInfos;

    private PackageManager mPM;
    private UserManager mUM;
    private LauncherApps mLauncherApps;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            app_listview_loading.setVisibility(View.INVISIBLE);
            app_listview.setAdapter(new AppLockAdapter());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        init();
    }

    private void init() {
        intentService = new Intent(this, WatchDogService.class);
        startService(intentService);

        mPM = this.getPackageManager();
        mLauncherApps = (LauncherApps) this.getSystemService(Context.LAUNCHER_APPS_SERVICE);
    }

    private void initView() {

        app_listview = (ListView) findViewById(R.id.app_listview);
        app_listview_loading = findViewById(R.id.applock_loading);

        appInfoProvide = new AppInfoProvide(this);
        appLockerData = new AppLockerData(this);
        lockedPacknames = appLockerData.getAll();

        app_listview_loading.setVisibility(View.VISIBLE);

        app_listview.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppsList();
    }

    private void loadAppsList() {
        AsyncTask.execute(mCollectAllAppsRunnable);
    }

    //获取系统中所有app
    private final Runnable mCollectAllAppsRunnable = new Runnable() {
        @Override
        public void run() {
            appInfos = appInfoProvide.getInstalledApps();
            handler.sendEmptyMessage(0);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        AppInfo appInfo = (AppInfo) app_listview.getItemAtPosition(position);
        String packagename = appInfo.getPackname();
        ImageView status_icon = (ImageView) view.findViewById(R.id.applock_status);

        if (lockedPacknames.contains(packagename)) {
            Uri uri = Uri.parse("content://lang.com.applocker.applock/DELETE");
            getContentResolver().delete(uri, null, new String[]{packagename});

            status_icon.setImageResource(R.drawable.unlock);
            lockedPacknames.remove(packagename);
        } else {
            Uri uri = Uri.parse("content://lang.com.applocker.applock/ADD");
            ContentValues values = new ContentValues();
            values.put("packname", packagename);
            getContentResolver().insert(uri, values);

            status_icon.setImageResource(R.drawable.lock);
            lockedPacknames.add(packagename);
        }
    }

    private class AppLockAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return appInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return appInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder holder;

            if (null == convertView) {
                view = convertView.inflate(getApplicationContext(), R.layout.app_list, null);
                holder = new ViewHolder();
                holder.app_icon = (ImageView) view.findViewById(R.id.applock_icon);
                holder.app_status = (ImageView) view.findViewById(R.id.applock_status);
                holder.app_name = (TextView) view.findViewById(R.id.applock_name);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }

            AppInfo appInfo = appInfos.get(position);

            holder.app_icon.setImageDrawable(appInfo.getAppicon());
            holder.app_name.setText(appInfo.getAppname());
            if (lockedPacknames.contains(appInfo.getPackname())) {
                holder.app_status.setImageResource(R.drawable.lock);
            } else {
                holder.app_status.setImageResource(R.drawable.unlock);
            }

            return view;
        }
    }

    private static class ViewHolder {
        ImageView app_icon;
        ImageView app_status;
        TextView app_name;
    }

}
