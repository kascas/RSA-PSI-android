package com.example.psi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.psi.client.Client;
import com.example.psi.util.Network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //private Button btn_getKey;
    //private ImageView img_getKey;
    private ProgressDialog progressDialog;
    private String phoneNum;
    private Button btn_compute;
    private ListView listView;
    private TextView text_user;
    private SearchView searchView;
    private List<String> numList;
    private List<PhoneNumData> dataList;
    private List<BigInteger> numIntList;
    private Button btn_logout;
    public static String rootDir;
    private Client client;
    private Socket socket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_main);
        // 检查读取联系人权限
        checkForPermission();
        // 活动初始化，包括控件的指定和设置监听等
        activityInit();
        // c/s 初始化
        netInit();
        // 检查数据的更新
        //getUpdate();
    }

    private void login() {
        client = new Client("10.0.2.2", 12345, rootDir);
        Log.d("主界面-login", "login开始");
        client.login(phoneNum);
        Log.d("主界面-login", "login结束");
    }

    private void baseAndsetup() {
        if (client.mode) {
            Log.d("主界面-base", "base阶段开始");
            client.base();
            Log.d("主界面-base", "base阶段结束");
            client.sendMsg();
            Log.d("主界面-setup", "setup阶段开始");
            client.setup();
            Log.d("主界面-setup", "setup阶段结束");
        }
    }

    private void online() {
        Log.d("主界面-online", "online阶段开始");
        client.online();
        Log.d("主界面-setup", "online阶段结束");
    }

    private void netInit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                login();
                baseAndsetup();
                client.setBloom(rootDir + "/bloom");
            }
        }).start();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // 开始计算
            case (R.id.button_compute): {
                // 开始计算
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        online();
                    }
                }).start();
                break;
            }
            // 退出登录
            case (R.id.button_logout): {
                // 写入登录的手机号为空
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("phoneNum", "");
                editor.apply();
                // 跳转到登录界面
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                this.finish();
                break;
            }
            default:
                break;
        }
    }


    private void activityInit() {
        Log.d("主界面-activityInit", "初始化控件");
        // 控件的指定
        //btn_getKey = findViewById(R.id.button_getKey);
        btn_compute = findViewById(R.id.button_compute);
        //img_getKey = findViewById(R.id.image_getKey);
        //btn_getKey.setOnClickListener(this);
        listView = findViewById(R.id.listView);
        searchView = findViewById(R.id.searchView);
        text_user = findViewById(R.id.textView_user);
        btn_compute.setOnClickListener(this);
        btn_logout = findViewById(R.id.button_logout);
        btn_logout.setOnClickListener(this);
        // 从登录界面获取登录的手机号
        Intent intent = getIntent();
        phoneNum = intent.getStringExtra("phoneNum");
        text_user.setText(phoneNum);
        // 获取应用存储文件的文件夹
        rootDir = this.getFilesDir().toString();
        // 数据初始化，包括联系人的读入、密钥的检查和生成
        dataInit();
        // ListView的适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, numList);
        listView.setAdapter(adapter);
        listView.setTextFilterEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s)) {
                    listView.setFilterText(s);
                } else {
                    listView.clearTextFilter();
                }
                return false;
            }
        });
        Log.d("主界面-activityInit", "初始化完毕");
    }


    /**
     * 检查权限
     */

    private void checkForPermission() {
        //判断是否有权限
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 201);
            Log.d("主界面-check", "正在获取权限");
        } else {
            Log.d("主界面-check", "已拥有权限");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 201) {
            Log.d("主界面-RequestPermissions", "获取权限成功");
        } else {
            Log.d("主界面-RequestPermissions", "获取权限失败");
            Toast.makeText(MainActivity.this, "请重新获取权限", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 数据初始化，显示所有联系人，并初始化rKey
     */
    private void dataInit() {
        setProgressDialog("", "数据初始化");
        dataList = new PhoneUtil(this).getPhone();
        numList = new ArrayList<>();
        numIntList = new ArrayList<>();
        // 将所有联系人写入phonenum.txt
        String num = null;
        dataWriter("y.txt", "", Context.MODE_PRIVATE);
        for (PhoneNumData data : dataList) {
            num = data.getNum().replace(" ", "").substring(3);
            numList.add(num);
            dataWriter("y.txt", num + "\n", Context.MODE_APPEND);
        }
        Log.d("主界面-dataInit", "写入联系人信息");
        deleteProgressDialog();
    }


    /**
     * 生成进度对话框
     *
     * @param title 对话框标题
     * @param msg   对话框内容
     */
    private void setProgressDialog(String title, String msg) {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle(title);
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    /**
     * 销毁进度对话框
     */
    private void deleteProgressDialog() {
        progressDialog.dismiss();
        progressDialog = null;
    }

    /**
     * 从服务器端实现数据更新
     */
    // TODO: 数据更新
    private void getUpdate() {
        Log.d("主界面-getUpdate", "检查更新");
        setProgressDialog("", "正在检查服务器端数据更新");
        boolean checkResult = serverDataCheck();
        deleteProgressDialog();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("数据更新");
        alertDialog.setCancelable(false);
        if (checkResult) {
            alertDialog.setMessage("服务器端数据有更改，请点击更新按钮进行数据更新");
            alertDialog.setPositiveButton("更新", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(MainActivity.this, "数据更新已完成", Toast.LENGTH_SHORT).show();
                }
            });
            alertDialog.show();
            setProgressDialog("", "正在更新数据");
            serverDataUpdate();
            deleteProgressDialog();
            Log.d("主界面-getUpdate", "需要更新");
        } else {
            alertDialog.setMessage("服务器端数据没有更改，无需数据更新");
            alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            alertDialog.show();
            Log.d("主界面-getUpdate", "无需更新");
        }
        Log.d("主界面-getUpdate", "检查完毕");
    }

    /**
     * 检查数据更新
     *
     * @return 是否需要更新
     */
    // TODO: 检查数据更新（服务器端可以用日期名作为bloom文件的命名）
    private boolean serverDataCheck() {
        return true;
    }

    /**
     * 获取数据更新
     */
    // TODO: 获取数据更新
    private void serverDataUpdate() {

    }


    /**
     * 文件的写入openFileOutput
     *
     * @param filename 文件名称
     * @param data     数据
     * @param mode     模式（追加/覆盖）
     */
    private void dataWriter(String filename, String data, int mode) {
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            if (mode == Context.MODE_APPEND) {
                out = openFileOutput(filename, Context.MODE_APPEND);
            } else if (mode == Context.MODE_PRIVATE) {
                out = openFileOutput(filename, Context.MODE_PRIVATE);
            }
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 文件的读取openFileInput
     *
     * @param filename 文件名称
     * @return String类型的数据内容
     */
    private Pair<String, Integer> dataReader(String filename) {
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        String line = null;
        int lineNum = 0;
        try {
            in = openFileInput(filename);
            reader = new BufferedReader(new InputStreamReader(in));
            while ((line = reader.readLine()) != null) {
                content.append(line);
                lineNum++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Pair<>(content.toString(), lineNum);
    }


}
