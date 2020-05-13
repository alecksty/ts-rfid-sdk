
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

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.tanso.rfid.R;
import com.tanso.rfid.user.RfidApp;
import com.tanso.rfidlib.comm.SDK;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 关于对话框
 *
 * @author Aleck
 * @version 1.00
 */
public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    @SuppressLint({"ObsoleteSdkInt", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        //  关于界面
        setContentView(R.layout.activity_about);

        //  返回处理
        ImageView imageReturn = this.findViewById(R.id.image_return);
        imageReturn.setOnClickListener(this);

        //  版本
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
                textVersion.setText("1.00");
            }
        }
    }

    @Override
    public void onClick(View v) {

        RfidApp.doEffect(this,RfidApp.EFFECT_KEY);

        switch (v.getId()) {
            case R.id.image_return:
                this.finish();
                break;
        }
    }
}
