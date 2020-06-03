using RfidLib.rfid;
using System.ComponentModel;

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
        public int _index;
        public int Index
        {
            get { return _index; }
            set
            {
                if (_index != value)
                {
                    _index = value;
                    PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(Index)));
                }
            }
        }

        ///------------------
        /// PC
        ///------------------
        private ushort _pc;
        public ushort Pc
        {
            get { return _pc; }
            set
            {
                if (_pc != value)
                {
                    _pc = value;
                    PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(Pc)));
                }
            }
        }

        ///------------------
        /// CRC
        ///------------------
        private uint _crc;

        public uint Crc
        {
            get { return _crc; }
            set
            {
                if (_crc != value)
                {
                    _crc = value;
                    PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(Crc)));
                }
            }
        }

        ///------------------
        /// 标签
        ///------------------
        private string _epc;
        public string Epc
        {
            get => _epc;
            set
            {
                if (_epc != value)
                {
                    _epc = value;
                    PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(Epc)));
                }
            }
        }

        ///------------------
        /// TID
        ///------------------
        private string _tid;

        public string Tid
        {
            get => _tid;
            set
            {
                if (_tid != value)
                {
                    _tid = value;
                    PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(Tid)));
                }
            }
        }

        ///------------------
        /// USR
        ///------------------
        private string _usr;

        public string Usr
        {
            get => _usr;
            set
            {
                if (_usr != value)
                {
                    _usr = value;
                    PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(Usr)));
                }
            }
        }

        ///------------------
        /// RFU
        ///------------------
        private string _rfu;

        public string Rfu
        {
            get => _rfu;
            set
            {
                if (_rfu != value)
                {
                    _rfu = value;
                    PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(Rfu)));
                }
            }
        }

        ///------------------
        /// 天线
        ///------------------
        private int _ant;

        public int Ant
        {
            get { return _ant; }
            set
            {
                if (_ant != value)
                {
                    _ant = value;
                    PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(Ant)));
                }
            }
        }

        ///------------------
        /// 方向
        ///------------------
        private string _dir;

        public string Dir
        {
            get { return _dir; }
            set
            {
                if (_dir != value)
                {
                    _dir = value;
                    PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(Dir)));
                }
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

        public int Rssi
        {
            get { return _rssi; }
            set
            {
                if (_rssi != value)
                {
                    _rssi = value;
                    PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(Rssi)));
                }
            }
        }

        ///------------------
        /// 数量
        ///------------------
        private int _count;

        public int Count
        {
            get { return _count; }
            set
            {
                if (_count != value)
                {
                    _count = value;
                    PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(Count)));
                }
            }
        }

        ///------------------
        /// 首次时间
        ///------------------
        public long FirstTime { get; set; }

        ///------------------
        /// 最后时间
        ///------------------
        public long LastTime { get; set; }

        ///------------------
        /// 状态码
        ///------------------
        private string _statu;
        public string Statu
        {
            get { return _statu; }
            set
            {
                if (_statu != value)
                {
                    _statu = value;
                    PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(Statu)));
                }
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
                this._pc = item.pc;
                //  校验码
                this._crc = item.crc;
                //  标签
                this._epc = item.GetTextEpc();
                this._tid = item.GetTextTidMsb();
                this._usr = item.GetTextUsrMsb();
                this._rfu = item.GetTextRfuMsb();
                //  天线
                this._ant = item.ant;
                this._rssi = item.rssi;
                this._count = item.count;
                this._dir = item.GetDirName();
                this._statu = "丢失次数:" + item.breakCount;
                this.tag = item.Clone();
            }
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 更新数据
        /// </summary>
        /// <param name="index"></param>
        /// <param name="pc"></param>
        /// <param name="crc"></param>
        /// <param name="epc"></param>
        /// <param name="tid"></param>
        /// <param name="usr"></param>
        /// <param name="rfu"></param>
        /// <param name="ant"></param>
        /// <param name="rssi"></param>
        /// <param name="count"></param>
        /// <param name="dir"></param>
        ///-------------------------------------------------------------------------------------------------------------
        public void Update(int index, ushort pc, uint crc, string epc, string tid, string usr, string rfu, int ant, sbyte rssi, int count, string dir)
        {
            this._index = index;
            //  协议
            this._pc = pc;
            //  校验码
            this._crc = crc;
            //  标签
            this._epc = epc;
            this._tid = tid;
            this._usr = usr;
            this._rfu = rfu;
            //  天线
            this._ant = ant;
            this._rssi = rssi;
            this._count = count;
            this._dir = dir;
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 更新数据
        /// </summary>
        /// <param name="index"></param>
        /// <param name="pc"></param>
        /// <param name="crc"></param>
        /// <param name="epc"></param>
        /// <param name="tid"></param>
        /// <param name="ant"></param>
        /// <param name="rssi"></param>
        /// <param name="count"></param>
        /// <param name="dir"></param>
        ///-------------------------------------------------------------------------------------------------------------
        public void Update(int index, ushort pc, uint crc, string epc, string tid, int ant, sbyte rssi, int count, string dir)
        {
            this._index = index;
            //  协议
            this._pc = pc;
            //  校验码
            this._crc = crc;
            //  标签
            this._epc = epc;
            this._tid = tid;
            //  天线
            this._ant = ant;
            this._rssi = rssi;
            this._count = count;
            this._dir = dir;
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 更新
        /// </summary>
        /// <param name="index"></param>
        /// <param name="ant"></param>
        /// <param name="rssi"></param>
        /// <param name="count"></param>
        /// <param name="firstTime"></param>
        /// <param name="lastTime"></param>
        ///-------------------------------------------------------------------------------------------------------------
        public void Update(int index, int ant, sbyte rssi, int count, long firstTime, long lastTime)
        {
            this._index = index;
            this._ant = ant;
            this._rssi = rssi;
            this.FirstTime = firstTime;
            this.LastTime = lastTime;
            this._count = count;
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
                return tag.breakCount;
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
                this._index = item.Index;
                //  协议
                this._pc = item.Pc;
                //  校验码
                this._crc = item.Crc;
                //  标签
                this._epc = item.Epc;
                this._tid = item.Tid;
                this._usr = item.Usr;
                this._rfu = item.Rfu;
                //  天线
                this._ant = item.Ant;
                this._rssi = item.Rssi;
                this._count = item.Count;
                this._dir = item.Dir;
                this._statu = item.Statu;
                //  关联
                this.tag = item.tag;
            }

            //  触发界面刷新           
        }

        public void UpdateAll()
        {
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs(nameof(Index)));
                PropertyChanged(this, new PropertyChangedEventArgs(nameof(Pc)));
                PropertyChanged(this, new PropertyChangedEventArgs(nameof(Epc)));
                PropertyChanged(this, new PropertyChangedEventArgs(nameof(Tid)));
                PropertyChanged(this, new PropertyChangedEventArgs(nameof(Ant)));
                PropertyChanged(this, new PropertyChangedEventArgs(nameof(Dir)));
                PropertyChanged(this, new PropertyChangedEventArgs(nameof(Rssi)));
                PropertyChanged(this, new PropertyChangedEventArgs(nameof(Count)));
                PropertyChanged(this, new PropertyChangedEventArgs(nameof(Statu)));        
            }
        }

    }
}