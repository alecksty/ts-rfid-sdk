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
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tanso.guilib.dialog.IDlgEvent;
import com.tanso.guilib.dialog.DlgMake;
import com.tanso.guilib.msgbox.EMessageType;
import com.tanso.guilib.msgbox.MsgMake;
import com.tanso.rfid.R;
import com.tanso.rfid.user.RfidApp;
import com.tanso.rfidlib.comm.SDK;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

/**
 * 消息记录
 */
public class LoggerActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener, IDlgEvent {

    //  调试
    private static final boolean DEBUG = true;

    //  最大数据量
    private static final int MAX_LOGGER = 10000;

    //  类型
    private static final int LOG_TYPE_UNKNOW = 0;
    private static final int LOG_TYPE_D      = 1;
    private static final int LOG_TYPE_I      = 2;
    private static final int LOG_TYPE_E      = 3;

    //  消息
    private static final int MSG_UPDATE_LIST = 1000;
    //  导出
    private static final int MSG_EXPORT_FILE = 1001;

    @SuppressLint("HandlerLeak")
    private static final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_LIST:
                    try {
                        if (getAdaptor() != null) {
                            getAdaptor().notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    this.removeMessages(MSG_UPDATE_LIST);
                    break;

                case MSG_EXPORT_FILE:
                    if (msg.obj instanceof LoggerActivity) {
                        LoggerActivity activity = (LoggerActivity) msg.obj;
                        exportToWX(activity);
                    }
                    break;
            }
        }
    };

    private static final int ID_DLG_LOG_MENU = 1000;

    @Override
    public boolean OnDialogEvent(int id, Dialog dlg, View v, int pos, Object param) {
        switch (id) {
            case 0:
                break;

            case ID_DLG_LOG_MENU:
                switch (pos) {
                    case 0:
                        log_clean();
                        break;

                    case 1:
                        exportToFile(activity);
                        break;

                    case 2:
                        exportToQQ(activity);
                        break;

                    case 3:
                        exportToWX(activity);
                        break;
                }
                return true;
        }
        return true;
    }


    /**
     * 消息元素
     */
    public static class LogItem {
        int    type;
        long   time;
        String tag;
        String msg;

        /**
         * 构造调试
         *
         * @param tag : 目标
         * @param msg : 消息
         */
        public LogItem(String tag, String msg) {
            this.type = LOG_TYPE_UNKNOW;
            this.time = System.currentTimeMillis();
            this.tag = tag;
            this.msg = msg;
        }

        /**
         * 构造调试
         *
         * @param type ： 类型
         * @param tag  ： 目标
         * @param msg  ： 消息
         */
        LogItem(int type, String tag, String msg) {
            this.type = type;
            this.time = System.currentTimeMillis();
            this.tag = tag;
            this.msg = msg;
        }
    }

    /**
     * 消息列表
     */
    private static ArrayList<LogItem> list = new ArrayList<>();

    /**
     * 本界面
     */
    private static LoggerActivity activity = null;

    /**
     * 数据源
     */
    private LoggerAdapter adp = null;

    /**
     * 获取实例
     *
     * @return : 本例
     */
    public static LoggerActivity getInstance() {
        return activity;
    }

    /**
     * 获取数据源
     *
     * @return : 数据源
     */
    private static LoggerAdapter getAdaptor() {
        if (activity != null) {
            return activity.adp;
        }
        return null;
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_logger);

        adp = new LoggerAdapter();

        ListView listView = this.findViewById(R.id.list_logger);
        if (listView != null) {
            listView.setAdapter(adp);
            listView.setOnItemClickListener(this);
        }

        activity = this;

        //  返回
        ImageView imgReturn = this.findViewById(R.id.image_return);
        if (imgReturn != null) {
            imgReturn.setOnClickListener(this);
        }

        //  发数据
        ImageView imgSend = this.findViewById(R.id.image_send);
        if (imgSend != null) {
            imgSend.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {

        RfidApp.doEffect(this,RfidApp.EFFECT_KEY);

        switch (v.getId()) {
            //  返回
            case R.id.image_return:
                this.finish();
                break;

            //  发送
            case R.id.image_send:
                DlgMake d = new DlgMake();
                d.dialogList(ID_DLG_LOG_MENU, this, "菜单",
                        new String[]{"清空", "导出文件", "导出到QQ", "导出到微信"},
                        null, 0, 1, this);
                break;
        }
    }

    /**
     * 调试输出
     *
     * @param tag : 目标
     * @param msg : 消息
     */
    public static synchronized void log_d(String tag, String msg) {
        try {
            if (list != null) {
                if (list.size() > MAX_LOGGER) {
                    list.remove(0);
                }
                list.add(new LogItem(LOG_TYPE_D, tag, msg));
            }

            if (getInstance() != null) {
                handler.sendEmptyMessage(MSG_UPDATE_LIST);
            }

            Log.d(tag, msg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 调试输出
     *
     * @param tag : 目标
     * @param msg : 消息
     */
    public static synchronized void log_e(String tag, String msg) {
        try {
            if (list != null) {
                if (list.size() > MAX_LOGGER) {
                    list.remove(0);
                }
                list.add(new LogItem(LOG_TYPE_E, tag, msg));
            }

            if (getInstance() != null) {
                handler.sendEmptyMessage(MSG_UPDATE_LIST);
            }
            //  输出消息
            Log.e(tag, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 调试输出
     *
     * @param tag : 目标
     * @param msg : 消息
     */
    public static synchronized void log_i(String tag, String msg) {
        try {
            if (list != null) {
                if (list.size() > MAX_LOGGER) {
                    list.remove(0);
                }
                list.add(new LogItem(LOG_TYPE_I, tag, msg));
            }

            if (getInstance() != null) {
                handler.sendEmptyMessage(MSG_UPDATE_LIST);
            }
            Log.i(tag, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清空
     */
    public static synchronized void log_clean() {
        if (getInstance() != null) {
            if (list != null) {
                list.clear();
            }
            handler.sendEmptyMessage(MSG_UPDATE_LIST);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    /**
     * 生产日志文件
     *
     * @return ： 文件路径
     */
    private static String makeLogFile(Context context) {
        String filePath  = "log.csv";
        File   parentDir = context.getExternalCacheDir();
        int    i         = 0;

        if (parentDir != null) {
            File file = new File(parentDir.getPath() + "/" + filePath);
            try {
                //  文字存在,先删除!
                if (file.exists()) {
                    if (!file.delete()) {
                        System.err.println("删除失败!");
                    }
                }

                //  创建新的文件
                if (file.createNewFile()) {
                    FileOutputStream fos = new FileOutputStream(file);
                    StringBuilder    sb  = new StringBuilder();

                    //  数据头
                    sb.append("IDX,TIME,TYPE,TAG,MSG\r\n");

                    //  数据行
                    for (LogItem e : list) {

                        int sec = (int) (e.time / 1000);
                        int min = sec / 60;
                        int hur = min / 60;

                        String sTime = String.format(Locale.ENGLISH, "%d:%02d:%02d.%03d", hur % 24, min % 60, sec % 60, e.time % 1000);

                        sb.append(String.format(Locale.ENGLISH, "%d,%s,%d,%s,%s\r\n", ++i, sTime, e.type, e.tag, e.msg));
                    }

                    //  写入
                    fos.write(sb.toString().getBytes());
                    //  填充
                    fos.flush();
                    //  关闭
                    fos.close();
                } else {
                    System.err.println("文件创建失败!" + file.getAbsolutePath());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //  返回文件路径
            return file.getPath();
        }
        return null;
    }

    /**
     * 微信导出
     */
    public static void exportToQQ(Context context) {
        Intent intent;
        File   file;
        Uri    data;

        // 生成路径文件失败!
        String filePath = makeLogFile(context);

        if (filePath != null) {
            //  打开文件
            file = new File(filePath);

            //  发送
            intent = new Intent(Intent.ACTION_SEND);
            // 判断版本大于等于(7.0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                data = FileProvider.getUriForFile(context, SDK.RFID_FILE_PROVIDER, file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                data = Uri.fromFile(file);
            }

            intent.putExtra(Intent.EXTRA_STREAM, data);
            intent.putExtra(Intent.EXTRA_SUBJECT, "RFID Log");
            intent.setType("application/octet-stream");
            intent.setComponent(new ComponentName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity"));
            //intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI"));
            context.startActivity(intent);
        } else {
            System.err.println("生成失败!");
        }
    }

    public static void exportToWX(Context context) {
        Intent intent;
        File   file;
        Uri    data;

        // 生成路径文件失败!
        String filePath = makeLogFile(context);

        if (filePath != null) {
            //  打开文件
            file = new File(filePath);
            //  发送
            intent = new Intent(Intent.ACTION_SEND);
            // 判断版本大于等于(7.0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                data = FileProvider.getUriForFile(context, SDK.RFID_FILE_PROVIDER, file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                data = Uri.fromFile(file);
            }

            intent.putExtra(Intent.EXTRA_STREAM, data);
            intent.putExtra(Intent.EXTRA_SUBJECT, "RFID Log");
            intent.setType("application/octet-stream");
            //intent.setComponent(new ComponentName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity"));
            intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI"));
            context.startActivity(intent);
        } else {
            System.err.println("生成失败!");
        }
    }

    public static void exportToFile(Context context) {
        // 生成路径文件失败!
        String filePath = makeLogFile(context);
        new MsgMake(getInstance(), EMessageType.MSG_NOTIFY, "生成文件:" + filePath, 1000);
    }

    /**
     * 服务
     */
    public class LoggerAdapter extends BaseAdapter {

        public synchronized void clear() {
            list.clear();
        }

        public synchronized void add(LogItem item) {
            list.add(item);
        }

        public synchronized void remove(int index) {
            list.remove(index);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public LogItem getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater mInflater = LayoutInflater.from(LoggerActivity.this);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_log_data, null);
            }

            //  消息包
            LogItem item = getItem(position);

            //  时间
            TextView text_time = convertView.findViewById(R.id.text_logger_time);
            if (text_time != null) {
                int sec = (int) (item.time / 1000);
                int min = sec / 60;
                int hur = min / 60;
                text_time.setText(String.format(Locale.ENGLISH, "%d:%02d:%02d.%03d", hur % 24, min % 60, sec % 60, item.time % 1000));
            }

            //  目标
            TextView text_tag = convertView.findViewById(R.id.text_logger_tag);
            if (text_tag != null) {
                text_tag.setText(item.tag);
            }

            //  消息
            TextView text_msg = convertView.findViewById(R.id.text_logger_msg);
            if (text_msg != null) {
                text_msg.setText(item.msg);

                switch (item.type) {
                    case LOG_TYPE_D:
                        text_msg.setTextColor(Color.BLUE);
                        text_msg.setBackgroundResource(R.drawable.sel_log_d);
                        break;
                    case LOG_TYPE_I:
                        text_msg.setTextColor(Color.GREEN);
                        text_msg.setBackgroundResource(R.drawable.sel_log_i);
                        break;
                    case LOG_TYPE_E:
                        text_msg.setTextColor(Color.RED);
                        text_msg.setBackgroundResource(R.drawable.sel_log_e);
                        break;
                    default:
                        text_msg.setTextColor(Color.GRAY);
                        text_msg.setBackgroundResource(R.drawable.sel_log_d);
                        break;
                }
            }
            convertView.setTag(item);
            return convertView;
        }
    }
}
