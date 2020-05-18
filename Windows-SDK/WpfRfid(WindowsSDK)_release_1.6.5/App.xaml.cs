using RfidLib.Port;
using RfidLib.rfid;
using RfidLib.rfid.based;
using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Diagnostics;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using CommLib.comm;
using GuiLib.comm;

namespace WpfRfid
{
    ///=================================================================================================================
    /// <summary>
    /// App.xaml 的交互逻辑
    /// </summary>
    ///=================================================================================================================
    public partial class App : Application
    {
        /// 调试开关
#if DEBUG
        public const bool DEBUG = true;
#else
        public const bool DEBUG = false;
#endif

        /// 标签管理器
        public readonly RfidSystem rfidSystem;

        /// <summary>
        /// 初始化
        /// </summary>
        public App()
        {
            // 初始化
            rfidSystem = new RfidSystem(EPortType.PORT_TYPE_UART, EReaderType.READER_TYPE_R2000, 4);

            Preferences.Set("App", "init");

            // 没有初始化
            if (!rfidSystem.IsInit())
            {
                Debug.Print("-->未初始化!");
                // 清除
                Preferences.Clean();
                // 保存
                rfidSystem.SetupUpdate(true);
                // 立即初始化
                rfidSystem.SetInit(true);
                // 记忆
                Preferences.Flush();
            }
            else
            {
                Debug.Print("-->已经初始化!");
                // 读取
                rfidSystem.SetupUpdate(false);
            }

            if (DEBUG)
            {
                Debug.Print(Preferences.GetInstance().ToString());
            }
        }
        
        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 获取实例
        /// </summary>
        /// <returns></returns>
        ///-------------------------------------------------------------------------------------------------------------
        public static App GetInstance()
        {
            return (App) Current;
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 获取系统
        /// </summary>
        /// <returns></returns>
        ///-------------------------------------------------------------------------------------------------------------
        public static RfidSystem GetSystem()
        {
            return GetInstance().rfidSystem;
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 获取 - 标签管理器
        /// </summary>
        /// <returns></returns>
        ///-------------------------------------------------------------------------------------------------------------
        public static RfidManager GetRfidManager()
        {
            return GetSystem().GetRfidManager();
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 获取 - 端口管理器
        /// </summary>
        /// <returns></returns>
        ///-------------------------------------------------------------------------------------------------------------
        public static PortManager GetPortManager()
        {
            return GetSystem().GetPortManager();
        }
    }
}