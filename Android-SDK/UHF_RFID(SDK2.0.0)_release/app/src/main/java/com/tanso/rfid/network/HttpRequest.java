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


import com.tanso.rfid.activity.LoggerActivity;
import com.tanso.rfid.user.Global;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * 网络请求
 */
public class HttpRequest {

    /**
     * 调试
     */
    private static final boolean DEBUG     = Global.DEBUG;
    /**
     * 调试 - 获取
     */
    private static final boolean DEBUG_GET = false;

    /**
     * 调试 - 推送
     */
    private static final boolean DEBUG_POST = false;

    /**
     * 目标
     */
    private static final String  TAG       = "HttpRequest";

    /**
     * 错误
     */
    private static String errorMsg = null;

    /**
     * 获取错误消息
     *
     * @return : 错误码
     */
    static String getErrorMessage() {
        if (errorMsg == null)
            return "";
        return errorMsg;
    }
    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url   发送请求的URL
     * @param param 请求参数，请求参数应该是 name1=value1 & name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    static synchronized String sendGet(String url, String param) {
        StringBuilder  result        = new StringBuilder();
        BufferedReader in            = null;
        String         urlNameString = url;

        errorMsg = null;

        try {
            if (param != null) {
                urlNameString += "?" + param;
            }

            URL realUrl = new URL(urlNameString);

            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();

            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");

            // 建立实际的连接
            connection.connect();

            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();

            if (DEBUG_GET) {
                // 遍历所有的响应头字段
                for (String key : map.keySet()) {
                    System.out.printf("%18s --> %s\n", key, map.get(key));
                }
            }

            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (ConnectException e) {
            if (DEBUG) {
                LoggerActivity.log_e(TAG, "连接失败!");
            }
            errorMsg = e.getLocalizedMessage();
        } catch (IOException e) {
            if (DEBUG) {
                LoggerActivity.log_e(TAG, "错误1:" + e.getMessage());
            }
            errorMsg = e.getLocalizedMessage();
        } catch (Exception e) {
            e.printStackTrace();
            if (DEBUG) {
                LoggerActivity.log_e(TAG, "错误2:" + e.getMessage());
            }
            errorMsg = e.getLocalizedMessage();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                if (DEBUG) {
                    e2.printStackTrace();
                }
                if (DEBUG) {
                    LoggerActivity.log_e(TAG, "错误3:" + e2.getMessage());
                }
                errorMsg = e2.getLocalizedMessage();
            }
        }

        return result.toString();
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url   发送请求的 URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    static synchronized String sendPost(String url, String param) {
        PrintWriter    out    = null;
        BufferedReader in     = null;
        StringBuilder  result = new StringBuilder();

        errorMsg = null;

        if (DEBUG) {
            System.out.println(param);
        }

        try {
            URL realUrl = new URL(url);

            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();

            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");

            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);

            OutputStream os = conn.getOutputStream();
            //1.获取URLConnection对象对应的输出流
            if (os == null) {
                return null;
            }

            out = new PrintWriter(os);

            //2.中文有乱码的需要将PrintWriter改为如下
            //out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");

            // 发送请求参数
            //out.append(param);
            out.print(param);

            // flush输出流的缓冲
            out.flush();

            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;

            while ((line = in.readLine()) != null) {
                result.append(line);
            }

        } catch (Exception e) {
            if (DEBUG) {
                System.err.println("POST - 请求异常！\n");
            }
            e.printStackTrace();
            if (DEBUG) {
                LoggerActivity.log_e(TAG, "错误4:" + e.getMessage());
            }
            errorMsg = e.getLocalizedMessage();
            result = null;
        }

        //使用finally块来关闭输出流、输入流

        finally {
            try {
                // 关闭输出
                if (out != null) {
                    out.close();
                }
                //  关闭输入
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                if (DEBUG) {
                    LoggerActivity.log_e(TAG, "错误5:" + ex.getMessage());
                }
                result = null;
                errorMsg = ex.getLocalizedMessage();
            }
        }

        if (DEBUG) {
            if (result != null) {
                System.out.println("post 结果：" + result);
            }
        }

        if (result != null) {
            return result.toString();
        }
        return null;
    }
}
