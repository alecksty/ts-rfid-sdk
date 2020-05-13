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

package com.tanso.rfid.network;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.tanso.guilib.msgbox.EMessageType;
import com.tanso.guilib.msgbox.MsgMake;
import com.tanso.rfid.R;
import com.tanso.rfid.activity.LoggerActivity;
import com.tanso.rfidlib.comm.ISettingEven;
import com.tanso.rfidlib.comm.SetupReadWrite;
import com.tanso.rfidlib.rfid.TagItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

/**
 * 丢数据
 */
public class PostData implements ISettingEven {

    private static final String TAG = "PostData";

    private static final boolean DEBUG = false;

    /**
     * 数据头
     */
    private static final boolean USE_POST_JSON_HEAD = true;

    /**
     * 定位
     */
    private static Location location;

    /**
     * 提交时间
     */
    private static long time = 0;

    /**
     * 编码
     */
    public String id = "10000";

    /**
     * 菜单显示名
     */
    public String name = "name";

    /**
     * 地址
     */
    //public String url = "http://www.website.com";
    public String url = "http://211.149.250.246:8090/queryData.aspx";

    /**
     * 用户名
     */
    public String user = "user";

    /**
     * 密码
     */
    public String pwd = "pwd";

    /**
     * 结果
     */
    public String result = "";

    /**
     * 列表
     */
    public ArrayList<TagItem> list = new ArrayList<>();

    /**
     * 上下文
     */
    private Context context;

    /**
     * 用户码
     */
    private String dev = "dev";

    /**
     * 导航
     */
    public boolean gps = false;

    /**
     * 自动
     */
    public boolean auto = false;

    /**
     * 打开
     */
    public boolean enable = false;

    /**
     * 延时 100 ms
     */
    public int delay = 100;

    /**
     * 提交最后一个标签
     */
    public static TagItem lastTag;

    /**
     * 消息接口
     */
    public interface OnPostEvent {
        /**
         * 回调
         *
         * @param post   : 对象
         * @param result : 结果
         * @param error  : 错误 (0:没错误)
         */
        void OnPostResult(PostData post, String result, int error);
    }

    /**
     * 消息接口
     */
    private OnPostEvent even;

    private static final int MSG_SHOW_BOX = 1000;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_SHOW_BOX:
                    if (msg.obj instanceof String) {
                        String txt = (String) msg.obj;
                        MsgMake.show(context, EMessageType.fromInt(msg.arg1), txt, msg.arg2);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 初始化
     *
     * @param context : 上下文
     */
    public PostData(Context context) {
        this.context = context;
    }

    /**
     * 设置消息接口
     *
     * @param even : 消息接口
     */
    public void setEven(OnPostEvent even) {
        this.even = even;
    }

    /**
     * 填充数据
     *
     * @param l : 列表
     */
    public void fillData(final ArrayList<TagItem> l) {
        //  清空
        list.clear();
        //  添加
        list.addAll(l);
    }

    public void clear() {
        //  添加
        list.clear();
    }

    public void add(TagItem tag) {
        //  添加
        list.add(tag);
    }

    /**
     * 获取设备ID
     *
     * @param context : 上下文
     * @return :串号
     */
    @SuppressLint("HardwareIds")
    private static String getUniqueId(Context context) {
        String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String id        = androidID + Build.SERIAL;
        try {
            return toMD5(id);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return id;
        }
    }

    /**
     * 生成MD5
     *
     * @param text : 文本
     * @return : 生成的MD5
     * @throws NoSuchAlgorithmException : 运算错误
     */
    private static String toMD5(String text) throws NoSuchAlgorithmException {
        //获取摘要器 MessageDigest
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        //通过摘要器对字符串的二进制字节数组进行hash计算
        byte[] digest = messageDigest.digest(text.getBytes());

        StringBuilder sb = new StringBuilder();
        for (byte aDigest : digest) {
            //循环每个字符 将计算结果转化为正整数;
            int digestInt = aDigest & 0xff;
            //将10进制转化为较短的16进制
            String hexString = Integer.toHexString(digestInt);
            //转化结果如果是个位数会省略0,因此判断并补0
            if (hexString.length() < 2) {
                sb.append(0);
            }
            //将循环结果添加到缓冲区
            sb.append(hexString);
        }
        //返回整个结果
        return sb.toString();
    }

    /**
     * 提交线程
     */
    public class ThreadPost extends Thread {
        @Override
        public void run() {
            //super.run();
            //  超时
            final int DEF_TIMEOUT_TIME = 1000;

            Message msg = new Message();

            try {
                if (context != null) {
                    //  设备序号
                    dev = getUniqueId(context);

                    if (dev != null) {
                        //  时间
                        time = System.currentTimeMillis();
                        //  非自动
                        if (!auto) {

                            try {
                                msg.what = MSG_SHOW_BOX;
                                msg.obj = context.getString(R.string.str_msg_posting) + "...";
                                msg.arg1 = EMessageType.toInt(EMessageType.MSG_SUCCESS);
                                msg.arg2 = DEF_TIMEOUT_TIME;
                                handler.sendMessage(msg);
                                //  提示
                                //new MsgMake(context, MessageType.MSG_SUCCESS, context.getString(R.string.str_msg_posting) + "...", DEF_TIMEOUT_TIME);
                                //MsgMake.show(context, MessageType.MSG_SUCCESS, context.getString(R.string.str_msg_posting) + "...", DEF_TIMEOUT_TIME);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        //  推送
                        result = HttpRequest.sendPost(PostData.this.url, toPostJSON().toString());

                        //===========================
                        //  消息处理
                        //===========================
                        if (even != null) {
                            //  消息处理
                            even.OnPostResult(PostData.this, result, 0);
                        } else
                            //  非自动
                            if (!auto) {
                                try {
                                    //  提示
                                    if (result != null) {
                                        msg.what = MSG_SHOW_BOX;
                                        msg.obj = context.getString(R.string.str_msg_post_done) + "\n" + result;
                                        msg.arg1 = EMessageType.toInt(EMessageType.MSG_SUCCESS);
                                        msg.arg2 = DEF_TIMEOUT_TIME;
                                        handler.sendMessage(msg);
                                        //  提示
                                        //MsgMake.show(context, MessageType.MSG_SUCCESS, context.getString(R.string.str_msg_post_done) + "\n" + result, DEF_TIMEOUT_TIME * 3);
                                    } else {
                                        msg.what = MSG_SHOW_BOX;
                                        msg.obj = context.getString(R.string.str_msg_post_failed) + "\n" + HttpRequest.getErrorMessage();
                                        msg.arg1 = EMessageType.toInt(EMessageType.MSG_ERROR);
                                        msg.arg2 = DEF_TIMEOUT_TIME;
                                        handler.sendMessage(msg);
                                        //MsgMake.show(context, MessageType.MSG_ERROR, context.getString(R.string.str_msg_post_failed) + "\n" + HttpRequest.getErrorMessage(), DEF_TIMEOUT_TIME);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (!auto) {
                    msg.what = MSG_SHOW_BOX;
                    msg.obj = context.getString(R.string.str_msg_post_failed) + "\n" + e.getMessage();
                    msg.arg1 = EMessageType.toInt(EMessageType.MSG_ERROR);
                    msg.arg2 = DEF_TIMEOUT_TIME;
                    handler.sendMessage(msg);

                    //new MsgMake(context, MessageType.MSG_ERROR, context.getString(R.string.str_msg_post_failed) + "\n" + e.getMessage(), DEF_TIMEOUT_TIME);
                } else {
                    LoggerActivity.log_e(TAG, e.getMessage());
                }
            } catch (ExceptionInInitializerError e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置开关
     *
     * @param onoff ： 开关
     */
    public void setGPS(boolean onoff) {
        if (onoff) {
            startLocate(context);
        }
        this.gps = onoff;
    }

    /**
     * 提交数据
     *
     * @param context : 上下文
     */
    public void doPost(Context context) {
        try {
            this.context = context;

            //  需要提交定位.
            if (this.gps) {
                //  没有定位,启动定位
                if (location == null) {
                    startLocate(context);
                }
            } else {
                System.err.println("GPS没有开启!");
            }

            //  线程处理
            ThreadPost threadPost = new ThreadPost();
            threadPost.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始定位
     */
    private static void startLocate(Context context) {

        if (context != null) {
            //  定位服务
            LocationManager mLocation = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            //  功能有效
            boolean enabled = false;

            if (mLocation != null) {
                enabled = mLocation.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } else {
                System.err.println("定位管理器没打开！");
            }

            if (enabled) {
                /*
                 * 绑定监听
                 * 参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种，前者是GPS,后者是GPRS以及WIFI定位
                 * 参数2，位置信息更新周期.单位是毫秒
                 * 参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
                 * 参数4，监听
                 * 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新
                 */
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                //  请求定位
                mLocation.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            } else {
                System.err.println("定位管理器 - 打开失败！");
            }
        }
    }

    /**
     * 定位监听
     */
    private static LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location loc) {
            //位置信息变化时触发
            if (DEBUG) {
                Log.e(TAG, "定位方式：" + loc.getProvider());
                Log.e(TAG, "纬度：" + loc.getLatitude());
                Log.e(TAG, "经度：" + loc.getLongitude());
                Log.e(TAG, "海拔：" + loc.getAltitude());
                Log.e(TAG, "时间：" + loc.getTime());
            }

            PostData.location = loc;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //  GPS状态变化时触发
            switch (status) {
                case LocationProvider.AVAILABLE:
                    if (DEBUG) {
                        Log.e(TAG, "当前GPS状态为可见状态");
                    }
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    if (DEBUG) {
                        Log.e(TAG, "当前GPS状态为服务区外状态");
                    }
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    if (DEBUG) {
                        Log.e(TAG, "当前GPS状态为暂停服务状态");
                    }
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            //  GPS开启时触发
            if (DEBUG) {
                Log.e(TAG, "onProviderEnabled: " + provider);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            //  GPS禁用时触发
            if (DEBUG) {
                Log.e(TAG, "onProviderDisabled: " + provider);
            }
        }
    };

    /**
     * 生成json数据
     *
     * @return : 数据
     */
    private synchronized JSONObject toPostJSON() {

        JSONObject json = new JSONObject();

        try {

            if (USE_POST_JSON_HEAD) {
                //  序号
                json.put("ID", id);
                //  设备
                json.put("dev", dev);
                //  用户
                json.put("usr", user);
                //  密码
                json.put("pwd", pwd);
                //  时间
                json.put("tim", time);

                //  如果有定位数据,提交.
                if (location != null) {
                    //  纬度
                    json.put("lat", location.getLatitude());
                    //  经度
                    json.put("lon", location.getLongitude());
                } else {
                    if (DEBUG) {
                        System.out.println("无定位!");
                    }
                }
            }

            //  列表
            JSONArray jsonArray = new JSONArray();
            if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    TagItem e = list.get(i);
                    jsonArray.put(i, e.postJSON());
                }
                //  列表
                json.put("list", jsonArray);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (DEBUG) {
            System.err.print(json.toString());
        }

        return json;
    }

    @Override
    public void OnSetupUpdate(SetupReadWrite set) {
        name = set.readWrite("name", name);
        url = set.readWrite("url", url);
        dev = set.readWrite("dev", dev);

        user = set.readWrite("user", user);
        pwd = set.readWrite("pwd", pwd);

        id = set.readWrite("id", id);
        auto = set.readWrite("auto", auto);
        enable = set.readWrite("enable", enable);
        delay = set.readWrite("delay", delay);
        gps = set.readWrite("gps", gps);
    }

    /**
     * 保存 / 读取数据
     *
     * @param write : 保存
     * @param type  : 类型
     */
    public void setupUpdate(boolean write, String type) {
        SetupReadWrite sr = new SetupReadWrite(context, write, this.getClass().getName() + type);
        this.OnSetupUpdate(sr);
    }
}
