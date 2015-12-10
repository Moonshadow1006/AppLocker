package lang.com.applocker.utils;

import android.graphics.drawable.Drawable;

/**
 * Created by duanlang on 12/3/15.
 */
public class AppInfo {

    private static final String TAG = "AppInfo";

    //包名
    private String packagename;

    //应用程序版本号
    private String version;

    //应用程序名
    private String name;

    //应用程序图标
    private Drawable icon;

    //标识是否是用户层应用
    private boolean userpp;

    public String getPackname() {
        return packagename;
    }

    public void setPackname(String packagename) {
        this.packagename = packagename;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAppname() {
        return name;
    }

    public void setAppname(String appname) {
        this.name = appname;
    }

    public Drawable getAppicon() {
        return icon;
    }

    public void setAppicon(Drawable appicon) {
        this.icon = appicon;
    }

    public boolean isUserpp() {
        return userpp;
    }

    public void setUserpp(boolean userpp) {
        this.userpp = userpp;
    }
}

