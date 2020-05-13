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
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tanso.guilib.msgbox.EMessageType;
import com.tanso.guilib.msgbox.MsgMake;
import com.tanso.rfid.R;
import com.tanso.rfid.user.Global;
import com.tanso.rfid.user.RfidApp;
import com.tanso.rfidlib.port.EPortEven;
import com.tanso.rfidlib.port.EPortType;
import com.tanso.rfidlib.port.IPortEvent;
import com.tanso.rfidlib.port.IPortMessage;
import com.tanso.rfidlib.port.PortBase;
import com.tanso.rfidlib.port.ble.ManagerBLE;
import com.tanso.rfidlib.port.ble.PortBLE;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import static com.tanso.rfidlib.port.ble.ManagerBLE.MSG_BLE_DEVICE_UPDATE;


/**
 * 蓝牙配置界面
 */
public class BleDeviceActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        IPortMessage,
        View.OnClickListener,
        IPortEvent {

    /**
     * 调试
     */
    private final static boolean DEBUG = Global.DEBUG;

    /**
     * 目标
     */
    private final static String TAG = "BleDeviceActivity";

    /**
     * 消息
     */
    private static final int MSG_LIST_UPDATE = 1000;

    /**
     * 读写BLE终端
     */
    private BleDeviceAdapter mBleDevices;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_LIST_UPDATE:
                    break;
            }
        }
    };

    @SuppressLint("ObsoleteSdkInt")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_ble_device);

        if (DEBUG) {
            LoggerActivity.log_d(TAG, "创建蓝牙对象!");
        }

        RfidApp rfidApp = (RfidApp) this.getApplication();
        rfidApp.rfidSystem.getPortManager().setType(EPortType.PORT_TYPE_BLE);
        rfidApp.rfidSystem.getPortManager().setPortEvent(this);

        PortBase port = rfidApp.rfidSystem.getPortManager().getPort();
        rfidApp.rfidSystem.getRfidManager().setPort(port);

        if (port instanceof PortBLE) {
            PortBLE portBLE = (PortBLE) port;
            //  蓝牙管理器
            ManagerBLE bleManager = portBLE.getManager();
            //  消息接口
            bleManager.setMessage(this);
            //  扫描先断开
            bleManager.disconnect();
            //  扫描
            bleManager.scan(true);

            //  扫描
            Button btnScan = this.findViewById(R.id.button_ble_scan);
            if (btnScan != null) {
                btnScan.setOnClickListener(this);
            }

            //  返回
            ImageView imgReturn = this.findViewById(R.id.image_return);
            if (imgReturn != null) {
                imgReturn.setOnClickListener(this);
            }

            //  设备
            mBleDevices = new BleDeviceAdapter();
            //  列表
            ListView listView = this.findViewById(R.id.list_ble_devices);
            if (listView != null) {
                listView.setAdapter(mBleDevices);
                listView.setOnItemClickListener(this);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        RfidApp rfidApp = (RfidApp) this.getApplication();
        rfidApp.rfidSystem.getPortManager().setType(EPortType.PORT_TYPE_BLE);
        rfidApp.rfidSystem.getPortManager().setPortEvent(this);
        PortBase port = rfidApp.rfidSystem.getPortManager().getPort();

        rfidApp.rfidSystem.getRfidManager().setPort(port);

        if (port instanceof PortBLE) {
            PortBLE portBLE = (PortBLE) port;

            //  这一步必须有，申请权限！
            portBLE.init(this);

            ManagerBLE bleManager = portBLE.getManager();
            bleManager.setEvent(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        RfidApp rfidApp = (RfidApp) this.getApplication();

        ManagerBLE bleManager = rfidApp.rfidSystem.getPortManager().getBleManager();

        //  停止扫描
        if (bleManager.isScanning()) {
            bleManager.scan(false);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        RfidApp.doEffect(this, RfidApp.EFFECT_KEY);

        BluetoothDevice device = mBleDevices.getItem(position);

        RfidApp rfidApp = (RfidApp) this.getApplication();

        PortBase port = rfidApp.rfidSystem.getPortManager().getPort();

        if (port instanceof PortBLE) {
            //  蓝牙接口
            PortBLE portBLE = (PortBLE) port;
            //  蓝牙管理器
            ManagerBLE bleManager = portBLE.getManager();
            //  断开
            bleManager.disconnect();
            //  连接
            bleManager.connect(device);
            //  设置当前设备
            bleManager.setDevice(device);

            //  记忆设置
            portBLE.setupUpdate(true);

            LoggerActivity.log_e(TAG, "记忆设备地址:" + device.getAddress());

            //==========================
            //  使用下面代码跳转到服务界面,调试蓝牙使用,一般不用!
            //  Debug for service
            //==========================
            //Intent i = new Intent();
            //i.setClass(this, BleServiceActivity.class);
            //this.startActivity(i);

            //  刷新
            mBleDevices.notifyDataSetChanged();
        }
    }

    @Override
    public void OnPortMessage(int msg, Object param) {
        switch (msg) {
            case MSG_BLE_DEVICE_UPDATE:

                if (param instanceof List) {
                    mBleDevices.clear();
                    List<BluetoothDevice> l = (List<BluetoothDevice>) (param);
                    for (int i = 0; i < l.size(); i++) {
                        mBleDevices.add(l.get(i));
                    }
                    //  刷新
                    mBleDevices.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {

        RfidApp.doEffect(this, RfidApp.EFFECT_KEY);

        switch (v.getId()) {
            case R.id.button_ble_scan:
                RfidApp rfidApp = (RfidApp) this.getApplication();
                PortBase port = rfidApp.rfidSystem.getPortManager().getPort();

                if (port instanceof PortBLE) {
                    PortBLE    portBLE    = (PortBLE) port;
                    ManagerBLE bleManager = portBLE.getManager();
                    //  断开
                    bleManager.disconnect();
                    //  关闭
                    bleManager.scan(false);
                    //  扫描
                    bleManager.scan(true);
                }
                break;

            case R.id.image_return:
                this.finish();
                break;
        }
    }

    @Override
    public void OnPortEvent(EPortType type, EPortEven even, int param, Object device, String message) {
        switch (even) {
            //  开始扫描
            case PORT_EVENT_SCAN_START:
                //  扫描关闭
            case PORT_EVENT_SCAN_OFF:
                //  扫描中
            case PORT_EVENT_SCANNING:

                //  连接中
            case PORT_EVENT_CONNECTING:
                new MsgMake(this, EMessageType.MSG_NOTIFY, message, 5000);
                break;

            //  扫描失败
            case PORT_EVENT_SCAN_FAILED:
                //  连接失败
            case PORT_EVENT_CONNECT_FAILED:
                //  断开失败
            case PORT_EVENT_DISCONNECT_FAILED:
                new MsgMake(this, EMessageType.MSG_ERROR, message, 1000);
                break;

            //  已连接
            case PORT_EVENT_CONNECTED:
                //  断开中
            case PORT_EVENT_DISCONNECTING:
                //  已经断开
            case PORT_EVENT_DISCONNECTED:
                new MsgMake(this, EMessageType.MSG_NOTIFY, message, 1000);
                break;

            //  找到设备！
            case PORT_EVENT_FIND_DEVICE:
                new MsgMake(this, EMessageType.MSG_SUCCESS, message, 2000);
                break;

            // 其他
            default:
                break;
        }
    }

    /**
     * 设备列表类
     */
    class BleDeviceAdapter extends BaseAdapter {

        private ArrayList<BluetoothDevice> list = new ArrayList<>();

        void clear() {
            list.clear();
        }

        int findItem(String addr) {
            int i = 1;
            for (BluetoothDevice e : list) {
                if (e.getAddress().equals(addr)) {
                    return i;
                }
                i++;
            }
            return 0;
        }

        /**
         * 添加设备
         *
         * @param device ： 设备
         */
        void add(BluetoothDevice device) {

            if (device == null)
                return;

            //  去掉重复的!
            if (findItem(device.getAddress()) > 0)
                return;

            //  列表
            list.add(device);

            //  刷新
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public BluetoothDevice getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater mInflater = LayoutInflater.from(BleDeviceActivity.this);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_ble_device, null);
            }

            //  设备
            BluetoothDevice device = getItem(position);

            //  名称
            TextView text_name = convertView.findViewById(R.id.text_device_title);

            if (text_name != null) {

                if (device.getName() != null) {
                    if (device.getName().length() > 0) {
                        text_name.setText(device.getName());
                    }
                } else {
                    ParcelUuid[]  pu = device.getUuids();
                    StringBuilder sb = new StringBuilder();
                    if (pu != null) {
                        for (ParcelUuid aPu : pu) {
                            sb.append(aPu.getUuid().toString());
                        }
                        text_name.setText(sb.toString());
                    } else {
                        text_name.setText("");
                    }
                }
            }

            //  设备描述
            TextView text_subs = convertView.findViewById(R.id.text_device_script);
            if (text_subs != null) {
                text_subs.setText(device.getAddress());
            }

            RfidApp    rfidApp    = (RfidApp) BleDeviceActivity.this.getApplication();
            PortBase   port       = rfidApp.rfidSystem.getPortManager().getPort();
            ManagerBLE bleManager = null;

            if (port instanceof PortBLE) {
                PortBLE portBLE = (PortBLE) port;
                bleManager = portBLE.getManager();
            }

            if (text_name != null) {
                if (text_subs != null) {
                    if (bleManager != null) {
                        if (device.getAddress().equals(bleManager.getPath())) {
                            text_name.setTextColor(0xff007700);
                            text_subs.setTextColor(0xff007700);
                        } else {
                            text_name.setTextColor(Color.DKGRAY);
                            text_subs.setTextColor(Color.GRAY);
                        }
                    }
                }
            }
            //  保存设备引用
            convertView.setTag(device);

            return convertView;
        }
    }
}
