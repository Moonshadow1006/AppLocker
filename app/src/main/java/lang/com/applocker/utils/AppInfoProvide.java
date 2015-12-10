package lang.com.applocker.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duanlang on 12/3/15.
 */
public class AppInfoProvide {
    private static final String TAG = "AppInfoProvide";

    private PackageManager pm;

    public AppInfoProvide(Context context) {
        pm = context.getPackageManager();
    }

    public List<AppInfo> getInstalledApps() {

//        List<PackageInfo> packageInfos = pm.getInstalledPackages(PackageManager.MATCH_DEFAULT_ONLY);
//        ArrayList<AppInfo> appList = new ArrayList<AppInfo>();
//        List<PackageInfo> packages = pm.getInstalledPackages(0);
//
//        for (PackageInfo info : packages) {
//            AppInfo appinfo = new AppInfo();
//
//            appinfo.setPackname(info.packageName);
//            appinfo.setVersion(info.versionName);
//            appinfo.setAppname(info.applicationInfo.loadLabel(pm).toString());
//            appinfo.setAppicon(info.applicationInfo.loadIcon(pm));
//
//            //过滤系统应用
//            if (filterApp(info.applicationInfo)) {
//                appList.add(appinfo);
//            }
//
//            appinfo = null;
//        }


        ArrayList<AppInfo> appList = new ArrayList<AppInfo>();
        List<ApplicationInfo> applicationInfo = pm.getInstalledApplications(0);

        for (ApplicationInfo info : applicationInfo) {
            AppInfo appinfo = new AppInfo();

            Drawable app_icon = info.loadIcon(pm);
            appinfo.setAppicon(app_icon);
            String app_name = info.loadLabel(pm).toString();
            appinfo.setAppname(app_name);
            String packageName = info.packageName;
            appinfo.setPackname(packageName);
            try {
                //获取应用的版本号
                PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
                String app_version = packageInfo.versionName;
                appinfo.setVersion(app_version);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            appList.add(appinfo);

//            Log.i("duanlang", "applicationInfos = " + applicationInfo);

        }

        return appList;
    }

    //判断某一个应用程序是不是系统的应用程序，如果不是返回true，否则返回false
    public boolean filterApp(ApplicationInfo info) {
        if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            return false;
        } else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            return true;
        }
        return false;
    }
}
