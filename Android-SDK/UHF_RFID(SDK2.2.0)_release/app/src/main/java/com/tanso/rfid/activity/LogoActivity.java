
/*
 *   *********************************************************************************
 *   版  权  声  明
 *   *********************************************************************************
 *   本公司既深圳市探索智能科技有限公司,致力于物联网领域应用技术的深度开发和研究.
 *   公司核心产品软硬件均为自主研发,深耕行业多年,多项技术在行业内领先,甚至填补空白.
 *
 *   以下原始代码为本公司开发,未经本公司许可,任何公司,个人,或组织,事业单位,不得使用,转让,租借,赠送,上传,
 *   或共享此代码,一经发现,违者将承担所有因此代码带来的各种损失和法律责任,违法必究,请大家遵守法律,尊重知识产权!
 *
 *   本代码在不断更新中,对于软件代码,功能定义,系统架构难免存在不足之处,实际效果最终解释权归本公司.
 *   有问题请与本公司反馈,用户需要定制软件或硬件接口,可以联系本公司!
 *
 *   公司 : 深圳市探索智能科技有限公司
 *   地址 : 深圳宝安西乡航城工业区,智汇创新中心B座西607室.
 *   作者 : 施探宇
 *   电话 : 18680399436 (同微信) QQ : 190376601.
 *   网店 : https://shop479675916.taobao.com
 *
 *
 */

package com.tanso.rfid.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.tanso.rfid.R;
import com.tanso.rfid.user.Global;
import com.tanso.rfid.user.RfidApp;
import com.tanso.rfidlib.comm.SDK;
import com.tanso.rfidlib.port.PortBase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static com.tanso.rfidlib.port.ble.ManagerBLE.REQUEST_ENABLE_BT;

/**
 * 启动界面
 */
public class LogoActivity extends AppCompatActivity {

    /**
     * 调试
     */
    private static final boolean DEBUG = Global.DEBUG;

    /**
     * 目标
     */
    private static final String TAG = "LogoActivity";

    /**
     * 跳转消息
     */
    private static final int MSG_GOTO_MAIN = 1000;

    /**
     * 权限申请
     */
    private final int RT_GET_ACCESS_COARSE_LOCATION = 10000;

    /**
     * 应用对象
     */
    private RfidApp rfidApp;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_GOTO_MAIN:
                    if (DEBUG) {
                        LoggerActivity.log_d(TAG, "MSG_GOTO_MAIN");
                    }
                    //  去掉消息
                    this.removeMessages(MSG_GOTO_MAIN);
                    //
                    Intent i = new Intent();
                    //  结算
                    i.setClass(LogoActivity.this, MainActivity.class);
                    //  启动
                    LogoActivity.this.startActivity(i);
                    //  完成
                    LogoActivity.this.finish();
                    break;
            }
        }
    };

    @SuppressLint({"ObsoleteSdkInt", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_logo);

        //  显示版本号
        TextView textVersion = this.findViewById(R.id.text_version);
        if (textVersion != null) {
            textVersion.setText(R.string.app_name);
            try {
                // 取得版本号
                PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
                String      str  = info.versionName;
                RfidApp     app  = (RfidApp) this.getApplication();

                textVersion.setText("APP : " + str + " \r\nSDK : " + SDK.getVersion() + "\r\nSET : " + app.getVersion());

            } catch (PackageManager.NameNotFoundException e) {
                textVersion.setText("版本 1.00");
            }
        }
        //  获取应用
        rfidApp = (RfidApp) this.getApplication();
        //  获取权限
        requestRights();
    }

    private static final String[] arrayPermission = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.BLUETOOTH,
            };

    /**
     * 申请权限
     */
    private void requestRights() {

        boolean getRightNow = false;

        if (DEBUG) {
            LoggerActivity.log_e(TAG, "申请权限!");
        }

        //  申请BLE权限权限
        for (String permiss : arrayPermission) {

            if (ContextCompat.checkSelfPermission(this, permiss) != PackageManager.PERMISSION_GRANTED) {
                //  申请
                ActivityCompat.requestPermissions(this, arrayPermission, RT_GET_ACCESS_COARSE_LOCATION);

                //  判断是否需要 向用户解释，为什么要申请该权限
                //ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);
                ActivityCompat.shouldShowRequestPermissionRationale(this, permiss);

                getRightNow = true;
                break;
            }
        }

        if (!getRightNow){
            checkRightDone();
        }
    }

    /**
     * 请求权限
     *
     * @param requestCode  : 请求
     * @param permissions  : 权限
     * @param grantResults : 反馈
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (DEBUG) {
            LoggerActivity.log_e(TAG, "requestCode:" + requestCode);
        }

        //  权限获取成功
        if (requestCode == RT_GET_ACCESS_COARSE_LOCATION) {
            checkRightDone();
        }
    }

    /**
     * 结果
     *
     * @param requestCode ： 请求
     * @param resultCode  ： 结果
     * @param data        ： 界面
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (DEBUG) {
            LoggerActivity.log_d(TAG, "onActivityResult(requestCode:" + requestCode + ",resultCode:" + resultCode + ")");
        }

        if (requestCode == REQUEST_ENABLE_BT) {
            checkRightDone();
        }
    }

    private void checkRightDone() {
        //  端口
        PortBase port = rfidApp.rfidSystem.getPortManager().getPort();
        //  初始化
        port.init(this);
        //  删除消息
        handler.removeMessages(MSG_GOTO_MAIN);
        //  准备跳转
        handler.sendEmptyMessageDelayed(MSG_GOTO_MAIN, 3000);
    }
}
