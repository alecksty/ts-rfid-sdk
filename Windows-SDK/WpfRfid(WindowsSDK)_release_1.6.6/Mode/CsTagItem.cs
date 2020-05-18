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
            get => _epc;
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
            get => _tid;
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
            get => _usr;
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
            get => _rfu;
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

        ///------------------
        /// 状态码
        ///------------------
        private string _statu;
        public string statu 
        {
            get { return _statu; }
            set
            {
                _statu = value;
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(statu)));
            }
        }

        ///------------------
        /// 关联数据
        ///------------------
        public TagItem tag;

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
                this._pc 	= item.pc;
                //  校验码
                this._crc 	= item.crc;
                //  标签
                this._epc 	= item.GetTextEpc();
                this._tid 	= item.GetTextTidMsb();
                this._usr 	= item.GetTextUsrMsb();
                this._rfu 	= item.GetTextRfuMsb();
                //  天线
                this._ant   = item.ant;
                this._rssi  = item.rssi;
                this._count = item.count;
                this._dir   = item.GetDirName();
                this.statu = "丢失次数:" + item.GetBreakCount();
                this.tag    = item.Clone();
            }
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 获取变动次数
        /// </summary>
        /// <returns></returns>
        ///-------------------------------------------------------------------------------------------------------------
        public int GetBreakCount()
        {
            if (tag != null)
            {
                return tag.GetBreakCount();
            }
            return 0;
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
                this._pc    = item.pc;
                //  校验码
                this._crc   = item.crc;
                //  标签
                this._epc   = item.epc;
                this._tid   = item.tid;
                this._usr   = item.usr;
                this._rfu   = item.rfu;
                //  天线
                this._ant   = item.ant;
                this._rssi  = item.rssi;
                this._count = item.count;
                this._dir   = item.dir;
                this._statu = "丢失次数:" + item.GetBreakCount();
                //  关联
                this.tag    = item.tag;
            }

            //  触发界面刷新
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs(nameof(index)));
                PropertyChanged(this, new PropertyChangedEventArgs(nameof(pc)));
                PropertyChanged(this, new PropertyChangedEventArgs(nameof(epc)));
                PropertyChanged(this, new PropertyChangedEventArgs(nameof(tid)));
                PropertyChanged(this, new PropertyChangedEventArgs(nameof(ant)));
                PropertyChanged(this, new PropertyChangedEventArgs(nameof(dir)));
                PropertyChanged(this, new PropertyChangedEventArgs(nameof(rssi)));
                PropertyChanged(this, new PropertyChangedEventArgs(nameof(count)));
                PropertyChanged(this, new PropertyChangedEventArgs(nameof(statu)));
            }
        }
    }
}