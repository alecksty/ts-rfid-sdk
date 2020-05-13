package com.nail.rfiddemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tanso.rfidlib.rfid.RfidManager;
import com.tanso.rfidlib.rfid.TagItem;

import java.util.ArrayList;
import java.util.Collections;

public class TagListAdapter extends BaseAdapter {

    private Context context;

    /**
     * 列表
     */
    private ArrayList<TagItem> list = new ArrayList<>();

    /**
     * 返回列表
     *
     * @return : 列表
     */
    public ArrayList<TagItem> getList() {
        return list;
    }

    public TagListAdapter(Context context) {
        this.context = context;
    }

    /**
     * 获取系统列表
     *
     * @return : 列表
     */
    ArrayList<TagItem> getRfidList() {
        RfidManager man = MainActivity.rfidSystem.getRfidManager();
        return man.getList();
    }


    /**
     * 清空列表
     */
    public synchronized void clear() {

        ArrayList<TagItem> l = getRfidList();

        //  清空列表
        if (l != null) {
            l.clear();
        }

        //  更新
        update();
    }


    /**
     * 添加标签
     */
    public synchronized void add(TagItem e) {
        ArrayList<TagItem> l = getRfidList();
        if (l != null) {
            //  列表
            l.add(e);
        }
    }

    /**
     * 删除标签
     */
    public synchronized void remove(TagItem tag) {
        ArrayList<TagItem> l = getRfidList();
        if (l != null) {
            int i = 0;
            for (TagItem e : l) {
                if (e.equal(tag)) {
                    l.remove(i);
                    break;
                }
                i++;
            }
        }
    }

    /**
     * 查找数据
     */
    public TagItem find(String epc) {
        if (epc != null) {
            for (TagItem e : this.list) {
                if (e.getTextEpc().equalsIgnoreCase(epc)) {
                    return e;
                }
            }
        }
        return null;
    }

    /**
     * 列表关联
     * 100以下,直接刷新
     * 1000个以下每秒10次
     * 只获取100个,滑动多,就再读100个.
     */
    synchronized void update() {

        synchronized (this) {
            //  更快的方式添加
            this.list = (ArrayList<TagItem>) getRfidList().clone();
        }

        synchronized (this) {
            //  排序
            Collections.sort(this.list);
        }

    }


    /**
     * 列表数量
     *
     * @return : 数量
     */
    public int getListCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    @Override
    public int getCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (list != null) {
            return list.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"DefaultLocale", "InflateParams", "SetTextI18n"})
    @Override
    public View getView(int position, View _convertView, ViewGroup parent) {

        View convertView = _convertView;

        LayoutInflater mInflater = LayoutInflater.from(context);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_tag_data, null);
        }

        //  单个项目
        TagItem e = (TagItem) getItem(position);
        //  索引
        TextView text_index = convertView.findViewById(R.id.text_item_index);

        //========================
        //  索引
        //========================
        if (text_index != null) {
            //
            text_index.setText(String.format("%d", position + 1));
            //  背景
            Drawable draw = text_index.getBackground();
            //  不使用硬件加速
            text_index.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            //  根据值来区分颜色
            draw.setColorFilter(e.getColorByEpc(), PorterDuff.Mode.SRC_IN);
            //draw.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
        }

        //========================
        //  协议
        //========================
        TextView text_head = convertView.findViewById(R.id.text_item_head);
        if (text_head != null) {
            text_head.setText(String.format("PC:%X", e.pc & 0xffff));
        }

        //========================
        //  校验码
        //========================
        TextView text_crc = convertView.findViewById(R.id.text_item_crc);
        if (text_crc != null) {
            int crc_tmp = e.crc;
            int d       = 0xffff & crc_tmp;
            //text_crc.setText(String.format("CRC:%04X %d", d, e.ant));
            text_crc.setText(String.format("ANT:%d", e.ant + 1));
        }

        //========================
        //  信号强度
        //========================
        TextView text_rssi = convertView.findViewById(R.id.text_item_rssi);
        if (text_rssi != null) {
            //  显示单位
            text_rssi.setText(String.format("RSSI:%d dB", e.rssi));
        }

        //========================
        //  EPC
        //========================
        TextView text_title = convertView.findViewById(R.id.text_item_title);
        if (text_title != null) {
            text_title.setText(e.getTextEpc());
            //
            long diff = System.currentTimeMillis() - e.time;
            //  1秒钟之内的数据显示红色
            if (diff < 1000) {
                text_title.setTextColor(Color.RED);
            } else {
                text_title.setTextColor(Color.DKGRAY);
            }
        }

        //========================
        //  TID
        //========================
        TextView text_tid = convertView.findViewById(R.id.text_item_tid);
        if (text_tid != null) {
            text_tid.setVisibility(View.GONE);
            if (e.tid != null) {
                if (e.tid.length > 0) {
                    text_tid.setText("TID:" + e.getTextTidMsb());
                    text_tid.setVisibility(View.VISIBLE);
                }
            }
        }

        //========================
        //  计数
        //========================
        TextView text_desc = convertView.findViewById(R.id.text_item_desc);
        if (text_desc != null) {
            text_desc.setText(String.format("%d", e.count));
        }

        //  关联数据
        convertView.setTag(e);

        return convertView;
    }
}
