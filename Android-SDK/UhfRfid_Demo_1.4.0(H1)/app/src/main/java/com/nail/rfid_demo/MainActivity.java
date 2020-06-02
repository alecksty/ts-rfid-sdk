package com.nail.rfid_demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tanso.rfidlib.RfidSystem;
import com.tanso.rfidlib.comm.SDK;
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
import com.tanso.rfidlib.rfid.base.EReaderType;

import java.util.List;
import java.util.Locale;

import static com.tanso.rfidlib.port.ble.ManagerBLE.MSG_BLE_DEVICE_UPDATE;
import static com.tanso.rfidlib.rfid.base.BaseCmd.RFID_CMD_INVENTORY;

/**
 * 主界面
 */
public class MainActivity extends Activity implements IRfidEvent,
                                                      IPortEvent,
                                                      IPortMessage,
                                                      AdapterView.OnItemClickListener,
                                                      View.OnClickListener {
    private static final String TAG = "MainActivity";

    /**
     * 获取到权限
     */
    private static final int RT_GET_PERMISS = 10000;

    /**
     * 刷新消息
     */
    private static final int MSG_UPDATE_LIST = 1000;

    /**
     * 读写器系统（必须有的）
     */
    public static RfidSystem rfidSystem;

    /**
     * 标签列表
     */
    private TagListAdapter tagListAdapter;
    /**
     * 蓝牙列表
     */
    private BleDeviceAdapter bleDeviceAdapter;

    //  消息接口
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        @Override
        public void handleMessage(Message msg) {
            //====================
            //  列表更新
            //====================
            if (msg.what == MSG_UPDATE_LIST) {
                //  更新列表
                tagListAdapter.update();
                //  刷新界面
                tagListAdapter.notifyDataSetChanged();
            }
        }
    };

    private static final int[] arrayButtons = {R.id.button_ble_scan, R.id.button_ble_stop, R.id.button_start, R.id.button_stop, R.id.button_clean};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  蓝牙列表
        bleDeviceAdapter = new BleDeviceAdapter(this);
        ListView listDevice = this.findViewById(R.id.list_device);
        if (listDevice != null) {
            listDevice.setOnItemClickListener(this);
            listDevice.setAdapter(bleDeviceAdapter);
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
            rfidSystem = new RfidSystem(this, EReaderType.READER_TYPE_M100, EPortType.PORT_TYPE_BLE, 1, this);
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

        Log.e(TAG, String.format(Locale.ENGLISH, "OnPortMessage(msg:%d)\n", msg));

        if (msg == MSG_BLE_DEVICE_UPDATE) {
            //  获取到设备列表
            if (param instanceof List) {
                bleDeviceAdapter.clear();
                List<BluetoothDevice> l = SDK.castList(param, BluetoothDevice.class);
                for (int i = 0; i < l.size(); i++) {
                    bleDeviceAdapter.add(l.get(i));
                }
                bleDeviceAdapter.notifyDataSetChanged();
            }
        }
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

        //   错误消息
        if (type == BaseAck.RESPONSE_TYPE_ERROR) {
            return;
        }

        //  轮询消息
        if (cmd == RFID_CMD_INVENTORY) {
            //  标签对象
            if (obj instanceof TagItem) {
                //  注意:
                //  这里为什么不直接刷新?
                //  因为这里不是主线线程消息,刷新可能报错!
                //  发消息到主线程刷新列表.
                handler.sendEmptyMessage(MSG_UPDATE_LIST);
            }
        }
    }

    @Override
    public void OnPortEvent(EPortType ePortType, EPortEven ePortEven, int i, Object o, String s) {

        Log.d(TAG, "OnPortEvent(obj:" + o + ",msg:" + s + ")");

        switch (ePortType) {
            case PORT_TYPE_BLE:
                switch (ePortEven) {
                    case PORT_EVENT_CONNECTED:
                        //  连上自动开始扫描，不要自动，就去掉！
                        rfidSystem.start();
                        Log.e(TAG, "连接成功!");
                        break;
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDevice dev = bleDeviceAdapter.getItem(position);
        if (dev != null) {
            rfidSystem.getPortManager().getBleManager().connect(dev);
        }
    }

    /**
     * 权限列表
     */
    private static final String[] arrayPermission = {
            //  定位权限
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            //  外出存储器
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            //  蓝牙权限
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,
            //  WIFI和网络权限
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET,
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

        //  未执行
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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

        Log.d(TAG, "checkRightDone");

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

        //  蓝牙管理器
        ManagerBLE bleMan = rfidSystem.getPortManager().getBleManager();

        switch (v.getId()) {
            case R.id.button_ble_scan:
                //  断开
                bleMan.disconnect();
                //  设置无效
                bleMan.setPath("");
                //  关闭
                bleMan.scan(false);
                //  扫描
                bleMan.scan(true);

                break;

            case R.id.button_ble_stop:
                if (bleMan.isScanning()) {
                    rfidSystem.getPortManager().getBleManager().scan(false);
                }
                break;

            case R.id.button_start:
                //  开始读取
                rfidSystem.start();
                break;

            case R.id.button_stop:
                //  停止读取
                rfidSystem.stop();
                break;

            case R.id.button_clean:
                //  清空列表
                tagListAdapter.clear();
                break;

        }
    }
}
