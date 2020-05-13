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
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tanso.rfid.R;
import com.tanso.rfid.network.UserData;
import com.tanso.rfid.network.UserItem;
import com.tanso.rfid.user.Global;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 用户数据
 */
public class UserDataActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private static final boolean DEBUG = Global.DEBUG;
    private static final String  TAG   = "UserDataActivity";

    /**
     * 结果：{
     * "list":{
     * assetNumber:"zc201900001",
     * machineCode:"JQ039390434",
     * category:"计算机设备",
     * titile:"IBM服务器 k839929",
     * specs:"k-3993-001",
     * stockDate:"2019-09-09",
     * repairPhone:"0753-88888888",
     * instructions:"相关说明",
     * amount:"8888.00",
     * location:"办公室A区",
     * department:"财务部",
     * userName:"李小明",
     * personInCharge:"张科",
     * remarks:"备注信息"
     * }
     * }
     */
    public static final String DEF_EXTRA_NAME = "NAME";
    public static final String DEF_EXTRA_JSON = "JSON";

    private String          jsonText;
    private String          nameText;
    private UserData        userData = new UserData();
    private UserDataAdapter adp;

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_user_data);

        Intent intent = this.getIntent();
        jsonText = intent.getStringExtra(UserDataActivity.DEF_EXTRA_JSON);
        nameText = intent.getStringExtra(UserDataActivity.DEF_EXTRA_NAME);

        if (jsonText != null) {
            JSONObject jsonObject;

            if (DEBUG) {
                System.err.println("收到消息:" + jsonText);
            }

            try {
                jsonObject = new JSONObject(jsonText);
                //
                userData.fromJson(jsonObject);
                //
                adp = new UserDataAdapter(userData);

                ListView listView = this.findViewById(R.id.list_data);
                listView.setAdapter(adp);
                listView.setOnItemClickListener(this);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        TextView text_name = this.findViewById(R.id.text_title);
        if (text_name != null) {
            text_name.setText(nameText);
        }

        ImageView image_return = this.findViewById(R.id.image_return);
        if (image_return != null) {
            image_return.setOnClickListener(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_return:
                this.finish();
                break;
        }
    }

    /**
     * 设备列表类
     */
    class UserDataAdapter extends BaseAdapter {

        private UserData data;

        public UserDataAdapter(UserData user) {
            data = user;
        }

        @Override
        public int getCount() {
            return data.list.size();
        }

        @Override
        public UserItem getItem(int position) {
            return data.list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater mInflater = LayoutInflater.from(UserDataActivity.this);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_user_data, null);
            }

            //  设备
            UserItem item = getItem(position);

            //  名称
            TextView text_title = convertView.findViewById(R.id.text_user_title);
            if (text_title != null) {
                text_title.setText(item.title);
            }

            //  设备描述
            TextView text_value = convertView.findViewById(R.id.text_user_value);
            if (text_value != null) {
                text_value.setText(item.value);
            }

            //  保存设备引用
            convertView.setTag(item);

            return convertView;
        }
    }
}
