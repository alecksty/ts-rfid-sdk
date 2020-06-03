using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using RfidLib.rfid;
using WpfRfid.Mode;

namespace WpfRfid
{
    public partial class DialogMessage : Window
    {
        //    没有显示
        public static bool IsShowed = false;

        //  消息列表
        private ObservableCollection<CsTagItem> listItems = new ObservableCollection<CsTagItem>();

        //
        private ObservableCollection<string> listTexts = new ObservableCollection<string>();

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 启动代码
        /// </summary>
        ///-------------------------------------------------------------------------------------------------------------
        public DialogMessage()
        {
            InitializeComponent();

            //  消息 - 列表
            lvItems.ItemsSource = listItems;
            //  内容 - 列表
            lvMessage.ItemsSource = listTexts;
            //   显示
            IsShowed = true;
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 关闭窗口
        /// </summary>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        protected override void OnClosed(EventArgs e)
        {
            base.OnClosed(e);
            IsShowed = false;
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 设置列表（只要移动的物品列表）
        /// </summary>
        /// <param name="list"></param>
        ///-------------------------------------------------------------------------------------------------------------
        public void SetItems(IEnumerable<TagItem> list)
        {
            listItems.Clear();
            foreach (var e in list)
            {
                listItems.Add(new CsTagItem(e));
            }
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 加载丢失数据
        /// </summary>
        /// <param name="tag"></param>
        ///-------------------------------------------------------------------------------------------------------------
        public void LoadInfo(TagItem tag)
        {
            Debug.Print("LoadInfo() : " + tag);

            listTexts.Clear();

            foreach (var ant in tag.ArrayAntLast)
            {
                if (ant.Records != null)
                {
                    if (ant.Records.Count > 0)
                    {
                        foreach (var record in ant.Records)
                        {
                            listTexts.Add(record.ToString());
                        }
                    }
                }
            }

            if (listTexts.Count == 0)
            {
                listTexts.Add("(无记录)");
            }

            //    标题
            textSelTitle.Text = tag.GetTextEpc();
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 选中内容
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        ///-------------------------------------------------------------------------------------------------------------
        private void LvItems_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (sender is ListView listView)
            {
                // 序号
                var sel = listView.SelectedIndex;
                // 选中
                var cur = listView.SelectedItem;

                //    当前项目
                if (cur is CsTagItem item)
                {
                    //sel = item.Index - 1;
                    LoadInfo(item.tag);
                    listView.SelectedIndex = sel;
                }
            }
        }
    }
}