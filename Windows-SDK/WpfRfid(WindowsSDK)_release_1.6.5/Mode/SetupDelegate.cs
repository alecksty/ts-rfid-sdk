using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Windows;
using System.Windows.Controls;
using CommLib.bytes;
using CommLib.comm;
using GuiLib.comm;
using GuiLib.setup;
using RfidLib.rfid;
using RfidLib.rfid.based;
using RfidLib.rfid.m100;

namespace WpfRfid.Mode
{
    public class SetupDelegate : ISetupEvent, IConfigEvent
    {
        private const bool DEBUG = true;

        private const string TAG = "SetupDelegate";

        //==============================================================================================================
        //  开关
        //==============================================================================================================
        private const bool USE_FILTER_LIST_SUPPORT = true;
        private const bool USE_SYSTEM_LOCATION_TEST = true;
        private const bool USE_LOST_CHECK_SUPPORT = true;
        private const bool USE_BAT_WRITE_SUPPORT = true;
        private const bool USE_POST_DATA_SUPPORT = true;
        private const bool USE_SORT_LIST_SUPPORT = true;
        private const bool USE_COMPARE_LIST_SUPPORT = true;
        private const bool USE_TREE_LIST_SUPPORT = true;
        private const bool USE_UPDATE_SLOW_SUPPORT = true;

        //==============================================================================================================
        //  常量
        //==============================================================================================================
        private const string DEF_HOST_POST_WEBSITE = "http://website.asp";
        private const string DEF_HOST_POST_IN = DEF_HOST_POST_WEBSITE;
        private const string DEF_HOST_POST_OUT = DEF_HOST_POST_WEBSITE;
        private const string DEF_HOST_POST_ITEM = DEF_HOST_POST_WEBSITE;
        private const string DEF_HOST_POST_SYNC = DEF_HOST_POST_WEBSITE;

        //  延时
        private const int MESSAGE_DELAY_CNT = 1000;

        //  对话框
        private const int ID_DLG_PROGRESS = 10000;

        //==============================================================================================================
        //  设置 - 标识
        //==============================================================================================================

        ///  网络
        private const int XITEM_NETWORK = 1200;

        private const int XITEM_NETWORK_TYPE = 1201;
        private const int XITEM_NETWORK_HOST = 1202;
        private const int XITEM_NETWORK_PORT = 1203;
        private const int XITEM_NETWORK_CONNECT = 1204;

        ///  指令集
        private const int XITEM_INSTRUCTION = 1400;

        ///  扫描模式
        private const int XITEM_SCAN_MODE = 1500;

        private const int XITEM_SESSION_ONOFF = 1510;
        private const int XITEM_SESSION_MODE = 1520;
        private const int XITEM_SESSION_TARGET = 1530;
        private const int XITEM_SESSION_Q_VALUE = 1550;
        private const int XITEM_SESSION_SCAN_TIME = 1560;
        private const int XITEM_SESSION_INVERT_EN = 1570;
        private const int XITEM_SESSION_INVERT_NUM = 1580;

        ///	 天线切换次数
        private const int XITEM_SWITCH_TIMES = 1600;

        ///  通道
        private const int XITEM_CHANNELS = 2000;

        ///  天线
        private const int XITEM_ANTENNA = 2100;

        ///  设备
        private const int XITEM_CONFIG = 3000;

        //  省电
        private const int XITEM_CONFIG_POWER_SAVE = 3010;
        private const int XITEM_CONFIG_AUTO = 3012;
        private const int XITEM_CONFIG_MODE = 3014;
        private const int XITEM_CONFIG_BEEPER = 3015;
        
        //=======================
        //  WIFI配置
        //=======================
        private const int XITEM_CONFIG_SSID = 3020;
        private const int XITEM_CONFIG_PWD = 3021;
        private const int XITEM_CONFIG_HOST = 3022;
        private const int XITEM_CONFIG_PORT = 3023;
        private const int XITEM_CONFIG_WIFI_READ = 3024;
        private const int XITEM_CONFIG_WIFI_WRITE = 3025;

        //=======================
        // 4G 配置
        //=======================
        private const int XITEM_CONFIG_CON_TYPE = 3060;
        private const int XITEM_CONFIG_APN_NAME = 3061;
        private const int XITEM_CONFIG_DEV_NAME = 3062;
        private const int XITEM_CONFIG_4G_READ = 3063;
        private const int XITEM_CONFIG_4G_WRITE = 3064;

        //=======================
        //  缓存模式
        //=======================
        private const int XITEM_CONFIG_BUFFER_START = 3090;
        private const int XITEM_CONFIG_BUFFER_STOP = 3091;
        private const int XITEM_CONFIG_BUFFER_CLEAN = 3092;
        private const int XITEM_CONFIG_BUFFER_TEST = 3093;


        ///  区域
        private const int XITEM_AREA = 3100;

        ///  频段
        private const int XITEM_FREQ = 3200;

        // 跳转
        private const int XITEM_SKIP = 3300;

        //  输出功率
        private const int XITEM_TX_POWER = 3400;

        //  Post配置
        private const int XITEM_CONFIG_POST = 3500;
        // 配置输出地址
        private const int XITEM_CONFIG_POST_URL = 3600;

        //  每秒次数
        private const int XITEM_TIMES = 4200;
        //  轮训次数
        private const int XITEM_INVENTRY = 4210;
        //  轮训间隔
        private const int XITEM_INTERVAL = 4220;

        //=======================
        //  推送/导出
        //=======================
        private const int XITEM_POST = 4300;
        //  入库
        private const int XITEM_POST_IN_URL = 4311;
        private const int XITEM_POST_IN_USR = 4312;
        private const int XITEM_POST_IN_PWD = 4313;
        //  出库
        private const int XITEM_POST_OUT_URL = 4321;
        private const int XITEM_POST_OUT_USR = 4322;
        private const int XITEM_POST_OUT_PWD = 4323;
        //  同步
        private const int XITEM_POST_SYN_URL = 4331;
        private const int XITEM_POST_SYN_USR = 4332;
        private const int XITEM_POST_SYN_PWD = 4333;
        //  详情
        private const int XITEM_POST_ITEM_URL = 4341;
        private const int XITEM_POST_ITEM_USR = 4342;
        private const int XITEM_POST_ITEM_PWD = 4343;
        private const int XITEM_POST_ITEM_ON = 4344;
        //  POST附加
        private const int XITEM_POST_WITH_GPS = 4400;
        private const int XITEM_POST_AUTO = 4410;
        private const int XITEM_POST_DELAY = 4420;
        private const int XITEM_POST_ID = 4430;

        //========================
        //  导出
        //=======================
        private const int XITEM_POST_EXPORT_FILE_IDX = 4440;
        private const int XITEM_POST_EXPORT_FILE_PC = 4441;
        private const int XITEM_POST_EXPORT_FILE_EPC = 4442;
        private const int XITEM_POST_EXPORT_FILE_TID = 4443;
        private const int XITEM_POST_EXPORT_FILE_USR = 4444;
        private const int XITEM_POST_EXPORT_FILE_CRC = 4445;
        private const int XITEM_POST_EXPORT_FILE_ANT = 4446;
        private const int XITEM_POST_EXPORT_FILE_RSSI = 4447;
        private const int XITEM_POST_EXPORT_FILE_CNT = 4448;
        private const int XITEM_POST_EXPORT_FILE_TIME = 4449;
        private const int XITEM_POST_EXPORT_TEXT_PREFIX = 4450;
        private const int XITEM_POST_EXPORT_TEXT_ENDFIX = 4451;

        //  排序
        private const int XITEM_SORT = 5000;

        //  方向
        private const int XITEM_SORT_ORDER = 5100;

        //  字段
        private const int XITEM_SORT_FILED = 5200;

        //  过滤
        private const int XITEM_FILTER = 5300;

        //  信号过滤
        private const int XITEM_FILTER_RSSI = 5400;

        //  次数过滤
        private const int XITEM_FILTER_COUNT = 5500;

        //  包含过滤
        private const int XITEM_FILTER_CONTAIN = 5600;

        //  树结构
        private const int XITEM_TREE = 5700;

        //  根节点名
        private const int XITEM_TREE_NAME = 5800;

        //  分割符号
        private const int XITEM_TREE_SPLITE = 5900;

        //  包含分隔符
        private const int XITEM_TREE_CONTAIN = 6000;

        //  调试
        private const int XITEM_DEBUG = 6100;

        //  显示顺序
        private const int XITEM_DISPLAY_ORDER = 6500;

        //  显示文本
        private const int XITEM_DISPLAY_TEXT = 6600;

        //  显示数量
        private const int XITEM_DISPLAY_REFRESH_COUNT = 8200;

        //  显示延时
        private const int XITEM_DISPLAY_REFRESH_DELAY = 8300;

        //  关于
        private const int XITEM_ABOUT = 9000;

        //==============================================================================================================
        //  列表
        //==============================================================================================================
        //    列表
        private List<XSetupItem> list = new List<XSetupItem>();

        //    管理器
        private XSetupManager manager;

        //    当前项
        private int curItemId = 0;

        //    窗口
        private Window win;

        //    树结构
        private TreeView tree;

        //    列表
        private Panel grid;

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 获取文字
        /// </summary>
        /// <param name="n"></param>
        /// <returns></returns>
        ///-------------------------------------------------------------------------------------------------------------
        private string getString(string n)
        {
            return n;
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 初始化
        /// </summary>
        ///-------------------------------------------------------------------------------------------------------------
        public SetupDelegate(Window win, TreeView tree, Panel grid)
        {
            this.win = win;
            this.tree = tree;
            this.grid = grid;

            //    初始化
            initRootMenu();
        }

        //  指令
        string[] arrayInstruct =
        {
            "QM100   - (H1/H2/T1)",
            "R2000   - (H3/H8/R4/R8/D1)",
            "J2000   - (J1/J4/J8/J16)",
            "G2000   - (G1/G4/G8/G16)",
            "PR9200  - (H9)",
            "SLR5300 - (H5)",
        };

        //  指令集
        private EReaderType[] arrayReaderTypes =
        {
            EReaderType.READER_TYPE_M100,
            EReaderType.READER_TYPE_R2000,
            EReaderType.READER_TYPE_J2000,
            EReaderType.READER_TYPE_G2000,
            EReaderType.READER_TYPE_PR9200,
            EReaderType.READER_TYPE_SLR5600,
        };

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 初始化系统菜单
        /// </summary>
        ///-------------------------------------------------------------------------------------------------------------
        private void initRootMenu()
        {
            //  通道
            int[] arrayChannelValue =
            {
                1, 4, 8, 16
            };

            //  频率
            int[] arrayTimesSecValue =
            {
                500, 200, 100, 50, 20, 10, 5, 2, 1, 0
            };

            //  区域
            string[] arrayArea =
            {
                getString(R.string_str_setup_region_cn800),
                getString(R.string_str_setup_region_cn900),
                getString(R.string_str_setup_region_usa),
                getString(R.string_str_setup_region_europe),
                getString(R.string_str_setup_region_korean)
            };

            //  区域值
            int[] arrayAreaValue =
            {
                1,
                4,
                2,
                3,
                6,
            };

            //  排序
            string[] arraySortOrder =
                {
                    getString(R.string_str_setup_sort_a2z),
                    getString(R.string_str_setup_sort_z2a)
                }
                ;

            //  字段
            string[] arraySortBy =
                {
                    getString(R.string_str_setup_sort_index),
                    getString(R.string_str_setup_sort_epc),
                    getString(R.string_str_setup_sort_signal),
                    getString(R.string_str_setup_sort_time),
                    getString(R.string_str_setup_sort_count)
                }
                ;

            //  会话模式
            string[] arraySessionMode =
                {
                    getString(R.string_str_session_mode_s0),
                    getString(R.string_str_session_mode_s1),
                    getString(R.string_str_session_mode_s2),
                    getString(R.string_str_session_mode_s3),
                    getString(R.string_str_session_mode_sl),
                }
                ;

            //  会话目标
            string[] arraySessionTarget =
                {
                    getString(R.string_str_session_target_a),
                    getString(R.string_str_session_target_b),
                    getString(R.string_str_session_target_auto),
                }
                ;

            //  蓝牙，无线网
            string[] arrayBootMode =
                {
                    getString(R.string_str_boot_mode_ble),
                    getString(R.string_str_boot_mode_wifi),
                }
                ;

            //  端口管理器
            var portMan = App.GetPortManager();            
            //  标签管理器
            var rfidMan = App.GetRfidManager();
            //  当前读写器
            var reader = rfidMan.GetReader();

            //  天线数量
            var antCount = reader.GetChannels();
            //  分配名称
            var arrayAntTitle = new string[antCount];
            //  选中
            var arrayAntCheck = new bool[antCount];

            //Debug.Print("-->Setup.Ant:{0}",antCount);

            //  天线选中
            for (var i = 0; i < antCount; i++)
            {
                //  标题
                arrayAntTitle[i] = getString(R.string_str_setup_antanna) + (i + 1);
                //  检查
                arrayAntCheck[i] = (reader.GetAntennas() & (1 << i)) != 0;
            }

            //  通道序号
            var posChannels = 0;
            for (var i = 0; i < arrayChannelValue.Length; i++)
            {
                if (antCount == arrayChannelValue[i])
                {
                    posChannels = i;
                    break;
                }
            }

            //  扫描次数
            int posTimes = 0;
            for (int i = 0; i < arrayTimesSecValue.Length; i++)
            {
                if (arrayTimesSecValue[i] == reader.set.scan_times)
                {
                    posTimes = i;
                    break;
                }
            }

            //  未创建管理器，必须创建
            if (manager == null)
            {
                //  是否根界面?
                //  0    - 根界面
                //  xxxx - 子界面
                if (curItemId == 0)
                {
                    //  清空
                    list.Clear();

                    //  指令集
                    list.Add(new XSetupItem(getString(R.string_str_setup_instruction)));
                    list.Add(new XSetupItem(XITEM_INSTRUCTION, getString(R.string_str_setup_instruction), (int)rfidMan.GetReaderType(), arrayInstruct));

                    //  扫描模式
                    list.Add(new XSetupItem(XITEM_SCAN_MODE, getString(R.string_str_setup_scan_mode), new[]
                    {
                        new XSetupItem(getString(R.string_str_setup_scan_mode)),
                        new XSetupItem(XITEM_SESSION_ONOFF,     getString(R.string_str_session_onoff),     reader.set.session_onoff),
                        new XSetupItem(XITEM_SESSION_MODE,      getString(R.string_str_session_mode),      reader.set.session_mode,      arraySessionMode),
                        new XSetupItem(XITEM_SESSION_TARGET,    getString(R.string_str_session_target),    reader.set.session_target,    arraySessionTarget),
                        new XSetupItem(XITEM_SESSION_Q_VALUE,   getString(R.string_str_session_q_value),   reader.set.inventory_Q,       0, 15),
                        new XSetupItem(XITEM_SESSION_SCAN_TIME, getString(R.string_str_session_scan_time), reader.set.session_scan_time, 0, 200, "x100ms"),

                        new XSetupItem(getString(R.string_str_session_invert)),
                        new XSetupItem(XITEM_SESSION_INVERT_EN,  getString(R.string_str_session_invert_en),  reader.set.session_invert_enable),
                        new XSetupItem(XITEM_SESSION_INVERT_NUM, getString(R.string_str_session_invert_num), reader.set.session_invert_number, 0, 100, getString(R.string_str_no_tag_times)),
                    }));

                    //  天线
                    list.Add(new XSetupItem(getString(R.string_str_setup_antanna)));
                    list.Add(new XSetupItem(XITEM_CHANNELS, getString(R.string_str_setup_channel), posChannels, arrayChannelValue, getString(R.string_str_setup_channel)));
                    list.Add(new XSetupItem(XITEM_ANTENNA, getString(R.string_str_setup_antanna), arrayAntTitle, arrayAntCheck));
                    list.Add(new XSetupItem(XITEM_SWITCH_TIMES, getString(R.string_str_switch_times), reader.set.antanna_switch_times, 1, 1000, getString(R.string_str_delay_ms)));

                    //  扫描
                    list.Add(new XSetupItem(getString(R.string_str_setup_scan)));
                    list.Add(new XSetupItem(XITEM_TIMES, getString(R.string_str_setup_per_second), posTimes, arrayTimesSecValue, getString(R.string_str_setup_times)));
                    list.Add(new XSetupItem(XITEM_INVENTRY, getString(R.string_str_setup_inventory), reader.set.inventory_times, 1, 255, getString(R.string_str_delay_times)));
                    list.Add(new XSetupItem(XITEM_INTERVAL, getString(R.string_str_setup_sw_interval), reader.set.sw_interval, 0, 1000, getString(R.string_str_delay_ms)));

                    //  设备
                    list.Add(new XSetupItem(getString(R.string_str_setup_device)));
                    //  设备配置
                    list.Add(new XSetupItem(XITEM_CONFIG, getString(R.string_str_setup_config), new[]
                    {
                        new XSetupItem(getString(R.string_str_setup_config)),
                        new XSetupItem(XITEM_CONFIG_POWER_SAVE,     getString(R.string_str_config_power_save),  reader.set.set_power_save),
                        
                        new XSetupItem(XITEM_CONFIG_AUTO,           getString(R.string_str_config_auto),        reader.set.set_auto_read),

                        new XSetupItem(XITEM_CONFIG_MODE,           getString(R.string_str_config_mode),        reader.set.set_mode,                    arrayBootMode),
                        new XSetupItem(XITEM_CONFIG_SSID,           getString(R.string_str_config_ssid),        reader.set.set_wifi_ssid,               "ssid"),
                        new XSetupItem(XITEM_CONFIG_PWD,            getString(R.string_str_config_pwd),         reader.set.set_wifi_pwd,                "pwd"),
                        new XSetupItem(XITEM_CONFIG_HOST,           getString(R.string_str_setup_host),         IpToString(reader.set.set_wifi_host),   "255.255.255.255"),
                        new XSetupItem(XITEM_CONFIG_PORT,           getString(R.string_str_setup_port),         PortToString(reader.set.set_wifi_port), "7000"),
                        new XSetupItem(XITEM_CONFIG_WIFI_READ,      getString(R.string_str_config_read)),
                        new XSetupItem(XITEM_CONFIG_WIFI_WRITE,     getString(R.string_str_config_write)),

                        new XSetupItem(getString(R.string_str_setup_4g)),
                        new XSetupItem(XITEM_CONFIG_CON_TYPE,       getString(R.string_str_setup_con_type), reader.set.set_con_type, "GPRS"),
                        new XSetupItem(XITEM_CONFIG_APN_NAME,       getString(R.string_str_setup_apn_name), reader.set.set_apn_name, "DNMS"),
                        new XSetupItem(XITEM_CONFIG_DEV_NAME,       getString(R.string_str_setup_dev_name), reader.set.set_dev_name, "DOOR-01"),
                        new XSetupItem(XITEM_CONFIG_4G_READ,        getString(R.string_str_config_read)),
                        new XSetupItem(XITEM_CONFIG_4G_WRITE,       getString(R.string_str_config_write)),

                        new XSetupItem(getString(R.string_str_setup_buffer)),
                        new XSetupItem(XITEM_CONFIG_BUFFER_START,   getString(R.string_str_setup_buffer_start)),
                        new XSetupItem(XITEM_CONFIG_BUFFER_STOP,    getString(R.string_str_setup_buffer_stop)),
                        new XSetupItem(XITEM_CONFIG_BUFFER_CLEAN,   getString(R.string_str_setup_buffer_clean)),
                        new XSetupItem(XITEM_CONFIG_BUFFER_TEST,    getString(R.string_str_setup_buffer_test)),
                    }));

                    list.Add(new XSetupItem(XITEM_AREA,             getString(R.string_str_setup_region), 0, arrayArea));
                    list.Add(new XSetupItem(XITEM_SKIP,             getString(R.string_str_setup_skip_freq), true));
                    list.Add(new XSetupItem(XITEM_TX_POWER,         getString(R.string_str_setup_power), reader.set.tx_power, 0, 33, getString(R.string_str_power_dbm)));

                    //==============================
                    //  操作
                    //==============================
                    list.Add(new XSetupItem(getString(R.string_str_setup_operate)));
                    list.Add(new XSetupItem(XITEM_CONFIG_POST, getString(R.string_str_post_config), new[]
                    {
                        new XSetupItem(getString(R.string_str_setup_config)),
                        new XSetupItem(XITEM_CONFIG_POST_URL, getString(R.string_str_post_url), reader.set.set_post_url,BaseSet.DEF_POST_URL),
                    }));

                    //==============================
                    //  排序
                    //==============================
                    if (USE_SORT_LIST_SUPPORT)
                    {
                        list.Add(new XSetupItem(XITEM_SORT, getString(R.string_str_setup_sort), new[]
                        {
                            new XSetupItem(getString(R.string_str_setup_sort)),
                            new XSetupItem(XITEM_SORT_ORDER, getString(R.string_str_setup_sort_order), (TagItem.SortOrder) ? 0 : 1, arraySortOrder),
                            new XSetupItem(XITEM_SORT_FILED, getString(R.string_str_setup_sort_title), TagItem.SortType,            arraySortBy),
                        }));
                    }

                    //==============================
                    //  过滤
                    //==============================
                    if (USE_FILTER_LIST_SUPPORT)
                    {
                        list.Add(new XSetupItem(XITEM_FILTER, getString(R.string_str_setup_filter), new[]
                        {
                            new XSetupItem(getString(R.string_str_setup_filter)),
                            new XSetupItem(XITEM_FILTER_RSSI,    getString(R.string_str_setup_signal),  0,  0, 127),
                            new XSetupItem(XITEM_FILTER_COUNT,   getString(R.string_str_setup_times),   0,  0, 100),
                            new XSetupItem(XITEM_FILTER_CONTAIN, getString(R.string_str_setup_contain), "", ""),
                        }));
                    }

                    //==============================
                    //  分类
                    //==============================
                    if (USE_TREE_LIST_SUPPORT)
                    {
                        list.Add(new XSetupItem(XITEM_TREE, getString(R.string_str_setup_tree), new[]
                        {
                            new XSetupItem(getString(R.string_str_setup_tree)),
                            new XSetupItem(XITEM_TREE_NAME,    getString(R.string_str_setup_root_name),      "ROOT NAME", getString(R.string_str_setup_all)),
                            new XSetupItem(XITEM_TREE_SPLITE,  getString(R.string_str_setup_tree_splite),    "-",         "-"),
                            new XSetupItem(XITEM_TREE_CONTAIN, getString(R.string_str_setup_contain_splite), "_"),
                        }));
                    }

                    //==============================
                    //  显示
                    //==============================
                    list.Add(new XSetupItem(getString(R.string_str_setup_display)));
                    list.Add(new XSetupItem(XITEM_DISPLAY_ORDER, getString(R.string_str_setup_display_invert), TagItem.UseShowEpcInvert));
                    list.Add(new XSetupItem(XITEM_DISPLAY_TEXT, getString(R.string_str_setup_display_text), TagItem.UseShowEpcUtf8));

                    //==============================
                    //  延时显示
                    //==============================
                    if (USE_UPDATE_SLOW_SUPPORT)
                    {
                        //  刷新数量
                        list.Add(new XSetupItem(XITEM_DISPLAY_REFRESH_COUNT, getString(R.string_str_setup_display_refresh_count), "" + 0, "100"));
                        list.Add(new XSetupItem(XITEM_DISPLAY_REFRESH_DELAY, getString(R.string_str_setup_display_refresh_delay), "" + 0, "1000"));
                    }

                    //==============================
                    //  其他
                    //==============================
                    list.Add(new XSetupItem(getString(R.string_str_setup_other)));
                    list.Add(new XSetupItem(XITEM_DEBUG, getString(R.string_str_setup_debug), ""));
                    list.Add(new XSetupItem(XITEM_ABOUT, getString(R.string_str_setup_about), ""));

                    //  末尾添加个空的是为了向上滚动预留空间
                    //list.Add(new XSetupItem(""));

                    //  创建设置 - 管理器
                    manager = new XSetupManager(win, tree, grid, list, this);

                    //===========================================
                    //  注册 - 根界面,
                    //          根界面必须注册。
                    //          方便子界面中访问根界面！
                    //          子界面可以无穷展开！
                    //===========================================
                    XSetupManager.Register(manager);
                }
                else
                {
                    //  获取设置(当前设置号)
                    var item = XSetupManager.GetInstance().FindSetupItem(curItemId);
                    //  有效设置
                    if (item != null)
                    {
                        // 有设置列表?
                        if (item.subMenu != null)
                        {
                            //  创建 - 子界面管理器
                            manager = new XSetupManager(win, tree, grid, item.subMenu, this);
                        }
                    }
                }
            }
            else
            {
                //  读取设置并刷新
                manager.SetupUpdate(false);
            }
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 转换
        /// </summary>
        /// <param name="port"></param>
        /// <returns></returns>
        ///-------------------------------------------------------------------------------------------------------------
        private static string PortToString(int port)
        {
            return "" + port;
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 地址转换
        /// </summary>
        /// <param name="bytes"></param>
        /// <returns></returns>
        ///-------------------------------------------------------------------------------------------------------------
        private static string IpToString(byte[] bytes)
        {
            string s = "";
            foreach (var e in bytes)
            {
                s += String.Format("{0}", e);
                s += ".";
            }
            return s.Substring(0, s.Length - 1);
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 设置响应
        /// </summary>
        /// <param name="msg">消息</param>
        /// <param name="item">项目</param>
        /// <param name="id">标号</param>
        /// <param name="on">逻辑</param>
        /// <param name="pos">选择</param>
        /// <param name="text">文本</param>
        /// <param name="param">参数</param>
        ///-------------------------------------------------------------------------------------------------------------
        public void OnSetupEvent(ESetupMessage msg, XSetupItem item, int id, bool @on, int pos, string text, object param)
        {
            //  应用
            var theApp = App.GetInstance();
            //  系统
            var rfidSystem = theApp.rfidSystem;
            //  管理器
            var rfidMan = rfidSystem.GetRfidManager();
            //  读写器
            var reader = rfidMan.GetReader();
            //  设置
            var set = reader.set;

            //  暂停
            rfidSystem.Pause();
            //  清空
            rfidMan.CleanCmd();

            //  停止
            rfidSystem.PortWrite(reader.cmd.rfid_stop(),200);

            //  支持抛数据
            if (DEBUG)
            {
                Debug.Print(TAG + "OnSetupEvent(msg:" + msg + ",item:" + item.name + ",id:" + id + ",on:" + on + ",pos:" + pos + ",text:" + text + ")");
            }

            // 默认是序号
            var num = pos;

            // 列表换算
            if (item.listDatas != null)
            {
                num = item.listDatas[pos % item.listDatas.Length];
            }

            switch (id)
            {
                //=====================
                //   指令集
                //=====================
                case XITEM_INSTRUCTION:
                    //  切换指令集
                    Debug.Print(TAG + "指令集 : " + pos);
                    //  从新初始化
                    rfidSystem.SetInit(false);
                    //  切换
                    rfidMan.SetReaderType(arrayReaderTypes[pos % arrayReaderTypes.Length]);
                    break;

                //=====================
                //   会话开关
                //=====================
                case XITEM_SESSION_ONOFF:
                    Debug.Print(TAG + "会话开关 : " + on);
                    reader.set.session_onoff = on;
                    break;

                //=====================
                //   会话模式
                //=====================
                case XITEM_SESSION_MODE:
                    Debug.Print(TAG + "会话模式 : " + pos);
                    reader.set.session_mode = pos;
                    break;

                //=====================
                //   会话目标
                //=====================
                case XITEM_SESSION_TARGET:
                    Debug.Print(TAG + "会话目标 : " + pos);
                    reader.set.session_target = pos;
                    break;

                //=====================
                //  Q值
                //=====================
                case XITEM_SESSION_Q_VALUE:
                    Debug.Print(TAG + "Q : " + pos);
                    reader.set.inventory_Q = pos;
                    break;

                //=====================
                //  扫描时间
                //=====================
                case XITEM_SESSION_SCAN_TIME:
                    Debug.Print(TAG + "扫描时间 : " + pos);
                    reader.set.session_scan_time = pos;
                    break;

                //=====================
                //  扫描时间
                //=====================
                case XITEM_SESSION_INVERT_EN:
                    Debug.Print(TAG + "翻转开关 : " + on);
                    reader.set.session_invert_enable = on;
                    break;

                //=====================
                //  扫描时间
                //=====================
                case XITEM_SESSION_INVERT_NUM:
                    Debug.Print(TAG + "翻转次数 : " + pos);
                    reader.set.session_invert_number = pos;
                    break;

                //=====================
                //  通道
                //=====================
                case XITEM_CHANNELS:
                    Debug.Print(TAG + "通道数:" + num);
                    //  设置通道数
                    reader.SetChannels(num);
                    //  天线选择
                    var curItem = manager.FindSetupItem(XITEM_ANTENNA);
                    //  创建菜单
                    if (curItem != null)
                    {
                        //  标题
                        var arrayAntTitle = new string[num];
                        //  检查
                        var arrayAntCheck = new bool[num];
                        //  默认都要选中
                        for (var i = 0; i < num; i++)
                        {
                            arrayAntTitle[i] = getString(R.string_str_setup_antanna) + (i + 1);
                            arrayAntCheck[i] = (reader.GetAntennas() & (1 << i)) != 0;
                        }

                        //  创建选择模式
                        curItem.MakeChecks(XITEM_ANTENNA, getString(R.string_str_setup_antanna), arrayAntTitle, arrayAntCheck);

                        //  刷新天线值
                        manager.UpdateItem(manager.FindSetupItem(XITEM_ANTENNA), App.Current);

                        //  界面刷新
                        manager.Update(App.Current);
                    }
                    break;

                //=====================
                //  天线
                //=====================
                case XITEM_ANTENNA:
                    Debug.Print(TAG + "天线参数:" + pos);
                    //  开关参数
                    if (param is bool[])
                    {
                        //  转换类型
                        var checks = (bool[])param;
                        //  天线数据
                        long antennas = 0;
                        //  按位计算
                        for (var i = 0; ((i < checks.Length) && (i < 32)); i++)
                        {
                            if (checks[i])
                            {
                                antennas |= 1 << i;
                            }
                        }

                        Debug.Print(TAG + "天线参数:{0:X}", antennas);

                        //  设置天线
                        reader.SetAntennas(antennas);
                        //    刷新
                        manager.Update(App.Current);
                    }
                    break;

                //=====================
                //  切换次数
                //=====================
                case XITEM_SWITCH_TIMES:
                    Debug.Print(TAG + "切换:" + pos);
                    //  切换
                    reader.set.antanna_switch_times = pos;
                    break;

                //=====================
                // 省电模式
                //=====================
                case XITEM_CONFIG_POWER_SAVE:
                    Debug.Print(TAG + "配置-省电模式:" + on);
                    reader.set.set_power_save = on;
                    break;

                //=====================
                // 开机模式:蓝牙、WIFI，4G
                //=====================
                case XITEM_CONFIG_MODE:
                    Debug.Print(TAG + "配置-开机模式:" + pos);
                    reader.set.set_mode = (byte)pos;
                    break;

                //=====================
                // WIFI - 网络 名称
                //=====================
                case XITEM_CONFIG_SSID:
                    Debug.Print(TAG + "配置-网名:" + item.textValue);
                    reader.set.set_wifi_ssid = item.textValue;
                    break;

                //=====================
                // WIFI - 网络 密码
                //=====================
                case XITEM_CONFIG_PWD:
                    Debug.Print(TAG + "配置-密码:" + item.textValue);
                    reader.set.set_wifi_pwd = item.textValue;
                    break;

                //=====================
                // WIFI - 主机地址
                //=====================
                case XITEM_CONFIG_HOST:
                    Debug.Print(TAG + "配置-地址:" + item.textValue);
                    reader.set.set_wifi_host = StringHelper.StringToIp(item.textValue);
                    if (reader.set.set_wifi_host == null)
                    {
                        reader.set.set_wifi_host = new byte[] { (byte)255, (byte)255, (byte)255, (byte)255 };
                    }
                    break;

                //=====================
                // WIFI - 主机端口
                //=====================
                case XITEM_CONFIG_PORT:
                    Debug.Print(TAG + "配置-端口:" + item.textValue);
                    reader.set.set_wifi_port = Convert.ToInt16(item.textValue);
                    break;

                //=====================
                // 4G - 承载类型
                //=====================
                case XITEM_CONFIG_CON_TYPE:
                    Debug.Print(TAG + "承载类型:" + item.textValue);
                    reader.set.set_con_type = item.textValue;
                    break;

                //=====================
                // 4G - APN
                //=====================
                case XITEM_CONFIG_APN_NAME:
                    Debug.Print(TAG + "APN:" + item.textValue);
                    reader.set.set_apn_name = item.textValue;
                    break;

                //=====================
                // 4G - 设备名
                //=====================
                case XITEM_CONFIG_DEV_NAME:
                    Debug.Print(TAG + "DEV NAME:" + item.textValue);
                    reader.set.set_dev_name = item.textValue;
                    break;

                //===============
                //  读取配置
                //===============
                case XITEM_CONFIG_WIFI_READ:
                    Debug.Print(TAG + "读取WIFI配置");
                    Config_Action_Wifi_Read(rfidMan);
                    break;

                //===============
                //  写入配置
                //===============
                case XITEM_CONFIG_WIFI_WRITE:
                    Debug.Print(TAG + "写入WIFI配置");
                    Config_Action_Wifi_Write(rfidMan);
                    break;

                //===============
                //  读取配置
                //===============
                case XITEM_CONFIG_4G_READ:
                    Debug.Print(TAG + "读取4G配置");
                    Config_Action_4G_Read(rfidMan);
                    break;

                //===============
                //  写入配置
                //===============
                case XITEM_CONFIG_4G_WRITE:
                    Debug.Print(TAG + "写入4G配置");
                    Config_Action_4G_Write(rfidMan);
                    break;

                //===============
                //  开始
                //===============
                case XITEM_CONFIG_BUFFER_START:
                    Debug.Print(TAG + "开始");
                    Config_Action_Command(rfidMan, RfidConfig.CFG_TYPE_SET_BUFFER_START);
                    break;

                //===============
                //  停止
                //===============
                case XITEM_CONFIG_BUFFER_STOP:
                    Debug.Print(TAG + "停止");
                    Config_Action_Command(rfidMan, RfidConfig.CFG_TYPE_SET_BUFFER_STOP);
                    break;

                //===============
                //  清除
                //===============
                case XITEM_CONFIG_BUFFER_CLEAN:
                    Debug.Print(TAG + "清除");
                    Config_Action_Command(rfidMan, RfidConfig.CFG_TYPE_SET_BUFFER_CLEAR);
                    break;

                //===============
                //  测试
                //===============
                case XITEM_CONFIG_BUFFER_TEST:
                    Debug.Print(TAG + "测试");
                    Config_Action_Command(rfidMan, RfidConfig.CFG_TYPE_SET_BUFFER_TEST);
                    break;

                //=====================
                //  区域
                //=====================
                case XITEM_AREA:
                    Debug.Print(TAG + "区域:" + pos);
                    rfidSystem.PortWrite(reader.GetCmd().rfid_set_region((byte)(1 + pos)), 200);
                    break;

                //=====================
                //  频率
                //=====================
                case XITEM_FREQ:
                    Debug.Print(TAG + "频率:" + pos);
                    break;

                //=====================
                //  跳频
                //=====================
                case XITEM_SKIP:
                    Debug.Print(TAG + "跳频:" + on);
                    //  M100才需要跳帧处理
                    if (reader is RfidM100Reader)
                    {
                        rfidSystem.PortWrite(RfidM100Cmd.cmd_set_auto(on));
                    }
                    break;

                //=====================
                //  功率
                //=====================
                case XITEM_TX_POWER:
                    Debug.Print(TAG + "功率:" + pos);
                    rfidSystem.PortWrite(reader.GetCmd().rfid_set_tx_power(pos), 300);
                    break;

                //=====================
                //  反向
                //=====================
                case XITEM_DISPLAY_ORDER:
                    Debug.Print(TAG + "反向:" + on);
                    TagItem.UseShowEpcInvert = on;
                    TagItem.UpdateSetup(this, true);
                    break;

                //=====================
                //  次数
                //=====================
                case XITEM_TIMES:
                    Debug.Print(TAG + "每秒次数:" + pos);
                    //  每秒钟扫描次数
                    set.scan_times = num;
                    break;

                //=====================
                //  轮询
                //=====================
                case XITEM_INVENTRY:
                    Debug.Print(TAG + "单个天线轮询次数:" + pos);
                    //  每次轮询次数
                    set.inventory_times = pos;
                    break;

                //=====================
                //  间隔
                //=====================
                case XITEM_INTERVAL:
                    Debug.Print(TAG + "切换天线间隔:" + pos);
                    set.sw_interval = pos;
                    break;

                //=====================
                //  排序方向
                //=====================
                case XITEM_SORT_ORDER:
                    Debug.Print(TAG + "顺序:" + pos);
                    TagItem.SortOrder = (pos == 0);
                    TagItem.UpdateSetup(this, true);
                    break;

                //=====================
                //  排序字段
                //=====================
                case XITEM_SORT_FILED:
                    Debug.Print(TAG + "字段:" + pos);
                    TagItem.SortType = pos;
                    TagItem.UpdateSetup(this, true);
                    break;

                //=====================
                //  名称
                //=====================
                case XITEM_TREE_NAME:
                    Debug.Print(TAG + "根节点:" + item.textValue);
                    //MainActivity.GetTree().SetRootName(item.textValue);
                    //MainActivity.GetTree().UpdateSetup(this, true);
                    break;

                //=====================
                //  分隔符
                //=====================
                case XITEM_TREE_SPLITE:
                    Debug.Print(TAG + "分隔符号:" + item.textValue);
                    //MainActivity.GetTree().SetSplite(item.textValue);
                    //MainActivity.GetTree().UpdateSetup(this, true);
                    break;

                //=====================
                //  包含
                //=====================
                case XITEM_TREE_CONTAIN:
                    Debug.Print(TAG + "包含分隔符号:" + item.boolValue);
                    //MainActivity.GetTree().SetContain(item.boolValue);
                    //MainActivity.GetTree().UpdateSetup(this, true);
                    break;

                //=====================
                //  显示文本
                //=====================
                case XITEM_DISPLAY_TEXT:
                    Debug.Print(TAG + "显示文本 - :" + on);
                    TagItem.UseShowEpcUtf8 = on;
                    TagItem.UpdateSetup(this, true);
                    break;

                //=====================
                //  数量
                //=====================
                case XITEM_DISPLAY_REFRESH_COUNT:
                    Debug.Print(TAG + "刷新抑制 - 数量:" + item.textValue);
                    //theApp.Settings.refreshCount = Integer.parseInt(item.textValue);
                    //theApp.Settings.SetupUpdate(this, true);
                    break;

                //=====================
                //  延时
                //=====================
                case XITEM_DISPLAY_REFRESH_DELAY:
                    Debug.Print(TAG + "刷新抑制 - 间隔:" + item.textValue);
                    //theApp.Settings.refreshDelay = Integer.parseInt(item.textValue);
                    //theApp.Settings.SetupUpdate(this, true);
                    break;

                default:
                    break;
            }

            // 保存
            rfidSystem.SetupUpdate(true);
            // 更新
            manager.SetupUpdate(true);
        }

        private const int CONFIG_DELAY = 200;

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 读取 - 配置 - WIFI
        /// </summary>
        /// <param name="man"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void Config_Action_Wifi_Read(RfidManager man)
        {
            if (DEBUG)
            {
                Debug.Print("Config_Action_Wifi_Read()");
            }

            //  配置
            man.config.setEven(this);

            //  读取参数
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_POWER | RfidConfig.CFG_TYPE_READ, 0, null), CONFIG_DELAY);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_BEEPER | RfidConfig.CFG_TYPE_READ, 0, null), CONFIG_DELAY);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_AUTO | RfidConfig.CFG_TYPE_READ, 0, null), CONFIG_DELAY);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_MODE | RfidConfig.CFG_TYPE_READ, 0, null), CONFIG_DELAY);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_HOST | RfidConfig.CFG_TYPE_READ, 0, null), CONFIG_DELAY);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_PORT | RfidConfig.CFG_TYPE_READ, 0, null), CONFIG_DELAY);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_SSID | RfidConfig.CFG_TYPE_READ, 0, null), CONFIG_DELAY);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_PWD | RfidConfig.CFG_TYPE_READ, 0, null), CONFIG_DELAY);

            MessageBox.Show("读取完成", "提示", MessageBoxButton.OK, MessageBoxImage.Information);
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 写入
        /// </summary>
        /// <param name="man"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void Config_Action_Wifi_Write(RfidManager man)
        {

            if (DEBUG)
            {
                Debug.Print("Config_Action_Wifi_Write()");
            }

            BaseReader reader = man.GetReader();
            BaseSet set = reader.set;
            ByteStream bs = new ByteStream(0);

            man.CleanCmd();

            //  省电
            bs.put(set.set_power_save);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_POWER, 0, bs.getData()), CONFIG_DELAY);

            //  蜂鸣器
            bs.reset();
            bs.put(set.set_beeper);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_BEEPER, 0, bs.getData()), CONFIG_DELAY);

            //  自动读取
            bs.reset();
            bs.put(set.set_auto_read);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_AUTO, 0, bs.getData()), CONFIG_DELAY);

            //  模式
            bs.reset();
            bs.put(set.set_mode);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_MODE, 0, bs.getData()), CONFIG_DELAY);

            //  自动
            bs.reset();
            bs.put(set.set_auto_read);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_AUTO, 0, bs.getData()), CONFIG_DELAY);

            //  设置地址
            bs.reset();
            bs.put(set.set_wifi_host);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_HOST, 0, bs.getData()), CONFIG_DELAY);

            //  设置端口
            bs.reset();
            bs.put((short)set.set_wifi_port & 0xffff);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_PORT, 0, bs.getData()), CONFIG_DELAY);

            //===========================
            //  网络名,分三次
            //===========================
            byte[] FixSsid = StringHelper.makeFixData(set.set_wifi_ssid, 32);
            byte[] FixPwd = StringHelper.makeFixData(set.set_wifi_pwd, 16);

            //===========================
            //  SSID(32)
            //===========================
            bs.reset();
            bs.put(FixSsid, 0, 11);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_SSID, 0, bs.getData()), CONFIG_DELAY);

            bs.reset();
            bs.put(FixSsid, 11, 11);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_SSID, 11, bs.getData()), CONFIG_DELAY);

            bs.reset();
            bs.put(FixSsid, 22, 10);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_SSID, 22, bs.getData()), CONFIG_DELAY);

            //===========================
            //  PWD(16)
            //===========================
            bs.reset();
            bs.put(FixPwd, 0, 8);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_PWD, 0, bs.getData()), CONFIG_DELAY);

            bs.reset();
            bs.put(FixPwd, 8, 8);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_PWD, 8, bs.getData()), CONFIG_DELAY);

            MessageBox.Show("写入完成", "提示", MessageBoxButton.OK, MessageBoxImage.Information);
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 读取4G参数
        /// </summary>
        /// <param name="manager"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void Config_Action_4G_Read(RfidManager manager)
        {
            if (DEBUG)
            {
                Debug.Print("Config_Action_4G_Read()");
            }

            manager.config.setEven(this);

            manager.PutCmd(manager.config.send(RfidConfig.CFG_TYPE_SET_CON_TYPE | RfidConfig.CFG_TYPE_READ, 0, null), CONFIG_DELAY);
            manager.PutCmd(manager.config.send(RfidConfig.CFG_TYPE_SET_APN_NAME | RfidConfig.CFG_TYPE_READ, 0, null), CONFIG_DELAY);
            manager.PutCmd(manager.config.send(RfidConfig.CFG_TYPE_SET_DEV_NAME | RfidConfig.CFG_TYPE_READ, 0, null), CONFIG_DELAY);

            MessageBox.Show("读取完成", "提示", MessageBoxButton.OK, MessageBoxImage.Information);
        }

        /**
         * 写4G数据
         *
         * @param man ： 管理器
         */
        private void Config_Action_4G_Write(RfidManager man)
        {
            if (DEBUG)
            {
                Debug.Print("Config_Action_4G_Write()");
            }

            BaseReader reader = man.GetReader();
            BaseSet set = reader.set;
            ByteStream bs = new ByteStream(0);

            //  4G设置
            byte[] FixCon = StringHelper.makeFixData(set.set_con_type, 16);
            byte[] FixApn = StringHelper.makeFixData(set.set_apn_name, 16);
            byte[] FixDev = StringHelper.makeFixData(set.set_dev_name, 16);

            //===========================
            //  CON(16)
            //===========================
            bs.reset();
            bs.put(FixCon, 0, 8);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_CON_TYPE, 0, bs.getData()), CONFIG_DELAY);

            bs.reset();
            bs.put(FixCon, 8, 8);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_CON_TYPE, 8, bs.getData()), CONFIG_DELAY);

            //===========================
            //  APN(16)
            //===========================
            bs.reset();
            bs.put(FixApn, 0, 8);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_APN_NAME, 0, bs.getData()), CONFIG_DELAY);

            bs.reset();
            bs.put(FixApn, 8, 8);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_APN_NAME, 8, bs.getData()), CONFIG_DELAY);

            //===========================
            //  DEV(16)
            //===========================
            bs.reset();
            bs.put(FixDev, 0, 8);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_DEV_NAME, 0, bs.getData()), CONFIG_DELAY);

            bs.reset();
            bs.put(FixDev, 8, 8);
            man.PutCmd(man.config.send(RfidConfig.CFG_TYPE_SET_DEV_NAME, 8, bs.getData()), CONFIG_DELAY);

            //new MsgMake(this, EMessageType.MSG_SUCCESS, getString(R.string.str_write_done), MESSAGE_DELAY_CNT);
            
            MessageBox.Show("写入完成", "提示", MessageBoxButton.OK, MessageBoxImage.Information);

        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 命令
        /// </summary>
        /// <param name="man"></param>
        /// <param name="cmd"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void Config_Action_Command(RfidManager man, int cmd)
        {
            if (DEBUG)
            {
                Debug.Print("Config_Action_Command()");
            }

            BaseReader reader = man.GetReader();
            //  省电
            man.PutCmd(man.config.send(cmd, 0, null), CONFIG_DELAY);
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 配置消息处理
        /// </summary>
        /// <param name="config"></param>
        /// <param name="type"></param>
        /// <param name="offset"></param>
        /// <param name="data"></param>
        ///-------------------------------------------------------------------------------------------------------------
        void IConfigEvent.OnConfigEvent(RfidConfig config, int type, int offset, byte[] data)
        {
            BaseReader reader = config.getManager().GetReader();
            BaseSet set = reader.set;

            if (DEBUG)
            {
                Debug.Print("OnConfigEvent(type:{0:X2}{1:X2})", type, type & 0x7F);
            }

            switch (type & 0x7F)
            {
                //  蜂鸣器
                case RfidConfig.CFG_TYPE_SET_BEEPER:
                    manager.SetValue(this, XITEM_CONFIG_BEEPER, set.set_beeper, true);
                    break;

                //  省电模式
                case RfidConfig.CFG_TYPE_SET_POWER:
                    manager.SetValue(this, XITEM_CONFIG_POWER_SAVE, set.set_power_save, true);                    
                    break;

                // 自动读取
                case RfidConfig.CFG_TYPE_SET_AUTO:
                    manager.SetValue(this, XITEM_CONFIG_AUTO, set.set_auto_read, true);
                    break;

                //  模式
                case RfidConfig.CFG_TYPE_SET_MODE:
                    manager.SetValue(this, XITEM_CONFIG_MODE, set.set_mode, true);
                    break;

                //  WIFI-网络名
                case RfidConfig.CFG_TYPE_SET_SSID:
                    manager.SetValue(this, XITEM_CONFIG_SSID, set.set_wifi_ssid, true);
                    break;

                //  WIFI-密码
                case RfidConfig.CFG_TYPE_SET_PWD:
                    manager.SetValue(this, XITEM_CONFIG_PWD, set.set_wifi_pwd, true);
                    break;

                //  WIFI-主机
                case RfidConfig.CFG_TYPE_SET_HOST:
                    manager.SetValue(this, XITEM_CONFIG_HOST, IpToString(set.set_wifi_host), true);
                    break;

                //  WIFI-端口
                case RfidConfig.CFG_TYPE_SET_PORT:
                    manager.SetValue(this, XITEM_CONFIG_PORT, PortToString(set.set_wifi_port), true);
                    break;

                //  4G-承载类型
                case RfidConfig.CFG_TYPE_SET_CON_TYPE:
                    manager.SetValue(this, XITEM_CONFIG_CON_TYPE, set.set_con_type, true);
                    break;

                //  4G-APN名称
                case RfidConfig.CFG_TYPE_SET_APN_NAME:
                    manager.SetValue(this, XITEM_CONFIG_APN_NAME, set.set_apn_name, true);
                    break;

                //  4G-设备名称
                case RfidConfig.CFG_TYPE_SET_DEV_NAME:
                    manager.SetValue(this, XITEM_CONFIG_DEV_NAME, set.set_dev_name, true);
                    break;
            }

            //  更新
            manager.Update(App.Current);
        }       
    }
}