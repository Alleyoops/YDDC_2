package com.example.yddc_2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.yddc_2.myinterface.APIService;
import com.example.yddc_2.utils.HideBar;
import com.example.yddc_2.utils.SecuritySP;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.security.GeneralSecurityException;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屛显示，再setContentView
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        setStatusBarTranslucent(WelcomeActivity.this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    checkLogin();//自动登录
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
            }
        },500);
    }


    public void setStatusBarTranslucent(Activity activity) {
        Window window = activity.getWindow();
        window.setNavigationBarColor(Color.BLACK);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        View decorView = window.getDecorView();
        int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(option);
        //透明着色
        window.setStatusBarColor(Color.TRANSPARENT);
    }

        //申请权限
//    private void myRequestPermission() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
//            //finish();
//        } else if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
//            //finish();
//        }else if(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
//            //finish();
//        }
//        Toast.makeText(this,"权限已申请", Toast.LENGTH_SHORT).show();
//    }
    private void checkLogin() throws GeneralSecurityException, IOException {
        //检查是否保存有token来判断是否自动登录(其实并没有登录，只是检验有没有token)
        String token = SecuritySP.DecryptSP(getApplicationContext(),"token");
        if(!token.equals(""))  startActivity(new Intent(WelcomeActivity.this,MainActivity.class));//如果有token，说明已经登录过，则直接跳转主页
        else startActivity(new Intent(WelcomeActivity.this,LoginActivity.class));
    }

}