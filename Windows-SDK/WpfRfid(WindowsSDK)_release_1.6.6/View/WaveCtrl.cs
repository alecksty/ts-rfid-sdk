#define USE_STEP_SCALE

using System.Collections.Generic;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows;
using System.Windows.Input;
using System.Diagnostics;
using System.Globalization;
using System;
using RfidLib.rfid;

namespace WpfRfid
{
    //==============================================================================================================
    /// <summary>
    /// 波形控件
    /// </summary>
    //==============================================================================================================
    public class WaveCtrl : Control
    {
        //==============================================================================================================
        // 常量
        //==============================================================================================================

        ///  隐藏
        private const int STATE_PREVIEW_HIDE = 0;

        ///  顶部
        private const int STATE_PREVIEW_TOP = 1;

        ///  底部
        private const int STATE_PREVIEW_BOTTOM = 2;

        /// 预览比例
        private const int PREVIEW_SCALE_H = 6;

        /// 纵向分段
        private const int MAX_V_SEGMENT = 12;

        /// 信号 - 最大值
        private const int MAX_RSSI_SEGMENT = 128;

        /// 网格 - 字体
        private const double FontSizeGrid = 7;

        /// 文字 - 字体 
        private const double FontSizeText = 9;

        /// 字体
        private readonly Typeface typefaceText = new Typeface("System");

        //  忙碌
        private bool isBusy = false;

        //==============================================================================================================
        //  局部变量
        //==============================================================================================================

        /// 滚动条位置 (1 - top, 2 - bottom)
        private int stateScroll = STATE_PREVIEW_HIDE;

        /// 按下
        private bool isPressDown;

        /// 数据块
        private readonly WaveBlock block = new WaveBlock();

        /// 波形数据  (根据时间time和信号值rssi生成)
        private List<WaveValue> waves = new List<WaveValue>();

        /// 按下点
        private Point pointDown;

        /// 初始化
        private bool isInit;

        //    标题
        private string title = "(未选中)";

        //==============================================================================================================
        // 颜色表
        //==============================================================================================================
        ///  全部 - 背景
        private readonly Brush _brushAll = new SolidColorBrush(Color.FromRgb(240, 240, 240));

        private readonly Pen _penAll = new Pen(new SolidColorBrush(Color.FromArgb(200, 200, 200, 200)), 0.5);

        ///  滚动条 - 背景
        private readonly Brush _brScroll = new LinearGradientBrush(Color.FromArgb(127, 200, 200, 200),
                                                                   Color.FromArgb(192, 255, 255, 255), 90);

        private readonly Pen _penScroll = new Pen(new SolidColorBrush(Color.FromArgb(255, 200, 200, 200)), 1);

        ///  滑块 - 颜色
        private readonly Brush _brBlock = new LinearGradientBrush(Color.FromArgb(127, 127, 192, 127),
                                                                  Color.FromArgb(192, 255, 255, 255), 90);

        private readonly Pen _penBlock = new Pen(new SolidColorBrush(Color.FromArgb(240, 0, 127, 0)), 2);

        ///  网格
        private readonly Pen penGrid = new Pen(new SolidColorBrush(Color.FromArgb(200, 200, 200, 200)), 0.5);

        // 平均值
        private readonly Pen penAdvange = new Pen(new SolidColorBrush(Color.FromArgb(192, 255, 200, 100)), 1);

        private readonly Brush brushAdavange = new LinearGradientBrush(
                                                                       Color.FromArgb(127, 255, 255, 255),
                                                                       Color.FromArgb(192, 255, 255, 255), 90);

        ///  数据 - 背景
        private readonly Brush brushData = new LinearGradientBrush(
                                                                   Color.FromArgb(127, 0,   0,   255),
                                                                   Color.FromArgb(192, 255, 255, 255), 90);

        ///  预览 - 背景
        private readonly Brush brushPreview = new LinearGradientBrush(
                                                                      Color.FromArgb(160, 0,   0,   255),
                                                                      Color.FromArgb(192, 255, 255, 255), 90);

        ///  文字 - 颜色
        private readonly Pen penText = new Pen(new SolidColorBrush(Color.FromArgb(200, 200, 200, 200)), 0.5);

        ///  曲线
        private readonly Pen penArc = new Pen(Brushes.Red, 1);

        /// 预览曲线
        private readonly Pen penPreviewArc = new Pen(Brushes.Red, 0.5);

        ///  线条
        private readonly Pen penLine = new Pen(Brushes.Blue, 2);

        private readonly Pen penPreviewLine = new Pen(Brushes.Blue, 0.5);

        /// 点刷子
        private readonly Pen penDot = new Pen(Brushes.DarkGreen, 1);

        ///  文字 -刷子
        private readonly Brush brushText = new SolidColorBrush(Color.FromArgb(220, 255, 255, 255));

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 初始化
        /// </summary>
        ///---------------------------------------------------------------------------------------------------------------
        private void Init()
        {
            /**
            var ran = new Random();

            for (var i = 0; i < 50; i++)
            {
                int[] diff = {11, 23, 7, 35};
                var e = new WaveValue {Time = DateTimeHelper.CurrentUnixTimeMillis() + i * diff[i % diff.Length], Rssi = ran.Next(1, 128)};
                _waves.Add(e);
            }

            _waves.Sort(new WaveValueCompare());
            */
        }

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 设置数据
        /// </summary>
        /// <param name="ants"></param>
        ///---------------------------------------------------------------------------------------------------------------
        public void SetData(List<TagAnt> ants)
        {
            if (ants != null)
            {
                lock (waves)
                {
                    if (waves == null)
                    {
                        waves = new List<WaveValue>();
                    }

                    //  清空
                    this.waves.Clear();

                    //  循环
                    for (var i = 0; i < ants.Count; i++)
                    {
                        var a = ants[i];
                        //  添加
                        waves.Add(new WaveValue(a.Rssi, a.Time));
                    }

                    //  排序数据
                    this.waves.Sort(new WaveValueCompare());

                    //  检查数据
                    this.CheckData();

                    //  刷新
                    this.InvalidateVisual();
                }
            }
        }

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 配置数据
        /// </summary>
        /// <param name="wav"></param>
        ///---------------------------------------------------------------------------------------------------------------
        public void SetData(List<WaveValue> wav)
        {
            if (wav != null)
            {
                lock (waves)
                {
                    //  不忙
                    if (!isBusy)
                    {
                        if (this.waves == null)
                        {
                            this.waves = new List<WaveValue>();
                        }
                        else
                        {
                            this.waves.Clear();
                        }

                        //  添加
                        this.waves.AddRange(wav);

                        //  排序数据
                        this.waves.Sort(new WaveValueCompare());

                        //  检查数据
                        this.CheckData();

                        //  刷新
                        this.InvalidateVisual();
                    }
                    else
                    {
                        Debug.Print("is Busy");
                    }
                }
            }
        }

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 检测数据
        /// </summary>
        ///---------------------------------------------------------------------------------------------------------------
        private void CheckData()
        {
            long timeMin = 0x7FFFFFFF;
            long timeMax = 0;
            long avage   = 0;

            lock (waves)
            {
                //  有数据
                if (waves.Count > 0)
                {
                    //  最小时间
                    timeMin = waves[0].Time;
                    timeMax = timeMin;

                    //  为什么有空类型
                    foreach (var e in waves)
                    {
                        //    不允许出现双 0 

                        //Debug.Assert(e.Rssi == 0 && e.Time == 0,"元素异常");

                        if (e.Rssi == 0 && e.Time == 0)
                        {
                            Debug.Print("元素异常~");
                        }

                        //  纠正
                        if (e.Time < 0)
                        {
                            e.Time = 0;
                        }

                        //  查找最小值
                        if ((timeMin > e.Time) || (0 == timeMin))
                        {
                            timeMin = e.Time;
                        }

                        //  查找最大值
                        if (timeMax < e.Time)
                        {
                            timeMax = e.Time;
                        }

                        //  纠正时间
                        if (e.Rssi < 0)
                        {
                            e.Rssi = 0 - e.Rssi;
                        }

                        //  累计值
                        avage += e.Rssi;
                    }

                    //  平均值
                    avage /= waves.Count;

                    //  计算最大值，最小值
                    lock (block)
                    {
                        //  查找时间范围
                        block.TimeBegin = timeMin;
                        block.TimeEnd   = timeMax;

                        Debug.Assert(block.TimeBegin <= block.TimeEnd);

                        if (block.TimeEnd > block.TimeBegin + 1000000)
                        {
                            //Debug.Assert(block.TimeBegin > block.TimeEnd + 100000);

                            Debug.Print("begin:{0}", block.TimeBegin);
                            Debug.Print("  end:{0}", block.TimeEnd);
                        }
                    }
                }
            }

            //  起始位置
            //_block.BeginX = 0;

            lock (block)
            {
                // 平均值
                block.AvageRssi = avage;
            }

            //  时间换算
            //_block.TimeScale = 1;
        }

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 设置标题
        /// </summary>
        /// <param name="text"></param>
        ///---------------------------------------------------------------------------------------------------------------
        public void SetTitle(string text)
        {
            title = text;
        }

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 时间计算 (返回单位宽度ms)
        /// </summary>
        ///---------------------------------------------------------------------------------------------------------------
        private long MakeGridUnit(double unit)
        {
            //  横向坐标是 时间
            // 最小
            return WaveBlock.MakeGridSize(1 / block.Scale, unit);
        }

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 时间
        /// </summary>
        /// <param name="tm">时间</param>
        /// <returns>像素</returns>
        ///---------------------------------------------------------------------------------------------------------------
        private double MakeTime2Pixel(long t, double w)
        {
            return t * w / block.GetTimeBlock();
        }

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 像素到时间
        /// </summary>
        /// <param name="sx">像素</param>
        /// <param name="w">宽度</param>
        /// <returns>时间</returns>
        ///---------------------------------------------------------------------------------------------------------------
        private long MakePixel2Time(double x, double w)
        {
            return (long) (x * block.GetTimeBlock() / w);
        }

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 获取横向比例
        /// </summary>
        /// <param name="rect"></param>
        /// <returns></returns>
        ///---------------------------------------------------------------------------------------------------------------
        private double GetScaleX(Rect rect)
        {
            //    全部时间
            var totalTime = block.GetTimeTotal();
            //    全部宽度
            var totalWidth = rect.Width / block.Scale;

            return totalWidth / totalTime;
        }

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 比例
        /// </summary>
        /// <param name="rect"></param>
        /// <returns></returns>
        ///---------------------------------------------------------------------------------------------------------------
        private double GetScalePreviewX(Rect rect)
        {
            //    全部时间
            var totalTime = block.GetTimeTotal();
            //    全部宽度
            var totalWidth = rect.Width;
            //    比例
            return totalWidth / totalTime;
        }

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 获取纵向比例
        /// </summary>
        /// <param name="rect"></param>
        /// <returns></returns>
        ///---------------------------------------------------------------------------------------------------------------
        private static double GetScaleY(Rect rect)
        {
            return (rect.Height) / MAX_RSSI_SEGMENT;
        }

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 预览比例
        /// </summary>
        /// <param name="rect"></param>
        /// <returns></returns>
        ///---------------------------------------------------------------------------------------------------------------
        private static double GetScalePreviewY(Rect rect)
        {
            return (rect.Height) / MAX_RSSI_SEGMENT;
        }

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        ///  重绘
        /// </summary>
        /// <param name="dc"></param>
        ///---------------------------------------------------------------------------------------------------------------
        protected override void OnRender(DrawingContext dc)
        {
            // 全部区域
            var rect = new Rect(0, 0, ActualWidth, ActualHeight);
            //  高度
            var maxY = rect.Height;
            //  宽度
            var maxX = rect.Width;

            if (isBusy)
            {
                return;
            }

            lock (this)
            {
                isBusy = true;
            }

            //    初始化
            if (!isInit)
            {
                isInit = true;
                Init();
                CheckData();
            }

            lock (this)
            {
                //--------------------
                //  剪切
                //--------------------
                var clip = new RectangleGeometry(rect);

                //--------------------
                //  使用剪切
                //--------------------
                dc.PushClip(clip);

                //--------------------
                //  背景
                //--------------------
                dc.DrawRectangle(_brushAll, _penAll, rect);

                //--------------------
                //  网格
                //--------------------
                DrawGrids(dc, rect);

                //  资源占用，必须锁定
                //lock (waves)
                {
                    //--------------------
                    // 数据
                    //--------------------
                    DrawDatas(dc, rect);

                    //--------------------
                    // 中线
                    //--------------------
                    DrawAvage(dc, rect);

                    //--------------------
                    //  滚动条
                    //--------------------
                    if (stateScroll > 0)
                    {
                        //  高度
                        var h = maxY / PREVIEW_SCALE_H;

                        //===============
                        // 上边
                        //===============
                        if (stateScroll == STATE_PREVIEW_TOP)
                        {
                            var rectScroll = new Rect(0, 0, (maxX), h);
                            DrawScroll(dc, rectScroll);
                        }
                        //===============
                        //  下边
                        //===============
                        else if (stateScroll == STATE_PREVIEW_BOTTOM)
                        {
                            var rectScroll = new Rect(0, (maxY - h), (maxX), h);
                            DrawScroll(dc, rectScroll);
                        }
                    }
                }


                //  还原剪切
                dc.Pop();
            }

            //  解锁
            lock (this)
            {
                isBusy = false;
            }
        }

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 绘制滚动条
        /// </summary>
        /// <param name="dc"></param>
        /// <param name="rect"></param>
        ///---------------------------------------------------------------------------------------------------------------
        private void DrawScroll(DrawingContext dc, Rect rect)
        {
            //   全线滑块
            dc.DrawRectangle(_brScroll, _penScroll, rect);

            var count = waves.Count;

            //  预览图,数量太大，不显示
            if (count > 1000)
            {
                // 显示预览
                DrawPreview(dc, rect, false, false, false);
            }
            else if (count > 500)
            {
                // 显示预览
                DrawPreview(dc, rect, true, false, false);
            }
            else if (count > 300)
            {
                // 显示预览
                DrawPreview(dc, rect, true, true, false);
            }
            else
            {
                DrawPreview(dc, rect, true, true, true);
            }

            lock (block)
            {
                //  块宽度
                block.Width = rect.Width * block.Scale;
            }

            //  滑块
            var rectBlock = new Rect(rect.X + block.BeginX, rect.Y, block.Width, rect.Height);
            //  滑块
            dc.DrawRoundedRectangle(_brBlock, _penBlock, rectBlock, 3, 3);
        }

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 绘制网格
        /// </summary>
        /// <param name="dc">绘制上下文</param>
        /// <param name="rect">区域</param>
        ///---------------------------------------------------------------------------------------------------------------
        private void DrawGrids(DrawingContext dc, Rect rect)
        {
            // 横线间隔
            var stepH = rect.Height / MAX_V_SEGMENT;
            // 高度
            var maxY = rect.Height;
            // 宽度
            var maxX = rect.Width;
            // X位置
            double sx = rect.X;
            // Y位置
            double sy = rect.Y;
            // 时间单位
            double r = 0;

            double x = 0;

            double y = 0;

            //  单位
            int step_unit = 1;
            // 时间宽度
            var step_ms = MakeGridUnit(step_unit);
            // 换算成宽度
            var step_px = MakeTime2Pixel(step_ms, maxX);
            // 深度限制
            var deep = 0;

            while ((step_px < 20) && (deep++ < 30))
            {
                step_unit *= 10;
                // 计算宽度
                step_ms = MakeGridUnit(step_unit);
                // 换算成宽度
                step_px = MakeTime2Pixel(step_ms, maxX);
            }

            // 起始时间
            var startMs = MakePixel2Time(block.TotalBeginX(), maxX) / step_ms * step_ms;

            //--------------------
            //  绘制网格 (竖线)
            //--------------------
            for (var i = 0; i < (rect.Width / step_px); i++)
            {
                //  起始位置
                sx = rect.X + i * step_px - block.TotalBeginX() % step_px;
                //
                sy = rect.Y + 0;
                //  竖线
                dc.DrawLine(penGrid, new Point(sx, sy), new Point(sx, sy + maxY));
                //  时间
                var tm = i * step_ms + startMs;
                //  时间单位
                var ft = new FormattedText("" + tm + " ms",
                                           CultureInfo.CurrentCulture,
                                           FlowDirection.LeftToRight,
                                           typefaceText,
                                           FontSizeGrid,
                                           Brushes.Gray);
                x = sx;
                y = sy;
                r = ft.Height / 2;

                if (i > 0)
                {
                    var rt = new RotateTransform
                    {
                        //    旋转
                        CenterX = x,
                        //    中间
                        CenterY = y,
                        //    角度
                        Angle = 90
                    };
                    //    旋转
                    dc.PushTransform(rt);
                    //    方框
                    dc.DrawRoundedRectangle(Brushes.White, penGrid, new Rect(x, y - r, ft.Width + r * 2, ft.Height), r, r);
                    //    文字
                    dc.DrawText(ft, new Point(x + r, y - r));
                    //    弹出
                    dc.Pop();
                }
            }

            //--------------------
            //   网格(横线)
            //--------------------
            for (var i = 0; i < MAX_V_SEGMENT; i++)
            {
                var ft = new FormattedText(
                                           textToFormat: "-" + i * (MAX_RSSI_SEGMENT / MAX_V_SEGMENT) + " dB",
                                           CultureInfo.CurrentCulture,
                                           FlowDirection.LeftToRight,
                                           typefaceText,
                                           FontSizeGrid,
                                           Brushes.Gray)
                {
                    //  对齐
                    TextAlignment = TextAlignment.Left
                };

                //  划线
                dc.DrawLine(penGrid, new Point(0, i * stepH), new Point(maxX, i * stepH));

                x = 0;
                y = i * stepH;

                if (i > 0)
                {
                    //  画了方框
                    dc.DrawRoundedRectangle(Brushes.White, penGrid, new Rect(x, y - r, ft.Width + r * 2, ft.Height), r, r);
                    //  文字
                    dc.DrawText(ft, new Point(x + r, y - r));
                }
            }
        }

        ///------------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 绘制平均值
        /// </summary>
        /// <param name="dc"></param>
        /// <param name="rect"></param>
        ///------------------------------------------------------------------------------------------------------------------
        private void DrawAvage(DrawingContext dc, Rect rect)
        {
            if (waves.Count > 0)
            {
                var f = new FormattedText(
                                          textToFormat: title + ",数据:" + waves.Count + "个," + "平均: -" + block.AvageRssi + " dB",
                                          CultureInfo.CurrentCulture,
                                          FlowDirection.LeftToRight,
                                          typefaceText,
                                          20,
                                          Brushes.Gray)
                {
                    //  对齐
                    TextAlignment = TextAlignment.Left
                };

                double r      = f.Height / 2;
                double x      = rect.X  + 0;
                double y      = rect.Y  + rect.Height * block.AvageRssi / MAX_RSSI_SEGMENT;
                double w      = f.Width + r                             * 2;
                double h      = f.Height;
                double deltaH = rect.Height * block.AvageDelta;

                //  划线
                dc.DrawRectangle(brushAdavange, penAdvange, new Rect(x, y - deltaH, rect.Width, 2 * deltaH));
                //
                x = (rect.Width - w) / 2;
                //  文字
                dc.DrawText(f, new Point(x + r, y - r));
            }
        }

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 绘制数据
        /// </summary>
        /// <param name="dc"></param>
        /// <param name="r"></param>
        ///---------------------------------------------------------------------------------------------------------------
        //[Obsolete]
        private void DrawDatas(DrawingContext dc, Rect rect)
        {
            //  曲线
            var poly = new PolyBezierSegment();
            //  直线
            var line = new LineSegment();

            //  下边
            double maxY = rect.Height;
            //  右边
            double maxX = rect.Width;

            double sx = 0 - block.TotalBeginX();

            double sy = 0;

            //  起始点
            var ptBegin = new Point(0 - block.TotalBeginX(), maxY);
            //  几何
            var pathGeo = new PathGeometry();
            //   路径点
            var pathFigs = new PathFigure();

            //    起始时间
            var start_ms = MakePixel2Time(block.TotalBeginX(), maxX);
            //    结束时间
            var end_ms = MakePixel2Time((block.TotalBeginX() + maxX), maxX);

            var start_idx = -1;
            var end_idx   = -1;

            //    时间比例
            //_block.TimeScale = (maxX / _block.Scale) / _block.GetTimeTotal();

            var scaleX = GetScaleX(rect);
            var scaleY = GetScaleY(rect);

            //    查找书籍
            for (var i = 0; i < waves.Count; i++)
            {
                // 时间
                var t = (waves[i].Time - block.TimeBegin);
                // 在有效范围内
                if ((t >= start_ms) && (t <= end_ms))
                {
                    // 起始序号
                    if (start_idx < 0)
                    {
                        start_idx = i;
                    }

                    // 当前序号
                    end_idx = i;
                }
            }

            if (start_idx > 0)
            {
                start_idx--;
            }

            if (start_idx > 0)
            {
                start_idx--;
            }

            if (end_idx < waves.Count - 1)
            {
                end_idx++;
            }

            if (end_idx < waves.Count - 1)
            {
                end_idx++;
            }

#if true
            //==================
            //    线条
            //==================
            if (start_idx >= 0 && end_idx >= 0)
            {
                for (var i = start_idx; ((i < waves.Count) && (i <= end_idx)); i++)
                {
                    // 位置
                    double x = waves[i].GetX(scaleX, block.TimeBegin) - block.TotalBeginX();
                    // 有效范围
                    double y = waves[i].GetY(scaleY);
                    // 当前位置
                    var ptEnd = new Point(x, y);
                    // 划线
                    dc.DrawLine(penLine, ptBegin, ptEnd);
                    // 记忆
                    ptBegin = ptEnd;
                }

                for (var i = start_idx; ((i < waves.Count) && (i <= end_idx)); i++)
                {
                    // 位置
                    double x = waves[i].GetX(scaleX, block.TimeBegin) - block.TotalBeginX();
                    // 有效范围
                    double y = waves[i].GetY(scaleY);
                    // 当前位置
                    var ptEnd = new Point(x, y);
                    // 绘制点
                    dc.DrawEllipse(Brushes.LightGreen, penDot, ptEnd, 3, 3);
                    // 记忆
                    //ptBegin = ptEnd;
                }
            }
#endif

#if true
            //==================
            //    曲线
            //==================
            if (start_idx >= 0 && end_idx >= 0)
            {
                //  第一个点            
                var prePoint = new Point(0 - block.TotalBeginX(), maxY);
                //  贝塞尔曲线
                poly.Points.Clear();
                //  添加第一个点
                poly.Points.Add(prePoint);
                //    起始位置
                for (var i = start_idx; ((i < waves.Count) && (i <= end_idx)); i++)
                {
                    //    位置
                    sx = waves[i].GetX(scaleX, block.TimeBegin) - block.TotalBeginX();
                    //    有效范围
                    sy = waves[i].GetY(scaleY);
                    //    当前
                    Point curPoint = new Point(sx, sy);
                    //    中间点
                    Point midPoint = new Point((sx + prePoint.X) / 2, (sy + prePoint.Y) / 2);
                    //    添加一个中转点
                    poly.Points.Add(midPoint);
                    //    添加一个中转点
                    poly.Points.Add(midPoint);
                    //    添加当前点
                    poly.Points.Add(curPoint);
                    //    记忆上一个
                    prePoint = curPoint;
                }

                //  添加两个苗点
                poly.Points.Add(new Point(prePoint.X, (maxY + prePoint.Y) / 2));
                //
                poly.Points.Add(new Point(prePoint.X, (maxY + prePoint.Y) / 2));
                // 末尾线条
                line.Point = new Point(prePoint.X, maxY);

                //   添加路径
                pathGeo.Figures.Add(pathFigs);
                //   添加曲线
                pathFigs.Segments.Add(poly);
                //  添加线条
                pathFigs.Segments.Add(line);

                //  起始点
                pathFigs.StartPoint = new Point(0 - block.TotalBeginX(), maxY);
                //  闭合
                pathFigs.IsClosed = true;
                //  填充
                pathFigs.IsFilled = true;
                //  冻结
                pathFigs.Freeze();

                //  绘制曲线
                dc.DrawGeometry(brushData, penArc, pathGeo);

                //-------------------------
                //  绘制标识
                //-------------------------
                for (var i = start_idx; ((i < waves.Count) && (i <= end_idx)); i++)
                {
                    var w = waves[i];
                    var ft = new FormattedText(
                                               //"-" + _waves[i].Rssi + " dB",
                                               "-" + w.Rssi + " dB," + (w.Time - block.TimeBegin) + "ms",
                                               CultureInfo.CurrentCulture,
                                               FlowDirection.LeftToRight,
                                               typefaceText,
                                               FontSizeText,
                                               Brushes.CadetBlue);

                    double r = ft.Height / 2;
                    //  位置
                    double x = w.GetX(scaleX, block.TimeBegin) - block.TotalBeginX() - ft.Width / 2 - r;
                    //  有效范围
                    double y = w.GetY(scaleY) + r + 5;
                    //  方框背景
                    dc.DrawRoundedRectangle(brushText, penText, new Rect(x, y - r, ft.Width + r + r, ft.Height), r, r);
                    //  文字标识
                    dc.DrawText(ft, new Point(x + r, y - r));
                }
            }
#endif
        }

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 绘制预览图
        /// </summary>
        /// <param name="dc"></param>
        /// <param name="r"></param>
        ///---------------------------------------------------------------------------------------------------------------
        //[Obsolete]
        private void DrawPreview(DrawingContext dc, Rect rect, bool showLine, bool showArc, bool showText)
        {
            //  画笔
            var pen = new Pen(new SolidColorBrush(Color.FromArgb(200, 200, 200, 200)), 0.5);
            //  红色
            var penRed = new Pen(new SolidColorBrush(Color.FromArgb(200, 200, 100, 100)), 1);
            //  蓝色
            var penBlue = new Pen(Brushes.Blue, 1);
            //  绿色
            var penGreen = new Pen(Brushes.Green, 0.5);

            //  刷子
            var brushPreview = new SolidColorBrush(Color.FromArgb(192, 255, 255, 255));

            //  下边
            var maxY = rect.Height;

            //  几何
            var pathGeo = new PathGeometry();
            //   路径点
            var pathFigs = new PathFigure();
            //  起始点
            var ptBegin = new Point(rect.X, rect.Y + maxY);
            //  曲线
            var poly = new PolyBezierSegment();
            //  直线
            var line = new LineSegment();
            //  第一个点            
            var prePoint = new Point(rect.X, rect.Y + maxY);

            //    横向比例
            double scaleX = GetScalePreviewX(rect);
            //    纵向比例
            double scaleY = GetScalePreviewY(rect);

#if true
            //--------------------
            //  绘制线条
            //--------------------
            if (showLine)
            {
                //  划线
                foreach (var e in waves)
                {
                    var x = e.GetX(scaleX, block.TimeBegin);
                    var y = e.GetY(scaleY);
                    // 结尾
                    var ptEnd = new Point(rect.X + x, rect.Y + y);
                    // 划线
                    dc.DrawLine(penPreviewLine, ptBegin, ptEnd);
                    // 绘制点
                    dc.DrawEllipse(Brushes.LightGreen, penGreen, ptEnd, 1, 1);
                    // 记忆
                    ptBegin = ptEnd;
                }
            }
#endif

#if true
            //--------------------
            //    显示曲线
            //--------------------
            if (showArc)
            {
                //  贝塞尔曲线
                poly.Points.Clear();
                //  添加第一个点
                poly.Points.Add(prePoint);

                foreach (var e in waves)
                {
                    double sx = rect.X + e.GetX(scaleX, block.TimeBegin);
                    double sy = rect.Y + e.GetY(scaleY);
                    //  目标点
                    Point curPoint = new Point(sx, sy);
                    //  中间点
                    Point midPoint = new Point((sx + prePoint.X) / 2, (sy + prePoint.Y) / 2);
                    //    添加一个中转点
                    poly.Points.Add(midPoint);
                    //    添加一个中转点
                    poly.Points.Add(midPoint);
                    //    添加当前点
                    poly.Points.Add(curPoint);
                    //    记忆上一个
                    prePoint = curPoint;
                }

                var endPoint = new Point(prePoint.X, (rect.Y + maxY + prePoint.Y) / 2);
                //  添加两个苗点
                poly.Points.Add(endPoint);
                //
                poly.Points.Add(endPoint);
                // 末尾线条
                line.Point = new Point(prePoint.X, rect.Y + maxY);
                //   添加路径
                pathGeo.Figures.Add(pathFigs);
                //   添加曲线
                pathFigs.Segments.Add(poly);
                //  添加线条
                pathFigs.Segments.Add(line);

                //  起始点
                pathFigs.StartPoint = new Point(rect.X + 0, rect.Y + rect.Height);
                //  闭合
                pathFigs.IsClosed = false;
                //  填充
                pathFigs.IsFilled = false;

                pathFigs.Freeze();

                //  绘制曲线
                dc.DrawGeometry(brushPreview, penRed, pathGeo);
            }
#endif

#if true
            //--------------------
            //  绘制文字
            //--------------------
            if (showText)
            {
                if (maxY > 0)
                {
                    //lock (_waves)
                    {
                        //  绘制标识
                        foreach (var t in waves)
                        {
                            var f = new FormattedText(
                                                      //"-" + t.Rssi + " dB," + t.Time +"ms",
                                                      "-" + t.Rssi + " dB",
                                                      CultureInfo.CurrentCulture,
                                                      FlowDirection.LeftToRight,
                                                      typefaceText,
                                                      maxY / 16,
                                                      Brushes.CadetBlue);

                            var r = f.Height / 2;
                            var y = t.GetY(scaleY);
                            var x = t.GetX(scaleX, block.TimeBegin) - f.Width / 2 - r;
                            //  方框背景
                            dc.DrawRoundedRectangle(brushPreview, pen, new Rect(rect.X + x, rect.Y + y - r, f.Width + r + r, f.Height), r, r);
                            //  文字标识
                            dc.DrawText(f, new Point(rect.X + x + r, rect.Y + y - r));
                        }
                    }
                }
            }
#endif
        }

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 检查鼠标
        /// </summary>
        ///---------------------------------------------------------------------------------------------------------------
        private void CheckMouse()
        {
            lock (block)
            {
                //    计算宽度
                block.Width = ActualWidth * block.Scale;
                //    纠正错误
                if (block.Width > ActualWidth)
                {
                    block.Width = ActualWidth;
                }
            }
        }

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 缩小
        /// </summary>
        ///---------------------------------------------------------------------------------------------------------------
        public void DoZoomIn()
        {
            lock (block)
            {
                if (block.Scale > 0.01)
                {
                    block.Scale /= 1.1;
                }
                else
                {
                    block.Scale = 0.01;
                }
            }

            InvalidateVisual();
        }

        ///---------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 放大
        /// </summary>
        ///---------------------------------------------------------------------------------------------------------------
        public void DoZoomOut()
        {
            lock (block)
            {
                if (block.Scale < 1)
                {
                    block.Scale *= 1.1;
                }
                else
                {
                    block.Scale = 1;
                }
            }

            InvalidateVisual();
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 移动鼠标
        /// </summary>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        protected override void OnMouseMove(MouseEventArgs e)
        {
            base.OnMouseMove(e);

            var v = e.GetPosition(this);

            //    预览区域
            var h = ActualHeight / PREVIEW_SCALE_H;

            //-------------------
            //    顶端
            //-------------------
            if ((v.Y <= h) && (v.Y >= 0))
            {
                if (!isPressDown)
                {
                    stateScroll = STATE_PREVIEW_TOP;
                }
            }
            //-------------------
            //    底端
            //-------------------
            else if ((v.Y >= ActualHeight - h) && (v.Y <= ActualHeight))
            {
                if (!isPressDown)
                {
                    stateScroll = STATE_PREVIEW_BOTTOM;
                }
            }
            //-------------------
            //    其他
            //-------------------
            else
            {
                if (stateScroll > STATE_PREVIEW_HIDE)
                {
                    if (!isPressDown)
                    {
                        //    其他区域，不按下，就设置默认隐藏
                        stateScroll = STATE_PREVIEW_HIDE;
                    }

                    //    刷新
                    InvalidateVisual();
                }
            }

            //  按住滑动
            if (isPressDown || stateScroll > STATE_PREVIEW_HIDE)
            {
                if (isPressDown)
                {
                    // 相对位置
                    var d = v.X - block.downX;

                    //    纠正鼠标
                    CheckMouse();

                    //    重新计算起始位置
                    if (d <= 0)
                    {
                        d = 0;
                    }

                    if (d > this.ActualWidth - block.Width)
                    {
                        d = this.ActualWidth - block.Width;
                    }

                    block.BeginX = d;
                }

                InvalidateVisual();
            }
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 点击鼠标
        /// </summary>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        protected override void OnMouseDown(MouseButtonEventArgs e)
        {
            base.OnMouseDown(e);

            // 检查鼠标
            CheckMouse();

            lock (block)
            {
                // 位置
                pointDown = e.GetPosition(this);

                // 有效范围
                if (block.IsInBlock(pointDown))
                {
                    //  按下数据
                    block.downX = pointDown.X - block.BeginX;
                    //  按下
                    isPressDown = true;
                    //  捕捉
                    CaptureMouse();
                }
            }
            // Debug.Print("down-X=" + _pointDown.X + "," + _block.downX + ",begin-X:" + _block.BeginX);

            //  刷新
            InvalidateVisual();
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 抬起
        /// </summary>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        protected override void OnMouseUp(MouseButtonEventArgs e)
        {
            base.OnMouseUp(e);
            //  抬起
            isPressDown = false;
            //  释放
            ReleaseMouseCapture();
            //  刷新
            InvalidateVisual();
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 滚轮
        /// </summary>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        protected override void OnMouseWheel(MouseWheelEventArgs e)
        {
            base.OnMouseWheel(e);

            //Debug.Print("Wheel:" + e.Delta);

            if (e.Delta > 0)
            {
                DoZoomIn();
            }
            else if (e.Delta < 0)
            {
                DoZoomOut();
            }
        }
    }
}