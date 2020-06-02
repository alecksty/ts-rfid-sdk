package com.nail.rfid_demo;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tanso.rfidlib.RfidSystem;
import com.tanso.rfidlib.port.PortBase;
import com.tanso.rfidlib.port.ble.ManagerBLE;
import com.tanso.rfidlib.port.ble.PortBLE;

import java.util.ArrayList;

public class BleDeviceAdapter extends BaseAdapter {

    private Context mContext;

    private ArrayList<BluetoothDevice> list = new ArrayList<>();

    BleDeviceAdapter(Context context) {
        mContext = context;
    }

    void clear() {
        list.clear();
    }

    private int findItem(String addr) {
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
        LayoutInflater mInflater = LayoutInflater.from(mContext);
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

        RfidSystem rfidSystem = MainActivity.rfidSystem;
        PortBase   port       = rfidSystem.getPortManager().getPort();
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
