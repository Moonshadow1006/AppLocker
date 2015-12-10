package lang.com.applocker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import lang.com.applocker.service.WatchDogService;
import lang.com.applocker.utils.IService;

/**
 * Created by duanlang on 12/3/15.
 * <p/>
 * 该界面是提示解除app锁定界面，需要输入开始设定的加密方式来解除锁定
 * <p/>
 */
public class EnterPwdActivity extends Activity implements View.OnClickListener {

    public static final String LOG_TAG = "duanlang";

    private ImageView application_icon;
    private TextView application_name;
    private EditText enter_password;
    private Button btn_enter;

    private Intent serviceIntent;

    private IService iService;

    private String packagename;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enterpwd_layout);

        Intent intent = getIntent();
        packagename = intent.getStringExtra("packagename");
        Log.i(LOG_TAG, "packagename = " + packagename);

        initUI();

        serviceIntent = new Intent(this, WatchDogService.class);
        bindService(serviceIntent, connection, BIND_AUTO_CREATE);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(packagename, 0);
            application_icon.setImageDrawable(info.applicationInfo.loadIcon(getPackageManager()));
            application_name.setText(info.applicationInfo.loadLabel(getPackageManager()));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initUI() {
        application_icon = (ImageView) findViewById(R.id.application_icon);
        application_name = (TextView) findViewById(R.id.application_name);
        enter_password = (EditText) findViewById(R.id.enter_password);
        btn_enter = (Button) findViewById(R.id.btn_enter);
        btn_enter.setOnClickListener(this);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iService = (IService) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //添加解除不同加密锁方式的解锁过程
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_enter) {

            //字符加密
            String pswd = enter_password.getText().toString().trim();
            if (pswd.isEmpty()) {
                Toast.makeText(EnterPwdActivity.this, "not empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (pswd.equals("1")) {
                iService.callTempStopProtect(packagename);
                finish();
            } else {
                Toast.makeText(EnterPwdActivity.this, "fail", Toast.LENGTH_SHORT).show();
                return;
            }

        }
    }
}