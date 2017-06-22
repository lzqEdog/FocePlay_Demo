package com.example.zdy.foceplay_demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.zdy.foceplay_demo.permission.MPermission;
import com.example.zdy.foceplay_demo.permission.annotation.OnMPermissionDenied;
import com.example.zdy.foceplay_demo.permission.annotation.OnMPermissionGranted;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ZDY on 2017/6/8.
 */

public class FirstActivity extends Activity {
    private final int BASIC_PERMISSION_REQUEST_CODE = 100;
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            bt.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(FirstActivity.this,VideoActivity.class));
                }
            });
        }
    };
    private Button bt;
    private ContentLoadingProgressBar progressBar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jumpac);
        bt = (Button) findViewById(R.id.button_j);
        progressBar = (ContentLoadingProgressBar) findViewById(R.id.empty_progress);
        requestPermiss();
    }

    private boolean canCopy = false;

    private void readAndWrite() {
        ;
        Test();
        if (canCopy) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    copy();
                }
            }).start();

        }
    }
    String filename1;
    private void copy() {
        InputStream input = null;
        try {
            String DATABASE_NAME="SHD.mp4";
            input = getAssets().open(DATABASE_NAME);
            if (input != null) {
                String DATABASE_PATH = Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + "/Movies";

                // 首先判断该目录下的文件夹是否存在
                File dir = new File(DATABASE_PATH);
                filename1 = DATABASE_PATH + "/" + DATABASE_NAME;
                if (!dir.exists()) {  // 文件夹不存在 ， 则创建文件夹
                    dir.mkdirs();
                }
                // 判断目标文件是否存在
                File file1 = new File(dir, DATABASE_NAME);
                if (!file1.exists()) {
                    try {
                        file1.createNewFile(); // 创建文件
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),"正在读取本地视频",Toast.LENGTH_LONG).show();
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
//                    Log.w("!!!!!!!!!!!!!","???????");
                    mHandler.sendEmptyMessage(1);
                    return;
                }
                FileOutputStream out = new FileOutputStream(filename1); // 文件输出流、用于将文件写到SD卡中
                // -- 从内存出去
                byte[] buffer = new byte[2048];
                int len = 0;
                while ((len = (input.read(buffer))) != -1) { // 读取文件，-- 进到内存
                    out.write(buffer, 0, len); // 写入数据 ，-- 从内存出
//                    Log.w("!!!!!!!!!!!!!","qqqqqq");
                }
                input.close();
                out.close(); // 关闭流
                mHandler.sendEmptyMessage(1);
            }else {
//                Log.w("!!!!!!!!!!!!!","++++++++++");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void Test() {
        Resources resources = getResources();
        AssetManager am = resources.getAssets();
        try {
            String[] files = am.list("");
            for (String file : files) {
                if (file.equals("SHD.mp4")) {
                    canCopy = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {
        readAndWrite();


    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
        Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
    }

    private void requestPermiss() {
        MPermission.with(this)
                .addRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).request();

    }

}
