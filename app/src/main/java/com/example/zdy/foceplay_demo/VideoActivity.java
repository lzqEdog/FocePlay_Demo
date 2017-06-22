package com.example.zdy.foceplay_demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.zdy.foceplay_demo.permission.MPermission;
import com.example.zdy.foceplay_demo.permission.annotation.OnMPermissionDenied;
import com.example.zdy.foceplay_demo.permission.annotation.OnMPermissionGranted;

public class VideoActivity extends Activity {
    private final int BASIC_PERMISSION_REQUEST_CODE = 100;
    private EdogPlayer edogPlayer;
    private  String url = "storage/emulated/0/Movies/SHD.mp4";;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setScreenArrts();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.foce_play_activity);
        requestPermiss();

    }
    /**
     * 设置屏幕属性
     */
    private void setScreenArrts() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                getWindow().getDecorView().setSystemUiVisibility(5895);
                getWindow().getDecorView().requestFocus();
            }
        });
    }

    private void requestPermiss() {
        MPermission.with(this)
                .addRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ).request();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (have) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(getApplicationContext())) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                } else {
                    //有了权限，具体的动作
                    if (edogPlayer !=null){
                        edogPlayer.canSetLight = true;
                        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        addContentView(edogPlayer,rl);
                    }
                }
            }
        }

    }
    private boolean have =false;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {

        permissionSuccess();

    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
        Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
    }

    private void permissionSuccess() {
        edogPlayer = new EdogPlayer(this);
        edogPlayer.setVideoPath(url);
        have = true;
        edogPlayer.setOnCloseOrJumpListener(new EdogPlayer.onCloseOrJumpListener() {
            @Override
            public void onClose() {
                finish();

            }

            @Override
            public void onJump() {
//                jumpToWhere();
                finish();
            }
        });
    }
    @Override
    public void finish() {
        super.finish();
        //回收内存
        edogPlayer.destroyAll();
    }

}
