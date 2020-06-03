using System.Windows;

namespace  WpfRfid
{
    /// <summary>
    /// 块波形
    /// </summary>
    public class WaveBlock
    {
        ///  块 - 缩放比例
        public double Scale = 0.1;

        /// 块 - 起始像素
        public double BeginX;

        /// 块 - 宽度
        public double Width = 50;

        ///=============================================================================================================
        ///
        ///=============================================================================================================
        ///  数据 - 起始时间(毫秒)
        public long TimeBegin = 0;

        ///  数据 - 结束时间(毫秒)
        public long TimeEnd = 0;

        //   鼠标按下位置
        public double downX = 0;

        //   平均 - 信号值
        public long AvageRssi;

        // 误差 - 信号值
        public double AvageDelta=0.1;

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 全部时间长度
        /// </summary>
        /// <returns></returns>
        ///-------------------------------------------------------------------------------------------------------------
        public long GetTimeTotal()
        {
            if (TimeEnd >= TimeBegin)
            {
                return (TimeEnd - TimeBegin);
            }
            return (TimeBegin - TimeEnd);
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 可视区域 - 时间长度
        /// </summary>
        /// <returns></returns>
        ///-------------------------------------------------------------------------------------------------------------
        public long GetTimeBlock()
        {
            return (long) (GetTimeTotal() * Scale);
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 网格计算
        /// </summary>
        /// <param name="scale">比例</param>
        /// <param name="sz">大小</param>
        /// <returns></returns>
        ///-------------------------------------------------------------------------------------------------------------
        public static int MakeGridSize(double scale, double sz)
        {
            int grid1Size = 1;
            int grid2Size = 2;
            int grid5Size = 5;
            int gridSize;

            //  1/10/100/1000/10000
            while (grid1Size * scale < sz)
            {
                grid1Size *= 10;
            }

            //  2/20/200/2000/20000
            while (grid2Size * scale < sz)
            {
                grid2Size *= 10;
            }

            //  5/50/500/5000/50000
            while (grid5Size * scale < sz)
            {
                grid5Size *= 10;
            }

            gridSize = grid1Size;

            if (gridSize > grid2Size)
            {
                gridSize = grid2Size;
            }

            if (gridSize > grid5Size)
            {
                gridSize = grid5Size;
            }

            if (gridSize < 1)
            {
                gridSize = 1;
            }

            //    最小网格
            return gridSize;
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 块开始位置
        /// </summary>
        /// <returns></returns>
        ///-------------------------------------------------------------------------------------------------------------
        public double BlockBeginX()
        {
            return BeginX;
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 全部开始位置
        /// </summary>
        /// <returns></returns>
        ///-------------------------------------------------------------------------------------------------------------
        public double TotalBeginX()
        {
            return BeginX / Scale;
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 在块中
        /// </summary>
        /// <param name="pt"></param>
        /// <returns></returns>
        ///-------------------------------------------------------------------------------------------------------------
        public bool IsInBlock(Point pt)
        {
            if ((pt.X >= BeginX) && (pt.X <= BeginX + Width))
            {
                return true;
            }

            return false;
        }
    }
}