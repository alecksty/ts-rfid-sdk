
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
 * 2018年12月25日
 *
 * 改进点:
 * 1.IPortEvent 针对界面
 * 2.IPortStream 针对数据
 * 3.添加自动模式.不用设置次数.50ms无响应,自动发指令
 *
 */

package com.tanso.rfid.activity;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tanso.devlib.cust.DeviceBase;
import com.tanso.guilib.dialog.DlgMake;
import com.tanso.guilib.dialog.IDlgEvent;
import com.tanso.guilib.msgbox.EMessageType;
import com.tanso.guilib.msgbox.MsgMake;
import com.tanso.rfid.R;
import com.tanso.rfid.network.PostData;
import com.tanso.rfid.user.Global;
import com.tanso.rfid.user.RfidApp;
import com.tanso.rfidlib.RfidSystem;
import com.tanso.rfidlib.comm.SDK;
import com.tanso.rfidlib.data.TagNode;
import com.tanso.rfidlib.data.TagTree;
import com.tanso.rfidlib.port.EPortEven;
import com.tanso.rfidlib.port.EPortType;
import com.tanso.rfidlib.port.IPortEvent;
import com.tanso.rfidlib.port.IPortMessage;
import com.tanso.rfidlib.port.PortManager;
import com.tanso.rfidlib.port.sio.PortSIO;
import com.tanso.rfidlib.rfid.IRfidEvent;
import com.tanso.rfidlib.rfid.RfidManager;
import com.tanso.rfidlib.rfid.TagItem;
import com.tanso.rfidlib.rfid.base.BaseAck;
import com.tanso.rfidlib.rfid.base.BaseCmd;
import com.tanso.rfidlib.rfid.base.BaseReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import static android.view.KeyEvent.KEYCODE_F4;
import static com.tanso.guilib.dialog.DlgItemAdapter.FLAG_ADJUST_CENTER;
import static com.tanso.guilib.dialog.DlgItemAdapter.FLAG_ADJUST_LEFT;
import static com.tanso.rfid.user.RfidApp.POST_DATA_IN;
import static com.tanso.rfid.user.RfidApp.POST_DATA_ITEM;
import static com.tanso.rfid.user.RfidApp.POST_DATA_OUT;
import static com.tanso.rfid.user.RfidApp.POST_DATA_SYNC;
import static com.tanso.rfid.user.RfidApp.USE_COMPARE_LIST_SUPPORT;
import static com.tanso.rfid.user.RfidApp.USE_EXPORT_LIST_SUPPORT;
import static com.tanso.rfid.user.RfidApp.USE_POST_DATA_SUPPORT;
import static com.tanso.rfid.user.RfidApp.USE_TREE_LIST_SUPPORT;
import static com.tanso.rfidlib.rfid.base.BaseCmd.RFID_CMD_INIT;
import static com.tanso.rfidlib.rfid.base.BaseCmd.RFID_CMD_INVENTORY;
import static com.tanso.rfidlib.rfid.base.BaseCmd.RFID_CMD_KILL;
import static com.tanso.rfidlib.rfid.base.BaseCmd.RFID_CMD_LOCK;
import static com.tanso.rfidlib.rfid.base.BaseCmd.RFID_CMD_READ;
import static com.tanso.rfidlib.rfid.base.BaseCmd.RFID_CMD_WRITE;

/**
 * 主界面
 */
public class MainActivity extends AppCompatActivity implements
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        TextView.OnEditorActionListener,
        View.OnClickListener,
        View.OnLongClickListener,
        IRfidEvent,
        IPortEvent,
        IPortMessage,
        IDlgEvent,
        TextWatcher,
        PostData.OnPostEvent {

    /**
     * 调试
     */
    private static final boolean DEBUG = Global.DEBUG;

    /**
     * 调试
     */
    private static final boolean DEBUG_RFID = false;

    /**
     * 目标
     */
    private static final String TAG = "MainActivity";

    //==============================================================================================
    //  视图
    //==============================================================================================
    /**
     * 列表模式
     */
    public final static int VIEW_MODE_LIST = 0;

    /**
     * 树结构 - 模式
     */
    public final static int VIEW_MODE_TREE = 1;

    /**
     * 单个 - 模式
     */
    public final static int VIEW_MODE_ONE = 2;

    //==============================================================================================
    //  消息定义
    //==============================================================================================
    /**
     * 列表更新
     */
    private static final int MSG_UPDATE_LIST   = 10001;
    
    /**
     * 定时推数据
     */
    private static final int MSG_CMD_POST      = 10004;

    //==============================================================================================
    //  对话框定义
    //==============================================================================================
    /**
     * 消息-对话框
     */
    private static final int ID_MSG_DIALOG = 10001;
    /**
     * 导出-对话框
     */
    private static final int ID_MSG_EXPORT = 20002;
    /**
     * 列表-对话框
     */
    private static final int ID_LIST_ITEM  = 30003;

    //==============================================================================================
    //  数据 - 导出
    //==============================================================================================
    //  导出
    private static final int ID_EXPORT_TO_FILE      = 0;
    private static final int ID_EXPORT_TO_EMAIL     = 1;
    private static final int ID_EXPORT_TO_QQ        = 2;
    private static final int ID_EXPORT_TO_WX        = 3;
    //  推送
    private static final int ID_EXPORT_POST_IN      = 4;
    private static final int ID_EXPORT_POST_OUT     = 5;
    private static final int ID_EXPORT_POST_SYNC    = 6;
    //  比较
    private static final int ID_EXPORT_COMPARE_INIT = 7;
    private static final int ID_EXPORT_COMPARE_VIEW = 8;
    private static final int ID_EXPORT_VIEW_MODE    = 9;

    //==============================================================================================
    //  其他
    //==============================================================================================
    /**
     * 文件共享 - 提供者
     */
    private static final String SHARED_FILE_PROVIDER = SDK.RFID_FILE_PROVIDER;

    /**
     * 一直刷新列表(停止也刷新)
     */
    private static final boolean USE_UPDATE_LIST_ANYTIME = false;

    /**
     * 命令间隙
     */
    public static int cmd_delay_index = 0;

    //==============================================================================================
    //  控件/视图
    //==============================================================================================
    /**
     * 输入框
     */
    private EditText mEditSearch;

    /**
     * 清除
     */
    private ImageView mImageClear;

    /**
     * 信号强度
     */
    private TextView text_rssi = null;

    /**
     * 数量统计
     */
    private TextView text_count = null;

    //==============================================================================================
    //  数据
    //==============================================================================================
    /**
     * 标签数量
     */
    private int tag_count = 0;

    /**
     * 当前信号
     */
    private int tag_rssi = 0;

    /**
     * 数据源
     */
    private static TagListAdapter adp;

    /**
     * 当前选择项
     */
    private static TagItem selectItem;

   

    /**
     * =============================================================================================
     * 特殊型号 - 接口
     * =============================================================================================
     */

    /**
     * 定时刷新
     */
    private long prevRefreshTime = 0;

    /**
     * 需要刷新
     */
    private boolean mNeedUpdate = false;

    //  消息接口
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        @Override
        public void handleMessage(Message msg) {
            //  需要刷新
            boolean needRefresh = false;
            //  应用
            RfidApp theApp = (RfidApp) MainActivity.this.getApplication();

            switch (msg.what) {
                //====================
                //  列表更新
                //====================
                case MSG_UPDATE_LIST:
                    //  显示电池
                    updateVolatile();
                    //  防止太多
                    removeMessages(MSG_UPDATE_LIST);
                    //  刷新条件
                    if (USE_UPDATE_LIST_ANYTIME || theApp.rfidSystem.getRfidManager().isRunning || mNeedUpdate) {
                        //  刷新控制
                        	needRefresh = true;
                        if (mNeedUpdate) {
                            mNeedUpdate = false;
                            needRefresh = true;
                        }

                        //  需要刷新!
                        if (needRefresh) {
                            //  播放音效
                            RfidApp.doEffect(MainActivity.this, RfidApp.EFFECT_RFID);
                            //  更新列表
                            adp.update();
                            //  刷新显示
                            adp.notifyDataSetChanged();

                            //--------------------
                            //  刷新 - 数量
                            //--------------------
                            if (text_count != null) {
                                //  数量
                                int listCount = adp.getListCount();
                                //  数量改变
                                if (listCount != tag_count) {
                                    //  数量
                                    tag_count = listCount;
                                    //  修改
                                    text_count.setText(String.format("%d", listCount));
                                }
                            }
                        }
                    }

                    //--------------------
                    //  刷新 - 信号值
                    //--------------------
                    if (text_rssi != null) {
                        text_rssi.setText(String.format("%d", tag_rssi));
                    }
                    break;


                //====================
                //  丢数据
                //====================
                case MSG_CMD_POST:
                    if (DEBUG) {
                        System.err.println("MSG_CMD_POST");
                    }
                    //  删除多余消息
                    removeMessages(MSG_CMD_POST);
                    //  支持推送消息
                    if (USE_POST_DATA_SUPPORT) {
                        //  同步接口
                        PostData postSyn = theApp.listPost.get(POST_DATA_SYNC);
                        if (postSyn != null) {
                            //  开始运行，才传数据
                            if (theApp.rfidSystem.getRfidManager().isRunning) {
                                //  自动传
                                if (postSyn.auto) {
                                    //  最后一个标签有
                                    if (PostData.lastTag != null) {
                                        //  清空
                                        postSyn.clear();
                                        //  添加
                                        postSyn.add(PostData.lastTag);
                                        //  发送数据
                                        postSyn.doPost(MainActivity.this);

                                        if (DEBUG) {
                                            System.out.println("发送数据:" + PostData.lastTag);
                                        }
                                    } else {
                                        System.out.println("lastTag 为空");
                                    }
                                } else {
                                    System.out.println("postSyn.auto 没打开");
                                }
                            }
                            //  延时发送
                            sendEmptyMessageDelayed(MSG_CMD_POST, postSyn.delay);
                        } else {
                            System.out.println("postSyn is (null)");
                        }
                    } else {
                        if (DEBUG) {
                            System.err.println("USE_POST_DATA_SUPPORT 不支持");
                        }
                    }
                    break;


            }
        }
    };

    /**
     * 显示电量
     */
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void updateVolatile() {
        //  获取电压
        int voltage = RfidApp.getBatteryVoltage(MainActivity.this);
        //  显示电量
        if (voltage > 0) {
            //  电量比例
            int percent = RfidApp.getBatteryPercent(MainActivity.this);

            //  显示电量
            TextView text_battery  = MainActivity.this.findViewById(R.id.text_value_battery);
            TextView title_battery = MainActivity.this.findViewById(R.id.text_title_battery);

            //  显示电压
            TextView text_volatile  = MainActivity.this.findViewById(R.id.text_value_volatile);
            TextView title_volatile = MainActivity.this.findViewById(R.id.text_title_volatile);

            //  显示比例
            text_volatile.setText(String.format("%d", voltage));
            text_battery.setText(String.format("%d", percent) + "%");

            if (percent > 0) {
                text_battery.setVisibility(View.VISIBLE);
                title_battery.setVisibility(View.VISIBLE);
                text_volatile.setVisibility(View.VISIBLE);
                title_volatile.setVisibility(View.VISIBLE);
            } else {
                text_battery.setVisibility(View.GONE);
                title_battery.setVisibility(View.GONE);
                text_volatile.setVisibility(View.GONE);
                title_volatile.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 对话框 - 消息处理
     */
    @Override
    public boolean OnDialogEvent(int id, Dialog dlg, View v, int pos, Object param) {

        File   file;
        Intent intent;
        Uri    data;

        RfidApp.doEffect(this, RfidApp.EFFECT_KEY);
        RfidApp theApp = (RfidApp) MainActivity.this.getApplication();
        RfidManager rfidMan = theApp.rfidSystem.getRfidManager();

        switch (id) {
            //=====================================
            //  无效值
            //=====================================
            case 0:
                return true;

            //=====================================
            //  长按列表对话框
            //=====================================
            case ID_LIST_ITEM:
                switch (pos) {
                    case 0:
                        //  当前清零
                        selectItem.count = 0;
                        mNeedUpdate = true;
                        handler.sendEmptyMessage(MSG_UPDATE_LIST);
                        break;

                    case 1:
                        //  全部清零
                        adp.cleanCount();
                        mNeedUpdate = true;
                        handler.sendEmptyMessage(MSG_UPDATE_LIST);
                        break;

                    case 2:
                        //  删除当前标签
                        adp.remove(selectItem);
                        mNeedUpdate = true;
                        handler.sendEmptyMessage(MSG_UPDATE_LIST);
                        break;

                    case 3:
                        //  清除全部标签
                        adp.clear();
                        mNeedUpdate = true;
                        handler.sendEmptyMessage(MSG_UPDATE_LIST);
                        break;

                    default:
                        break;
                }
                break;

            //=====================================
            //  导出对话框
            //=====================================
            case ID_MSG_EXPORT:
                String filePath;

                switch (adp.viewMode) {
                    //  树视图
                    case VIEW_MODE_TREE:
                        filePath = rfidMan.exportToFile(this, adp.getTree());
                        break;

                    //  列表视图
                    case VIEW_MODE_LIST:
                        //filePath = rfidMan.exportToFile(this, adp.getList());
                        //break;

                        //  单项视图
                    default:
                        filePath = rfidMan.exportToFile(this, adp.getList());
                        break;
                }

                int base;

                if (USE_EXPORT_LIST_SUPPORT) {
                    base = ID_EXPORT_TO_FILE;
                } else if (USE_POST_DATA_SUPPORT) {
                    base = ID_EXPORT_POST_IN;
                } else if (USE_COMPARE_LIST_SUPPORT) {
                    base = ID_EXPORT_COMPARE_INIT;
                } else {
                    base = 0;
                }

                if (DEBUG) {
                    System.out.println("File : " + filePath);
                }

                //=======================
                //  导出数据
                //=======================
                switch (pos + base) {
                    case ID_EXPORT_TO_FILE:
                        if (USE_EXPORT_LIST_SUPPORT) {
                            //=======================
                            //  导出到文件
                            //=======================
                            String strDstPath = Environment.getExternalStorageDirectory() + "/rfid_doc";

                            //  目标文件
                            String strDstFile = strDstPath + "/" + "rfid_"
                                    + SDK.getYear() + "-"
                                    + String.format(Locale.ENGLISH, "%02d", SDK.getMonth()) + "-"
                                    + String.format(Locale.ENGLISH, "%02d", SDK.getDay()) + "_"
                                    + SDK.getHour() + "-"
                                    + String.format(Locale.ENGLISH, "%02d", SDK.getMinute()) + "-"
                                    + String.format(Locale.ENGLISH, "%02d", SDK.getSecond()) + ".csv";

                            //  目标文件
                            File fileDst = new File(strDstFile);

                            //  创建目录
                            if (!fileDst.getParentFile().exists()) {
                                if (!fileDst.getParentFile().mkdirs()) {
                                    if (DEBUG) {
                                        System.err.print("-->目录创建失败 : " + fileDst.getAbsolutePath() + "\r\n");
                                    }
                                }
                            }

//                            if (DEBUG) {
//                                System.err.print("-->绝对路径 : " + fileDst.getAbsolutePath() + "\r\n");
//                            }

                            if (!fileDst.exists()) {
                                try {
                                    if (!fileDst.createNewFile()) {
                                        if (DEBUG) {
                                            System.err.print("--->文件创建失败 : " + fileDst.getAbsolutePath() + "\r\n");
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            DlgMake d = new DlgMake();
                            //  创建文件
                            if (fileDst.exists()) {
                                //  复制文件
                                if (SDK.copyFile(filePath, fileDst.getAbsolutePath())) {
                                    d.dialogMsgBox(0, this,
                                            this.getString(R.string.str_info),
                                            this.getString(R.string.str_export_to_path) + ": \n" + strDstFile,
                                            new String[]{this.getString(R.string.str_ok)},
                                            0, null);
                                } else {
                                    d.dialogMsgBox(0, this,
                                            this.getString(R.string.str_info),
                                            this.getString(R.string.str_export_to_path) + ": \n" + strDstFile,
                                            new String[]{this.getString(R.string.str_ok)},
                                            0, null);
                                }

                                //  添加这里方便找到文件。
                                MediaScannerConnection.scanFile(this, new String[]{fileDst.getAbsolutePath()}, null, null);

                            } else {
                                d.dialogMsgBox(0, this,
                                        this.getString(R.string.str_info),
                                        this.getString(R.string.str_export_to_path) + ": \n" + strDstFile,
                                        new String[]{this.getString(R.string.str_ok)},
                                        0, null);
                            }
                        }
                        break;

                    case ID_EXPORT_TO_EMAIL:
                        //=======================
                        //  导出-->到邮件
                        //=======================
                        if (USE_EXPORT_LIST_SUPPORT) {
                            try {
                                //  打开文件
                                file = new File(filePath); //附件文件地址
                                //  发送
                                intent = new Intent(Intent.ACTION_SEND);
                                //  接收者
                                String[] tos = {"somebody@163.com"};
                                // 判断版本大于等于(7.0)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    data = FileProvider.getUriForFile(this, SHARED_FILE_PROVIDER, file);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                } else {
                                    data = Uri.fromFile(file);
                                }

                                intent.setType("application/octet-stream");
                                intent.putExtra(Intent.EXTRA_STREAM, data);
                                intent.putExtra(Intent.EXTRA_SUBJECT, this.getString(R.string.str_export_to_csv_file));
                                intent.putExtra(Intent.EXTRA_TEXT, this.getString(R.string.str_this_is_a_tag_list_file));
                                intent.putExtra(Intent.EXTRA_EMAIL, tos);
                                intent.setType("message/rfc882");
                                startActivity(intent);

                            } catch (ActivityNotFoundException e) {
                                new DlgMake().dialogMsgBox(0, this,
                                        this.getString(R.string.str_info),
                                        this.getString(R.string.str_error_no_email_service),
                                        new String[]{this.getString(R.string.str_ok)},
                                        0, null);
                            }
                        }
                        break;

                    case ID_EXPORT_TO_QQ:
                        //=======================
                        //  导出到QQ
                        //=======================
                        if (USE_EXPORT_LIST_SUPPORT) {
                            try {
                                //  打开文件
                                file = new File(filePath);
                                //  发送
                                intent = new Intent(Intent.ACTION_SEND);

                                MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, null);

                                // 判断版本大于等于(7.0)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    data = FileProvider.getUriForFile(this, SHARED_FILE_PROVIDER, file);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                } else {
                                    data = Uri.fromFile(file);
                                }

                                intent.putExtra(Intent.EXTRA_STREAM, data);
                                intent.putExtra(Intent.EXTRA_SUBJECT, this.getString(R.string.str_export_to_csv_file));
                                intent.setType("application/octet-stream");
                                intent.setComponent(new ComponentName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity"));
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                new DlgMake().dialogMsgBox(0, this,
                                        this.getString(R.string.str_info),
                                        this.getString(R.string.str_error_no_qq_service),
                                        new String[]{this.getString(R.string.str_ok)},
                                        0, null);
                            }
                        }
                        break;

                    case ID_EXPORT_TO_WX:
                        //=======================
                        //  导出到微信
                        //=======================
                        if (USE_EXPORT_LIST_SUPPORT) {
                            try {
                                //  打开文件
                                file = new File(filePath);
                                if (file.exists()) {
                                    //  可以找到
                                    MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, null);
                                    //  发送
                                    intent = new Intent(Intent.ACTION_SEND);
                                    // 判断版本大于等于(7.0)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        data = FileProvider.getUriForFile(this, SHARED_FILE_PROVIDER, file);
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    } else {
                                        data = Uri.fromFile(file);
                                    }
                                    intent.putExtra(Intent.EXTRA_STREAM, data);
                                    intent.putExtra(Intent.EXTRA_SUBJECT, this.getString(R.string.str_export_to_csv_file));
                                    intent.setType("application/octet-stream");
                                    intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI"));
                                    startActivity(intent);
                                } else {
                                    LoggerActivity.log_e(TAG, "文件不存在:" + file);
                                }
                            } catch (ActivityNotFoundException e) {
                                new DlgMake().dialogMsgBox(0, this,
                                        this.getString(R.string.str_info),
                                        this.getString(R.string.str_error_no_wx_service),
                                        new String[]{this.getString(R.string.str_ok)},
                                        0, null);
                            }
                        }
                        break;

                    case ID_EXPORT_POST_IN:
                        //=======================
                        //  入库
                        //=======================
                        if (USE_POST_DATA_SUPPORT) {
                            PostData postIn = theApp.listPost.get(POST_DATA_IN);
                            postIn.fillData(adp.list);
                            postIn.doPost(this);
                        }
                        break;

                    case ID_EXPORT_POST_OUT:
                        //=======================
                        //  出库
                        //=======================
                        if (USE_POST_DATA_SUPPORT) {
                            PostData postOut = theApp.listPost.get(POST_DATA_OUT);
                            postOut.fillData(adp.list);
                            postOut.doPost(this);
                        }
                        break;

                    case ID_EXPORT_POST_SYNC:
                        //=======================
                        //  同步
                        //=======================
                        if (USE_POST_DATA_SUPPORT) {
                            PostData postSyn = theApp.listPost.get(POST_DATA_SYNC);
                            postSyn.fillData(adp.list);
                            postSyn.doPost(this);
                        }
                        break;

                    case ID_EXPORT_COMPARE_INIT:
                        //=======================
                        //  比较-出门
                        //=======================
                        if (USE_COMPARE_LIST_SUPPORT) {
//                            //  填充数据
//                            theApp.dataBase.fillData(MainActivity.getList());
//                            //  保存
//                            theApp.dataBase.readWrite(true);
//                            //  消息
//                            String strState = this.getString(R.string.str_compare_init) + " (" + theApp.dataBase.list.size() + ")\n";
//                            //  对话框
//                            new DlgMake().dialogMsgBox(0, this,
//                                    this.getString(R.string.str_info),
//                                    strState,
//                                    new String[]{this.getString(R.string.str_ok)},
//                                    0, null);
                        }
                        break;

                    case ID_EXPORT_COMPARE_VIEW:
                        //=======================
                        //  比较-归来
                        //=======================
                        if (USE_COMPARE_LIST_SUPPORT) {
//                            Intent intentCompare = new Intent();
//                            intentCompare.setClass(this, CompareActivity.class);
//                            startActivity(intentCompare);
                        }
                        break;

                    case ID_EXPORT_VIEW_MODE:
                        //=======================
                        //  视图
                        //=======================
                        if (adp.viewMode == VIEW_MODE_LIST) {
                            adp.viewMode = VIEW_MODE_TREE;
                        } else {
                            adp.viewMode = VIEW_MODE_LIST;
                        }
                        adp.clearTree();
                        adp.update();
                        adp.notifyDataSetChanged();
                        break;

                    default:
                        break;
                }
                break;
        }
        return true;
    }

    /**
     * RFID - 消息响应
     *
     * @param type  : 类型
     * @param cmd   : 命令
     * @param param : 参数
     * @param obj   : 对象
     */
    @Override
    public void OnRfidResponse(BaseReader reader,int type, int cmd, byte[] param, Object obj) {
        //  应用
        RfidApp theApp = (RfidApp) MainActivity.this.getApplication();

        if (DEBUG_RFID) {
            if (obj != null) {
                LoggerActivity.log_e(TAG, String.format(Locale.ENGLISH, "OnRfidResponse(type:%d,cmd:%02x,param:%s,%s)\n", type, cmd & 0xFF, Global.arrayToText(param), obj.toString()));
            } else {
                LoggerActivity.log_e(TAG, String.format(Locale.ENGLISH, "OnRfidResponse(type:%d,cmd:%02x,param:%s)\n", type, cmd & 0xFF, Global.arrayToText(param)));
            }
        }

        //=======================
        //   错误
        //=======================
        if (type == BaseAck.RESPONSE_TYPE_ERROR) {
            if (obj instanceof String) {
                LoggerActivity.log_e(TAG, String.format(Locale.ENGLISH, "ERROR : (type:%d,cmd:%02x,ERR:%s)\n", type, cmd & 0xFF, obj.toString()));
            }
            return;
        }

        //=======================
        //  正常消息
        //=======================
        switch (cmd) {
            //==================
            //  收到标签
            //==================
            case RFID_CMD_INVENTORY:
                //  收到标签
                if (obj instanceof TagItem) {
                    //  标签
                    TagItem tag = (TagItem) obj;
                    //  最后一次的目标
                    PostData po = theApp.getPostSync();
                    if (po != null) {
                        if (po.auto) {
                            if (po.delay > 0) {
                                //  包含
//                                if (theApp.settings.contain.length() > 0) {
//                                    //  过滤
//                                    if (tag.isMatch(theApp.settings.contain)) {
//                                        //  保存最后一次
//                                        PostData.lastTag = tag;
//                                    }
//                                } else {
                                //  保存最后一次
                                PostData.lastTag = tag;
//                                }
                                //  上传数据
                                //handler.sendEmptyMessageDelayed(MSG_CMD_POST, po.delay);
                            }
                        }
                    }

                    //  信号
                    this.tag_rssi = tag.rssi;
                    //  删除之前的
                    handler.removeMessages(MSG_UPDATE_LIST);
                    //  刷新之后的
                    handler.sendEmptyMessage(MSG_UPDATE_LIST);
                }
                break;

            case RFID_CMD_INIT:
                break;
            case RFID_CMD_READ:
                break;
            case RFID_CMD_WRITE:
                break;
            case RFID_CMD_LOCK:
                break;
            case RFID_CMD_KILL:
                break;
            default:
                break;
        }
    }

    /**
     * 点击事件
     */
    @Override
    public void onClick(View v) {
        DlgMake d      = new DlgMake();
        Intent  intent = new Intent();
        RfidApp theApp = (RfidApp) this.getApplication();

        RfidApp.doEffect(this, RfidApp.EFFECT_KEY);

        if (v != null) {
            //==============
            // 设置
            //==============
            switch (v.getId()) {
                //==============
                //  清除
                //==============
                case R.id.image_search_clear:
                    mEditSearch.setText("");
                    mImageClear.setFocusable(true);
                    mImageClear.setFocusableInTouchMode(true);
                    mImageClear.requestFocus();
                    break;

                //==============
                //  设置
                //==============
                case R.id.text_setup:
                    intent.setClass(this, SetupActivity.class);
                    startActivity(intent);
                    break;

                //==============
                //  清除
                //==============
                case R.id.text_title_count:
                    adp.clear();
                    mNeedUpdate = true;
                    handler.sendEmptyMessage(MSG_UPDATE_LIST);
                    new MsgMake(this, EMessageType.MSG_NOTIFY, this.getString(R.string.str_clean_msg), 500);
                    break;

                //==============
                //  导出
                //==============
                case R.id.text_data:
                    ArrayList<String> arrayExport = new ArrayList<>();
                    ArrayList<String> arrayContent = new ArrayList<>();
                    String[] dlgList;
                    String[] dlgCont;

                    //  列表支持
                    if (USE_EXPORT_LIST_SUPPORT) {
                        arrayExport.add(this.getString(R.string.str_export_to_file));
                        arrayExport.add(this.getString(R.string.str_export_to_email));
                        arrayExport.add(this.getString(R.string.str_export_to_qq));
                        arrayExport.add(this.getString(R.string.str_export_to_wx));

                        arrayContent.add("(csv)");
                        arrayContent.add("(csv)");
                        arrayContent.add("(csv)");
                        arrayContent.add("(csv)");
                    }

                    //  数据导出
                    if (USE_POST_DATA_SUPPORT) {
                        arrayExport.add(this.getString(R.string.str_data_post_in));
                        arrayExport.add(this.getString(R.string.str_data_post_out));
                        arrayExport.add(this.getString(R.string.str_data_post_sync));

                        arrayContent.add("(json)");
                        arrayContent.add("(json)");
                        arrayContent.add("(json)");
                    }

                    //  列表
                    if (USE_COMPARE_LIST_SUPPORT) {
                        arrayExport.add(this.getString(R.string.str_data_compare_init));
                        arrayExport.add(this.getString(R.string.str_data_compare_list));

                        arrayContent.add("(Compare)");
                        arrayContent.add("(Compare)");
                    }

                    if (USE_TREE_LIST_SUPPORT) {
                        arrayExport.add(this.getString(R.string.str_data_view_mode));

                        arrayContent.add("(View)");
                    }

                    dlgList = new String[arrayExport.size()];
                    dlgCont = new String[arrayContent.size()];

                    int i = 0;
                    for (String s : arrayExport) {
                        dlgList[i++] = s;
                    }

                    i = 0;
                    for (String s : arrayContent) {
                        dlgCont[i++] = s;
                    }
                    d.dialogList(ID_MSG_EXPORT,
                            this,
                            this.getString(R.string.str_data),
                            dlgList,
                            dlgCont,
                            -1,
                            FLAG_ADJUST_LEFT,
                            this);
                    break;

                //==============
                //  开始
                //==============
                case R.id.text_start:
                    //====================
                    //  还没有连接
                    //====================
                    if (!theApp.rfidSystem.getPortManager().getPort().isConnected()) {
                        d.dialogMsgBox(ID_MSG_DIALOG,
                                this,
                                this.getString(R.string.str_info),
                                this.getString(R.string.str_no_found_device_msg),
                                new String[]{this.getString(R.string.str_ok)},
                                0,
                                this);
                    } else {
                        RfidManager manager = theApp.rfidSystem.getRfidManager();
                        //  状态反转
                        manager.isRunning = !manager.isRunning;
                        //====================
                        //  检测状态
                        //====================
                        if (!manager.isRunning) {
                            //  刷新存盘
                            manager.dataUpdate(this, true);

                            theApp.rfidSystem.stop();
                        } else {
                            theApp.rfidSystem.start();
                        }

                        //  刷新按钮显示
                        updateButton();
                    }
                    break;
            }
        }
    }

    @SuppressLint({"ObsoleteSdkInt", "SetTextI18n"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        //  显示收银界面
        setContentView(R.layout.activity_main);

        if (adp == null) {
            //  数据源
            adp = new TagListAdapter();
        }

        //  更新
        adp.update();

        //  数据标签
        ListView listTags = this.findViewById(R.id.list_tags);
        listTags.setAdapter(adp);
        listTags.setOnItemClickListener(this);
        listTags.setOnItemLongClickListener(this);

        //  导出
        TextView textExport = this.findViewById(R.id.text_data);
        textExport.setOnClickListener(this);

        //  开始
        TextView textStart = this.findViewById(R.id.text_start);
        textStart.setClickable(true);
        textStart.setLongClickable(true);
        textStart.setOnClickListener(this);
        textStart.setOnLongClickListener(this);

        //  设置
        TextView textSetup = this.findViewById(R.id.text_setup);
        textSetup.setOnClickListener(this);

        //  搜索数据
        mEditSearch = this.findViewById(R.id.edit_search_text);
        if (mEditSearch != null) {
            mEditSearch.setOnEditorActionListener(this);
            mEditSearch.addTextChangedListener(this);
        }

        //  清空
        mImageClear = this.findViewById(R.id.image_search_clear);
        if (mImageClear != null) {
            mImageClear.setClickable(true);
            mImageClear.setOnClickListener(this);
        }

        //  信号
        text_rssi = this.findViewById(R.id.text_title_rssi);
        text_rssi.setOnClickListener(this);

        //  合计
        text_count = this.findViewById(R.id.text_title_count);
        text_count.setOnClickListener(this);

        //  电池
        TextView text_battery = this.findViewById(R.id.text_title_battery);
        text_battery.setOnClickListener(this);

        //  应用
        RfidApp theApp = (RfidApp) this.getApplication();
        //  管理器
        PortManager portManager = theApp.rfidSystem.getPortManager();
        //  读取 设置
        TagItem.updateSetup(this, false);
        //  消息接口(RFID)
        theApp.rfidSystem.getRfidManager().setRfidEvent(this);

        //  消息接口(PORT)
        portManager.setPortEvent(this);
        //  端口 - 连接
        portManager.getPort().connect();

        //  读取 - 树结构
        adp.getTree().updateSetup(this, false);

        if (DEBUG) {
            LoggerActivity.log_e(TAG, "开始");
        }

        //  初始化设备
        DeviceOpenTread openTread = new DeviceOpenTread(theApp);
        openTread.start();
    }

    /**
     * 获取数据源
     *
     * @return : 数据源
     */
    public static TagListAdapter getAdapter() {
        return adp;
    }

    /**
     * 获取数据源树
     *
     * @return : 数据树
     */
    public static TagTree getTree() {
        return adp.tree;
    }

    /**
     * 获取列表
     *
     * @return : 列表
     */
    public static ArrayList<TagItem> getList() {
        if (adp != null) {
            return adp.getList();
        }
        return null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (DEBUG) {
            LoggerActivity.log_e(TAG, "onKeyDown(" + keyCode + ")");
        }

        switch (keyCode) {
            //==============
            //  for H8
            //==============
            case KEYCODE_F4:
                TextView view = this.findViewById(R.id.text_start);
                if (view != null) {
                    view.callOnClick();
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (DEBUG) {
            LoggerActivity.log_d(TAG, "onStart()");
        }

        //  应用
        RfidApp theApp = (RfidApp) this.getApplication();
        //  管理器
        PortManager portMan = theApp.rfidSystem.getPortManager();

        //  消息接口
        theApp.rfidSystem.getRfidManager().setRfidEvent(this);
        //  消息接口
        portMan.setPortEvent(this);
        //  恢复
        theApp.rfidSystem.resume();

        //  按钮更新
        updateButton();

        //  当前接口
        if (!portMan.getPort().isConnected()) {
            new MsgMake(this, EMessageType.MSG_NOTIFY, getString(R.string.str_no_connect_msg), 3000);
        }
    }

	// 开线程打开设备
    class DeviceOpenTread extends Thread {

        private RfidApp theApp;

        DeviceOpenTread(Application a) {
            theApp = (RfidApp) a;
        }

        @Override
        public void run() {
            //  管理器
            PortManager portMan = theApp.rfidSystem.getPortManager();

            if (theApp.curDevice != null) {

                DeviceBase dev = theApp.curDevice;
                //  开电
                //  这个好像跟什么有冲突
                //  会搞得设置很慢
                dev.setPower(true);

                try {
                    Thread.sleep(200L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (dev.getInputStream() != null) {
                    //  流结构
                    portMan.setType(EPortType.PORT_TYPE_STREAM);
                    //  接口
                    PortSIO port = (PortSIO) portMan.getPort();
                    //	配置流
                    port.Config(dev.getInputStream(), dev.getOutputStream());
                    //  连接
                    port.connect();
                    //	关联
                    theApp.rfidSystem.getRfidManager().setPort(port);
                }
            }
        }
    }


    /**
     * 刷新按钮显示
     */
    private void updateButton() {
        //  应用
        RfidApp theApp = (RfidApp) this.getApplication();
        //  开始
        TextView textStart = this.findViewById(R.id.text_start);
        //  状态
        if (theApp.rfidSystem.getRfidManager().isRunning) {
            textStart.setText(R.string.str_stop);
        } else {
            textStart.setText(R.string.str_start);
        }

        //  同步触发
        PostData po = theApp.getPostSync();
        if (po != null) {
            if (po.auto) {
                if (po.delay > 0) {
                    //  上传数据
                    handler.sendEmptyMessage(MSG_CMD_POST);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //  应用
        RfidApp theApp = (RfidApp) this.getApplication();
        //  保存
        theApp.rfidSystem.getRfidManager().dataUpdate(this, true);
        //  设置
        TagItem.updateSetup(this, true);

        if (adp != null) {
            //  保存
            adp.getTree().updateSetup(this, true);
        }

        //==================
        //  特定机型
        //==================
        if (theApp.curDevice != null) {
            theApp.curDevice.setPower(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeUI();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        resumeUI();
    }

    public RfidApp getApp() {
        return (RfidApp) this.getApplication();
    }

    /**
     * 恢复界面状态
     */
    private void resumeUI() {
        //  事件接口
        RfidApp theApp = (RfidApp) this.getApplication();
        //  接口
        PortManager portManager = theApp.rfidSystem.getPortManager();
        //  RFID
        RfidManager rfidManager = theApp.rfidSystem.getRfidManager();
        //  数据接口
        rfidManager.setPort(portManager.getPort());
        //  消息接口
        rfidManager.getReader().setEvent(this);
        //  端口
        portManager.setPortEvent(this);
        //  更新按钮
        updateButton();
    }

    /**
     * 消息
     */
    @Override
    public void OnPortMessage(int msg, Object param) {
        if (param instanceof String) {
            String txt = (String) param;
            new MsgMake(this, EMessageType.MSG_NOTIFY, txt, 1000);
        }
    }

    /**
     * 点击项目
     *
     * @param parent   : 上级
     * @param view     : 视图
     * @param position : 位置
     * @param id       : 序号
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (adp != null) {
            //  按键
            RfidApp.doEffect(this, RfidApp.EFFECT_KEY);

            synchronized (this) {
                //  获取
                Object n = adp.getItem(position);

                //==================
                //  类型 (树结构)
                //==================
                if (n instanceof TagNode) {
                    //  类型
                    TagNode node = (TagNode) n;
                    //  刷新
                    node.expan = !node.expan;
                    //  关联树 - 结构
                    if (node.tag instanceof TagNode) {
                        //  树结构
                        TagNode t = (TagNode) node.tag;
                        //  解析
                        t.expan = node.expan;
                    }
                    //  快速更新
                    adp.fastUpdate();
                }

                //==================
                //  标签 (列表)
                //==================
                if (n instanceof TagItem) {

                    //  类型转换
                    TagItem item = (TagItem) n;

                    //  这个需要设定地址
                    if (USE_POST_DATA_SUPPORT) {

                        PostData postItem = getApp().listPost.get(POST_DATA_ITEM);
                        //	详情有效
                        if (postItem.enable) {
                            postItem.clear();
                            postItem.add(item);
                            postItem.doPost(this);
                            postItem.setEven(this);
                            return;
                        }
                    }

                    //  请求
                    Intent i = new Intent();
                    //  内容界面
                    i.setClass(this, ContentActivity.class);
                    //  当前标签
                    ContentActivity.setTagItem(item);
                    //  启动
                    startActivity(i);
                }
            }
        }
    }

    /**
     * 长按项目,出菜单！
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        Object n = adp.getItem(position);

        if (n instanceof TagItem) {
            //  选中的数据
            selectItem = (TagItem) n;
            //  制作对话框
            DlgMake d = new DlgMake();
            //  列表对话框
            d.dialogList(ID_LIST_ITEM,
                    this,
                    getString(R.string.str_oprate),
                    new String[]{
                            getString(R.string.str_oprate_clean_one),
                            getString(R.string.str_oprate_clean_all),
                            getString(R.string.str_oprate_delete_one),
                            getString(R.string.str_oprate_delete_all)
                    },
                    new String[]{"" + selectItem.count, "" + adp.getCount(), "" + selectItem.getTextEpc(),
                                 getString(R.string.str_main_total)
                                         + "(" + adp.getCount() + ")"
                                         + getString(R.string.str_main_pcs)},
                    -1,
                    FLAG_ADJUST_CENTER,
                    this);
        }

        return true;
    }

    @Override
    public boolean onLongClick(View v) {

        RfidApp theApp = (RfidApp) this.getApplication();

        switch (v.getId()) {
            //==================================
            //  长按,直接清掉,刷新!
            //==================================
            case R.id.text_start:
                adp.clear();
                mNeedUpdate = true;
                handler.sendEmptyMessage(MSG_UPDATE_LIST);
                theApp.rfidSystem.start();
                updateButton();
                break;

            default:
                break;

        }
        return false;
    }

    /**
     * 端口消息
     *
     * @param type    : 类型
     * @param param   : 参数
     * @param device  : 设备
     * @param message : 消息
     */
    @Override
    public void OnPortEvent(EPortType type, EPortEven even, int param, Object device, String message) {

        if (DEBUG) {
            System.out.printf(TAG + "--->OnPortEvent(type:%d,param:%d,message:%s)\n", type.ordinal(), param, message);
        }

        switch (even) {
            //  开始扫描
            case PORT_EVENT_SCAN_START:
                new MsgMake(this, EMessageType.MSG_NOTIFY, message, 1000);
                break;

            //  扫描中
            case PORT_EVENT_SCANNING:
                new MsgMake(this, EMessageType.MSG_NOTIFY, message, 1000);
                break;

            //  扫描失败
            case PORT_EVENT_SCAN_FAILED:
                new MsgMake(this, EMessageType.MSG_ERROR, message, 1000);
                break;

            //  扫描关闭
            case PORT_EVENT_SCAN_OFF:
                new MsgMake(this, EMessageType.MSG_NOTIFY, message, 1000);
                break;

            case PORT_EVENT_CONNECTING:
                new MsgMake(this, EMessageType.MSG_NOTIFY, message, 3000);
                break;

            case PORT_EVENT_CONNECT_FAILED:
                new MsgMake(this, EMessageType.MSG_ERROR, message, 1000);
                break;

            case PORT_EVENT_CONNECTED:
                new MsgMake(this, EMessageType.MSG_NOTIFY, message, 2000);
                break;

            case PORT_EVENT_DISCONNECTING:
                new MsgMake(this, EMessageType.MSG_NOTIFY, message, 1000);
                break;

            case PORT_EVENT_DISCONNECT_FAILED:
                new MsgMake(this, EMessageType.MSG_ERROR, message, 1000);
                break;

            case PORT_EVENT_DISCONNECTED:
                new MsgMake(this, EMessageType.MSG_NOTIFY, message, 1000);
                break;

            case PORT_EVENT_FIND_SEVICE:
                new MsgMake(this, EMessageType.MSG_NOTIFY, message, 1000);
                break;

            case PORT_EVENT_FIND_DEVICE:
                new MsgMake(this, EMessageType.MSG_SUCCESS, message, 2000);
                break;

            default:
                new MsgMake(this, EMessageType.MSG_NOTIFY, message, 1000);
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        adp.searchText = s.toString();
        adp.update();
    }

    @Override
    public void afterTextChanged(Editable s) {
        adp.searchText = s.toString();
        adp.update();
    }

    /**
     * 查询结果回调(适用于查询)
     *
     * @param post   : 对象
     * @param result : 结果
     * @param error  : 错误 (0:没错误)
     */
    @Override
    public void OnPostResult(PostData post, String result, int error) {
        if (result != null) {
            //	显示详情界面
            Intent i = new Intent(this, UserDataActivity.class);
            //	推入参数
            i.putExtra(UserDataActivity.DEF_EXTRA_JSON, result);
            if (post.list.size() > 0) {
                i.putExtra(UserDataActivity.DEF_EXTRA_NAME, post.list.get(0).getTextEpc());
            } else {
                i.putExtra(UserDataActivity.DEF_EXTRA_NAME, "详情");
            }
            startActivity(i);
        } else {
            new MsgMake(this, EMessageType.MSG_ERROR, "获取数据失败!请检查网络以及参数是否正确!", 1000);
        }
    }

    /**
     * 标签列表数据源
     */
    class TagListAdapter extends BaseAdapter {

        /**
         * 显示 - 模式(列表/树结构)
         */
        private int viewMode = VIEW_MODE_LIST;

        /**
         * 查询 - 内容
         */
        private String searchText = "";

        /**
         * 列表
         */
        private ArrayList<TagItem> list = new ArrayList<>();

        /**
         * 树结构
         */
        private ArrayList<TagNode> nodes = new ArrayList<>();

        /**
         * 树结构
         */
        private TagTree tree = new TagTree();

        /**
         * 返回列表
         *
         * @return : 列表
         */
        public ArrayList<TagItem> getList() {
            return list;
        }

        /**
         * 获取系统列表
         *
         * @return : 列表
         */
        ArrayList<TagItem> getRfidList() {
            RfidApp theApp = (RfidApp) MainActivity.this.getApplication();
            return theApp.rfidSystem.getRfidManager().getList();
        }

        /**
         * 获取模式
         *
         * @return : 模式
         */
        public int getViewMode() {
            return viewMode;
        }

        /**
         * 设置显示模式
         *
         * @param mode : 模式
         */
        public void setViewMode(int mode) {
            viewMode = mode;
        }

        /**
         * 取得树结构
         *
         * @return : 数据树
         */
        TagTree getTree() {
            return tree;
        }

        /**
         * 清掉树
         */
        synchronized void clearTree() {
            tree.clean();
        }

        /**
         * 清空计数器
         */
        void cleanCount() {
            ArrayList<TagItem> l = getRfidList();
            if (l != null) {
                for (TagItem a : l) {
                    a.count = 0;
                }
            }
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

            //  清空树结构
            tree.clean();
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

//            if (searchText != null) {
//                Log.d(TAG, "searchText(" + searchText + ")");
//            }

            //  应用
            RfidApp theApp = (RfidApp) MainActivity.this.getApplication();

            //  添加列表
            if ((searchText == null) || searchText.isEmpty()) {
                synchronized (this) {
                    //  更快的方式添加
                        this.list = (ArrayList<TagItem>) theApp.rfidSystem.getRfidManager().getList().clone();
                }
            } else {

                synchronized (this) {
                    //  清空本地
                    this.list.clear();
                }

                ArrayList<TagItem> l = (ArrayList<TagItem>) theApp.rfidSystem.getRfidManager().getList().clone();
                //  过滤有效的数据
                for (TagItem e : l) {
                    //  匹配就添加
                    if (e.isMatch(searchText)) {
                        this.list.add(e);
                    }
                }
            }

            synchronized (this) {
                //  排序
                Collections.sort(this.list);
            }

            //  支持 - 树结构
            if (USE_TREE_LIST_SUPPORT) {
                synchronized (this) {
                    //  导入
                    tree.doImport(this.list);
                    //  多余
                    tree.autoRemove(this.list);
                    //  导出
                    nodes = tree.doExport();
                }
            }
        }

        /**
         * 快速刷新
         */
        public synchronized void fastUpdate() {
            if (USE_TREE_LIST_SUPPORT) {
                if (viewMode == VIEW_MODE_TREE) {
                    nodes = tree.doExport();
                }
            }
            this.notifyDataSetChanged();
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
            if (USE_TREE_LIST_SUPPORT) {
                if (viewMode == VIEW_MODE_TREE) {
                    if (nodes != null) {
                        return nodes.size();
                    }
                } else {
                    if (list != null) {
                        return list.size();
                    }
                }
            } else {
                if (list != null) {
                    return list.size();
                }
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (USE_TREE_LIST_SUPPORT) {
                if (viewMode == VIEW_MODE_TREE) {
                    if (nodes != null) {
                        return nodes.get(position);
                    }
                } else {
                    if (list != null) {
                        return list.get(position);
                    }
                }
            } else {
                if (list != null) {
                    return list.get(position);
                }
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

            LayoutInflater mInflater = LayoutInflater.from(MainActivity.this);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_tag_data, null);
            }

            if (viewMode == VIEW_MODE_LIST) {
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
            }

            return convertView;
        }
    }
}
