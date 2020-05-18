using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using RfidLib;
using RfidLib.rfid;

namespace WpfRfid.Mode
{
    /// <summary>
    /// C#使用的标签结构
    /// </summary>
    public class CsTagItem : INotifyPropertyChanged
    {
        /// 属性改变接口
        public event PropertyChangedEventHandler PropertyChanged;

        ///------------------
        /// 序号
        ///------------------
        private int _index;
        public int index
        {
            get { return _index; }
            set
            {
                _index = value;
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(index)));
            }
        }

        ///------------------
        /// PC
        ///------------------
        private ushort _pc;
        public ushort pc
        {
            get { return _pc; }
            set
            {
                _pc = value;
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(pc)));
            }
        }

        ///------------------
        /// CRC
        ///------------------
        private uint _crc;

        public uint crc
        {
            get { return _crc; }
            set
            {
                _crc = value;
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(crc)));
            }
        }

        ///------------------
        /// 标签
        ///------------------
        private string _epc;

        public string epc
        {
            get { return _epc; }
            set
            {
                _epc = value;
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(epc)));
            }
        }

        ///------------------
        /// TID
        ///------------------
        private string _tid;

        public string tid
        {
            get { return _tid; }
            set
            {
                _tid = value;
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(tid)));
            }
        }

        ///------------------
        /// USR
        ///------------------
        private string _usr;

        public string usr
        {
            get { return _usr; }
            set
            {
                _usr = value;
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(usr)));
            }
        }

        ///------------------
        /// RFU
        ///------------------
        private string _rfu;

        public string rfu
        {
            get { return _rfu; }
            set
            {
                _rfu = value;
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(rfu)));
            }
        }

        ///------------------
        /// 天线
        ///------------------
        private int _ant;

        public int ant
        {
            get { return _ant; }
            set
            {
                _ant = value;
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(ant)));
            }
        }

        ///------------------
        /// 方向
        ///------------------
        private string _dir;

        public string dir
        {
            get { return _dir; }
            set
            {
                _dir = value;
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(dir)));
            }
        }

        ///------------------
        /// 天线矩阵
        ///------------------
        public string antArray { get; set; }

        ///------------------
        /// 信号强度
        ///------------------
        private int _rssi;

        public int rssi
        {
            get { return _rssi; }
            set
            {
                _rssi = value;
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(rssi)));
            }
        }

        ///------------------
        /// 数量
        ///------------------
        private int _count;

        public int count
        {
            get { return _count; }
            set
            {
                _count = value;
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(count)));
            }
        }

        ///------------------
        /// 首次时间
        ///------------------
        public long firstTime { get; set; }

        ///------------------
        /// 最后时间
        ///------------------
        public long lastTime { get; set; }

        ///--------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 构造函数
        /// </summary>
        /// <param name="item"></param>
        ///--------------------------------------------------------------------------------------------------------------
        public CsTagItem(TagItem item)
        {
            if (item != null)
            {
                //  序号
                this._index = item.index;
                //  协议
                this._pc = item.pc;
                //  校验码
                this._crc = item.crc;
                //  标签
                this._epc = item.GetTextEpc();
                this._tid = item.GetTextTidMsb();
                this._usr = item.GetTextUsrMsb();
                this._rfu = item.GetTextRfuMsb();
                //  天线
                this._ant   = item.ant;
                this._rssi  = item.rssi;
                this._count = item.count;
                this._dir   = item.GetDirName();
            }
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 复制数据
        /// </summary>
        /// <param name="item"></param>
        ///-------------------------------------------------------------------------------------------------------------
        public void Copy(CsTagItem item)
        {
            if (item != null)
            {
                //  序号
                this._index = item.index;
                //  协议
                this._pc = item.pc;
                //  校验码
                this._crc = item.crc;
                //  标签
                this._epc = item.epc;
                this._tid = item.tid;
                this._usr = item.usr;
                this._rfu = item.rfu;
                //  天线
                this._ant   = item.ant;
                this._rssi  = item.rssi;
                this._count = item.count;
                this._dir   = item.dir;
            }

            //  触发界面刷新
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs("index"));
                PropertyChanged(this, new PropertyChangedEventArgs("pc"));
                PropertyChanged(this, new PropertyChangedEventArgs("epc"));
                PropertyChanged(this, new PropertyChangedEventArgs("tid"));
                PropertyChanged(this, new PropertyChangedEventArgs("ant"));
                PropertyChanged(this, new PropertyChangedEventArgs("dir"));
                PropertyChanged(this, new PropertyChangedEventArgs("rssi"));
                PropertyChanged(this, new PropertyChangedEventArgs("count"));
            }
        }
    }
}