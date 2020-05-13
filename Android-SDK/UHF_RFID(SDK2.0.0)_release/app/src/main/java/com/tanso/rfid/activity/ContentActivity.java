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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.tanso.guilib.dialog.IDlgEvent;
import com.tanso.guilib.dialog.DlgMake;
import com.tanso.guilib.msgbox.EMessageType;
import com.tanso.guilib.msgbox.MsgMake;
import com.tanso.rfid.R;
import com.tanso.rfid.user.Global;
import com.tanso.rfid.user.RfidApp;
import com.tanso.rfidlib.bytes.ByteStream;
import com.tanso.rfidlib.comm.SDK;
import com.tanso.rfidlib.rfid.IRfidEvent;
import com.tanso.rfidlib.rfid.RfidManager;
import com.tanso.rfidlib.rfid.TagItem;
import com.tanso.rfidlib.rfid.base.BaseAck;
import com.tanso.rfidlib.rfid.base.BaseCmd;
import com.tanso.rfidlib.rfid.base.BaseReader;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

import static com.tanso.rfid.user.RfidApp.USE_TAG_WRITE_SUPPORT;
import static com.tanso.rfidlib.rfid.base.BaseCmd.RFID_CMD_ERROR;
import static com.tanso.rfidlib.rfid.base.BaseCmd.RFID_CMD_GET_SELECT;
import static com.tanso.rfidlib.rfid.base.BaseCmd.RFID_CMD_KILL;
import static com.tanso.rfidlib.rfid.base.BaseCmd.RFID_CMD_LOCK;
import static com.tanso.rfidlib.rfid.base.BaseCmd.RFID_CMD_READ;
import static com.tanso.rfidlib.rfid.base.BaseCmd.RFID_CMD_SET_SELECT;
import static com.tanso.rfidlib.rfid.base.BaseCmd.RFID_CMD_WRITE;

/**
 * 详细标签处理(读/写/锁/灭)
 */
public class ContentActivity extends AppCompatActivity implements View.OnClickListener, IDlgEvent, IRfidEvent {

    private static final boolean DEBUG = Global.DEBUG;
    private static final String  TAG   = "ContentActivity";

    private static final int MSG_DELAY_TIME = 1000;

    //   等待时间300ms
    private static final int WAIT_DELAY = 50;

    //  消息
    private static final int ID_MSG_MENU     = 1000;
    private static final int ID_MSG_EDIT_EPC = 1001;
    private static final int ID_MSG_EDIT_TID = 1002;
    private static final int ID_MSG_EDIT_RFU = 1003;
    private static final int ID_MSG_EDIT_USR = 1004;

    private static final int ID_MSG_LOCK_TAG = 1005;
    private static final int ID_MSG_KILL_TAG = 1006;

    /**
     * 当前项目
     */
    private static TagItem tagItem;

    private static final int TAG_TID_SIZE = 12;
    private static final int TAG_USR_SIZE = 32;

    /**
     * 操作次数
     */
    private static final int MAX_COUNT = 5;

    /**
     * 错误累计
     */
    private static int errorCount = 0;

    /**
     * 响应
     */
    private static final int MSG_WRITE_EPC = 1000;
    private static final int MSG_WRITE_USR = 1001;
    private static final int MSG_WRITE_RFU = 1002;
    private static final int MSG_LOCK_TAG  = 1003;
    private static final int MSG_KILL_TAG  = 1004;

    //  更新文字
    private static final int MSG_UPDATE_TEXT = 2001;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //==========================
                //  写标签
                //==========================
                case MSG_WRITE_EPC:
                    if (USE_TAG_WRITE_SUPPORT) {
                        if (msg.obj instanceof String) {
                            String txt = (String) msg.obj;
                            tagChangeEPC(tagItem, txt);
                        }
                    } else {
                        new MsgMake(ContentActivity.this, EMessageType.MSG_NOTIFY, getString(R.string.str_msg_not_support), MSG_DELAY_TIME);
                    }
                    break;

                //==========================
                //  写标签
                //==========================
                case MSG_WRITE_USR:
                    if (USE_TAG_WRITE_SUPPORT) {
                        if (msg.obj instanceof String) {
                            String txt = (String) msg.obj;
                            tagChangeUSR(tagItem, txt);
                        }
                    } else {
                        new MsgMake(ContentActivity.this, EMessageType.MSG_NOTIFY, getString(R.string.str_msg_not_support), MSG_DELAY_TIME);
                    }
                    break;

                //==========================
                //  锁定
                //==========================
                case MSG_LOCK_TAG:
                    if (USE_TAG_WRITE_SUPPORT) {
                        if (msg.obj instanceof String) {
                            //String txt = (String) msg.obj;
                            //tagLockTag(tagItem);
                        }
                    } else {
                        new MsgMake(ContentActivity.this, EMessageType.MSG_NOTIFY, getString(R.string.str_msg_not_support), MSG_DELAY_TIME);
                    }
                    break;

                //==========================
                //  灭活
                //==========================
                case MSG_KILL_TAG:
                    if (USE_TAG_WRITE_SUPPORT) {
                        if (msg.obj instanceof String) {
                            String txt = (String) msg.obj;
                            txt = txt.replace(" ", "");
                            BaseCmd.mKillPassword = Integer.parseInt(txt, 16);
                            tagKillTag(tagItem);
                        }
                    } else {
                        new MsgMake(ContentActivity.this, EMessageType.MSG_NOTIFY, getString(R.string.str_msg_not_support), MSG_DELAY_TIME);
                    }
                    break;

                //==========================
                //  刷新
                //==========================
                case MSG_UPDATE_TEXT:
                    if (tagItem != null) {
                        updateText();
                    }
                    break;
            }
        }
    };

    /**
     *  刷新
     */
    private void updateText() {
        //  PC
        TextView textPC = this.findViewById(R.id.text_card_pc);
        textPC.setText(String.format("PC %04X", tagItem.pc));

        //  EPC
        TextView textEPC = this.findViewById(R.id.text_card_epc);
        textEPC.setText(tagItem.getTextSpaceEpc(4));

        //  TID
        TextView textTID = this.findViewById(R.id.text_card_tid);
        textTID.setText(tagItem.getTextSpaceTid(4));

        //  USR
        TextView textUSR = this.findViewById(R.id.text_card_user);
        textUSR.setText(tagItem.getTextSpaceUsr(4));

        //  RFU
        TextView textRfu = this.findViewById(R.id.text_card_rfu);
        textRfu.setText(tagItem.getTextSpaceRfu(4));

        //  COUNT
        TextView textCount = this.findViewById(R.id.text_card_count);
        textCount.setText(String.format(Locale.ENGLISH, "CNT %04d", tagItem.count));
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_content);

        //  返回
        ImageView imgReturn = this.findViewById(R.id.image_return);
        if (imgReturn != null) {
            imgReturn.setOnClickListener(this);
        }

        //  其他
        ImageView imgOther = this.findViewById(R.id.image_other);
        if (imgOther != null) {
            imgOther.setOnClickListener(this);
        }

        //  读数据
        TextView textTagRead = this.findViewById(R.id.text_tag_read);
        if (textTagRead != null) {
            textTagRead.setClickable(true);
            textTagRead.setOnClickListener(this);
        }

        //  写数据
        TextView textTagWrite = this.findViewById(R.id.text_tag_write);
        if (textTagWrite != null) {
            textTagWrite.setClickable(true);
            textTagWrite.setOnClickListener(this);
        }

        //  锁定
        TextView textTagLock = this.findViewById(R.id.text_tag_lock);
        if (textTagLock != null) {
            textTagLock.setClickable(true);
            textTagLock.setOnClickListener(this);
        }

        //  灭活
        TextView textTagKill = this.findViewById(R.id.text_tag_kill);
        if (textTagKill != null) {
            textTagKill.setClickable(true);
            textTagKill.setOnClickListener(this);
        }

        //  密码
        TextView textTagPass = this.findViewById(R.id.text_tag_pass);
        if (textTagPass != null) {
            textTagPass.setClickable(true);
            textTagPass.setOnClickListener(this);
        }

        //  设置
        TextView textTagSetup = this.findViewById(R.id.text_tag_setup);
        if (textTagSetup != null) {
            textTagSetup.setClickable(true);
            textTagSetup.setOnClickListener(this);
        }

        updateText();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //  应用
        RfidApp theApp = (RfidApp) this.getApplication();
        //  暂停
        MainActivity.setPaused(true);

        RfidManager man = theApp.rfidManager;
        //  清空指令
        man.cleanCmd();
        //  读写器
        BaseReader reader = man.getReader();
        //  停止
        theApp.portWrite(reader.getCmd().rfid_stop(), true);
        //  消息接口
        reader.ack.setRfidEvent(this);
        //  读取 - 数据
        tagReadAll(tagItem);
    }

    /**
     * 当前目标
     *
     * @param tag ： 标签
     */
    public static void setTagItem(TagItem tag) {
        tagItem = tag;
    }

    @Override
    public void onClick(View v) {

        RfidApp.doEffect(this, RfidApp.EFFECT_KEY);

        switch (v.getId()) {
            case R.id.image_return:
                this.finish();
                break;

            case R.id.image_other:
                break;

            case R.id.text_tag_read:
                tagReadAll(tagItem);
                break;

            case R.id.text_tag_write:
                menuEditEPC(tagItem);
                break;

            case R.id.text_tag_lock:
                tagLockTag(tagItem);
                break;

            case R.id.text_tag_kill:
                menuKillTag(tagItem);
                break;

            case R.id.text_tag_pass:
                break;

            case R.id.text_tag_setup:
                break;
        }
    }

    private void tagLockTag(TagItem tagItem) {

    }

    @Override
    public boolean OnDialogEvent(int id, Dialog dlg, View v, int pos, Object param) {
        switch (id) {
            //====================
            //  菜单
            //====================
            case ID_MSG_MENU:
                switch (pos) {
                    case 0:
                        menuEditEPC(tagItem);
                        break;

                    case 1:
                        menuEditUSR(tagItem);
                        break;
                }
                return true;

            //====================
            //  修改EPC
            //====================
            case ID_MSG_EDIT_EPC:
                if (param instanceof String) {
                    String  txt = (String) param;
                    Message msg = new Message();
                    msg.obj = txt;
                    msg.what = MSG_WRITE_EPC;
                    handler.sendMessage(msg);
                }
                return true;

            //====================
            //  修改TID
            //====================
            case ID_MSG_EDIT_TID:
                return true;

            //====================
            //  修改RUF
            //====================
            case ID_MSG_EDIT_RFU:
                if (param instanceof String) {
                    String txt = (String) param;
                    tagChangeEPC(tagItem, txt);
                }
                return true;

            //====================
            //  修改USR
            //====================
            case ID_MSG_EDIT_USR:
                if (param instanceof String) {
                    String  txt = (String) param;
                    Message msg = new Message();
                    msg.obj = txt;
                    msg.what = MSG_WRITE_USR;
                    handler.sendMessage(msg);
                }
                return true;

            //====================
            //  修改USR
            //====================
            case ID_MSG_KILL_TAG:
                if (param instanceof String) {
                    String  txt = (String) param;
                    Message msg = new Message();
                    msg.obj = txt;
                    msg.what = MSG_KILL_TAG;
                    handler.sendMessage(msg);
                }
                return true;
        }
        return false;
    }

    /**
     * 修改EPC
     *
     * @param tag : 标签
     */
    private void menuEditEPC(TagItem tag) {
        DlgMake d = new DlgMake();
        d.dialogEditor(ID_MSG_EDIT_EPC, this, getString(R.string.str_edit) + " EPC", tag.getTextEpc(), this);
    }

    /**
     * 修改USR
     *
     * @param tag : 标签
     */
    private void menuEditUSR(TagItem tag) {
        DlgMake d = new DlgMake();
        d.dialogEditor(ID_MSG_EDIT_EPC, this, getString(R.string.str_edit) + " USR", tag.getTextEpc(), this);
    }

    /**
     * 修改RFU
     *
     * @param tag :标签
     */
    private void menuEditRfu(TagItem tag) {
        DlgMake d = new DlgMake();
        d.dialogEditor(ID_MSG_EDIT_RFU, this, getString(R.string.str_edit) + " Rfu", tag.getTextEpc(), this);
    }

    /**
     * 灭活标签
     *
     * @param tag : 标签
     */
    private void menuKillTag(TagItem tag) {
        DlgMake d = new DlgMake();
        d.dialogEditor(ID_MSG_KILL_TAG, this, getString(R.string.str_kill_password), "00 00 00 00", this);
    }

    /**
     * 改变 - EPC
     *
     * @param tag : 标签
     * @return : 成功/失败
     */
    private boolean tagReadAll(TagItem tag) {
        //  事件接口
        RfidApp theApp = (RfidApp) this.getApplication();
        //  读写器
        BaseReader reader = theApp.rfidManager.getReader();
        //  命令对象
        BaseCmd cmd = reader.getCmd();
        //  暂停
        MainActivity.setPaused(true);
        //  清空队列
        theApp.rfidManager.cleanCmd();

        //  停止
        theApp.portWrite(cmd.rfid_stop(), true);
        //  选择
        theApp.portWrite(cmd.rfid_set_select(BaseCmd.RFID_BANK_EPC, 0, tag.epc), true);
        
        errorCount = 0;

        //  读取数据 (64 bits - 8 Bytes)
        theApp.portWrite(cmd.rfid_read(BaseCmd.mAccessPassword, BaseCmd.RFID_BANK_TID, 0, TAG_TID_SIZE), true);


        //  提示状态
        new MsgMake(this, EMessageType.MSG_WARNING, getString(R.string.str_msg_processing) + "...", MSG_DELAY_TIME);

        return true;
    }

    /**
     * 改变EPC
     *
     * @param tag    : 标签
     * @param newEpc : 新标签
     * @return : 成功/失败
     */
    private synchronized boolean tagChangeEPC(TagItem tag, String newEpc) {

        byte[] array = null;
        //  事件接口
        RfidApp theApp = (RfidApp) this.getApplication();
        //  管理器
        RfidManager manager = theApp.rfidManager;
        //  读写器
        BaseReader reader = manager.getReader();
        //  命令
        BaseCmd cmd = reader.getCmd();

        //  暂停
        MainActivity.setPaused(true);

        //  清空指令
        manager.cleanCmd();

        //	停止
        theApp.portWrite(cmd.rfid_stop(), true);

        //  选择
        theApp.portWrite(cmd.rfid_set_select(BaseCmd.RFID_BANK_EPC, 0, tag.epc), true);

        //  转码
        try {
            array = newEpc.getBytes(SDK.TAG_CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (array == null) {
            array = newEpc.getBytes();
        }

        //  计数
        errorCount = 0;

        for (int i = 0; i < MAX_COUNT; i++) {
            //  选中
            theApp.portWrite(cmd.rfid_set_select(BaseCmd.RFID_BANK_EPC, 0, tag.epc), true);
            //  写标签(要是选中不成功)
            theApp.portWrite(cmd.rfid_write(0, BaseCmd.RFID_BANK_EPC, 0, array), true);
        }

        //  去掉选择
        theApp.portWrite(cmd.rfid_set_select(BaseCmd.RFID_BANK_EPC, 0, null), true);
        //  删除此标签
        manager.remove(tag);
        //  提示状态
        new MsgMake(this, EMessageType.MSG_WARNING, getString(R.string.str_msg_processed) + "!", MSG_DELAY_TIME * 2);

        return true;
    }

    /**
     * 改变EPC
     *
     * @param tag    : 标签
     * @param newUsr : 内容
     * @return : 成功/失败
     */
    private synchronized boolean tagChangeUSR(TagItem tag, String newUsr) {

        byte[] array = null;
        //  事件接口
        RfidApp theApp = (RfidApp) this.getApplication();
        //  读写器
        BaseReader reader = theApp.rfidManager.getReader();
        //  暂停
        MainActivity.setPaused(true);

        //  清空
        theApp.rfidManager.cleanCmd();

        //	停止
        theApp.portWrite(reader.getCmd().rfid_stop(), true);
        //  选择
        theApp.portWrite(reader.getCmd().rfid_set_select(BaseCmd.RFID_BANK_EPC, 0, tag.epc), true);
        //  转码
        try {
            array = newUsr.getBytes(SDK.TAG_CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (array == null) {
            array = newUsr.getBytes();
        }

        //  计数
        errorCount = 0;

        for (int i = 0; i < MAX_COUNT; i++) {
            //  写标签
            theApp.portWrite(reader.getCmd().rfid_write(0, BaseCmd.RFID_BANK_USR, 0, array), true);
        }

        //  提示状态
        new MsgMake(this, EMessageType.MSG_WARNING, getString(R.string.str_msg_processed) + "!", MSG_DELAY_TIME * 2);

        return true;
    }

    /**
     * 改变 - EPC
     *
     * @param tag : 标签
     * @return : 成功/失败
     */
    private synchronized boolean tagKillTag(TagItem tag) {
        //  事件接口
        RfidApp theApp = (RfidApp) this.getApplication();
        //  读写器
        BaseReader reader = theApp.rfidManager.getReader();
        //  命令
        BaseCmd cmd = reader.getCmd();
        //  暂停
        MainActivity.setPaused(true);
        //  清空队列
        theApp.rfidManager.cleanCmd();

        //  停止
        theApp.portWrite(cmd.rfid_stop(), true);
        //  选择标签
        theApp.portWrite(cmd.rfid_set_select(BaseCmd.RFID_BANK_EPC, 0, tag.epc), true);

        //  灭活 (00,00,00,00)
        byte[]     pwd = new byte[]{1, 1, 1, 1};
        ByteStream bs  = new ByteStream(pwd);

        //bs.put(BaseCmd.mKillPassword);

        //  访问密码
        theApp.portWrite(cmd.rfid_write(BaseCmd.mAccessPassword, BaseCmd.RFID_BANK_RFU, 0, bs.getData()), true);

        //  灭活密码
        theApp.portWrite(cmd.rfid_kill(0x01010101), true);

        if (DEBUG) {
            LoggerActivity.log_e(TAG, "灭活 : " + tag.getTextEpc() + ",密码 : " + BaseCmd.mKillPassword);
        }

        errorCount = 0;
        //  提示状态
        new MsgMake(this, EMessageType.MSG_WARNING, getString(R.string.str_msg_processing) + "...", MSG_DELAY_TIME);

        return true;
    }

    @Override
    public void OnRfidResponse(BaseReader reader, int type, int cmd, byte[] param, Object obj) {

        if (DEBUG) {
            if (obj == null) {
                LoggerActivity.log_e(TAG, String.format(Locale.ENGLISH, "OnRfidResponse(type:%d,cmd:%02x,para:%s,obj:null)\n", type, cmd & 0xFF, Global.arrayToText(param)));
            } else {
                LoggerActivity.log_e(TAG, String.format(Locale.ENGLISH, "OnRfidResponse(type:%d,cmd:%02x,para:%s,obj:%s)\n", type, cmd & 0xFF, Global.arrayToText(param), obj.toString()));
            }
        }

        //  响应数据
        if (type == BaseAck.RESPONSE_TYPE_RESPONSE) {
            if (param != null) {
                if (param.length > 0) {
                    //====================
                    //  命令处理
                    //====================
                    switch (cmd) {
                        //====================
                        //  设置选择
                        //====================
                        case RFID_CMD_SET_SELECT:
                            LoggerActivity.log_d(TAG, "选中标签 : \"" + tagItem.getTextEpc() + "\"");
                            break;

                        //====================
                        //  读取选择
                        //====================
                        case RFID_CMD_GET_SELECT:
                            LoggerActivity.log_d(TAG, "选中标签 : \"" + tagItem.getTextEpc() + "\"");
                            break;

                        //====================
                        //  读到数据
                        //====================
                        case RFID_CMD_READ:
                            errorCount = 0 - MAX_COUNT - 1;
                            LoggerActivity.log_d(TAG, "读取标签 : \"" + tagItem + "\"");
                            //  刷新
                            handler.sendEmptyMessage(MSG_UPDATE_TEXT);
                            break;

                        //====================
                        //  写入数据
                        //====================
                        case RFID_CMD_WRITE:
                            new MsgMake(this, EMessageType.MSG_SUCCESS, getString(R.string.str_msg_write_tag) + " : " + tagItem.getTextEpc() + getString(R.string.str_success) + "!", MSG_DELAY_TIME * 2);
                            errorCount = 0 - MAX_COUNT - 1;
                            handler.sendEmptyMessage(MSG_UPDATE_TEXT);
                            break;

                        //====================
                        //  锁定
                        //====================
                        case RFID_CMD_LOCK:
                            new MsgMake(this, EMessageType.MSG_SUCCESS, getString(R.string.str_msg_lock_tag) + " : " + tagItem.getTextEpc() + getString(R.string.str_success) + "!", MSG_DELAY_TIME * 2);
                            errorCount = 0 - MAX_COUNT - 1;
                            break;

                        //====================
                        //  灭活
                        //====================
                        case RFID_CMD_KILL:
                            new MsgMake(this, EMessageType.MSG_SUCCESS, getString(R.string.str_msg_kill_tag) + " : " + tagItem.getTextEpc() + getString(R.string.str_success) + "!", MSG_DELAY_TIME * 2);
                            errorCount = 0 - MAX_COUNT - 1;
                            break;

                        //====================
                        //  错误
                        //====================
                        case RFID_CMD_ERROR:
                            errorCount++;
                            if (errorCount >= MAX_COUNT) {
                                new MsgMake(this, EMessageType.MSG_ERROR, getString(R.string.str_msg_write_failed) + "!", MSG_DELAY_TIME);
                            }
                            break;

                        default:
                            break;
                    }
                }
            }
        }
    }
}
