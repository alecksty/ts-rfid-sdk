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

package com.tanso.rfid.user;

import android.app.Activity;
import android.app.Application;

import com.tanso.devlib.cust.DeviceBase;
import com.tanso.devlib.cust.device.DevH10;
import com.tanso.devlib.cust.device.DevH5;
import com.tanso.devlib.cust.device.DevH6;
import com.tanso.devlib.cust.device.DevH7;
import com.tanso.devlib.cust.device.DevH8;
import com.tanso.devlib.cust.device.DevH9;
import com.tanso.guilib.media.SoundItem;
import com.tanso.guilib.media.SoundUtils;
import com.tanso.rfid.R;
import com.tanso.rfid.activity.LoggerActivity;

import com.tanso.rfid.network.PostData;
import com.tanso.rfidlib.port.PortManager;
import com.tanso.rfidlib.port.EPortType;
import com.tanso.rfidlib.rfid.RfidManager;
import com.tanso.rfidlib.rfid.base.EReaderType;

import java.util.ArrayList;

import static com.tanso.rfidlib.port.EPortType.PORT_TYPE_BLE;
import static com.tanso.rfidlib.port.EPortType.PORT_TYPE_STREAM;
import static com.tanso.rfidlib.port.EPortType.PORT_TYPE_UART;
import static com.tanso.rfidlib.rfid.base.EReaderType.READER_TYPE_J2000;
import static com.tanso.rfidlib.rfid.base.EReaderType.READER_TYPE_M100;
import static com.tanso.rfidlib.rfid.base.EReaderType.READER_TYPE_R2000;

/**
 * 应用类
 *
 * @author Aleck
 * @version 1.00
 * <p>
 * 改进：
 * <p>
 * 1. 搜索算法 (前缀：^(....),包含：(....),尾缀：(.....)^)
 * 2. 页面刷新 (刷新太频繁,数据多,很卡)
 * 3. 及时上传（列表/最新）(自行设置时间 1-ms)
 */
public class RfidApp extends Application {

    /**
     * 调试
     */
    private static final boolean DEBUG = Global.DEBUG;
    //  目标
    private static final String TAG = "RfidApp";

    //  推送类型
    public final static int POST_DATA_IN   = 0;
    public final static int POST_DATA_OUT  = 1;
    public final static int POST_DATA_SYNC = 2;
    public final static int POST_DATA_ITEM = 3;
    public final static int POST_DATA_MAX  = 4;

    //==============================================================================================
    //  不同类型机器
    //==============================================================================================
    //  蓝牙手持
    public static final int USE_DEVICE_H1  = 1001;
    public static final int USE_DEVICE_H2  = 1002;
    public static final int USE_DEVICE_H3  = 1003;
    //  安卓手持
    public static final int USE_DEVICE_H5  = 1005;
    public static final int USE_DEVICE_H6  = 1006;
    public static final int USE_DEVICE_H7  = 1007;
    public static final int USE_DEVICE_H8  = 1008;
    public static final int USE_DEVICE_H9  = 1009;
    public static final int USE_DEVICE_H10 = 1010;
    //  固定式
    public static final int USE_DEVICE_R1  = 1021;
    public static final int USE_DEVICE_R4  = 1024;
    public static final int USE_DEVICE_R8  = 1028;
    public static final int USE_DEVICE_R16 = 1026;
    //  多天线
    public static final int USE_DEVICE_J1  = 1031;
    public static final int USE_DEVICE_J4  = 1034;
    public static final int USE_DEVICE_J8  = 1038;
    public static final int USE_DEVICE_J16 = 1036;
    //  多天线
    public static final int USE_DEVICE_G1  = 1041;
    public static final int USE_DEVICE_G4  = 1044;
    public static final int USE_DEVICE_G8  = 1048;
    public static final int USE_DEVICE_G16 = 1046;
    //  通道门
    public static final int USE_DEVICE_D1  = 1100;
    public static final int USE_DEVICE_D2  = 1200;
    //  教学机
    public static final int USE_DEVICE_T1  = 1300;

    /*
     * 设备
     */
    public static final int USE_DEVICE_CURRENT = USE_DEVICE_H1;
    //public static final int USE_DEVICE_CURRENT = USE_DEVICE_H2;
    //public static final int USE_DEVICE_CURRENT = USE_DEVICE_H3;
    //
    //public static final int USE_DEVICE_CURRENT = USE_DEVICE_H5;
    //public static final int USE_DEVICE_CURRENT = USE_DEVICE_H6;
    //public static final int USE_DEVICE_CURRENT = USE_DEVICE_H7;
    //public static final int USE_DEVICE_CURRENT = USE_DEVICE_H8;
    //public static final int USE_DEVICE_CURRENT = USE_DEVICE_H9;
    //public static final int USE_DEVICE_CURRENT = USE_DEVICE_H10;

    //public static final int USE_DEVICE_CURRENT = USE_DEVICE_R4;
    //public static final int USE_DEVICE_CURRENT = USE_DEVICE_R8;
    //public static final int USE_DEVICE_CURRENT = USE_DEVICE_R16;

    //public static final int USE_DEVICE_CURRENT = USE_DEVICE_G4;
    //public static final int USE_DEVICE_CURRENT = USE_DEVICE_G8;
    //public static final int USE_DEVICE_CURRENT = USE_DEVICE_G16;

    //public static final int USE_DEVICE_CURRENT = USE_DEVICE_J4;
    //public static final int USE_DEVICE_CURRENT = USE_DEVICE_J8;
    //public static final int USE_DEVICE_CURRENT = USE_DEVICE_J16;

    //  支持 - 导出列表
    public static final boolean USE_EXPORT_LIST_SUPPORT     = true;
    //  支持 - 数据推送
    public static final boolean USE_POST_DATA_SUPPORT       = true;
    //  支持 - 内容比较
    public static final boolean USE_COMPARE_LIST_SUPPORT    = false;
    //  支持 - 写标签
    public static final boolean USE_TAG_WRITE_SUPPORT       = true;
    //  支持 - 批量写标签
    public static final boolean USE_BAT_WRITE_SUPPORT       = false;
    //  支持 - 丢失报警
    public static final boolean USE_LOST_CHECK_SUPPORT      = false;
    //	支持 - 分类统计 - 树结构
    public static final boolean USE_TREE_LIST_SUPPORT       = false;
    //  支持 - 排序
    public static final boolean USE_SORT_LIST_SUPPORT       = true;
    //  支持 - 过滤
    public static final boolean USE_FILTER_LIST_SUPPORT     = false;
    //  支持 - 高级查询
    public static final boolean USE_ADVANCED_SEARCH_SUPPORT = true;
    //  支持 - 自动同步
    public static final boolean USE_AUTO_SYNC_SUPPORT       = false;
    //  支持 - 低速刷新
    public static final boolean USE_UPDATE_SLOW_SUPPORT     = false;
    //  支持 - 定位测试
    public static final boolean USE_SYSTEM_LOCATION_TEST    = false;

    /**
     * 接口 - 管理器
     */
    public PortManager portManager;

    /**
     * 标签 - 管理器
     */
    public RfidManager rfidManager;

    /**
     * 当前设备
     */
    public DeviceBase curDevice;
    /**
     * 初始化 - 标志
     */
    public boolean inited = false;

    /**
     * 推送数据
     */
    public ArrayList<PostData> listPost = new ArrayList<>();

    /**
     * 按键音效
     */
    public final static int EFFECT_KEY = 0;

    /**
     * 扫描音效
     */
    public final static int EFFECT_RFID = 1;

    /**
     * 效果发生器
     */
    public SoundUtils soundUtils = new SoundUtils();

    /**
     * 获取 - 配置版本
     *
     * @return : 文本
     */
    public String getVersion() {
        String s = "";
        if (rfidManager != null) {
            s += rfidManager.getReader().getName();
        } else {
            s += "NULL";
        }
        s += "-";
        if (portManager != null) {
            s += portManager.getPort().getName();
        } else {
            s += "NULL";
        }
        return s;
    }

    /**
     * 初始化 - 应用
     */
    public RfidApp() {
        super();

        /*
         * 指令集
         */
        EReaderType USE_RFID_READER;

        /*
         * 接口
         */
        EPortType USE_RFID_PORT;

        /*
         * 通道数(1/2/4/8/16/24/32)
         */
        int USE_RFID_CHANNELS = 1;

        //  判断设备
        switch (USE_DEVICE_CURRENT) {
            case USE_DEVICE_H1:
            case USE_DEVICE_H2:
                USE_RFID_PORT = PORT_TYPE_BLE;
                USE_RFID_READER = READER_TYPE_M100;
                break;

            case USE_DEVICE_H3:
                USE_RFID_PORT = PORT_TYPE_BLE;
                USE_RFID_READER = READER_TYPE_R2000;
                break;

            case USE_DEVICE_H5:
                USE_RFID_PORT = PORT_TYPE_STREAM;
                USE_RFID_READER = READER_TYPE_M100;
                curDevice = new DevH5(this);
                break;

            case USE_DEVICE_H6:
                USE_RFID_PORT = PORT_TYPE_UART;
                USE_RFID_READER = READER_TYPE_R2000;
                curDevice = new DevH6(this);
                break;

            case USE_DEVICE_H7:
                USE_RFID_PORT = PORT_TYPE_UART;
                USE_RFID_READER = READER_TYPE_R2000;
                curDevice = new DevH7(this);
                break;

            case USE_DEVICE_H8:
                USE_RFID_PORT = PORT_TYPE_UART;
                USE_RFID_READER = READER_TYPE_R2000;
                curDevice = new DevH8(this);
                break;

            case USE_DEVICE_H9:
                USE_RFID_PORT = PORT_TYPE_UART;
                USE_RFID_READER = READER_TYPE_R2000;
                curDevice = new DevH9(this);
                break;

            case USE_DEVICE_H10:
                USE_RFID_PORT = PORT_TYPE_UART;
                USE_RFID_READER = READER_TYPE_R2000;
                curDevice = new DevH10(this);
                break;

            case USE_DEVICE_R1:
                USE_RFID_PORT = PORT_TYPE_UART;
                USE_RFID_READER = READER_TYPE_R2000;
                break;

            case USE_DEVICE_R4:
                USE_RFID_PORT = PORT_TYPE_UART;
                USE_RFID_READER = READER_TYPE_R2000;
                USE_RFID_CHANNELS = 4;
                break;

            case USE_DEVICE_R8:
                USE_RFID_PORT = PORT_TYPE_UART;
                USE_RFID_READER = READER_TYPE_R2000;
                USE_RFID_CHANNELS = 8;
                break;

            case USE_DEVICE_R16:
                USE_RFID_PORT = PORT_TYPE_UART;
                USE_RFID_READER = READER_TYPE_R2000;
                USE_RFID_CHANNELS = 16;
                break;

            case USE_DEVICE_J1:
                USE_RFID_PORT = PORT_TYPE_UART;
                USE_RFID_READER = READER_TYPE_J2000;
                USE_RFID_CHANNELS = 1;
                break;

            case USE_DEVICE_J4:
                USE_RFID_PORT = PORT_TYPE_UART;
                USE_RFID_READER = READER_TYPE_J2000;
                USE_RFID_CHANNELS = 4;
                break;

            case USE_DEVICE_J8:
                USE_RFID_PORT = PORT_TYPE_UART;
                USE_RFID_READER = READER_TYPE_J2000;
                USE_RFID_CHANNELS = 8;
                break;

            case USE_DEVICE_J16:
                USE_RFID_PORT = PORT_TYPE_UART;
                USE_RFID_READER = READER_TYPE_J2000;
                USE_RFID_CHANNELS = 16;
                break;

            case USE_DEVICE_D1:
            case USE_DEVICE_D2:
                USE_RFID_READER = READER_TYPE_R2000;
                USE_RFID_PORT = PORT_TYPE_UART;
                USE_RFID_CHANNELS = 4;
                break;

            case USE_DEVICE_T1:
                USE_RFID_PORT = PORT_TYPE_UART;
                USE_RFID_READER = READER_TYPE_M100;
                USE_RFID_CHANNELS = 1;
                break;

            default:
                USE_RFID_PORT = PORT_TYPE_BLE;
                USE_RFID_READER = READER_TYPE_M100;
                USE_RFID_CHANNELS = 1;
                break;
        }

//        if (DEBUG) {
//            System.err.println("读写器:" + USE_RFID_READER);
//            System.err.println("端  口:" + USE_RFID_PORT);
//            System.err.println("通  道:" + USE_RFID_CHANNELS);
//        }

        //  标签管理器
        rfidManager = new RfidManager(this, USE_RFID_READER, USE_RFID_CHANNELS);

        //  接口管理器
        portManager = new PortManager(this, USE_RFID_PORT);
        //  推送数据
        if (USE_POST_DATA_SUPPORT) {
            for (int i = 0; i < POST_DATA_MAX; i++) {
                listPost.add(new PostData(this));
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (DEBUG) {
            System.err.println("系统 - 初始化!\n");
        }

        //  是否初始化?
        if (!rfidManager.isInit(this, false)) {
            if (DEBUG) {
                System.err.println("系统 - 初始化!\n");
            }
            //  端口设置
            portManager.setupUpdate(true);
            //  读写器设置
            rfidManager.setupUpdate(true);
            //  已经初始化
            rfidManager.isInit(this, true);
        } else {
            if (DEBUG) {
                System.err.println("系统 - 已经初始化!\n");
            }
        }

        //  推送数据
        if (USE_POST_DATA_SUPPORT) {
            for (int i = 0; i < POST_DATA_MAX; i++) {
                listPost.get(i).setupUpdate(false, "post" + i);
            }
        }

        //  加载 - 端口设置
        portManager.setupUpdate(false);
        //  加载 - 读写器设置
        rfidManager.setupUpdate(false);
        //  关联 - 接口
        rfidManager.setPort(portManager.getPort());

        if (DEBUG) {
            System.err.println("端口:" + portManager.getPort());
        }
        //  清空 - 数据
        rfidManager.clear();

        //  音效
        soundUtils.clear();
        //  按键效果
        soundUtils.add(new SoundItem(this, R.raw.key_sound, new long[]{30, 10}));
        //  读卡效果
        soundUtils.add(new SoundItem(this, R.raw.rfid_notify, new long[]{50, 10}));
        //
        //soundUtils.add(new SoundItem(this, R.raw.beeper_short, new long[]{50, 10}));

        //  读取 - 音效设置
        soundUtils.setupUpdate(this, false);
    }

    /**
     * 获取数据
     *
     * @return ：同步数据
     */
    public PostData getPostSync() {
        if (listPost != null) {
            if (POST_DATA_SYNC < listPost.size()) {
                return listPost.get(POST_DATA_SYNC);
            }
        }
        return null;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        //  推送数据
        if (USE_POST_DATA_SUPPORT) {
            for (int i = 0; i < POST_DATA_MAX; i++) {
                listPost.get(i).setupUpdate(true, "post" + i);
            }
        }

        //  保存 - 接口设置
        portManager.setupUpdate(true);
        //  保存 - 读写器设置
        rfidManager.setupUpdate(true);
        //  断开 (退出如果不断开蓝牙!下次进来会联机失败!)
        portManager.getPort().disconnect();
        //  保存 - 音效设置
        soundUtils.setupUpdate(this, true);
    }

    /**
     * 发送数据
     *
     * @param array : 数据
     */
    public void portWrite(byte[] array) {

        if (array == null) {
            if (DEBUG) {
                LoggerActivity.log_e(TAG, "array is (null)");
            }
            return;
        }

        if (array.length == 0) {
            if (DEBUG) {
                LoggerActivity.log_e(TAG, "array.length == 0");
            }
            return;
        }

        rfidManager.putCmd(array);
    }

    /**
     * 输出命令,等待返回
     *
     * @param array : 数据
     * @param wait  : 等待
     */
    public void portWrite(byte[] array, boolean wait) {

        if (array == null) {
            if (DEBUG) {
                LoggerActivity.log_e(TAG, "array is (null)");
            }
            return;
        }

        if (array.length == 0) {
            if (DEBUG) {
                LoggerActivity.log_e(TAG, "array.length == 0");
            }
            return;
        }

        rfidManager.putCmd(array, wait);
    }

    public void portWrite(byte[] array, int wait) {

        if (array == null) {
            if (DEBUG) {
                LoggerActivity.log_e(TAG, "array is (null)");
            }
            return;
        }

        if (array.length == 0) {
            if (DEBUG) {
                LoggerActivity.log_e(TAG, "array.length == 0");
            }
            return;
        }

        rfidManager.putCmd(array, wait);
    }

    /**
     * 获取电量
     *
     * @param activity : 界面
     * @return : 电量
     */
    public static int getBatteryVoltage(Activity activity) {
        int level = 0;
        if (activity != null) {
            RfidApp app = (RfidApp) activity.getApplication();
            if (app != null) {
                level = app.rfidManager.getReader().battery;
            }
        }
        return level;
    }

    /**
     * 电池百分比
     *
     * @param activity : 界面
     * @return : 百分比
     */
    public static int getBatteryPercent(Activity activity) {
        final int VOL_MIN = 160;
        final int VOL_MAX = 210;

        int level = 0;
        if (activity != null) {
            RfidApp app = (RfidApp) activity.getApplication();
            if (app != null) {
                level = app.rfidManager.getReader().battery;
                if (level > 0) {

                    if (level < VOL_MIN) {
                        level = 1;
                    } else if (level > VOL_MAX) {
                        level = 100;
                    } else {
                        level = (level - VOL_MIN) * 100 / (VOL_MAX - VOL_MIN);
                    }
                }
            }
        }
        return level;
    }

    /**
     * 音效
     *
     * @param activity : 界面
     * @param index    : 序号
     */
    public static void doEffect(Activity activity, int index) {
        RfidApp app = (RfidApp) activity.getApplication();
        app.soundUtils.effect(index);
    }
}
