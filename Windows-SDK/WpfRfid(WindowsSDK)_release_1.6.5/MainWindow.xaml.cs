using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using System.IO;
using System.IO.Ports;
using CommLib.comm;
using Microsoft.Win32;
using RfidLib;
using RfidLib.comm;
using RfidLib.Port;
using RfidLib.rfid;
using RfidLib.rfid.based;
using WpfRfid.Mode;

namespace WpfRfid
{
    ///=================================================================================================================
    /// <summary>
    /// 主界面的交互逻辑
    /// </summary>
    ///=================================================================================================================
    public partial class MainWindow : Window, IRfidEvent, IPortEvent
    {
        /// 调试
#if DEBUG
        public const bool DEBUG = true;
#else
        public const bool DEBUG = false;
#endif


        /// 目标
        private const string TAG = "MainWindow";

        /// UDP接口
        private const int NET_TYPE_UDP = 0;

        /// TCP接口
        private const int NET_TYPE_TCP = 1;

        /// 消息 - 数据源
        private readonly ObservableCollection<string> listMsgs = new ObservableCollection<string>();

        /// 标签 - 数据源
        private readonly ObservableCollection<CsTagItem> listTags = new ObservableCollection<CsTagItem>();

        /// 读写器 - 类型
        private readonly EReaderType[] arrayEReaderType =
        {
            EReaderType.READER_TYPE_M100,
            EReaderType.READER_TYPE_R2000,
            EReaderType.READER_TYPE_J2000,
            EReaderType.READER_TYPE_G2000,
            EReaderType.READER_TYPE_PR9200,
            EReaderType.READER_TYPE_SLR5600,
        };
        
        /// 起始字节
        private const int EPC_START_POS = 4;

        /// 超时时间 100ms
        private const int RESPONE_TIMEOUT = 100;

        /// 当前选中的标签
        private TagItem curSelect;

        /// 当前项目
        //private TagItem curNewItem;

        /// 延时显示 
        private long delayShow;

        /// 设置 
        private SetupDelegate setup;

        /// 刷新忙
        private static bool IsUpdateBusy = false;

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 关闭窗口
        /// </summary>
        /// <param name="e">消息</param>
        ///-------------------------------------------------------------------------------------------------------------
        protected override void OnClosed(EventArgs e)
        {
            base.OnClosed(e);

            if (DEBUG)
            {
                Debug.WriteLine("结束");
            }

            Environment.Exit(0);
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 主界面
        /// </summary>
        ///-------------------------------------------------------------------------------------------------------------
        public MainWindow()
        {
            //  初始化
            InitializeComponent();

            //  数据上下文
            DataContext = this;

            //  消息数据源
            list_status.ItemsSource = listMsgs;

            //  标签数据源
            ListView_Inventory.ItemsSource = listTags;

            //  标签列表
            ListView_Read.ItemsSource = listTags;
            
            //  管理器
            var rfidMan = App.GetRfidManager();

            var portMan = App.GetPortManager();
            //  设置 - 标签消息 - 监听
            rfidMan.SetRfidEvent(this);
            //  设置 - 端口消息 - 监听
            portMan.SetPortEvent(this);

            //    初始化串口列表
            _ = InitUartPortsAsync();
            //    初始化USB口列表
            _ = InitUsbPortsAsync();
            //    初始化蓝牙接口列表
            _ = InitBlePortsAsync();

            //  扫描设备(BLE)
            portMan.GetBleManager().Scan(true);
            //  扫描设备(USB)
            portMan.GetUsbManager().Scan(true);
            //  扫描设备(UART)
            portMan.GetUartManager().Scan(true);
            //  扫描设备(UART)
            portMan.GetUdpManager().Scan(true);
            //  扫描设备(UART)
            portMan.GetTcpManager().Scan(true);

            //  设置数据
            setup = new SetupDelegate(this, TreeSetup, GridSetup);

            // 恢复记忆
            var sel = (int) rfidMan.GetReaderType();

            // 读写器类型
            cmb_reader_type.SelectedIndex = sel;

            // 默认类型
            var portType = portMan.GetPortType();

            switch (portType)
            {
                //  UDP类型
                case EPortType.PORT_TYPE_UDP:
                    cmb_net_type.SelectedIndex = NET_TYPE_UDP;
                    text_net_host.Text         = portMan.GetUdpManager().GetHost();
                    text_net_port.Text         = portMan.GetUdpManager().GetPort() + "";
                    break;

                // TCP类型
                case EPortType.PORT_TYPE_TCP:
                    cmb_net_type.SelectedIndex = NET_TYPE_TCP;
                    text_net_host.Text         = portMan.GetTcpManager().GetHost();
                    text_net_port.Text         = portMan.GetTcpManager().GetPort() + "";
                    break;

                // 类型
                default:
                    cmb_net_type.SelectedIndex = NET_TYPE_UDP;
                    text_net_host.Text         = portMan.GetUdpManager().GetHost();
                    text_net_port.Text         = portMan.GetUdpManager().GetPort() + "";
                    break;
            }

            //  网络类型选中
            cmb_net_type.SelectionChanged += CmbNetTypeOnSelectionChanged;
            //  文字改变
            text_net_host.TextChanged += TextNetHostOnTextChanged;
            text_net_port.TextChanged += TextNetPortOnTextChanged;

            //  读写器
            var reader = rfidMan.GetReader();
            //  天线数量
            var antCount = reader.GetChannels();

            Debug.Print("-->读写器 : {0}", reader.GetName());
            Debug.Print("-->天线数 : {0}", antCount);

            if(antCount == 1)
            {
                radio_ch1.IsChecked = true;
            }
            else if (antCount == 4)
            {
                radio_ch4.IsChecked = true;
            }
            else if (antCount == 8)
            {
                radio_ch8.IsChecked = true;
            }
            else if (antCount == 16)
            {
                radio_ch16.IsChecked = true;
            }

        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 选中类型
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void CmbNetTypeOnSelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            var portMan = App.GetPortManager();
            var netType = cmb_net_type.SelectedIndex;
            
            switch (netType)
            {
                case NET_TYPE_UDP:
                    text_net_host.Text = portMan.GetUdpManager().GetHost();
                    text_net_port.Text = portMan.GetUdpManager().GetPort() + "";
                    App.GetSystem().SetupUpdate(true);
                    break;

                case NET_TYPE_TCP:
                    text_net_host.Text = portMan.GetTcpManager().GetHost();
                    text_net_port.Text = portMan.GetTcpManager().GetPort() + "";
                    App.GetSystem().SetupUpdate(true);
                    break;
            }
        }

        #region "网络设置"

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 网络连接
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void Btn_net_connect_Click(object sender, RoutedEventArgs e)
        {
            PortBase port;

            var portMan = App.GetPortManager();
            var rfidMan = App.GetRfidManager();

            var netType = cmb_net_type.SelectedIndex;
            var netHost = text_net_host.Text;
            var netPort = text_net_port.Text;

            switch (netType)
            {
                //============
                //    UDP
                //============
                case NET_TYPE_UDP:
                    //  端口                    
                    port = portMan.GetPort(EPortType.PORT_TYPE_UDP);
                    //  管理器
                    var udpMan = portMan.GetUdpManager();
                    //  主机
                    udpMan.SetHost(netHost);
                    //  端口
                    udpMan.SetPort(int.Parse(netPort));

                    //  断开连接
                    if (!port.IsConnected())
                    {
                        //  连接
                        port.Connect();
                        port.WaitTime();
                        btn_net_connect.Content = "连接中...";
                    }
                    else
                    {
                        port.Disconnect();
                        Log_d(TAG, "UDP-断开 : " + netHost + ":" + netPort);
                    }

                    App.GetSystem().SetupUpdate(true);
                    break;

                //============
                // TCP
                //============
                case NET_TYPE_TCP:
                    //  端口
                    port = portMan.GetPort(EPortType.PORT_TYPE_TCP);
                    //    管理器
                    var tcpMan = portMan.GetTcpManager();
                    //    选择
                    tcpMan.SetHost(netHost);
                    //    端口
                    tcpMan.SetPort(int.Parse(netPort));

                    //  断开连接
                    if (!port.IsConnected())
                    {
                        port.Connect();
                        port.WaitTime();
                        btn_net_connect.Content = "连接中...";
                    }
                    else
                    {
                        port.Disconnect();
                        Log_d(TAG, "TCP-断开 : " + netHost + ":" + netPort);
                    }

                    App.GetSystem().SetupUpdate(true);

                    break;

                default:
                    break;
            }
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 文字改变
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void TextNetPortOnTextChanged(object sender, TextChangedEventArgs e)
        {
            var port = 0;
            port = short.Parse(text_net_port.Text);
            //---------
            // UDP
            //---------
            if (cmb_net_type.SelectedIndex == NET_TYPE_UDP)
            {
                App.GetPortManager().GetUdpManager().SetPort(port);
                App.GetSystem().SetupUpdate(true);
            }
            //---------
            // TCP
            //---------
            else if (cmb_net_type.SelectedIndex == NET_TYPE_TCP)
            {
                App.GetPortManager().GetTcpManager().SetPort(port);
                App.GetSystem().SetupUpdate(true);
            }
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 文字改变
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void TextNetHostOnTextChanged(object sender, TextChangedEventArgs e)
        {
            var host = text_net_host.Text;
            //---------
            // UDP
            //---------
            if (cmb_net_type.SelectedIndex == NET_TYPE_UDP)
            {
                App.GetPortManager().GetUdpManager().SetHost(host);
                App.GetSystem().SetupUpdate(true);
            }
            //---------
            // TCP
            //---------
            else if (cmb_net_type.SelectedIndex == NET_TYPE_TCP)
            {
                App.GetPortManager().GetTcpManager().SetHost(host);
                App.GetSystem().SetupUpdate(true);
            }
        }

        #endregion

        #region "管理器

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 获取端口管理器
        /// </summary>
        /// <returns>管理器</returns>
        ///-------------------------------------------------------------------------------------------------------------
        private static PortManager GetPortManager()
        {
            return App.GetPortManager();
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 获取标签管理器
        /// </summary>
        /// <returns>管理器</returns>
        ///-------------------------------------------------------------------------------------------------------------
        private static RfidManager GetRfidManager()
        {
            return App.GetRfidManager();
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 切换 - 端口
        /// </summary>
        /// <param name="ePort"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private static void SetManagerPort(EPortType ePort)
        {
            GetPortManager().SetType(ePort);
            GetRfidManager().SetPort(GetPortManager().GetPort());
        }

        #endregion

        #region "刷新设备"

        ///=============================================================================================================
        /// <summary>
        /// 初始化串口列表
        /// </summary>
        /// <returns></returns>
        ///=============================================================================================================
        private async Task InitUartPortsAsync()
        {
            //    异步
            Action<ComboBox, string[], int> updateAction = Update_Combo_UI;
            //    调用
            await Dispatcher.BeginInvoke(updateAction, cmb_uart_devices, GetPortManager().GetUartManager().GetDevices(), 0);
        }

        ///=============================================================================================================
        /// <summary>
        /// 初始化USB端口列表
        /// </summary>
        /// <returns></returns>
        ///=============================================================================================================
        private async Task InitUsbPortsAsync()
        {
            //  异步
            Action<ComboBox, string[], int> updateAction = Update_Combo_UI;
            //  调用
            await Dispatcher.BeginInvoke(updateAction, cmb_usb_devices, GetPortManager().GetUsbManager().GetDevices(), 0);
        }

        ///=============================================================================================================
        /// <summary>
        /// 初始化蓝牙列表
        /// </summary>
        /// <returns></returns>
        ///=============================================================================================================
        private async Task InitBlePortsAsync()
        {
            var man = GetPortManager().GetBleManager();
            //  异步
            Action<ComboBox, string[], int> updateAction = Update_Combo_UI;
            //  调用
            await Dispatcher.BeginInvoke(updateAction, cmb_ble_devices, man.GetDevices(), 0);
        }

        #endregion

        #region "开始和清除按钮"

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 
        /// 强制停止，或开始
        /// </summary>
        /// <param name="stop">停止</param>
        ///-------------------------------------------------------------------------------------------------------------
        private void ActionStartStop(bool stop)
        {
            var system = App.GetSystem();
            if (stop)
            {
                system.Stop();
                Button_Start.Content = "开始";
            }
            else
            {
                system.Start();
                Button_Start.Content = "停止";
            }
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 开始 - 按钮
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void Button_Start_Click(object sender, RoutedEventArgs e)
        {
            var system = App.GetSystem();

            //    运行
            if (system.IsRunning())
            {
                ActionStartStop(true);
                Log_d(TAG, "停止");
            }
            else
            {
                //    连接成功
                if (system.GetPortManager().IsConnected())
                {
                    ActionStartStop(false);
                    Log_d(TAG, "开始");
                }
                else
                {
                    MessageBox.Show("设备还没连接,请先连接设备!", "提示", MessageBoxButton.OK, MessageBoxImage.Warning);
                }
            }
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 清除 - 按钮
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void Button_Clean_Click(object sender, RoutedEventArgs e)
        {
            //    清空
            App.GetRfidManager().Clear();
            //    更新列表
            Update_All_List();

            Log_d(TAG, "清空数据");
        }

        #endregion

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 刷新列表
        /// </summary>
        ///-------------------------------------------------------------------------------------------------------------
        private void Update_All_List()
        {
            //  管理器
            var man = App.GetRfidManager();
            //  文字
            var text = "" + man.GetList().Count;
            //  异步更新文本框
            Action<Label, ListView, string, List<TagItem>> updateAction = Update_Total;
            //  执行调用
            Dispatcher.BeginInvoke(updateAction, label_total, ListView_Inventory, text, man.GetList());
        }


        #region 刷新文本框
        ///=============================================================================================================
        /// <summary>
        /// 更新文本框文字
        /// </summary>
        /// <param name="text">控件</param>
        /// <param name="textContent">内容</param>
        /// <returns></returns>
        ///=============================================================================================================
        private async Task DoUpdateTextBox(TextBox text, string textContent)
        {
            //    异步
            Action<TextBox, string> updateAction = Update_TextBox_UI;
            //    调用
            await Dispatcher.BeginInvoke(updateAction, text, textContent);
        }

        ///=============================================================================================================
        /// <summary>
        /// 更新过程(异步调用)
        /// </summary>
        /// <param name="text"></param>
        /// <param name="textContent"></param>
        ///=============================================================================================================
        private static void Update_TextBox_UI(TextBox text, string textContent)
        {
            text.Text = textContent;
        }

        ///=============================================================================================================
        /// <summary>
        /// 更新按钮文字
        /// </summary>
        /// <param name="button">按钮</param>
        /// <param name="textContent">文字</param>
        /// <returns>任务</returns>
        ///=============================================================================================================
        private async Task DoUpdateButtonText(Button button, string textContent)
        {
            //    异步
            Action<Button, string> updateAction = Update_Button_UI;
            //    调用
            await Dispatcher.BeginInvoke(updateAction, button, textContent);
        }

        ///=============================================================================================================
        /// <summary>
        /// 更新按键
        /// </summary>
        /// <param name="text"></param>
        /// <param name="textContent"></param>
        ///=============================================================================================================
        private static void Update_Button_UI(Button text, string textContent)
        {
            text.Content = textContent;
        }

        #endregion

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 消息响应
        /// </summary>
        /// <param name="type">类型</param>
        /// <param name="cmd">命令</param>
        /// <param name="param">参数</param>
        /// <param name="obj">对象</param>
        ///-------------------------------------------------------------------------------------------------------------
        public void OnRfidResponse(BaseReader reader, int type, int cmd, byte[] param, object obj)
        {
            //  管理器
            var man = App.GetRfidManager();

            if (DEBUG)
            {
                if (obj != null)
                {
                    //Debug.Print("OnRfidResponse(type:{0},cmd:{1},param:{2},{3})\n", type, cmd, SDK.DumpArray(param), obj);
                }
            }

            //=======================
            //  错误消息
            //=======================
            if (obj is string s)
            {
                Log_d(TAG, s);
            }

            //=======================
            //  正常消息
            //=======================
            switch (cmd)
            {
                //==================
                //  收到标签
                //==================
                case BaseCmd.RFID_CMD_INVENTORY:
                    //  错误
                    if (type == BaseAck.RESPONSE_TYPE_ERROR)
                    {
                        return;
                    }

                    //  收到标签
                    if (obj is TagItem tag)
                    {
                        //  大小
                        var curSize = man.GetList().Count;
                        //  数量
                        var text = "" + curSize;

                        //
                        //  这个更新的是列表的
                        //  当前标签
                        //
                        //curNewItem = tag.Clone();

                        //============================
                        //  异步
                        Action<Label, ListView, string, List<TagItem>> updateAction = Update_Total;
                        //  调用
                        Dispatcher.BeginInvoke(updateAction, label_total, ListView_Inventory, text, man.GetList());

                        //============================
                        //  网络
                        Action<TagItem> postAction = Update_Post;
                        //  调用
                        Dispatcher.BeginInvoke(postAction, tag);
                    }

                    break;

                case BaseCmd.RFID_CMD_INIT:
                    break;

                case BaseCmd.RFID_CMD_READ:
                    if (type == BaseAck.RESPONSE_TYPE_ERROR)
                    {
                        _ = DoUpdateTextBox(TextString_ReadWrite, "读取失败");
                        _ = DoUpdateTextBox(TextNumber_ReadWrite, "读取失败");
                    }
                    else
                    {
                        _ = DoUpdateTextBox(TextString_ReadWrite, StringHelper.HexToText(param, true));
                        _ = DoUpdateTextBox(TextNumber_ReadWrite, StringHelper.HexToText(param, true));
                        //    刷新
                        Update_All_List();
                    }

                    break;

                case BaseCmd.RFID_CMD_WRITE:
                    if (type == BaseAck.RESPONSE_TYPE_ERROR)
                    {
                        _ = DoUpdateTextBox(TextString_ReadWrite, "写入失败");
                        _ = DoUpdateTextBox(TextNumber_ReadWrite, "写入失败");
                    }
                    else
                    {
                        _ = DoUpdateTextBox(TextString_ReadWrite, StringHelper.HexToText(param, true));
                        _ = DoUpdateTextBox(TextNumber_ReadWrite, StringHelper.HexToText(param, true));
                        //    刷新
                        Update_All_List();
                    }

                    break;

                case BaseCmd.RFID_CMD_LOCK:
                    break;

                case BaseCmd.RFID_CMD_KILL:
                    break;

                default:
                    break;
            }
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 更新 Post Json 
        /// </summary>
        /// <param name="tag">标签</param>
        ///-------------------------------------------------------------------------------------------------------------
        private void Update_Post(TagItem tag)
        {
            //  检测
            if (MenuItem_DebugLogs.IsChecked)
            {
                //  有效
                if (tag != null)
                {
                    //  管理器
                    var man = App.GetRfidManager();
                    //  推送
                    _ = PostJson.DoPost(man.GetReader().set.set_post_url, tag.GetJsonText());
                }
            }
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 更新数据
        /// </summary>
        /// <param name="lab">标签</param>
        /// <param name="dg">数据表</param>
        /// <param name="text">文本</param>
        /// <param name="list">列表</param>
        ///-------------------------------------------------------------------------------------------------------------
        private void Update_Total(Label lab, ListView lv, string text, List<TagItem> list)
        {
            //  忙就退出
            if (IsUpdateBusy)
                return;

            //    设置忙碌
            IsUpdateBusy = true;
            
            //  标签列表，锁定
            var l = listTags;
            //  时间
            var tm = DateTimeHelper.CurrentUnixTimeMillis();
            //  管理器
            var man = App.GetRfidManager();

            //  标签文字
            lab.Content = text;

            lock (l)
            {
                //    如果多了，删除多的
                while (list.Count < l.Count)
                {
                    //    删除
                    l.Remove(l.Last());
                }

                //  修改或添加
                for (var i = 0; i < list.Count; i++)
                {
                    //  生成本地数据
                    var e = new CsTagItem(list[i]);

                    //  序号
                    e.index = i + 1;
                    //  添加
                    if (i >= l.Count)
                    {
                        //    添加
                        l.Add(e);
                    }
                    else
                    {
                        //    替换
                        //l[i] = e;
                        l[i].Copy(e);
                    }
                }
            }
            
            //  停止忙碌标志
            IsUpdateBusy = false;
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 更新列表框
        /// </summary>
        /// <param name="cmb"></param>
        /// <param name="list"></param>
        /// <param name="sel"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private static void Update_Combo_UI(ComboBox cmb, string[] list, int sel)
        {
            if (DEBUG)
            {
                Debug.WriteLine("Update_Combo_UI(" + cmb.Name + "," + list.Length + "," + sel + ")");
            }

            if (list.Length == 0)
            {
                return;
            }

            var l = new ObservableCollection<string>();

            foreach (var t in list)
            {
                l.Add(t);
            }

            cmb.ItemsSource   = l;
            cmb.SelectedIndex = sel;
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 切换 - 读写器类型
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void Cmb_reader_type_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (DEBUG)
            {
                Debug.Print("<Sender:" + sender + ",e:" + e + ">");
            }

            if (sender is ComboBox cmb)
            {
                //  选项
                var sel = cmb.SelectedIndex;
                //  有效
                if (sel >= 0)
                {
                    //  类型
                    var type = arrayEReaderType[sel % arrayEReaderType.Length];
                    //  系统管理器
                    var rfidMan = App.GetRfidManager();
                    //  类别
                    rfidMan.SetReaderType(type);
                    //  清空
                    rfidMan.CleanCmd();
                    //  调试
                    Log_d(TAG, "指令集:" + sel + "-" + type);
                    //  记忆
                    App.GetSystem().SetupUpdate(true);

                    //Debug.Print(Preferences.GetInstance().ToString());
                }
            }
        }

        #region "消息处理"

        ///=============================================================================================================
        /// <summary>
        /// 输出消息
        /// </summary>
        /// <param name="tag">目标</param>
        /// <param name="msg">消息</param>
        ///=============================================================================================================
        public void Log_d(string tag, string msg)
        {
            //  异步
            Action<ListBox, string> updateAction = Update_Msg_List;
            //  调用
            Dispatcher.BeginInvoke(updateAction, list_status, msg);
        }

        ///=============================================================================================================
        /// <summary>
        /// 输出消息
        /// </summary>
        /// <param name="tag"></param>
        /// <param name="msg"></param>
        ///=============================================================================================================
        public void Log_e(string tag, string msg)
        {
            //  异步
            Action<ListBox, string> updateAction = Update_Msg_List;
            //  调用
            Dispatcher.BeginInvoke(updateAction, list_status, msg);
        }

        #endregion

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 刷新列表
        /// </summary>
        /// <param name="list">列表</param>
        /// <param name="msg">消息</param>
        ///-------------------------------------------------------------------------------------------------------------
        private void Update_Msg_List(ListBox list, string msg)
        {
            //  添加消息
            lock (listMsgs)
            {
                listMsgs.Add(msg);
            }

            //    数据源
            lock (list)
            {
                list.ItemsSource = listMsgs;
            }

            //    滚动到最后
            list.ScrollIntoView(list.Items[list.Items.Count - 1]);
        }

        #region 蓝牙按钮

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 扫描蓝牙
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void Btn_ble_scan_Click(object sender, RoutedEventArgs e)
        {
            //  接口
            var portMan = GetPortManager();
            //  当前端口
            var port = portMan.GetPort(EPortType.PORT_TYPE_BLE);
            //  管理器
            var man = port.GetManager();

            //    端口有效
            if (port != null)
            {
                //  获取管理器
                if (port.GetManager() != null)
                {
                    //  扫描
                    man.Scan(true);
                    //  获取设备列表
                    var array = man.GetDevices();

                    //  设备列表
                    if (array != null)
                    {
                        //  设备列表 - 数据源
                        cmb_ble_devices.ItemsSource = array;
                        //  选中
                        if (array.Length > 0)
                        {
                            // 选择第一个  
                            cmb_ble_devices.SelectedIndex = 0;

                            Log_d(TAG, "选择 : " + array[cmb_ble_devices.SelectedIndex % array.Length]);
                        }
                        else
                        {
                            //  未选择
                            cmb_ble_devices.SelectedIndex = -1;
                        }
                    }
                }
                else
                {
                    Debug.WriteLine("port.GetManager() is (null)");
                }
            }
            else
            {
                Debug.WriteLine("port is (null)");
            }
        }

        ///=============================================================================================================
        /// 连接设备
        ///=============================================================================================================
        private void Btn_ble_connect_Click(object sender, RoutedEventArgs e)
        {
            //  端口管理器
            var portMan = GetPortManager();
            //  当前端口
            var port = portMan.GetPort(EPortType.PORT_TYPE_BLE);
            //  管理器
            var man = port.GetManager();
            //  有效
            if (port != null)
            {
                //  序号
                var sel = cmb_ble_devices.SelectedIndex;
                //  选择
                man.SetSelect(sel);
                //  没有连接
                if (!port.IsConnected())
                {
                    //  连接
                    port.Connect();
                    //  等待一下
                    port.WaitTime();
                    //  提示
                    btn_ble_connect.Content = "连接中...";
                }
                else
                {
                    //  断开
                    port.Disconnect();

                    Log_d(TAG, "断开 : " + sel);

                    btn_ble_connect.Content = "连接";
                }

                if (DEBUG)
                {
                    Debug.Print("连接到(" + sel + ")\n");
                }
            }
        }

        #endregion

        #region 串口按钮
        ///=============================================================================================================
        /// <summary>
        /// 扫描串口
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///=============================================================================================================
        private void Btn_uart_scan_Click(object sender, RoutedEventArgs e)
        {
            var man = GetPortManager().GetUartManager();
            man.Scan(true);
            man.SetEvent(this);
        }
        ///=============================================================================================================
        /// <summary>
        /// 串口连接
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///=============================================================================================================
        private void Btn_uart_connect_Click(object sender, RoutedEventArgs e)
        {
            int[] arrayBaudrate =
            {
                115200, 57600, 38400, 19200, 9600,
            };

            //    端口管理器
            var portMan = App.GetSystem().GetPortManager();
            //    获取串口端口
            var port = portMan.GetPort(EPortType.PORT_TYPE_UART);
            //    获取串口管理器
            var man = portMan.GetUartManager();

            //  设备名
            var dev = cmb_uart_devices.SelectedItem as string;

            //  波特率
            var baud = arrayBaudrate[cmb_uart_baudrate.SelectedIndex % arrayBaudrate.Length];

            //  连接
            if (!port.IsConnected())
            {
                //  配置
                man.Config(dev, baud, 0);
                //  连接
                port.Connect();
                //  等待
                port.WaitTime();

                btn_uart_connect.Content = "连接中...";
            }
            else
            {
                //  断开
                man.Disconnect();
                //  显示
                btn_uart_connect.Content = "连接";
            }
        }

        #endregion

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 端口处理
        /// </summary>
        /// <param name="type">类型</param>
        /// <param name="param">参数</param>
        /// <param name="device">设备</param>
        /// <param name="message">消息</param>
        ///-------------------------------------------------------------------------------------------------------------
        public void OnPortEvent(EPortType type, PortEvenType param, object device, string message)
        {
            if (DEBUG)
            {
                Debug.WriteLine("OnPortEvent(" + type + "," + param + "," + device + "," + message + ")");
            }

            // 显示端口状态
            if (message != null)
            {
                Log_d(TAG, message);
            }

            switch (param)
            {
                //--------------
                //  扫描刷新
                //--------------
                case PortEvenType.PORT_EVENT_SCAN_UPDATE:
                //  扫描完成
                case PortEvenType.PORT_EVENT_SCAN_DONE:
                    switch (type)
                    {
                        case EPortType.PORT_TYPE_BLE:
                            //  蓝牙
                            _ = InitBlePortsAsync();
                            break;

                        case EPortType.PORT_TYPE_UART:
                            //  串口
                            _ = InitUartPortsAsync();
                            break;

                        case EPortType.PORT_TYPE_USB:
                            //  USB
                            _ = InitUsbPortsAsync();
                            break;

                        case EPortType.PORT_TYPE_UDP:
                            break;

                        case EPortType.PORT_TYPE_TCP:
                            break;

                        default:
                            break;
                    }

                    break;

                //--------------
                //  连接中
                //--------------
                case PortEvenType.PORT_EVENT_CONNECTING:
                    switch (type)
                    {
                        case EPortType.PORT_TYPE_BLE:
                            _ = DoUpdateButtonText(btn_ble_connect, "连接中...");
                            break;
                        case EPortType.PORT_TYPE_UART:
                            _ = DoUpdateButtonText(btn_uart_connect, "连接中...");
                            break;
                        case EPortType.PORT_TYPE_USB:
                            _ = DoUpdateButtonText(btn_usb_connect, "连接中...");
                            break;
                        case EPortType.PORT_TYPE_TCP:
                            _ = DoUpdateButtonText(btn_net_connect, "连接中...");
                            break;
                        case EPortType.PORT_TYPE_UDP:
                            _ = DoUpdateButtonText(btn_net_connect, "连接中...");
                            break;
                        default:
                            break;
                    }

                    break;
                //--------------
                //  连接上
                //--------------
                case PortEvenType.PORT_EVENT_CONNECTED:
                    switch (type)
                    {
                        case EPortType.PORT_TYPE_BLE:
                            _ = DoUpdateButtonText(btn_ble_connect, "断开");
                            SetManagerPort(EPortType.PORT_TYPE_BLE);
                            break;

                        case EPortType.PORT_TYPE_UART:
                            _ = DoUpdateButtonText(btn_uart_connect, "断开");
                            SetManagerPort(EPortType.PORT_TYPE_UART);
                            break;

                        case EPortType.PORT_TYPE_USB:
                            _ = DoUpdateButtonText(btn_usb_connect, "断开");
                            SetManagerPort(EPortType.PORT_TYPE_USB);
                            break;

                        case EPortType.PORT_TYPE_TCP:
                            _ = DoUpdateButtonText(btn_net_connect, "断开");
                            SetManagerPort(EPortType.PORT_TYPE_TCP);
                            break;

                        case EPortType.PORT_TYPE_UDP:
                            _ = DoUpdateButtonText(btn_net_connect, "断开");
                            SetManagerPort(EPortType.PORT_TYPE_UDP);
                            break;
                        default:
                            break;
                    }

                    break;

                //--------------
                //  断开
                //--------------
                case PortEvenType.PORT_EVENT_CONNECT_FAILED:
                case PortEvenType.PORT_EVENT_DISCONNECTED:
                    switch (type)
                    {
                        case EPortType.PORT_TYPE_BLE:
                            _ = DoUpdateButtonText(btn_ble_connect, "连接");
                            break;
                        case EPortType.PORT_TYPE_UART:
                            _ = DoUpdateButtonText(btn_uart_connect, "连接");
                            break;
                        case EPortType.PORT_TYPE_USB:
                            _ = DoUpdateButtonText(btn_usb_connect, "连接");
                            break;
                        case EPortType.PORT_TYPE_TCP:
                            _ = DoUpdateButtonText(btn_net_connect, "连接");
                            break;
                        case EPortType.PORT_TYPE_UDP:
                            _ = DoUpdateButtonText(btn_net_connect, "连接");
                            break;
                    }

                    //  当前接口断开
                    if (type == GetPortManager().GetPortType())
                    {
                        //   停止
                        App.GetSystem().Stop();
                        //  开始
                        _ = DoUpdateButtonText(Button_Start, "开始");
                    }

                    break;
            }
        }

        #region "USB按钮处理"

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// USB扫描设备
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void Btn_usb_scan_Click(object sender, RoutedEventArgs e)
        {
            var man = App.GetPortManager().GetUsbManager();
            man.Scan(true);
            man.SetEvent(this);
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 连接
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void Btn_usb_connect_Click(object sender, RoutedEventArgs e)
        {
            //  端口管理器
            var portMan = App.GetPortManager();
            //  获取USB端口
            var port = portMan.GetPort(EPortType.PORT_TYPE_USB);
            //  设备列表
            var cmb = cmb_usb_devices;
            //  连接按钮
            var btn = btn_usb_connect;

            //  端口有效
            if (port != null)
            {
                //  设置
                App.GetRfidManager().SetPort(port);
                //  管理器
                var man = port.GetManager();
                //  序号
                var sel = cmb.SelectedIndex;
                //  选择
                man.SetSelect(sel);
                //  断开连接
                if (!port.IsConnected())
                {
                    //  连接设备
                    port.Connect();
                    port.WaitTime();
                    btn.Content = "连接中...";
                }
                else
                {
                    port.Disconnect();
                    Log_d(TAG, "断开 : " + sel);
                    btn.Content = "连接";
                }
            }
        }

        #endregion

        #region 天线检查框

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 设置检查
        /// </summary>
        /// <param name="ant">天线</param>
        ///-------------------------------------------------------------------------------------------------------------
        private void SetChecks(int ant)
        {
            var allChecks = new CheckBox[16];

            allChecks[0]  = chk_ch1;
            allChecks[1]  = chk_ch2;
            allChecks[2]  = chk_ch3;
            allChecks[3]  = chk_ch4;
            allChecks[4]  = chk_ch5;
            allChecks[5]  = chk_ch6;
            allChecks[6]  = chk_ch7;
            allChecks[7]  = chk_ch8;
            allChecks[8]  = chk_ch9;
            allChecks[9]  = chk_ch10;
            allChecks[10] = chk_ch11;
            allChecks[11] = chk_ch12;
            allChecks[12] = chk_ch13;
            allChecks[13] = chk_ch14;
            allChecks[14] = chk_ch15;
            allChecks[15] = chk_ch16;

            for (var i = 0; i < allChecks.Length; i++)
            {
                if (allChecks[i] != null)
                {
                    if (i < ant)
                    {
                        allChecks[i].IsChecked = true;
                    }
                    else
                    {
                        allChecks[i].IsChecked = false;
                    }
                }
            }
        }

        private void Radio_ch1_Checked(object sender, RoutedEventArgs e)
        {
            var reader = GetRfidManager().GetReader();
            reader.SetChannels(1);
            SetChecks(1);
        }

        private void Radio_ch4_Checked(object sender, RoutedEventArgs e)
        {
            var reader = GetRfidManager().GetReader();
            reader.SetChannels(4);
            SetChecks(4);
        }

        private void Radio_ch8_Checked(object sender, RoutedEventArgs e)
        {
            var reader = GetRfidManager().GetReader();
            reader.SetChannels(8);
            SetChecks(8);
        }

        private void Radio_ch16_Checked(object sender, RoutedEventArgs e)
        {
            var reader = GetRfidManager().GetReader();
            reader.SetChannels(16);
            SetChecks(16);
        }

        private void Chk_chx_Checked(object sender, RoutedEventArgs e)
        {
            if (sender is CheckBox chk)
            {
            }
        }

        #endregion

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 读取选择
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void ListView_Read_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (DEBUG)
            {
                Debug.WriteLine("选中()");
            }

            if (sender is ListView listView)
            {
                // 序号
                var sel = listView.SelectedIndex;
                // 选中
                var cur = listView.SelectedItem;

                if (cur is CsTagItem item)
                {
                    sel = item.index - 1;
                }

                // 列表
                var lst = GetRfidManager().GetList();
                // 列表非空
                if (lst != null)
                {
                    //    有效范围
                    if (sel >= 0 && sel < lst.Count)
                    {
                        //  标签
                        var tag = lst[sel];
                        if (tag != null)
                        {
                            //  选择
                            curSelect = tag;
                            //  命令
                            var cmd = GetRfidManager().GetReader().GetCmd();
                            //  文字
                            TextSelect_Read.Text = tag.GetTextEpc();
                            //  文字
                            TextString_ReadWrite.Text = tag.GetTextEpc();
                            //  选中标签
                            //  PC , EPC
                            GetRfidManager().PutCmd(cmd.rfid_set_select(BaseCmd.RFID_BANK_EPC, EPC_START_POS, tag.epc));
                        }
                    }
                }
            }
        }

        #region "读,写,锁,杀"

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 读取
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void Button_Read_Click(object sender, RoutedEventArgs e)
        {
            var man = GetRfidManager();
            // 读写器
            var reader = man.GetReader();
            // 命令
            var cmd = reader.GetCmd();
            //  密码
            var pass = StringHelper.ParserHex(TextPass_Read.Text);
            //  区域
            var bank = ComboArea_Read.SelectedIndex;
            //  位置
            var pos = StringHelper.ParserDecimer(TextBegin_Read.Text);
            //  长度
            var len = StringHelper.ParserDecimer(TextLenght_Read.Text);

            if (DEBUG)
            {
                Debug.WriteLine("rfid_read(pass:{0,0:X4},bank:{1},pos:{2},len:{3})", pass, bank, pos, len);
            }

            if (curSelect != null)
            {
                // 选择标签(选择EPC)
                man.PutCmd(cmd.rfid_set_select(BaseCmd.RFID_BANK_EPC, EPC_START_POS, curSelect.epc));
                WaitCommand();
            }

            // 发送命令
            man.PutCmd(cmd.rfid_read(pass, bank, pos, len));
            WaitCommand();
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 写标签
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void Button_Write_Click(object sender, RoutedEventArgs e)
        {
            var man = GetRfidManager();
            // 读写器
            var reader = man.GetReader();
            //  命令
            var cmd = reader.GetCmd();
            // 密码
            var pass = StringHelper.ParserHex(TextPass_Read.Text);
            //  区域
            var bank = ComboArea_Read.SelectedIndex;
            // 位置
            var pos = StringHelper.ParserDecimer(TextBegin_Read.Text);
            // 长度
            var len = StringHelper.ParserDecimer(TextLenght_Read.Text);
            // 文本
            var text = TextString_ReadWrite.Text;
            // 缓存
            byte[] buf = null;

            if (DEBUG)
            {
                Debug.WriteLine("rfid_write(pass:{0,0:X4},bank:{1},pos:{2},len:{3})", pass, bank, pos,text);
            }

            if (curSelect != null)
            {
                //  选择 EPC
                man.PutCmd(cmd.rfid_set_select(BaseCmd.RFID_BANK_EPC, EPC_START_POS, curSelect.epc));
                //  线程
                WaitCommand();
            }

            //    文字方式
            if (TabItem_Text_Mode.IsSelected)
            {
                pos = -1;
                buf = TagItem.Text2Array(text);
                Debug.WriteLine("写文本:{0}",SDK.DumpArray(buf));
            }
            //  数字模式
            else if (TabItem_Number_Mode.IsSelected) 
            {
                text = TextNumber_ReadWrite.Text;
                Debug.WriteLine("写数据:{0}", text);

                buf = StringHelper.TextToHex(text, true);
                Debug.WriteLine("写数值:{0}",SDK.DumpArray(buf));
            }
            else
            {
                pos = 0;
                Debug.WriteLine("未知焦点");
                return;
            }

            //  是不是要颠倒?
            man.PutCmd(cmd.rfid_write(pass, bank, pos, buf));
            WaitCommand();
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 锁定标签
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void Button_Lock_Click(object sender, RoutedEventArgs e)
        {
            var man = GetRfidManager();
            //  读写器
            var reader = man.GetReader();
            //  命令
            var cmd = reader.GetCmd();
            //  密码
            var pass = StringHelper.ParserHex(TextPass_Read.Text);
            //  区域
            var bank = ComboArea_Lock.SelectedIndex;
            //  类型
            var type = ComboType_Lock.SelectedIndex;

            if (DEBUG)
            {
                Debug.WriteLine("rfid_lock(pass:{0,0:X4},bank:{1},type:{2})", pass, bank, type);
            }

            //  选择 EPC
            man.PutCmd(cmd.rfid_set_select(BaseCmd.RFID_BANK_EPC, EPC_START_POS, curSelect.epc));
            WaitCommand();

            //  是不是要颠倒?
            man.PutCmd(cmd.rfid_lock(pass, bank, type));
            WaitCommand();
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 灭活标签
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void Button_Kill_Click(object sender, RoutedEventArgs e)
        {
            var man = GetRfidManager();
            // 读写器
            var reader = man.GetReader();
            // 命令
            var cmd = reader.GetCmd();
            // 密码
            var pass = StringHelper.ParserHex(TextPass_Kill.Text);
            //  区域
            var bank = ComboArea_Lock.SelectedIndex;
            //  类型
            var type = ComboType_Lock.SelectedIndex;

            if (DEBUG)
            {
                Debug.WriteLine("rfid_lock(pass:{0,0:X4},bank:{1},type:{2})", pass, bank, type);
            }

            //  选择 EPC
            man.PutCmd(cmd.rfid_set_select(BaseCmd.RFID_BANK_EPC, EPC_START_POS, curSelect.epc));
            WaitCommand();

            //  是不是要颠倒?
            man.PutCmd(cmd.rfid_lock(pass, bank, type));
            WaitCommand();
        }

        #endregion

        #region "导出"

        ///=============================================================================================================
        /// <summary>
        /// 导出文件
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///=============================================================================================================
        private void Button_Export_Csv_Click(object sender, EventArgs e)
        {
            var saveDlg = new SaveFileDialog();
            // 文件过滤器
            saveDlg.Filter = "*.csv|*.CSV|*.*|(*.*)";

            var result = saveDlg.ShowDialog();

            //    显示对话框
            if (result != null)
            {
                var          name   = saveDlg.FileName;
                var          info   = new FileInfo(name);
                StreamWriter writer = null;
                try
                {
                    writer = info.CreateText();
                }
                catch (Exception ex)
                {
                    MessageBox.Show(ex.Message, "错误");
                    return;
                }

                try
                {
                    writer.WriteLine("IDX,EPC,TID,USR,RFU,RSSI,CNT");

                    foreach (var item in listTags)
                    {
                        writer.Write("{0},", item.index);
                        writer.Write("{0},", item.epc);
                        writer.Write("{0},", item.tid);
                        writer.Write("{0},", item.usr);
                        writer.Write("{0},", item.rfu);
                        writer.Write("{0},", item.rssi);
                        writer.Write("{0},", item.count);

                        writer.WriteLine();
                    }

                    writer.Close();
                }
                catch (Exception ex)
                {
                    MessageBox.Show(ex.Message, "错误");
                }
            }
        }

        private void MenuItem_Log_Click(object sender, RoutedEventArgs e)
        {
            //MenuItem_DebugLogs.c
        }

        ///=============================================================================================================
        /// <summary>
        /// 导出到CSV格式
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///=============================================================================================================
        private void MenuItem_Csv_Click(object sender, RoutedEventArgs e)
        {
            var saveDlg = new SaveFileDialog();
            // 文件过滤器
            saveDlg.Filter = "*.csv|*.CSV|*.*|(*.*)";
            // 文件
            saveDlg.FileName = "Rfid_Export.csv";

            var result = saveDlg.ShowDialog();

            //    显示对话框
            if (result != null)
            {
                var          name   = saveDlg.FileName;
                var          info   = new FileInfo(name);
                StreamWriter writer = null;
                try
                {
                    writer = info.CreateText();
                }
                catch (Exception ex)
                {
                    MessageBox.Show(ex.Message, "错误");
                    return;
                }

                try
                {
                    writer.WriteLine("IDX,EPC,TID,USR,RFU,RSSI,CNT");

                    foreach (var item in listTags)
                    {
                        writer.Write("{0},", item.index);
                        writer.Write("{0},", item.epc);
                        writer.Write("{0},", item.tid);
                        writer.Write("{0},", item.usr);
                        writer.Write("{0},", item.rfu);
                        writer.Write("{0},", item.rssi);
                        writer.Write("{0},", item.count);

                        writer.Write("{0},", item.firstTime);
                        writer.Write("{0},", item.lastTime);

                        writer.WriteLine();
                    }

                    writer.Close();
                }
                catch (Exception ex)
                {
                    MessageBox.Show(ex.Message, "错误");
                }
            }
        }

        #endregion

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 等待命令完成
        /// </summary>
        ///-------------------------------------------------------------------------------------------------------------
        private static void WaitCommand()
        {
            System.Threading.Thread.Sleep(RESPONE_TIMEOUT);
        }
    }
}