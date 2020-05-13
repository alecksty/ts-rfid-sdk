
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
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.tanso.guilib.dialog.DlgMake;
import com.tanso.guilib.dialog.IDlgEvent;
import com.tanso.guilib.dialog.IDlgUser;
import com.tanso.guilib.msgbox.EMessageType;
import com.tanso.guilib.msgbox.MsgMake;
import com.tanso.guilib.setup.ISetupEven;
import com.tanso.guilib.setup.XSetupItem;
import com.tanso.guilib.setup.XSetupManager;
import com.tanso.rfid.R;
import com.tanso.rfid.network.PostData;
import com.tanso.rfid.user.Global;
import com.tanso.rfid.user.RfidApp;
import com.tanso.rfidlib.bytes.ByteStream;
import com.tanso.rfidlib.port.EPortEven;
import com.tanso.rfidlib.port.EPortType;
import com.tanso.rfidlib.port.IPortEvent;
import com.tanso.rfidlib.port.PortManager;
import com.tanso.rfidlib.port.ble.ManagerBLE;
import com.tanso.rfidlib.port.ble.PortBLE;
import com.tanso.rfidlib.port.tcp.ManagerTCP;
import com.tanso.rfidlib.port.tcp.PortTCP;
import com.tanso.rfidlib.port.uart.PortUART;
import com.tanso.rfidlib.port.udp.ManagerUDP;
import com.tanso.rfidlib.port.udp.PortUDP;
import com.tanso.rfidlib.port.usb.PortUSB;
import com.tanso.rfidlib.rfid.IConfigEvent;
import com.tanso.rfidlib.rfid.RfidConfig;
import com.tanso.rfidlib.rfid.RfidManager;
import com.tanso.rfidlib.rfid.TagItem;
import com.tanso.rfidlib.rfid.base.BaseReader;
import com.tanso.rfidlib.rfid.base.BaseSet;
import com.tanso.rfidlib.rfid.base.EReaderType;
import com.tanso.rfidlib.rfid.m100.RfidM100Cmd;
import com.tanso.rfidlib.rfid.m100.RfidM100Reader;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

import static com.tanso.rfid.user.RfidApp.EFFECT_KEY;
import static com.tanso.rfid.user.RfidApp.EFFECT_RFID;
import static com.tanso.rfid.user.RfidApp.POST_DATA_IN;
import static com.tanso.rfid.user.RfidApp.POST_DATA_ITEM;
import static com.tanso.rfid.user.RfidApp.POST_DATA_OUT;
import static com.tanso.rfid.user.RfidApp.POST_DATA_SYNC;
import static com.tanso.rfid.user.RfidApp.USE_AUTO_SYNC_SUPPORT;
import static com.tanso.rfid.user.RfidApp.USE_POST_DATA_SUPPORT;
import static com.tanso.rfid.user.RfidApp.USE_SORT_LIST_SUPPORT;
import static com.tanso.rfid.user.RfidApp.USE_TREE_LIST_SUPPORT;
import static com.tanso.rfidlib.comm.StringUtils.IpFromString;
import static com.tanso.rfidlib.comm.StringUtils.IpToString;
import static com.tanso.rfidlib.comm.StringUtils.PortToString;
import static com.tanso.rfidlib.comm.StringUtils.makeFixData;

/**
 * 设置代码
 */
public class SetupActivity extends AppCompatActivity implements ISetupEven, View.OnClickListener, IDlgEvent, IDlgUser, IPortEvent, IConfigEvent {

    //  调试
    private static final boolean DEBUG    = Global.DEBUG;
    //  目标
    private static final String  TAG      = "SetupActivity";
    //  显示消息
    private static final boolean SHOW_MSG = true;

    //==============================================================================================
    //
    //==============================================================================================
    private static final String DEF_HOST_POST_WEBSITE = "http://website.asp";
    private static final String DEF_HOST_POST_IN      = DEF_HOST_POST_WEBSITE;
    private static final String DEF_HOST_POST_OUT     = DEF_HOST_POST_WEBSITE;
    private static final String DEF_HOST_POST_ITEM    = DEF_HOST_POST_WEBSITE;
    //private static final String DEF_HOST_POST_ITEM    = "http://211.149.250.246:8090/queryData.aspx";
    private static final String DEF_HOST_POST_SYNC    = DEF_HOST_POST_WEBSITE;

    //  延时
    private static final int MESSAGE_DELAY_CNT                 = 1000;
    //  对话框
    private static final int ID_DLG_PROGRESS                   = 10000;
    //
    //==============================================================================================
    //  设置 - 标识
    //==============================================================================================
    //  串口
    private static final int XITEM_USART                       = 1000;
    private static final int XITEM_USART_DEVICES               = 1001;
    private static final int XITEM_USART_BAUD                  = 1002;
    private static final int XITEM_USART_OPEN                  = 1003;
    //  蓝牙
    private static final int XITEM_BLE                         = 1100;
    //  网络
    private static final int XITEM_NETWORK                     = 1200;
    private static final int XITEM_NETWORK_TYPE                = 1201;
    private static final int XITEM_NETWORK_HOST                = 1202;
    private static final int XITEM_NETWORK_PORT                = 1203;
    private static final int XITEM_NETWORK_CONNECT             = 1204;
    //  通用串行总线
    private static final int XITEM_USB                         = 1300;
    private static final int XITEM_USB_DEVICES                 = 1301;
    private static final int XITEM_USB_OPEN                    = 1302;
    //  指令集
    private static final int XITEM_INSTRUCTION                 = 1400;
    //  扫描模式
    private static final int XITEM_SCAN_MODE                   = 1500;
    private static final int XITEM_SESSION_ONOFF               = 1510;
    private static final int XITEM_SESSION_MODE                = 1520;
    private static final int XITEM_SESSION_TARGET              = 1530;
    private static final int XITEM_SESSION_Q_VALUE             = 1550;
    private static final int XITEM_SESSION_SCAN_TIME           = 1560;
    private static final int XITEM_SESSION_INVERT_EN           = 1570;
    private static final int XITEM_SESSION_INVERT_NUM          = 1580;
    //	天线切换次数
    private static final int XITEM_SWITCH_TIMES                = 1600;
    //  通道
    private static final int XITEM_CHANNELS                    = 2000;
    //  天线
    private static final int XITEM_ANTENNA                     = 2100;
    //  设备
    private static final int XITEM_CONFIG                      = 3000;
    private static final int XITEM_CONFIG_POWER_SAVE           = 3010;
    private static final int XITEM_CONFIG_MODE                 = 3020;
    private static final int XITEM_CONFIG_SSID                 = 3030;
    private static final int XITEM_CONFIG_PWD                  = 3040;
    private static final int XITEM_CONFIG_HOST                 = 3050;
    private static final int XITEM_CONFIG_PORT                 = 3060;
    private static final int XITEM_CONFIG_READ                 = 3070;
    private static final int XITEM_CONFIG_WRITE                = 3080;
    //  区域
    private static final int XITEM_AREA                        = 3100;
    private static final int XITEM_FREQ                        = 3200;
    private static final int XITEM_SKIP                        = 3300;
    //  输出功率
    private static final int XITEM_TX_POWER                    = 3400;
    //  效果
    private static final int XITEM_EFFECT                      = 4000;
    //  按键效果
    private static final int XITEM_EFFECT_KEY_SHAKE            = 4100;
    private static final int XITEM_EFFECT_KEY_SOUND            = 4110;
    private static final int XITEM_EFFECT_KEY_VOLUME           = 4120;
    private static final int XITEM_EFFECT_KEY_SOURCE           = 4130;
    //  扫描效果
    private static final int XITEM_EFFECT_RFID_SHAKE           = 4150;
    private static final int XITEM_EFFECT_RFID_SOUND           = 4160;
    private static final int XITEM_EFFECT_RFID_VOLUME          = 4170;
    private static final int XITEM_EFFECT_RFID_SOURCE          = 4180;
    //
    private static final int XITEM_TIMES                       = 4200;
    private static final int XITEM_INVENTRY                    = 4210;
    private static final int XITEM_INTERVAL                    = 4220;
    //  推送/导出
    private static final int XITEM_POST                        = 4300;
    //  入库
    private static final int XITEM_POST_IN_URL                 = 4311;
    private static final int XITEM_POST_IN_USR                 = 4312;
    private static final int XITEM_POST_IN_PWD                 = 4313;
    //  出库
    private static final int XITEM_POST_OUT_URL                = 4321;
    private static final int XITEM_POST_OUT_USR                = 4322;
    private static final int XITEM_POST_OUT_PWD                = 4323;
    //  同步
    private static final int XITEM_POST_SYN_URL                = 4331;
    private static final int XITEM_POST_SYN_USR                = 4332;
    private static final int XITEM_POST_SYN_PWD                = 4333;
    //  详情
    private static final int XITEM_POST_ITEM_URL               = 4341;
    private static final int XITEM_POST_ITEM_USR               = 4342;
    private static final int XITEM_POST_ITEM_PWD               = 4343;
    private static final int XITEM_POST_ITEM_ON                = 4344;
    //
    private static final int XITEM_POST_WITH_GPS               = 4400;
    private static final int XITEM_POST_AUTO                   = 4410;
    private static final int XITEM_POST_DELAY                  = 4420;
    private static final int XITEM_POST_ID                     = 4430;
    //  导出
    private static final int XITEM_POST_EXPORT_FILE_IDX        = 4440;
    private static final int XITEM_POST_EXPORT_FILE_PC         = 4441;
    private static final int XITEM_POST_EXPORT_FILE_EPC        = 4442;
    private static final int XITEM_POST_EXPORT_FILE_TID        = 4443;
    private static final int XITEM_POST_EXPORT_FILE_USR        = 4444;
    private static final int XITEM_POST_EXPORT_FILE_CRC        = 4445;
    private static final int XITEM_POST_EXPORT_FILE_ANT        = 4446;
    private static final int XITEM_POST_EXPORT_FILE_RSSI       = 4447;
    private static final int XITEM_POST_EXPORT_FILE_CNT        = 4448;
    private static final int XITEM_POST_EXPORT_FILE_TIME       = 4449;
    private static final int XITEM_POST_EXPORT_TEXT_PREFIX     = 4450;
    private static final int XITEM_POST_EXPORT_TEXT_ENDFIX     = 4451;
    //  对比 - 数据对比
    private static final int XITEM_COMPARE                     = 4500;
    //  对比 - 初始化
    private static final int XITEM_COMPARE_INIT                = 4600;
    //  对比 - 报错提示
    private static final int XITEM_COMPARE_ERROR               = 4700;
    //  对比 - 成功提示
    private static final int XITEM_COMPARE_SUCCESS             = 4800;
    //  对比 - 列表
    private static final int XITEM_COMPARE_LIST                = 4900;
    //  排序
    private static final int XITEM_SORT                        = 5000;
    //  方向
    private static final int XITEM_SORT_ORDER                  = 5100;
    //  字段
    private static final int XITEM_SORT_FILED                  = 5200;
    //  过滤
    private static final int XITEM_FILTER                      = 5300;
    //  信号过滤
    private static final int XITEM_FILTER_RSSI                 = 5400;
    //  次数过滤
    private static final int XITEM_FILTER_COUNT                = 5500;
    //  包含过滤
    private static final int XITEM_FILTER_CONTAIN              = 5600;
    //  树结构
    private static final int XITEM_TREE                        = 5700;
    //  根节点名
    private static final int XITEM_TREE_NAME                   = 5800;
    //  分割符号
    private static final int XITEM_TREE_SPLITE                 = 5900;
    //  包含分隔符
    private static final int XITEM_TREE_CONTAIN                = 6000;
    //  调试
    private static final int XITEM_DEBUG                       = 6100;
    //  实验室
    private static final int XITEM_SYSTEM                      = 6200;
    private static final int XITEM_SYSTEM_TEST_SDK             = 6210;
    private static final int XITEM_SYSTEM_TEST_TAG1            = 6220;
    private static final int XITEM_SYSTEM_TEST_TAG2            = 6230;
    private static final int XITEM_SYSTEM_TEST_TAG3            = 6240;
    private static final int XITEM_SYSTEM_TEST_TAG4            = 6250;
    private static final int XITEM_SYSTEM_TEST_TAG5            = 6260;
    private static final int XITEM_SYSTEM_TEST_TAG6            = 6270;
    private static final int XITEM_SYSTEM_TEST_TAG7            = 6280;
    private static final int XITEM_SYSTEM_TEST_TAG8            = 6290;
    private static final int XITEM_SYSTEM_TEST_TAG9            = 6300;
    //  对话框测试
    private static final int XITEM_SYSTEM_TEST_SDK_DLG1        = 6300;
    private static final int XITEM_SYSTEM_TEST_SDK_DLG2        = 6310;
    private static final int XITEM_SYSTEM_TEST_SDK_DLG3        = 6320;
    private static final int XITEM_SYSTEM_TEST_SDK_DLG_LIST    = 6330;
    private static final int XITEM_SYSTEM_TEST_SDK_DLG_CHKS    = 6340;
    private static final int XITEM_SYSTEM_TEST_SDK_DLG_EDIT    = 6350;
    private static final int XITEM_SYSTEM_TEST_SDK_DLG_USER    = 6351;
    private static final int XITEM_SYSTEM_TEST_SDK_DLG_PROG    = 6352;
    //  消息框测试
    private static final int XITEM_SYSTEM_TEST_SDK_MSG_INFO    = 6400;
    private static final int XITEM_SYSTEM_TEST_SDK_MSG_WARING  = 6410;
    private static final int XITEM_SYSTEM_TEST_SDK_MSG_ERROR   = 6420;
    private static final int XITEM_SYSTEM_TEST_SDK_MSG_SUCCESS = 6430;
    //  显示顺序
    private static final int XITEM_DISPLAY_ORDER               = 6500;
    //  显示文本
    private static final int XITEM_DISPLAY_TEXT                = 6600;
    //  显示数量
    private static final int XITEM_DISPLAY_REFRESH_COUNT       = 8200;
    //  显示延时
    private static final int XITEM_DISPLAY_REFRESH_DELAY       = 8300;
    //  关于
    private static final int XITEM_ABOUT                       = 9000;
    //

    //==============================================================================================
    //  列表
    //==============================================================================================
    /**
     * 当前列表
     */
    private ArrayList<XSetupItem> list        = new ArrayList<>();
    /**
     * 串口界面
     */
    private ArrayList<XSetupItem> listUart    = new ArrayList<>();
    /**
     * USB界面
     */
    private ArrayList<XSetupItem> listUsb     = new ArrayList<>();
    /**
     * 网络界面
     */
    private ArrayList<XSetupItem> listNetwork = new ArrayList<>();
    /**
     * 系统测试
     */
    private ArrayList<XSetupItem> listSystem  = new ArrayList<>();
    /**
     * 导出菜单
     */
    private ArrayList<XSetupItem> listPost    = new ArrayList<>();

    /**
     * 设置 - 管理器
     */
    private XSetupManager manager;

    /**
     * 设置序号
     */
    private int curItemId = 0;

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_setup);

        //  返回
        ImageView imgReturn = this.findViewById(R.id.image_return);
        if (imgReturn != null) {
            imgReturn.setOnClickListener(this);
        }

        //  标题
        TextView textTitle = this.findViewById(R.id.text_setup_title);
        if (textTitle != null) {
            textTitle.setText(R.string.str_setup);
        }

        //  动作
        Intent intent = getIntent();
        //  当前设置ID
        curItemId = intent.getIntExtra(XSetupManager.XSETUP_INTENT_ID, 0);

        //  获取全局菜单
        if ((XSetupManager.getInstance() != null) && (curItemId != 0)) {
            //  当前设置名称
            XSetupItem item = XSetupManager.getInstance().findSetupItem(curItemId);
            //  当前设置ID
            if (item != null) {
                //  设置标题
                if (textTitle != null) {
                    //  当前名称
                    textTitle.setText(item.name);
                }
            }
        }

        //  加载菜单
        initRootMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();

        RfidApp theApp = (RfidApp) this.getApplication();
        //  拦截端口消息
        theApp.portManager.setPortEvent(this);
        //  加载菜单 - 刷新
        initRootMenu();
    }

    /**
     * 生成界面
     *
     * @param cls : 类
     * @return : 请求
     */
    private Intent makeIntent(Class<?> cls) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(this, cls);
        return intent;
    }

    /**
     * 初始化 - 串口
     */
    private void initUartMenu() {
        RfidApp  theApp   = (RfidApp) this.getApplication();
        PortUART portUart = (PortUART) theApp.portManager.getPort(EPortType.PORT_TYPE_UART);
        //  获取列表
        String[] devices  = portUart.getDevices();
        String[] strEmpty = {getString(R.string.str_none)};

        //  没有设备,或者报错!
        if (devices == null) {
            devices = strEmpty;
        } else if (devices.length == 0) {
            devices = strEmpty;
        }

        listUart.clear();
        listUart.add(new XSetupItem(getString(R.string.str_setup_uart)));
        listUart.add(new XSetupItem(XITEM_USART_DEVICES, getString(R.string.str_setup_port), portUart.getPort(), devices));
        listUart.add(new XSetupItem(XITEM_USART_BAUD, getString(R.string.str_setup_port_uart_baud), 0, new int[]{115200, 57600, 38400, 19200, 9600}, "bps"));
        listUart.add(new XSetupItem(XITEM_USART_OPEN, (portUart.isConnected()) ? getString(R.string.str_close) : getString(R.string.str_open)));
    }

    /**
     * 初始化 - 网络
     */
    private void initNetworkMenu() {
        RfidApp     theApp      = (RfidApp) this.getApplication();
        PortManager port        = theApp.portManager;
        ManagerUDP  udpManager  = port.getUdpManager();
        ManagerTCP  tcpManager  = port.getTcpManager();
        boolean     isConnected = false;

        int pos;
        final String[] array_type = {
                "UDP",
                "TCP",
                };

        //  分配列表
        listNetwork.clear();
        listNetwork.add(new XSetupItem(getString(R.string.str_setup_network)));

        if (port.getType() == EPortType.PORT_TYPE_UDP) {
            pos = 0;
        } else {
            pos = 1;
        }

        //  类型
        listNetwork.add(new XSetupItem(XITEM_NETWORK_TYPE, getString(R.string.str_setup_net_type), pos, array_type));

        //  有效类型
        if (port.getType() == EPortType.PORT_TYPE_UDP) {
            listNetwork.add(new XSetupItem(XITEM_NETWORK_HOST, getString(R.string.str_setup_host), udpManager.getHost(), "192.168.1.100"));
            listNetwork.add(new XSetupItem(XITEM_NETWORK_PORT, getString(R.string.str_setup_port), "" + udpManager.getPort(), "4000"));
        } else {
            listNetwork.add(new XSetupItem(XITEM_NETWORK_HOST, getString(R.string.str_setup_host), tcpManager.getHost(), "192.168.1.100"));
            listNetwork.add(new XSetupItem(XITEM_NETWORK_PORT, getString(R.string.str_setup_port), "" + tcpManager.getPort(), "4000"));
        }

        //  是否连接
        if (port.getType() == EPortType.PORT_TYPE_UDP || port.getType() == EPortType.PORT_TYPE_TCP) {
            if (port.getPort().isConnected()) {
                isConnected = true;
            }
        }

        if (!isConnected) {
            listNetwork.add(new XSetupItem(XITEM_NETWORK_CONNECT, getString(R.string.str_connect)));
        } else {
            listNetwork.add(new XSetupItem(XITEM_NETWORK_CONNECT, getString(R.string.str_disconnect)));
        }
    }

    /**
     * 初始化 - USB
     */
    private void initUsbMenu() {
        RfidApp  theApp   = (RfidApp) this.getApplication();
        PortUSB  portUSB  = (PortUSB) theApp.portManager.getPort(EPortType.PORT_TYPE_USB);
        String[] devices  = portUSB.getDevices();
        String[] strEmpty = {getString(R.string.str_none)};

        if (devices == null) {
            devices = strEmpty;
        } else if (devices.length == 0) {
            devices = strEmpty;
        }

        listUsb.clear();
        listUsb.add(new XSetupItem(getString(R.string.str_setup_usb)));
        listUsb.add(new XSetupItem(XITEM_USB_DEVICES, getString(R.string.str_setup_device), portUSB.getPort(), devices));
        listUsb.add(new XSetupItem(XITEM_USB_OPEN, (portUSB.isConnected()) ? getString(R.string.str_close) : getString(R.string.str_open)));
    }

    /**
     * 初始化 - 系统
     */
    private void initSystemMenu() {
        //  分配列表
        listSystem.clear();
        listSystem.add(new XSetupItem(getString(R.string.str_setup_sdk)));
        listSystem.add(new XSetupItem(XITEM_SYSTEM_TEST_SDK, getString(R.string.str_setup_sdk_dialog), new XSetupItem[]{
                new XSetupItem(getString(R.string.str_setup_sdk_dialog)),
                new XSetupItem(XITEM_SYSTEM_TEST_SDK_DLG1, getString(R.string.str_setup_sdk_dialog_one)),
                new XSetupItem(XITEM_SYSTEM_TEST_SDK_DLG2, getString(R.string.str_setup_sdk_dialog_two)),
                new XSetupItem(XITEM_SYSTEM_TEST_SDK_DLG3, getString(R.string.str_setup_sdk_dialog_more)),
                new XSetupItem(XITEM_SYSTEM_TEST_SDK_DLG_LIST, getString(R.string.str_setup_sdk_dialog_list)),
                new XSetupItem(XITEM_SYSTEM_TEST_SDK_DLG_CHKS, getString(R.string.str_setup_sdk_dialog_more)),
                new XSetupItem(XITEM_SYSTEM_TEST_SDK_DLG_EDIT, getString(R.string.str_setup_sdk_dialog_edit)),
                new XSetupItem(getString(R.string.str_setup_sdk_msgbox)),
                new XSetupItem(XITEM_SYSTEM_TEST_SDK_MSG_INFO, getString(R.string.str_setup_sdk_msgbox_info)),
                new XSetupItem(XITEM_SYSTEM_TEST_SDK_MSG_WARING, getString(R.string.str_setup_sdk_msgbox_waring)),
                new XSetupItem(XITEM_SYSTEM_TEST_SDK_MSG_ERROR, getString(R.string.str_setup_sdk_msgbox_error)),
                new XSetupItem(XITEM_SYSTEM_TEST_SDK_MSG_SUCCESS, getString(R.string.str_setup_sdk_msgbox_success)),
                }));

    }

    /**
     * 初始化 - 系统
     */
    private void initPostMenu() {

        if (USE_POST_DATA_SUPPORT) {
            //
            RfidApp theApp = (RfidApp) this.getApplication();
            //
            PostData pos;
            //  子界面
            listPost.clear();

            //  入库地址
            pos = theApp.listPost.get(POST_DATA_IN);
            listPost.add(new XSetupItem(getString(R.string.str_setup_post_in)));
            listPost.add(new XSetupItem(XITEM_POST_IN_URL, getString(R.string.str_setup_host), pos.url, DEF_HOST_POST_IN));
            listPost.add(new XSetupItem(XITEM_POST_IN_USR, getString(R.string.str_setup_user), pos.user, "user"));
            listPost.add(new XSetupItem(XITEM_POST_IN_PWD, getString(R.string.str_setup_pass), pos.pwd, "pwd"));

            //  出库地址
            pos = theApp.listPost.get(POST_DATA_OUT);
            listPost.add(new XSetupItem(getString(R.string.str_setup_post_out)));
            listPost.add(new XSetupItem(XITEM_POST_OUT_URL, getString(R.string.str_setup_host), pos.url, DEF_HOST_POST_OUT));
            listPost.add(new XSetupItem(XITEM_POST_OUT_USR, getString(R.string.str_setup_user), pos.user, "user"));
            listPost.add(new XSetupItem(XITEM_POST_OUT_PWD, getString(R.string.str_setup_pass), pos.pwd, "pwd"));

            //  详情地址
            pos = theApp.listPost.get(POST_DATA_ITEM);
            listPost.add(new XSetupItem(getString(R.string.str_setup_post_item)));
            listPost.add(new XSetupItem(XITEM_POST_ITEM_URL, getString(R.string.str_setup_host), pos.url, DEF_HOST_POST_ITEM));
            listPost.add(new XSetupItem(XITEM_POST_ITEM_USR, getString(R.string.str_setup_user), pos.user, "user"));
            listPost.add(new XSetupItem(XITEM_POST_ITEM_PWD, getString(R.string.str_setup_pass), pos.pwd, "pwd"));
            listPost.add(new XSetupItem(XITEM_POST_ITEM_ON, getString(R.string.str_item_onoff), pos.enable));

            //  同步地址
            pos = theApp.listPost.get(POST_DATA_SYNC);
            listPost.add(new XSetupItem(getString(R.string.str_setup_post_sync)));
            listPost.add(new XSetupItem(XITEM_POST_SYN_URL, getString(R.string.str_setup_host), pos.url, DEF_HOST_POST_SYNC));
            listPost.add(new XSetupItem(XITEM_POST_SYN_USR, getString(R.string.str_setup_user), pos.user, "user"));
            listPost.add(new XSetupItem(XITEM_POST_SYN_PWD, getString(R.string.str_setup_pass), pos.pwd, "pwd"));

            if (USE_AUTO_SYNC_SUPPORT) {
                //  自动提交
                listPost.add(new XSetupItem(getString(R.string.str_setup_post_auto)));
                //  标识
                listPost.add(new XSetupItem(XITEM_POST_ID, getString(R.string.str_setup_pos_id), pos.id, "10000"));
                //  提交定位
                listPost.add(new XSetupItem(XITEM_POST_WITH_GPS, getString(R.string.str_setup_with_gps), pos.gps));
                //  自动提交
                listPost.add(new XSetupItem(XITEM_POST_AUTO, getString(R.string.str_setup_post_auto), pos.auto));
                //  提交间隔 (10ms ~ 10sec)
                listPost.add(new XSetupItem(XITEM_POST_DELAY, getString(R.string.str_setup_post_delay), pos.delay, 100, 10000, 100, getString(R.string.str_delay_ms)));
            }

            listPost.add(new XSetupItem(getString(R.string.str_setup_export)));
            listPost.add(new XSetupItem(XITEM_POST_EXPORT_FILE_IDX, "IDX", RfidManager.EXPORT_FILE_IDX));
            listPost.add(new XSetupItem(XITEM_POST_EXPORT_FILE_PC, "PC", RfidManager.EXPORT_FILE_PC));
            listPost.add(new XSetupItem(XITEM_POST_EXPORT_FILE_EPC, "EPC", RfidManager.EXPORT_FILE_EPC));
            listPost.add(new XSetupItem(XITEM_POST_EXPORT_FILE_TID, "TID", RfidManager.EXPORT_FILE_TID));
            listPost.add(new XSetupItem(XITEM_POST_EXPORT_FILE_USR, "USR", RfidManager.EXPORT_FILE_USR));
            listPost.add(new XSetupItem(XITEM_POST_EXPORT_FILE_CRC, "CRC", RfidManager.EXPORT_FILE_CRC));
            listPost.add(new XSetupItem(XITEM_POST_EXPORT_FILE_ANT, "ANT", RfidManager.EXPORT_FILE_ANT));
            listPost.add(new XSetupItem(XITEM_POST_EXPORT_FILE_RSSI, "RSSI", RfidManager.EXPORT_FILE_RSSI));
            listPost.add(new XSetupItem(XITEM_POST_EXPORT_FILE_CNT, "CNT", RfidManager.EXPORT_FILE_CNT));
            listPost.add(new XSetupItem(XITEM_POST_EXPORT_FILE_TIME, "TIME", RfidManager.EXPORT_FILE_TIME));

            listPost.add(new XSetupItem(getString(R.string.str_setup_export_format)));
            listPost.add(new XSetupItem(XITEM_POST_EXPORT_TEXT_PREFIX, getString(R.string.str_setup_prefix), RfidManager.EXPORT_TEXT_PREFIX, "'"));
            listPost.add(new XSetupItem(XITEM_POST_EXPORT_TEXT_ENDFIX, getString(R.string.str_setup_endfix), RfidManager.EXPORT_TEXT_ENDFIX, ""));
            listPost.add(new XSetupItem(""));
        }
    }

    /**
     * 初始化 - 设置
     */
    private void initRootMenu() {
        //  通道
        final int[] arrayChannelValue = {1, 4, 8, 16};
        //  频率
        final int[] arrayTimesSecValue = {500, 200, 100, 50, 20, 10, 5, 2, 1, 0};
        //  区域
        final String[] arrayArea = {
                getString(R.string.str_setup_region_cn800),
                getString(R.string.str_setup_region_cn900),
                getString(R.string.str_setup_region_usa),
                getString(R.string.str_setup_region_europe),
                getString(R.string.str_setup_region_korean)
        };
        //  区域值
        final int[] arrayAreaValue = {
                1,
                4,
                2,
                3,
                6,
                };
        //  指令
        final String[] arrayInstruct = {
                "QM100   - (H1/H2/T1)",
                "R2000   - (H3/H8/R4/R8/D1)",
                "J2000   - (J1/J4/J8/J16)",
                "G2000   - (G1/G4/G8/G16)",
                "PR9200  - (H9)",
                "SLR5300 - (H5)",
                "SLR1200 - (H6/H7)",
                };

        //  排序
        final String[] arraySortOrder = {getString(R.string.str_setup_sort_a2z), getString(R.string.str_setup_sort_z2a)};

        //  字段
        final String[] arraySortBy = {
                getString(R.string.str_setup_sort_index),
                getString(R.string.str_setup_sort_epc),
                getString(R.string.str_setup_sort_signal),
                getString(R.string.str_setup_sort_time),
                getString(R.string.str_setup_sort_count)
        };

        //  会话模式
        final String[] arraySessionMode = {
                getString(R.string.str_session_mode_s0),
                getString(R.string.str_session_mode_s1),
                getString(R.string.str_session_mode_s2),
                getString(R.string.str_session_mode_s3),
                getString(R.string.str_session_mode_sl),
                };

        //  会话目标
        final String[] arraySessionTarget = {
                getString(R.string.str_session_target_a),
                getString(R.string.str_session_target_b),
                getString(R.string.str_session_target_auto),
                };

        //  蓝牙，无线网
        final String[] arrayBootMode = {
                getString(R.string.str_boot_mode_ble),
                getString(R.string.str_boot_mode_wifi),
                };

        String devBleName  = getString(R.string.str_no_connect);
        String devNetName  = getString(R.string.str_no_connect);
        String devUartName = getString(R.string.str_no_connect);
        String devUsbName  = getString(R.string.str_no_connect);

        RfidApp theApp = (RfidApp) this.getApplication();

        PortManager portMan = theApp.portManager;
        RfidManager rfidMan = theApp.rfidManager;
        BaseReader  reader  = rfidMan.getReader();

        //===============
        //  获取蓝牙名称
        //===============
        if (portMan.getPort(EPortType.PORT_TYPE_BLE) != null) {
            PortBLE         portBLE = (PortBLE) portMan.getPort(EPortType.PORT_TYPE_BLE);
            ManagerBLE      manager = portBLE.getManager();
            BluetoothDevice dev     = manager.getDevice();
            if (portMan.getType() == EPortType.PORT_TYPE_BLE) {
                if (manager.isConnected()) {
                    if (dev != null) {
                        devBleName = dev.getName();
                    }
                }
            }
        }

        //===============
        //  获取网络UDP名称
        //===============
        if (portMan.getPort(EPortType.PORT_TYPE_UDP) != null) {
            PortUDP    portUdp = (PortUDP) portMan.getPort(EPortType.PORT_TYPE_UDP);
            ManagerUDP manager = portUdp.getManager();
            if (portMan.getType() == EPortType.PORT_TYPE_UDP) {
                if (portUdp.isConnected()) {
                    devNetName = manager.getHost();
                }
            }
        }

        //===============
        //  获取网络TCP名称
        //===============
        if (portMan.getPort(EPortType.PORT_TYPE_TCP) != null) {
            PortTCP    portTcp = (PortTCP) portMan.getPort(EPortType.PORT_TYPE_TCP);
            ManagerTCP manager = portTcp.getManager();
            if (portMan.getType() == EPortType.PORT_TYPE_TCP) {
                if (portTcp.isConnected()) {
                    devNetName = manager.getHost();
                }
            }
        }

        //===============
        //  获取串口名称
        //===============
        if (portMan.getPort(EPortType.PORT_TYPE_UART) != null) {
            PortUART portUart = (PortUART) portMan.getPort(EPortType.PORT_TYPE_UART);
            if (portMan.getType() == EPortType.PORT_TYPE_UART) {
                if (portUart.isConnected()) {
                    if (portUart.getDevices() != null) {
                        devUartName = portUart.getDevices()[portUart.getPort()];
                    }
                }
            }
        }

        //===============
        //  获取USB名称
        //===============
        if (portMan.getPort(EPortType.PORT_TYPE_USB) != null) {
            PortUSB portUSB = (PortUSB) portMan.getPort(EPortType.PORT_TYPE_USB);
            if (portMan.getType() == EPortType.PORT_TYPE_USB) {
                if (portUSB.isConnected()) {
                    if (portUSB.getDevices() != null) {
                        devUsbName = portUSB.getDevices()[portUSB.getPort()];
                    }
                }
            }
        }

        //===============
        //  界面触发
        //===============
        //  设置
        final Intent intentSetup = new Intent();
        intentSetup.setClass(this, SetupActivity.class);

        //  天线数量
        int antCount = reader.getChannels();
        //  分配名称
        String[] arrayAntTitle = new String[antCount];
        //  选中
        boolean[] arrayAntCheck = new boolean[antCount];
        //  天线选中
        for (int i = 0; i < antCount; i++) {
            arrayAntTitle[i] = getString(R.string.str_setup_antanna) + (i + 1);
            arrayAntCheck[i] = (reader.getAntennas() & (1 << i)) != 0;
        }

        //  通道序号
        int posChannels = 0;
        for (int i = 0; i < arrayChannelValue.length; i++) {
            if (antCount == arrayChannelValue[i]) {
                posChannels = i;
                break;
            }
        }

        //  扫描次数
        int posTimes = 0;
        for (int i = 0; i < arrayTimesSecValue.length; i++) {
            if (arrayTimesSecValue[i] == reader.set.scan_times) {
                posTimes = i;
                break;
            }
        }

        //  设置子界面 - 初始化
        initUsbMenu();
        initUartMenu();
        initNetworkMenu();
        initSystemMenu();
        initPostMenu();

        //  未创建管理器，必须创建
        if (manager == null) {
            //  是否根界面?
            //  0    - 根界面
            //  xxxx - 子界面
            if (curItemId == 0) {
                //  清空
                list.clear();

                //  端口
                list.add(new XSetupItem(getString(R.string.str_setup_port)));
                list.add(new XSetupItem(XITEM_USART, getString(R.string.str_setup_uart), devUartName, listUart));
                list.add(new XSetupItem(XITEM_BLE, getString(R.string.str_setup_ble), devBleName, makeIntent(BleDeviceActivity.class)));
                list.add(new XSetupItem(XITEM_NETWORK, getString(R.string.str_setup_network), devNetName, listNetwork));
                list.add(new XSetupItem(XITEM_USB, getString(R.string.str_setup_usb), devUsbName, listUsb));

                //  指令集
                list.add(new XSetupItem(getString(R.string.str_setup_instruction)));
                list.add(new XSetupItem(XITEM_INSTRUCTION, getString(R.string.str_setup_instruction), EReaderType.toValue(theApp.rfidManager.getType()), arrayInstruct));

                //  扫描模式
                list.add(new XSetupItem(XITEM_SCAN_MODE, getString(R.string.str_setup_scan_mode), new XSetupItem[]{
                        new XSetupItem(getString(R.string.str_setup_scan_mode)),
                        new XSetupItem(XITEM_SESSION_ONOFF, getString(R.string.str_session_onoff), reader.set.session_onoff),
                        new XSetupItem(XITEM_SESSION_MODE, getString(R.string.str_session_mode), reader.set.session_mode, arraySessionMode),
                        new XSetupItem(XITEM_SESSION_TARGET, getString(R.string.str_session_target), reader.set.session_target, arraySessionTarget),
                        new XSetupItem(XITEM_SESSION_Q_VALUE, getString(R.string.str_session_q_value), reader.set.inventory_Q, 0, 15),
                        new XSetupItem(XITEM_SESSION_SCAN_TIME, getString(R.string.str_session_scan_time), reader.set.session_scan_time, 0, 200, "x100ms"),
                        new XSetupItem(getString(R.string.str_session_invert)),
                        new XSetupItem(XITEM_SESSION_INVERT_EN, getString(R.string.str_session_invert_en), reader.set.session_invert_enable),
                        new XSetupItem(XITEM_SESSION_INVERT_NUM, getString(R.string.str_session_invert_num), reader.set.session_invert_number, 0, 100, getString(R.string.str_no_tag_times)),
                        new XSetupItem(""),
                        }));

                //  天线
                list.add(new XSetupItem(getString(R.string.str_setup_antanna)));
                list.add(new XSetupItem(XITEM_CHANNELS, getString(R.string.str_setup_channel), posChannels, arrayChannelValue, getString(R.string.str_setup_channel)));
                list.add(new XSetupItem(XITEM_ANTENNA, getString(R.string.str_setup_antanna), arrayAntTitle, arrayAntCheck));
                list.add(new XSetupItem(XITEM_SWITCH_TIMES, getString(R.string.str_switch_times), reader.set.antanna_switch_times, 1, 1000, getString(R.string.str_delay_ms)));

                //  扫描
                list.add(new XSetupItem(getString(R.string.str_setup_scan)));
                list.add(new XSetupItem(XITEM_TIMES, getString(R.string.str_setup_per_second), posTimes, arrayTimesSecValue, getString(R.string.str_setup_times)));
                list.add(new XSetupItem(XITEM_INVENTRY, getString(R.string.str_setup_inventory), reader.set.inventory_times, 1, 255, getString(R.string.str_delay_times)));
                list.add(new XSetupItem(XITEM_INTERVAL, getString(R.string.str_setup_sw_interval), reader.set.sw_interval, 0, 1000, getString(R.string.str_delay_ms)));

                //  设备
                list.add(new XSetupItem(getString(R.string.str_setup_device)));
                //  设备配置
                list.add(new XSetupItem(XITEM_CONFIG, getString(R.string.str_setup_config), new XSetupItem[]{
                        new XSetupItem(getString(R.string.str_setup_config)),
                        new XSetupItem(XITEM_CONFIG_POWER_SAVE, getString(R.string.str_config_power_save), reader.set.set_power_save),
                        new XSetupItem(XITEM_CONFIG_MODE, getString(R.string.str_config_mode), reader.set.set_mode, arrayBootMode),
                        new XSetupItem(XITEM_CONFIG_SSID, getString(R.string.str_config_ssid), reader.set.set_wifi_ssid, "ssid"),
                        new XSetupItem(XITEM_CONFIG_PWD, getString(R.string.str_config_pwd), reader.set.set_wifi_pwd, "pwd"),
                        new XSetupItem(XITEM_CONFIG_HOST, getString(R.string.str_setup_host), IpToString(reader.set.set_wifi_host), "255.255.255.255"),
                        new XSetupItem(XITEM_CONFIG_PORT, getString(R.string.str_setup_port), PortToString(reader.set.set_wifi_port), "7000"),
                        new XSetupItem(getString(R.string.str_setup_action)),
                        new XSetupItem(XITEM_CONFIG_READ, getString(R.string.str_config_read)),
                        new XSetupItem(XITEM_CONFIG_WRITE, getString(R.string.str_config_write)),
                        new XSetupItem(""),
                        }));

                list.add(new XSetupItem(XITEM_AREA, getString(R.string.str_setup_region), 0, arrayArea));
                list.add(new XSetupItem(XITEM_SKIP, getString(R.string.str_setup_skip_freq), true));
                list.add(new XSetupItem(XITEM_TX_POWER, getString(R.string.str_setup_power), reader.set.tx_power, 0, 33, getString(R.string.str_power_dbm)));

                list.add(new XSetupItem(getString(R.string.str_setup_effect)));
                list.add(new XSetupItem(XITEM_EFFECT, getString(R.string.str_setup_effect), new XSetupItem[]{
                        new XSetupItem(getString(R.string.str_setup_effect_key)),
                        new XSetupItem(XITEM_EFFECT_KEY_SHAKE, getString(R.string.str_setup_shake), theApp.soundUtils.get(EFFECT_KEY).isVibrateOn),
                        new XSetupItem(XITEM_EFFECT_KEY_SOUND, getString(R.string.str_setup_sound), theApp.soundUtils.get(EFFECT_KEY).isSoundOn),
                        new XSetupItem(XITEM_EFFECT_KEY_VOLUME, getString(R.string.str_setup_volume), (int) (theApp.soundUtils.get(EFFECT_KEY).volume * 100), 0, 100, "%"),
                        new XSetupItem(getString(R.string.str_setup_effect_rfid)),
                        new XSetupItem(XITEM_EFFECT_RFID_SHAKE, getString(R.string.str_setup_shake), theApp.soundUtils.get(EFFECT_RFID).isVibrateOn),
                        new XSetupItem(XITEM_EFFECT_RFID_SOUND, getString(R.string.str_setup_sound), theApp.soundUtils.get(EFFECT_RFID).isSoundOn),
                        new XSetupItem(XITEM_EFFECT_RFID_VOLUME, getString(R.string.str_setup_volume), (int) (theApp.soundUtils.get(EFFECT_RFID).volume * 100), 0, 100, "%"),
                        new XSetupItem(""),
                        }));

                //==============================
                //  操作
                //==============================
                list.add(new XSetupItem(getString(R.string.str_setup_operate)));

                //==============================
                //  导出
                //==============================
                if (USE_POST_DATA_SUPPORT) {
                    list.add(new XSetupItem(XITEM_POST, getString(R.string.str_setup_export), listPost));
                }

                //==============================
                //  排序
                //==============================
                if (USE_SORT_LIST_SUPPORT) {
                    list.add(new XSetupItem(XITEM_SORT, getString(R.string.str_setup_sort), new XSetupItem[]{
                            new XSetupItem(getString(R.string.str_setup_sort)),
                            new XSetupItem(XITEM_SORT_ORDER, getString(R.string.str_setup_sort_order), (TagItem.sortOrder) ? 0 : 1, arraySortOrder),
                            new XSetupItem(XITEM_SORT_FILED, getString(R.string.str_setup_sort_title), TagItem.sortType, arraySortBy),
                            }));
                }


                //==============================
                //  分类
                //==============================
                if (USE_TREE_LIST_SUPPORT) {
                    list.add(new XSetupItem(XITEM_TREE, getString(R.string.str_setup_tree), new XSetupItem[]{
                            new XSetupItem(getString(R.string.str_setup_tree)),
                            new XSetupItem(XITEM_TREE_NAME, getString(R.string.str_setup_root_name), MainActivity.getTree().getRootName(), getString(R.string.str_setup_all)),
                            new XSetupItem(XITEM_TREE_SPLITE, getString(R.string.str_setup_tree_splite), MainActivity.getTree().getSplite(), "-"),
                            new XSetupItem(XITEM_TREE_CONTAIN, getString(R.string.str_setup_contain_splite), MainActivity.getTree().getContain()),
                            }));
                }

                //==============================
                //  显示
                //==============================
                list.add(new XSetupItem(getString(R.string.str_setup_display)));
                list.add(new XSetupItem(XITEM_DISPLAY_ORDER, getString(R.string.str_setup_display_invert), TagItem.UseShowEpcInvert));
                list.add(new XSetupItem(XITEM_DISPLAY_TEXT, getString(R.string.str_setup_display_text), TagItem.UseShowEpcUtf8));

                //==============================
                //  延时显示
                //==============================
//                if (USE_UPDATE_SLOW_SUPPORT) {
//                    //  刷新数量
//                    list.add(new XSetupItem(XITEM_DISPLAY_REFRESH_COUNT, getString(R.string.str_setup_display_refresh_count), "" + theApp.settings.refreshCount, "100"));
//                    list.add(new XSetupItem(XITEM_DISPLAY_REFRESH_DELAY, getString(R.string.str_setup_display_refresh_delay), "" + theApp.settings.refreshDelay, "1000"));
//                }

                //==============================
                //  其他
                //==============================
                list.add(new XSetupItem(getString(R.string.str_setup_other)));
                list.add(new XSetupItem(XITEM_DEBUG, getString(R.string.str_setup_debug), makeIntent(LoggerActivity.class)));
                list.add(new XSetupItem(XITEM_SYSTEM, getString(R.string.str_setup_advanced), listSystem));
                list.add(new XSetupItem(XITEM_ABOUT, getString(R.string.str_setup_about), makeIntent(AboutActivity.class)));

                //  末尾添加个空的是为了向上滚动预留空间
                list.add(new XSetupItem(""));

                //  创建设置 - 管理器
                manager = new XSetupManager(this, intentSetup, R.id.list_setup, list, this);

                //===========================================
                //  注册 - 根界面,
                //          根界面必须注册。
                //          方便子界面中访问根界面！
                //          子界面可以无穷展开！
                //===========================================
                XSetupManager.register(manager);
            } else {
                //  获取设置（当前设置号）
                XSetupItem item = XSetupManager.getInstance().findSetupItem(curItemId);
                //  有效设置
                if (item != null) {
                    // 有设置列表?
                    if (item.subMenu != null) {
                        //  创建 - 子界面管理器
                        manager = new XSetupManager(this, intentSetup, R.id.list_setup, item.subMenu, this);
                    }
                }
            }
        } else {
            ManagerUDP udpMan = portMan.getUdpManager();
            ManagerTCP tcpMan = portMan.getTcpManager();

            //  刷新网络模式
            if (portMan.getType() == EPortType.PORT_TYPE_UDP) {
                manager.setValue(this, XITEM_NETWORK_TYPE, 0, true);
                manager.setValue(this, XITEM_NETWORK_HOST, udpMan.getHost(), true);
                manager.setValue(this, XITEM_NETWORK_PORT, "" + udpMan.getPort(), true);
            } else if (portMan.getType() == EPortType.PORT_TYPE_TCP) {
                manager.setValue(this, XITEM_NETWORK_TYPE, 1, true);
                manager.setValue(this, XITEM_NETWORK_HOST, tcpMan.getHost(), true);
                manager.setValue(this, XITEM_NETWORK_PORT, "" + tcpMan.getPort(), true);
            } else {
                manager.setValue(this, XITEM_NETWORK_TYPE, 1, true);
                manager.setValue(this, XITEM_NETWORK_HOST, "192.168.1.100", true);
                manager.setValue(this, XITEM_NETWORK_PORT, "9527", true);
            }

            //  蓝牙名
            manager.setValue(this, XITEM_BLE, devBleName, true);
            //  网络名
            manager.setValue(this, XITEM_NETWORK, devNetName, true);
            //  串口名
            manager.setValue(this, XITEM_USART, devUartName, true);
            //  USB名称
            manager.setValue(this, XITEM_USB, devUsbName, true);
            //  读取设置并刷新
            manager.update();
        }
    }

    /**
     * 设置响应
     *
     * @param msg   : 消息
     * @param item  : 项目
     * @param id    : 标号
     * @param on    : 逻辑
     * @param pos   : 选择
     * @param text  : 文本
     * @param param : 参数
     */
    @Override
    public void OnSetupEvent(int msg, XSetupItem item, int id, boolean on, int pos, String text, Object param) {

        RfidApp.doEffect(this, RfidApp.EFFECT_KEY);

        PostData postIn;
        PostData postOut;
        PostData postSyn;
        PostData postItem;

        RfidApp     theApp  = (RfidApp) this.getApplication();
        PortManager portMan = theApp.portManager;
        RfidManager rfidMan = theApp.rfidManager;

        //  USART
        PortUART portUart = (PortUART) portMan.getPort(EPortType.PORT_TYPE_UART);
        //  USB
        PortUSB portUsb = (PortUSB) portMan.getPort(EPortType.PORT_TYPE_USB);

        //  UDP
        ManagerUDP udpMan = portMan.getUdpManager();
        //	TCP
        ManagerTCP tcpMan = portMan.getTcpManager();

        //  读写器
        BaseReader reader = theApp.rfidManager.getReader();
        //  设置
        BaseSet set = reader.set;
        //  暂停
        MainActivity.setPaused(true);
        //  清空
        theApp.rfidManager.cleanCmd();
        //  停止
        theApp.portWrite(reader.cmd.rfid_stop());

        //  支持抛数据
        if (USE_POST_DATA_SUPPORT) {
            postIn = theApp.listPost.get(POST_DATA_IN);
            postOut = theApp.listPost.get(POST_DATA_OUT);
            postSyn = theApp.listPost.get(POST_DATA_SYNC);
            postItem = theApp.listPost.get(POST_DATA_ITEM);
        }

        if (DEBUG) {
            LoggerActivity.log_d(TAG, "OnSetupEvent(msg:" + msg + ",item:" + item.name + ",id:" + id + ",on:" + on + ",pos:" + pos + ",text:" + text + ")");
        }

        int num = pos;

        //  列表换算
        if (item.listDatas != null) {
            num = item.listDatas[pos % item.listDatas.length];
        }

        switch (id) {
            //=====================
            //  串口设备
            //=====================
            case XITEM_USART_DEVICES:
                //  第几个串口
                portUart.setPort(pos);
                //  记忆设定
                portUart.setupUpdate(true);
                portMan.setupUpdate(true);
                break;

            //=====================
            //  串口波特率
            //=====================
            case XITEM_USART_BAUD:
                if (item.listDatas != null) {
                    portUart.setBaudrate(num);
                }
                portUart.setupUpdate(true);
                portMan.setupUpdate(true);
                break;

            //=====================
            //  串口打开
            //=====================
            case XITEM_USART_OPEN:
                if (!portUart.isConnected()) {
                    try {
                        //  配置
                        portUart.config(portUart.getPort(), portUart.getBaudrate());
                        //  连接
                        if (portUart.connect()) {
                            //  端口
                            theApp.portManager.setType(EPortType.PORT_TYPE_UART);
                            theApp.rfidManager.setPort(theApp.portManager.getPort());

                            if (SHOW_MSG) {
                                LoggerActivity.log_i(TAG, "打开串口");
                            }
                            //  成功
                            new MsgMake(this, EMessageType.MSG_SUCCESS, getString(R.string.str_msg_uart_open_success), MESSAGE_DELAY_CNT);
                            //  关闭
                            manager.updateName(XITEM_USART_OPEN, getString(R.string.str_close));
                            //	保存
                            portUart.setupUpdate(true);
                            portMan.setupUpdate(true);
                        } else {
                            if (SHOW_MSG) {
                                LoggerActivity.log_i(TAG, "打开串口失败!");
                            }
                            new MsgMake(this, EMessageType.MSG_ERROR, getString(R.string.str_msg_uart_open_faild), MESSAGE_DELAY_CNT);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (SHOW_MSG) {
                            LoggerActivity.log_e(TAG, "错误:" + e);
                        }
                        new MsgMake(this, EMessageType.MSG_ERROR, getString(R.string.str_msg_uart_open_error) + " : " + e.getMessage(), MESSAGE_DELAY_CNT);
                    }
                } else {
                    //  关闭
                    if (portUart.disconnect()) {
                        manager.updateName(XITEM_USART_OPEN, getString(R.string.str_open));
                        new MsgMake(this, EMessageType.MSG_NOTIFY, getString(R.string.str_msg_uart_close_success), MESSAGE_DELAY_CNT);
                    } else {
                        new MsgMake(this, EMessageType.MSG_ERROR, getString(R.string.str_msg_uart_close_faild), MESSAGE_DELAY_CNT);
                    }
                }
                break;

            //=====================
            //  网络类型(TCP/UDP)
            //=====================
            case XITEM_NETWORK_TYPE:
                if (DEBUG) {
                    LoggerActivity.log_d(TAG, "类型 : " + pos);
                }
                final EPortType[] arrayPort = {EPortType.PORT_TYPE_UDP, EPortType.PORT_TYPE_TCP};
                portMan.setType(arrayPort[pos % arrayPort.length]);
                rfidMan.setPort(portMan.getPort());

                if (portMan.getType() == EPortType.PORT_TYPE_UDP) {
                    manager.setValue(this, XITEM_NETWORK_TYPE, 0, true);
                    manager.setValue(this, XITEM_NETWORK_HOST, udpMan.getHost(), true);
                    manager.setValue(this, XITEM_NETWORK_PORT, "" + udpMan.getPort(), true);
                } else if (portMan.getType() == EPortType.PORT_TYPE_TCP) {
                    manager.setValue(this, XITEM_NETWORK_TYPE, 1, true);
                    manager.setValue(this, XITEM_NETWORK_HOST, tcpMan.getHost(), true);
                    manager.setValue(this, XITEM_NETWORK_PORT, "" + tcpMan.getPort(), true);
                }
                portMan.setupUpdate(true);
                break;

            //=====================
            //  网络主机
            //=====================
            case XITEM_NETWORK_HOST:
                if (DEBUG) {
                    LoggerActivity.log_d(TAG, "主机 : " + item.textValue);
                }
                if (portMan.getType() == EPortType.PORT_TYPE_UDP) {
                    udpMan.setHost(item.textValue);
                    udpMan.setupUpdate(true);
                } else {
                    tcpMan.setHost(item.textValue);
                    tcpMan.setupUpdate(true);
                }
                portMan.setupUpdate(true);
                break;

            //=====================
            //  网络端口
            //=====================
            case XITEM_NETWORK_PORT:
                if (DEBUG) {
                    LoggerActivity.log_d(TAG, "端口 : " + item.textValue);
                }
                if (portMan.getType() == EPortType.PORT_TYPE_UDP) {
                    udpMan.setPort(Integer.parseInt(item.textValue));
                    udpMan.setupUpdate(true);
                } else {
                    tcpMan.setPort(Integer.parseInt(item.textValue));
                    tcpMan.setupUpdate(true);
                }
                portMan.setupUpdate(true);
                break;

            //=====================
            //  网络连接
            //=====================
            case XITEM_NETWORK_CONNECT:
                if (DEBUG) {
                    LoggerActivity.log_d(TAG, "打开/关闭 - 网络");
                }
                if (portMan.getType() == EPortType.PORT_TYPE_UDP) {
                    if (!udpMan.isConnected()) {
                        udpMan.connect();
                        new MsgMake(this, EMessageType.MSG_WARNING,
                                getString(R.string.str_msg_net_connect_to)
                                        + getString(R.string.str_setup_port_network_host)
                                        + " ："
                                        + udpMan.getHost()
                                        + ", "
                                        + getString(R.string.str_setup_port_network_port)
                                        + " : "
                                        + udpMan.getPort(),
                                MESSAGE_DELAY_CNT);
                    } else {
                        udpMan.disconnect();
                        new MsgMake(this, EMessageType.MSG_WARNING, getString(R.string.str_msg_net_disconnect), MESSAGE_DELAY_CNT);
                    }
                } else {
                    if (!tcpMan.isConnected()) {
                        tcpMan.connect();
                        new MsgMake(this, EMessageType.MSG_WARNING,
                                getString(R.string.str_msg_net_connect_to)
                                        + getString(R.string.str_setup_port_network_host)
                                        + " ："
                                        + tcpMan.getHost()
                                        + ", "
                                        + getString(R.string.str_setup_port_network_port)
                                        + " : "
                                        + tcpMan.getPort(),
                                MESSAGE_DELAY_CNT);
                    } else {
                        tcpMan.disconnect();
                        new MsgMake(this, EMessageType.MSG_WARNING, getString(R.string.str_msg_net_disconnect), MESSAGE_DELAY_CNT);
                    }
                }
                break;

            //=====================
            //  选择USB设备
            //=====================
            case XITEM_USB_DEVICES:
                portUsb.setPort(pos);
                break;

            //=====================
            //  打开USB设备
            //=====================
            case XITEM_USB_OPEN:
                if (!portUsb.isConnected()) {
                    new MsgMake(this, EMessageType.MSG_WARNING, getString(R.string.str_msg_open_device) + "...", MESSAGE_DELAY_CNT);
                    portUsb.connect();
                } else {
                    new MsgMake(this, EMessageType.MSG_WARNING, getString(R.string.str_msg_close_device) + "...", MESSAGE_DELAY_CNT);
                    portUsb.disconnect();
                }

                if (portUsb.isConnected()) {
                    theApp.portManager.setType(EPortType.PORT_TYPE_USB);
                    theApp.rfidManager.setPort(theApp.portManager.getPort());
                    manager.updateName(XITEM_USB_OPEN, getString(R.string.str_close));
                } else {
                    manager.updateName(XITEM_USB_OPEN, getString(R.string.str_open));
                }
                break;

            //=====================
            //   指令集
            //=====================
            case XITEM_INSTRUCTION:
                //  切换指令集
                LoggerActivity.log_d(TAG, "指令集 : " + pos);
                //  需要从新初始化
                theApp.inited = false;
                //  切换
                rfidMan.setType(EReaderType.fromValue(pos));
                //  更新
                rfidMan.setupUpdate(true);
                break;

            //=====================
            //   会话开关
            //=====================
            case XITEM_SESSION_ONOFF:
                LoggerActivity.log_d(TAG, "会话开关 : " + on);
                reader.set.session_onoff = on;
                reader.setupUpdate(this, true);
                rfidMan.setupUpdate(true);
                break;

            //=====================
            //   会话模式
            //=====================
            case XITEM_SESSION_MODE:
                LoggerActivity.log_d(TAG, "会话模式 : " + pos);
                reader.set.session_mode = pos;
                reader.setupUpdate(this, true);
                rfidMan.setupUpdate(true);
                break;

            //=====================
            //   会话目标
            //=====================
            case XITEM_SESSION_TARGET:
                LoggerActivity.log_d(TAG, "会话目标 : " + pos);
                reader.set.session_target = pos;
                reader.setupUpdate(this, true);
                rfidMan.setupUpdate(true);
                break;

            //=====================
            //  Q值
            //=====================
            case XITEM_SESSION_Q_VALUE:
                LoggerActivity.log_d(TAG, "Q : " + pos);
                reader.set.inventory_Q = pos;
                reader.setupUpdate(this, true);
                rfidMan.setupUpdate(true);
                break;

            //=====================
            //  扫描时间
            //=====================
            case XITEM_SESSION_SCAN_TIME:
                LoggerActivity.log_d(TAG, "扫描时间 : " + pos);
                reader.set.session_scan_time = pos;
                reader.setupUpdate(this, true);
                rfidMan.setupUpdate(true);
                break;

            //=====================
            //  扫描时间
            //=====================
            case XITEM_SESSION_INVERT_EN:
                LoggerActivity.log_d(TAG, "翻转开关 : " + on);
                reader.set.session_invert_enable = on;
                reader.setupUpdate(this, true);
                rfidMan.setupUpdate(true);
                break;

            //=====================
            //  扫描时间
            //=====================
            case XITEM_SESSION_INVERT_NUM:
                LoggerActivity.log_d(TAG, "翻转次数 : " + pos);
                reader.set.session_invert_number = pos;
                reader.setupUpdate(this, true);
                rfidMan.setupUpdate(true);
                break;

            //=====================
            //  通道
            //=====================
            case XITEM_CHANNELS:
                LoggerActivity.log_d(TAG, "通道数:" + pos);
                //  设置通道数
                reader.setChannels(num);
                //  记忆
                reader.setupUpdate(this, true);
                //  记忆
                rfidMan.setupUpdate(true);
                //  天线选择
                item = manager.findSetupItem(XITEM_ANTENNA);
                //  创建菜单
                if (item != null) {
                    String[]  arrayAntTitle = new String[num];
                    boolean[] arrayAntCheck = new boolean[num];
                    //  默认都要选中
                    for (int i = 0; i < num; i++) {
                        arrayAntTitle[i] = getString(R.string.str_setup_antanna) + (i + 1);
                        arrayAntCheck[i] = (reader.getAntennas() & (1 << i)) != 0;
                    }
                    //  创建选择模式
                    item.makeChecks(XITEM_ANTENNA, getString(R.string.str_setup_antanna), arrayAntTitle, arrayAntCheck);
                }
                //  刷新界面
                manager.update();

                break;

            //=====================
            //  天线
            //=====================
            case XITEM_ANTENNA:
                LoggerActivity.log_d(TAG, "天线参数:" + pos);
                //  开关参数
                if (param instanceof boolean[]) {
                    //  转换类型
                    boolean[] checks = (boolean[]) param;
                    //  天线数据
                    long antennas = 0;
                    //  按位计算
                    for (int i = 0; ((i < checks.length) && (i < 32)); i++) {
                        if (checks[i]) {
                            antennas |= 1 << i;
                        }
                    }
                    //  设置天线
                    reader.setAntennas(antennas);
                    //  保存数据
                    reader.setupUpdate(this, true);
                    //  保存数据
                    rfidMan.setupUpdate(true);
                    //  刷新
                    manager.update();
                } else {
                    if (param != null) {
                        LoggerActivity.log_e(TAG, "param type error!" + param.getClass().toString());
                    } else {
                        LoggerActivity.log_e(TAG, "param is (null)!");
                    }
                }
                break;

            //  切换次数
            case XITEM_SWITCH_TIMES:
                LoggerActivity.log_d(TAG, "切换:" + pos);
                //  切换
                reader.set.antanna_switch_times = pos;
                reader.setupUpdate(this, true);
                rfidMan.setupUpdate(true);
                manager.update();
                break;

            case XITEM_CONFIG_POWER_SAVE:
                LoggerActivity.log_d(TAG, "配置-省电模式:" + on);
                reader.set.set_power_save = on;
                rfidMan.setupUpdate(true);
                manager.update();
                break;

            case XITEM_CONFIG_MODE:
                LoggerActivity.log_d(TAG, "配置-开机模式:" + pos);
                reader.set.set_mode = (byte) pos;
                rfidMan.setupUpdate(true);
                manager.update();
                break;

            case XITEM_CONFIG_SSID:
                LoggerActivity.log_d(TAG, "配置-网名:" + item.textValue);
                reader.set.set_wifi_ssid = item.textValue;
                rfidMan.setupUpdate(true);
                manager.update();
                break;

            case XITEM_CONFIG_PWD:
                LoggerActivity.log_d(TAG, "配置-密码:" + item.textValue);
                reader.set.set_wifi_pwd = item.textValue;
                rfidMan.setupUpdate(true);
                manager.update();
                break;

            case XITEM_CONFIG_HOST:
                LoggerActivity.log_d(TAG, "配置-地址:" + item.textValue);
                reader.set.set_wifi_host = IpFromString(item.textValue);
                if (reader.set.set_wifi_host == null) {
                    reader.set.set_wifi_host = new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255};
                }
                rfidMan.setupUpdate(true);
                manager.update();
                break;

            case XITEM_CONFIG_PORT:
                LoggerActivity.log_d(TAG, "配置-端口:" + item.textValue);
                reader.set.set_wifi_port = Integer.parseInt(item.textValue);
                rfidMan.setupUpdate(true);
                manager.update();
                break;

            //===============
            //  读取配置
            //===============
            case XITEM_CONFIG_READ:
                LoggerActivity.log_d(TAG, "读取配置");
                Config_Action_Read(rfidMan);
                new MsgMake(this, EMessageType.MSG_NOTIFY, getString(R.string.str_config_read), 1000);
                break;

            //===============
            //  写入配置
            //===============
            case XITEM_CONFIG_WRITE:
                LoggerActivity.log_d(TAG, "写入配置");
                Config_Action_Write(rfidMan);
                new MsgMake(this, EMessageType.MSG_NOTIFY, getString(R.string.str_config_write), 1000);
                break;

            //===============
            //  区域
            //===============
            case XITEM_AREA:
                LoggerActivity.log_d(TAG, "区域:" + pos);
                //  发指令
                theApp.portWrite(reader.getCmd().rfid_set_region((byte) (1 + pos)));
                break;

            //===============
            //  频率
            //===============
            case XITEM_FREQ:
                LoggerActivity.log_d(TAG, "频率:" + pos);
                break;

            //===============
            //  跳频
            //===============
            case XITEM_SKIP:
                LoggerActivity.log_d(TAG, "跳频:" + on);
                //  M100才需要跳帧处理
                if (reader instanceof RfidM100Reader) {
                    theApp.portWrite(RfidM100Cmd.cmd_set_auto(on));
                }
                break;

            //===============
            //  功率
            //===============
            case XITEM_TX_POWER:
                LoggerActivity.log_d(TAG, "功率:" + pos);
                theApp.portWrite(reader.getCmd().rfid_set_tx_power(pos));
                break;

            //===============
            //  声音
            //===============
            case XITEM_EFFECT_RFID_SOUND:
                LoggerActivity.log_d(TAG, "读取声音:" + on);
                theApp.soundUtils.get(EFFECT_RFID).isSoundOn = on;
                theApp.soundUtils.setupUpdate(this, true);
                manager.update();
                break;

            //===============
            //  震动
            //===============
            case XITEM_EFFECT_RFID_SHAKE:
                //  扫描震动效果
                LoggerActivity.log_d(TAG, "读取震动:" + on);

                theApp.soundUtils.get(EFFECT_RFID).isVibrateOn = on;
                theApp.soundUtils.setupUpdate(this, true);
                manager.update();
                break;

            //===============
            //  音量
            //===============
            case XITEM_EFFECT_RFID_VOLUME:
                LoggerActivity.log_d(TAG, "音量:" + pos);
                theApp.soundUtils.get(EFFECT_RFID).volume = (float) pos / 100;
                theApp.soundUtils.setupUpdate(this, true);
                manager.update();
                break;

            //===============
            //  声音
            //===============
            case XITEM_EFFECT_KEY_SOUND:
                LoggerActivity.log_d(TAG, "操作声音:" + on);
                theApp.soundUtils.get(EFFECT_KEY).isSoundOn = on;
                theApp.soundUtils.setupUpdate(this, true);
                manager.update();
                break;

            //===============
            //  震动
            //===============
            case XITEM_EFFECT_KEY_SHAKE:
                //  扫描震动效果
                LoggerActivity.log_d(TAG, "操作震动:" + on);
                theApp.soundUtils.get(EFFECT_KEY).isVibrateOn = on;
                theApp.soundUtils.setupUpdate(this, true);
                manager.update();
                break;

            //===============
            //  音量
            //===============
            case XITEM_EFFECT_KEY_VOLUME:
                LoggerActivity.log_d(TAG, "音量:" + pos);
                theApp.soundUtils.get(EFFECT_KEY).volume = (float) pos / 100;
                theApp.soundUtils.setupUpdate(this, true);
                manager.update();
                break;

            //===============
            //  反向
            //===============
            case XITEM_DISPLAY_ORDER:
                LoggerActivity.log_d(TAG, "反向:" + on);
                TagItem.UseShowEpcInvert = on;
                TagItem.updateSetup(this, true);
                manager.update();
                break;

            //===============
            //  次数
            //===============
            case XITEM_TIMES:
                LoggerActivity.log_d(TAG, "每秒次数:" + pos);
                //  每秒钟扫描次数
                set.scan_times = num;
                rfidMan.setupUpdate(true);
                manager.update();
                break;

            //===============
            //  轮询
            //===============
            case XITEM_INVENTRY:
                LoggerActivity.log_d(TAG, "轮询:" + pos);
                //  每次轮询次数
                set.inventory_times = pos;
                rfidMan.setupUpdate(true);
                manager.update();
                break;

            //===============
            //  间隔
            //===============
            case XITEM_INTERVAL:
                LoggerActivity.log_d(TAG, "间隔:" + pos);
                set.sw_interval = pos;
                rfidMan.setupUpdate(true);
                manager.update();
                break;

            //===============
            //  排序方向
            //===============
            case XITEM_SORT_ORDER:
                LoggerActivity.log_d(TAG, "顺序:" + pos);
                TagItem.sortOrder = (pos == 0);
                TagItem.updateSetup(this, true);
                manager.update();
                break;

            //===============
            //  排序字段
            //===============
            case XITEM_SORT_FILED:
                LoggerActivity.log_d(TAG, "字段:" + pos);
                TagItem.sortType = pos;
                TagItem.updateSetup(this, true);
                manager.update();
                break;

            //===============
            //  入库设置
            //===============
            case XITEM_POST_IN_URL:
                if (USE_POST_DATA_SUPPORT) {
                    postIn.url = item.textValue;
                }
                break;

            case XITEM_POST_IN_USR:
                if (USE_POST_DATA_SUPPORT) {
                    postIn.user = item.textValue;
                }
                break;

            case XITEM_POST_IN_PWD:
                if (USE_POST_DATA_SUPPORT) {
                    postIn.pwd = item.textValue;
                }
                break;

            //===============
            //  出库设置
            //===============
            case XITEM_POST_OUT_URL:
                if (USE_POST_DATA_SUPPORT) {
                    postOut.url = item.textValue;
                }
                break;

            case XITEM_POST_OUT_USR:
                if (USE_POST_DATA_SUPPORT) {
                    postOut.user = item.textValue;
                }
                break;

            case XITEM_POST_OUT_PWD:
                if (USE_POST_DATA_SUPPORT) {
                    postOut.pwd = item.textValue;
                }
                break;

            //===============
            //  同步设置
            //===============
            case XITEM_POST_SYN_URL:
                if (USE_POST_DATA_SUPPORT) {
                    postSyn.url = item.textValue;
                }
                break;

            case XITEM_POST_SYN_USR:
                if (USE_POST_DATA_SUPPORT) {
                    postSyn.user = item.textValue;
                }
                break;

            case XITEM_POST_SYN_PWD:
                if (USE_POST_DATA_SUPPORT) {
                    postSyn.pwd = item.textValue;
                }
                break;

            //===============
            //  详情设置
            //===============
            case XITEM_POST_ITEM_URL:
                if (USE_POST_DATA_SUPPORT) {
                    postItem.url = item.textValue;
                }
                break;

            case XITEM_POST_ITEM_USR:
                if (USE_POST_DATA_SUPPORT) {
                    postItem.user = item.textValue;
                }
                break;

            case XITEM_POST_ITEM_PWD:
                if (USE_POST_DATA_SUPPORT) {
                    postItem.pwd = item.textValue;
                }
                break;

            case XITEM_POST_ITEM_ON:
                if (USE_POST_DATA_SUPPORT) {
                    postItem.enable = on;
                }
                break;

            //  编号
            case XITEM_POST_ID:
                if (USE_POST_DATA_SUPPORT) {
                    postSyn.id = item.textValue;
                }
                break;

            //	使用定位数据
            case XITEM_POST_WITH_GPS:
                if (USE_POST_DATA_SUPPORT) {
                    postSyn.gps = item.boolValue;
                }
                break;

            //	自动提交数据
            case XITEM_POST_AUTO:
                if (USE_POST_DATA_SUPPORT) {
                    postSyn.auto = item.boolValue;
                }
                break;

            //	提交间隔
            case XITEM_POST_DELAY:
                if (USE_POST_DATA_SUPPORT) {
                    postSyn.delay = pos;
                }
                break;

            case XITEM_POST_EXPORT_FILE_IDX:
                RfidManager.EXPORT_FILE_IDX = item.boolValue;
                rfidMan.setupUpdate(true);
                break;

            case XITEM_POST_EXPORT_FILE_PC:
                RfidManager.EXPORT_FILE_PC = item.boolValue;
                rfidMan.setupUpdate(true);
                break;

            case XITEM_POST_EXPORT_FILE_EPC:
                RfidManager.EXPORT_FILE_EPC = item.boolValue;
                rfidMan.setupUpdate(true);
                break;

            case XITEM_POST_EXPORT_FILE_TID:
                RfidManager.EXPORT_FILE_TID = item.boolValue;
                rfidMan.setupUpdate(true);
                break;

            case XITEM_POST_EXPORT_FILE_USR:
                RfidManager.EXPORT_FILE_USR = item.boolValue;
                rfidMan.setupUpdate(true);
                break;

            case XITEM_POST_EXPORT_FILE_CRC:
                RfidManager.EXPORT_FILE_CRC = item.boolValue;
                rfidMan.setupUpdate(true);
                break;

            case XITEM_POST_EXPORT_FILE_ANT:
                RfidManager.EXPORT_FILE_ANT = item.boolValue;
                rfidMan.setupUpdate(true);
                break;

            case XITEM_POST_EXPORT_FILE_RSSI:
                RfidManager.EXPORT_FILE_RSSI = item.boolValue;
                rfidMan.setupUpdate(true);
                break;

            case XITEM_POST_EXPORT_FILE_CNT:
                RfidManager.EXPORT_FILE_CNT = item.boolValue;
                rfidMan.setupUpdate(true);
                break;

            case XITEM_POST_EXPORT_FILE_TIME:
                RfidManager.EXPORT_FILE_TIME = item.boolValue;
                rfidMan.setupUpdate(true);
                break;

            case XITEM_POST_EXPORT_TEXT_PREFIX:
                RfidManager.EXPORT_TEXT_PREFIX = item.textValue;
                rfidMan.setupUpdate(true);
                break;

            case XITEM_POST_EXPORT_TEXT_ENDFIX:
                RfidManager.EXPORT_TEXT_ENDFIX = item.textValue;
                rfidMan.setupUpdate(true);
                break;

            //===============
            //  名称
            //===============
            case XITEM_TREE_NAME:
                LoggerActivity.log_d(TAG, "根节点:" + item.textValue);
                MainActivity.getTree().setRootName(item.textValue);
                MainActivity.getTree().updateSetup(this, true);
                break;

            //===============
            //  分隔符
            //===============
            case XITEM_TREE_SPLITE:
                LoggerActivity.log_d(TAG, "分隔符号:" + item.textValue);
                MainActivity.getTree().setSplite(item.textValue);
                MainActivity.getTree().updateSetup(this, true);
                break;

            //===============
            //  包含
            //===============
            case XITEM_TREE_CONTAIN:
                LoggerActivity.log_d(TAG, "包含分隔符号:" + item.boolValue);
                MainActivity.getTree().setContain(item.boolValue);
                MainActivity.getTree().updateSetup(this, true);
                break;

            //===============
            //  单对话框
            //===============
            case XITEM_SYSTEM_TEST_SDK_DLG1:
                DlgMake d1 = new DlgMake();
                d1.dialogMsgBox(0, this, getString(R.string.str_info), getString(R.string.str_setup_sdk_dialog_one), new String[]{this.getString(R.string.str_ok)}, 0, this);
                break;

            //===============
            //  单对话框
            //===============
            case XITEM_SYSTEM_TEST_SDK_DLG2:
                DlgMake d2 = new DlgMake();
                d2.dialogMsgBox(0, this, getString(R.string.str_info), getString(R.string.str_setup_sdk_dialog_two), new String[]{this.getString(R.string.str_cancel), this.getString(R.string.str_ok)}, 0, this);
                break;

            //===============
            //  单对话框
            //===============
            case XITEM_SYSTEM_TEST_SDK_DLG3:
                DlgMake d3 = new DlgMake();
                d3.dialogMsgBox(0, this, getString(R.string.str_info),
                        getString(R.string.str_setup_sdk_dialog_more),
                        new String[]{
                                this.getString(R.string.str_cancel),
                                this.getString(R.string.str_other),
                                this.getString(R.string.str_ok)},
                        0, this);
                break;

            //===============
            //  单对话框
            //===============
            case XITEM_SYSTEM_TEST_SDK_DLG_LIST:
                DlgMake d4 = new DlgMake();
                d4.dialogList(0, this, getString(R.string.str_info), new String[]{
                                getString(R.string.str_setup_sdk_dialog_list) + "1",
                                getString(R.string.str_setup_sdk_dialog_list) + "2",
                                getString(R.string.str_setup_sdk_dialog_list) + "3",
                                getString(R.string.str_setup_sdk_dialog_list) + "4"},
                        null, 1, 0, this);
                break;

            //===============
            //  单对话框
            //===============
            case XITEM_SYSTEM_TEST_SDK_DLG_CHKS:
                new DlgMake().dialogChecks(0, this, getString(R.string.str_info), new String[]{
                                getString(R.string.str_setup_sdk_dialog_list) + "1",
                                getString(R.string.str_setup_sdk_dialog_list) + "2",
                                getString(R.string.str_setup_sdk_dialog_list) + "3",
                                getString(R.string.str_setup_sdk_dialog_list) + "4"},
                        new boolean[]{true, true, true, true}, 0, this);
                break;

            //===============
            //  单对话框
            //===============
            case XITEM_SYSTEM_TEST_SDK_DLG_EDIT:
                DlgMake d6 = new DlgMake();
                d6.dialogEditor(0, this, getString(R.string.str_info), getString(R.string.str_setup_dialog_edit_text), this);
                break;

            //===============
            //  单对话框
            //===============
            case XITEM_SYSTEM_TEST_SDK_MSG_INFO:
                new MsgMake(this, EMessageType.MSG_NOTIFY, getString(R.string.str_setup_msgbox_test), MESSAGE_DELAY_CNT);
                break;

            //===============
            //  单对话框
            //===============
            case XITEM_SYSTEM_TEST_SDK_MSG_WARING:
                new MsgMake(this, EMessageType.MSG_WARNING, getString(R.string.str_setup_msgbox_test), MESSAGE_DELAY_CNT);
                break;

            //===============
            //  单对话框
            //===============
            case XITEM_SYSTEM_TEST_SDK_MSG_ERROR:
                new MsgMake(this, EMessageType.MSG_ERROR, getString(R.string.str_setup_msgbox_test), MESSAGE_DELAY_CNT);
                break;

            //===============
            //  单对话框
            //===============
            case XITEM_SYSTEM_TEST_SDK_MSG_SUCCESS:
                new MsgMake(this, EMessageType.MSG_SUCCESS, getString(R.string.str_setup_msgbox_test), MESSAGE_DELAY_CNT);
                break;

            default:
                break;
        }

        /*
         * 提交数据包
         */
        if (USE_POST_DATA_SUPPORT) {
            switch (id) {
                case XITEM_POST_IN_URL:
                case XITEM_POST_IN_USR:
                case XITEM_POST_IN_PWD:
                    postIn.setupUpdate(true, "post" + POST_DATA_IN);
                    break;

                case XITEM_POST_OUT_URL:
                case XITEM_POST_OUT_USR:
                case XITEM_POST_OUT_PWD:
                    postOut.setupUpdate(true, "post" + POST_DATA_OUT);
                    break;

                case XITEM_POST_ITEM_URL:
                case XITEM_POST_ITEM_USR:
                case XITEM_POST_ITEM_PWD:
                case XITEM_POST_ITEM_ON:
                    postItem.setupUpdate(true, "post" + POST_DATA_ITEM);
                    break;

                case XITEM_POST_SYN_URL:
                case XITEM_POST_SYN_USR:
                case XITEM_POST_SYN_PWD:
                case XITEM_POST_ID:
                case XITEM_POST_AUTO:
                case XITEM_POST_DELAY:
                case XITEM_POST_WITH_GPS:
                    postSyn.setupUpdate(true, "post" + POST_DATA_SYNC);
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {

        RfidApp.doEffect(this, RfidApp.EFFECT_KEY);

        switch (v.getId()) {
            //  返回
            case R.id.image_return:
                this.finish();
                break;
        }
    }

    @Override
    public boolean OnDialogEvent(int id, Dialog dlg, View v, int pos, Object param) {

        RfidApp.doEffect(this, RfidApp.EFFECT_KEY);

        switch (id) {
            case 0:
                break;

            default:
                break;
        }

        //  返回TRUE，关闭对话框！
        return true;
    }

    @Override
    public void OnDialogCreate(int dlgId, View root) {
    }

    @Override
    public void OnDialogDestory(int dlgId) {
    }

    @Override
    public void OnPortEvent(EPortType type, EPortEven even, int param, Object device, String message) {

        RfidApp theApp = (RfidApp) this.getApplication();

        PortManager portMan = theApp.portManager;
        RfidManager rfidMan = theApp.rfidManager;

        if (DEBUG) {
            System.out.printf(TAG + "--->OnPortEvent(type:%d,param:%d,message:%s)\n", type.ordinal(), param, message);
        }

        switch (even) {
            case PORT_EVENT_CONNECTING:

            case PORT_EVENT_DISCONNECTING:
                new MsgMake(this, EMessageType.MSG_NOTIFY, message, MESSAGE_DELAY_CNT);
                break;

            case PORT_EVENT_CONNECT_FAILED:

            case PORT_EVENT_DISCONNECT_FAILED:
                new MsgMake(this, EMessageType.MSG_ERROR, message, MESSAGE_DELAY_CNT);
                break;

            case PORT_EVENT_CONNECTED:
                if (type == EPortType.PORT_TYPE_UDP) {
                    portMan.setType(EPortType.PORT_TYPE_UDP);
                    rfidMan.setPort(portMan.getPort());
                    //  按钮文字
                    manager.updateName(XITEM_NETWORK_CONNECT, getString(R.string.str_disconnect));
                } else if (type == EPortType.PORT_TYPE_TCP) {
                    portMan.setType(EPortType.PORT_TYPE_TCP);
                    rfidMan.setPort(portMan.getPort());
                    //  按钮文字
                    manager.updateName(XITEM_NETWORK_CONNECT, getString(R.string.str_disconnect));
                }
                new MsgMake(this, EMessageType.MSG_SUCCESS, getString(R.string.str_connected), MESSAGE_DELAY_CNT * 2);
                break;

            case PORT_EVENT_DISCONNECTED:
                if (type == EPortType.PORT_TYPE_UDP) {
                    manager.updateName(XITEM_NETWORK_CONNECT, getString(R.string.str_connect));
                } else if (type == EPortType.PORT_TYPE_TCP) {
                    manager.updateName(XITEM_NETWORK_CONNECT, getString(R.string.str_connect));
                }
                new MsgMake(this, EMessageType.MSG_NOTIFY, getString(R.string.str_disconnected), MESSAGE_DELAY_CNT);
                break;

            default:
                new MsgMake(this, EMessageType.MSG_NOTIFY, message, MESSAGE_DELAY_CNT);
                break;
        }
    }

    /**
     * 配置读
     *
     * @param manager ： 管理器
     */
    private void Config_Action_Read(RfidManager manager) {
        System.err.println("Config_Action_Read()");

        manager.config.setEven(this);

        //  读取参数
        manager.putCmd(manager.config.send(RfidConfig.CFG_TYPE_SET_POWER | RfidConfig.CFG_TYPE_READ, 0, null));
        //manager.putCmd(manager.config.send(RfidConfig.CFG_TYPE_SET_BEEPER | RfidConfig.CFG_TYPE_READ, 0, null));
        manager.putCmd(manager.config.send(RfidConfig.CFG_TYPE_SET_MODE | RfidConfig.CFG_TYPE_READ, 0, null));
        //manager.putCmd(manager.config.send(RfidConfig.CFG_TYPE_SET_AUTO | RfidConfig.CFG_TYPE_READ, 0, null));

        manager.putCmd(manager.config.send(RfidConfig.CFG_TYPE_SET_HOST | RfidConfig.CFG_TYPE_READ, 0, null));
        manager.putCmd(manager.config.send(RfidConfig.CFG_TYPE_SET_PORT | RfidConfig.CFG_TYPE_READ, 0, null));

        manager.putCmd(manager.config.send(RfidConfig.CFG_TYPE_SET_SSID | RfidConfig.CFG_TYPE_READ, 0, null));
        manager.putCmd(manager.config.send(RfidConfig.CFG_TYPE_SET_PWD | RfidConfig.CFG_TYPE_READ, 0, null));
    }

    /**
     * 配置写
     *
     * @param man ： 管理器
     */
    private void Config_Action_Write(RfidManager man) {

        System.err.println("Config_Action_Write()");

        BaseReader reader = man.getReader();
        BaseSet    set    = reader.set;
        ByteStream bs     = new ByteStream(0);

        //  省电
        bs.put(set.set_power_save);
        man.putCmd(man.config.send(RfidConfig.CFG_TYPE_SET_POWER, 0, bs.getData()));

        //  蜂鸣器
        bs.reset();
        bs.put(set.set_beeper);
        man.putCmd(man.config.send(RfidConfig.CFG_TYPE_SET_BEEPER, 0, bs.getData()));

        //  蜂鸣器
        bs.reset();
        bs.put(set.set_auto_read);
        man.putCmd(man.config.send(RfidConfig.CFG_TYPE_SET_AUTO, 0, bs.getData()));

        bs.reset();
        bs.put(set.set_wifi_host);
        man.putCmd(man.config.send(RfidConfig.CFG_TYPE_SET_HOST, 0, bs.getData()));

        bs.reset();
        bs.put((short) set.set_wifi_port & 0xffff);
        man.putCmd(man.config.send(RfidConfig.CFG_TYPE_SET_PORT, 0, bs.getData()));

        //  网络名,分三次
        byte[] FixSsid = makeFixData(set.set_wifi_ssid, 32);
        byte[] FixPwd  = makeFixData(set.set_wifi_pwd, 16);

        //  SSID
        bs.reset();
        bs.put(FixSsid, 0, 11);
        man.putCmd(man.config.send(RfidConfig.CFG_TYPE_SET_SSID, 0, bs.getData()));

        bs.reset();
        bs.put(FixSsid, 11, 11);
        man.putCmd(man.config.send(RfidConfig.CFG_TYPE_SET_SSID, 11, bs.getData()));

        bs.reset();
        bs.put(FixSsid, 22, 10);
        man.putCmd(man.config.send(RfidConfig.CFG_TYPE_SET_SSID, 22, bs.getData()));

        //  PWD
        bs.reset();
        bs.put(FixPwd, 0, 8);
        man.putCmd(man.config.send(RfidConfig.CFG_TYPE_SET_PWD, 0, bs.getData()));

        bs.reset();
        bs.put(FixPwd, 8, 8);
        man.putCmd(man.config.send(RfidConfig.CFG_TYPE_SET_PWD, 8, bs.getData()));

        //  模式
        bs.reset();
        bs.put(set.set_mode);
        man.putCmd(man.config.send(RfidConfig.CFG_TYPE_SET_MODE, 0, bs.getData()));

        new MsgMake(this, EMessageType.MSG_SUCCESS, getString(R.string.str_write_done), MESSAGE_DELAY_CNT);
    }

    @Override
    public void OnConfigEvent(RfidConfig config, int type, int offset, byte[] data) {

        BaseReader reader = config.getManager().getReader();
        BaseSet    set    = reader.set;

        switch (type & 0x7F) {
            case RfidConfig.CFG_TYPE_SET_POWER:
                //  蓝牙名
                manager.setValue(this, XITEM_CONFIG_POWER_SAVE, set.set_power_save, true);
                //  读取设置并刷新
                manager.update();
                break;

            case RfidConfig.CFG_TYPE_SET_MODE:
                //  蓝牙名
                manager.setValue(this, XITEM_CONFIG_MODE, set.set_mode, true);
                //  读取设置并刷新
                manager.update();
                break;

            case RfidConfig.CFG_TYPE_SET_SSID:
                //  蓝牙名
                manager.setValue(this, XITEM_CONFIG_SSID, set.set_wifi_ssid, true);
                //  读取设置并刷新
                manager.update();
                break;

            case RfidConfig.CFG_TYPE_SET_PWD:
                manager.setValue(this, XITEM_CONFIG_PWD, set.set_wifi_pwd, true);
                //  读取设置并刷新
                manager.update();
                new MsgMake(this, EMessageType.MSG_SUCCESS, getString(R.string.str_read_done), MESSAGE_DELAY_CNT);
                break;

            case RfidConfig.CFG_TYPE_SET_HOST:
                manager.setValue(this, XITEM_CONFIG_HOST, IpToString(set.set_wifi_host), true);
                manager.update();
                break;

            case RfidConfig.CFG_TYPE_SET_PORT:
                manager.setValue(this, XITEM_CONFIG_PORT, PortToString(set.set_wifi_port), true);
                manager.update();
                break;
        }
    }
}
