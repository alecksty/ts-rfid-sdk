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

import com.tanso.rfid.user.Global;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 用户数据
 * <p>
 * JSON:{
 * List:{
 * {title:"xxxxxxxx",value:"xxxxxxx},
 * {title:"xxxxxxxx",value:"xxxxxxx},
 * {title:"xxxxxxxx",value:"xxxxxxx},
 * {title:"xxxxxxxx",value:"xxxxxxx},
 * },
 * }
 */
public class UserData {

    private static final boolean DEBUG = Global.DEBUG;

    public ArrayList<UserItem> list = new ArrayList<>();

    public UserData() {
        list.clear();

//        if (DEBUG) {
//            list.add(new UserItem("姓名", "李德胜"));
//            list.add(new UserItem("性别", "女"));
//            list.add(new UserItem("年龄", "18"));
//            list.add(new UserItem("籍贯", "火星"));
//            list.add(new UserItem("地域", "未知"));
//        }

        System.err.println(this);
    }

    /**
     * 输出格式
     *
     * @return : json
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            JSONArray jarray = new JSONArray();
            for (UserItem e : list) {
                jarray.put(e.toJson());
            }
            json.put("list", jarray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * 输入格式
     *
     * @param json : 对象
     */
    public void fromJson(JSONObject json) {
        JSONObject jroot;

        try {
            jroot = json.getJSONObject("list");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        Iterator<String> it = jroot.keys();

        list.clear();

        while (it.hasNext()) {
            String key = it.next();
            String value;

            try {
                value = jroot.getString(key);

                System.out.println("key: " + key + ",value:" + value);

                list.add(new UserItem(key, value));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("json:{");
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).toString());
            sb.append(",");
        }
        sb.append("}");
        return sb.toString();
    }

    public void test() {
        String s = "{\"list\":{" +
                "assetNumber:\"zc201900001\"," +
                "machineCode:\"JQ039390434\"," +
                "category:\"计算机设备\"," +
                "titile:\"IBM服务器 k839929\"," +
                "specs:\"k-3993-001\"," +
                "stockDate:\"2019-09-09\"," +
                "repairPhone:\"0753-88888888\"," +
                "instructions:\"相关说明\"," +
                "amount:\"8888.00\"," +
                "location:\"办公室A区\"," +
                "department:\"财务部\"," +
                "userName:\"李小明\"," +
                "personInCharge:\"张科\"," +
                "remarks:\"备注信息\"" +
                "}}";
    }
}
