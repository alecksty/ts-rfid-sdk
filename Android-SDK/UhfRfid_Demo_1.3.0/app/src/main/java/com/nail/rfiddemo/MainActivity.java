package com.nail.rfiddemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.tanso.devlib.cust.DeviceBase;
import com.tanso.devlib.cust.device.DevH20;
import com.tanso.rfidlib.RfidSystem;
import com.tanso.rfidlib.port.EPortEven;
import com.tanso.rfidlib.port.EPortType;
import com.tanso.rfidlib.port.IPortEvent;
import com.tanso.rfidlib.port.IPortMessage;
import com.tanso.rfidlib.port.PortBase;
import com.tanso.rfidlib.port.ble.ManagerBLE;
import com.tanso.rfidlib.rfid.IRfidEvent;
import com.tanso.rfidlib.rfid.TagItem;
import com.tanso.rfidlib.rfid.base.BaseAck;
import com.tanso.rfidlib.rfid.base.BaseReader;

import java.util.Locale;

import static com.tanso.rfidlib.rfid.base.BaseCmd.RFID_CMD_INVENTORY;

/**
 * 主界面
 */
public class MainActivity extends Activity implements IRfidEvent, IPortEvent, AdapterView.OnItemClickListener, IPortMessage, View.OnClickListener {

    private static final String TAG = "MainActivity";

    private static final int RT_GET_PERMISS = 10000;

    /**
     * 刷新消息
     */
    private static final int MSG_UPDATE_LIST = 1000;

    /**
     * 读写器系统
     */
    public static RfidSystem rfidSystem;

    /**
     * 标签列表
     */
    private TagListAdapter tagListAdapter;

    //  消息接口
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        @Override
        public void handleMessage(Message msg) {
            //====================
            //  列表更新
            //====================
            if (msg.what == MSG_UPDATE_LIST) {//  更新列表
                tagListAdapter.update();
                //  刷新界面
                tagListAdapter.notifyDataSetChanged();
            }
        }
    };

    int[] arrayButtons = {R.id.button_ble_scan, R.id.button_ble_stop, R.id.button_start, R.id.button_stop, R.id.button_clean};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  蓝牙列表
        //bleDeviceAdapter = new BleDeviceAdapter(this);
        ListView listDevice = this.findViewById(R.id.list_device);
        if (listDevice != null) {
            listDevice.setOnItemClickListener(this);
          //  listDevice.setAdapter(bleDeviceAdapter);
        }

        //  标签列表
        tagListAdapter = new TagListAdapter(this);
        ListView listTags = this.findViewById(R.id.list_tags);
        if (listTags != null) {
            listTags.setOnItemClickListener(this);
            listTags.setAdapter(tagListAdapter);
        }

        //   按钮处理
        for (int arrayButton : arrayButtons) {
            Button button = findViewById(arrayButton);
            if (button != null) {
                button.setOnClickListener(this);
            }
        }

        //  标签系统对象
        if (rfidSystem == null) {
            //  创建系统
            //rfidSystem = new RfidSystem(this, EDeviceType.DEV_TYPE_H1, this);

            //rfidSystem = new RfidSystem(this, "Reader#type='QM100',ch=1", "BLE#dev='00:00:00:00:00:00'", this, this);
//            rfidSystem = new RfidSystem(this,
//                    "Reader#type='R2000',ch=4",
//                    "UART#path='/dev/ttyS4',baud=115200",
//                    this, this);
            //rfidSystem = new RfidSystem(this, "Reader#type='J2000',ch=16", "UDP#ip='192.168.0.1',port=7000", this, this);
            //rfidSystem = new RfidSystem(this, "Reader#type='QM100',ch=1", "TCP#ip='192.168.0.1',port=8000", this, this);
            //rfidSystem = new RfidSystem(this, "Reader#type='STREAM',ch=1", "TCP#ip='192.168.0.1',port=8000", this, this);

            //  如果是H20、H8，需要定义DevH8/DevH20

            DeviceBase device = new DevH20(this);

            device.setPower(true);

            rfidSystem = new RfidSystem(this,
                    "Reader#type='R2000',ch=1",
                    "UART#path='/dev/ttyMT1',baud=115200",
                    this,
                    this);

			/*
            rfidSystem = new RfidSystem(this,
                    "Reader#type='QM100',ch=1",
                    "BLE#dev='00:00:00:00:00:00'",
                    this,
                    this,
                    this,
                    new RfidSystem.OnEventCall() {
                        @Override
                        public void WhenEventDone(Object obj) {
                            Log.e(TAG, "WhenEventDone()" + obj);
                            if (rfidSystem != null) {
                                PortManager portMan = rfidSystem.getPortManager();
                                if (portMan != null) {
                                    //  关联接口消息
                                    portMan.setPortEvent(MainActivity.this);
                                    //  当前接口
                                    PortBase portBase = portMan.getPort();
                                    //  初始化(必须有)
                                    portBase.init(MainActivity.this);
                                    //  蓝牙管理器
                                    ManagerBLE man = portMan.getBleManager();

                                    if (man != null) {
                                        //  连接状态-消息接口
                                        man.setEvent(MainActivity.this);
                                        //  设备列表-消息接口
                                        man.setMessage(MainActivity.this);
                                        //  扫描先断开
                                        man.disconnect();
                                        //  扫描
                                        man.scan(true);
                                    }
                                }
                            }
                        }
                    });
					*/

            //rfidSystem = new RfidSystem(this, EDeviceType.DEV_TYPE_H3, this);
            //rfidSystem.getPortManager().getPort().connect();
        }

        //  请求权限
        requestRights();
    }

    /**
     * 获取设备列表
     *
     * @param msg   : 消息
     * @param param : 参数
     */
    @Override
    public void OnPortMessage(int msg, Object param) {

    }

    /**
     * 标签数据消息接口
     *
     * @param baseReader : 读写器
     * @param type       : 消息
     * @param cmd        : 参数
     * @param bytes      : 数据
     * @param obj        : 对象
     */
    @Override
    public void OnRfidResponse(BaseReader baseReader, int type, int cmd, byte[] bytes, Object obj) {

        if (obj != null) {
            Log.e(TAG, String.format(Locale.ENGLISH, "OnRfidResponse(type:%d,cmd:%02x,obj:%s)\n", type, cmd & 0xFF, obj.toString()));
        } else {
            Log.e(TAG, String.format(Locale.ENGLISH, "OnRfidResponse(type:%d,cmd:%02x)\n", type, cmd & 0xFF));
        }

        //=======================
        //   错误
        //=======================
        if (type == BaseAck.RESPONSE_TYPE_ERROR) {
            return;
        }

        //=======================
        //  正常消息
        //=======================
        switch (cmd) {
            //  轮询消息
            case RFID_CMD_INVENTORY:
                //  标签对象
                if (obj instanceof TagItem) {
                    //  注意:
                    //  这里为什么不直接刷新?
                    //  因为这里不是主线线程消息,刷新可能报错!
                    //  发消息到主线程刷新列表.
                    handler.sendEmptyMessage(MSG_UPDATE_LIST);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void OnPortEvent(EPortType ePortType, EPortEven ePortEven, int i, Object o, String s) {

        Log.d(TAG, "OnPortEvent(obj:" + o + ",msg:" + s + ")");

        switch (ePortType) {
            case PORT_TYPE_BLE:
                switch (ePortEven) {
                    case PORT_EVENT_CONNECTED:
                        //  开始扫描
                        //rfidSystem.start();
                        Log.e(TAG, "连接成功!");
                        break;
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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

        //  申请BLE权限权限
        for (String permits : arrayPermission) {
            //  没有权限
            if (ContextCompat.checkSelfPermission(this, permits) != PackageManager.PERMISSION_GRANTED) {
                //  申请
                ActivityCompat.requestPermissions(this, arrayPermission, RT_GET_PERMISS);
                //  判断是否需要 向用户解释，为什么要申请该权限
                ActivityCompat.shouldShowRequestPermissionRationale(this, permits);
                //  已经请求到
                getRightNow = true;

                break;
            }
        }

        if (!getRightNow) {
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
        //  权限获取成功
        if (requestCode == RT_GET_PERMISS) {
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
    }

    /**
     * 获取权限之后
     */
    private void checkRightDone() {
        //  当前接口
        PortBase portBase = rfidSystem.getPortManager().getPort();
        //  初始化
        portBase.init(this);
        //  蓝牙管理器
        ManagerBLE man = rfidSystem.getPortManager().getBleManager();
        //  消息接口
        man.setEvent(this);
        //  设备列表更新消息接口
        man.setMessage(this);
        //  扫描先断开
        man.disconnect();
        //  扫描
        man.scan(true);
        //  关联接口消息
        rfidSystem.getPortManager().setPortEvent(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button_start:
                rfidSystem.start();
                break;

            case R.id.button_stop:
                rfidSystem.stop();
                break;

            case R.id.button_clean:
                tagListAdapter.clear();
                break;

        }
    }
}
