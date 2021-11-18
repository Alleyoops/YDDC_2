package com.example.yddc_2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {

                    checkLogin();//自动登录
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
            }
        },1000);
    }
//    //申请权限
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
//        //Toast.makeText(this,"权限已申请",Toast.LENGTH_SHORT).show();
//    }
    private void checkLogin() throws GeneralSecurityException, IOException {
        //检查是否保存有token来判断是否自动登录
        String token = SecuritySP.DecryptSP(getApplicationContext(),"token");
        if(!token.equals(""))  startActivity(new Intent(WelcomeActivity.this,MainActivity.class));//如果有token，说明已经登录过，则直接跳转主页
        else startActivity(new Intent(WelcomeActivity.this,LoginActivity.class));
    }
//    private void LoginByPwd(String ph,String pwd){
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://youdian.asedrfa.top")
//                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        Observable<ResponseBody> observable = retrofit.create(APIService.class).load("1",pwd,ph);
//        observable.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<ResponseBody>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onNext(ResponseBody responseBody) {
//                        /*
//                         * 这里有个坑：
//                         * 当我们第一次调用 response.body().string() 时，OkHttp 将响应体的缓冲资源返回的同时，调用 closeQuietly() 方法默默释放了资源。
//                         * 响应主体 ResponseBody 持有的资源可能会很大，所以 OkHttp 并不会将其直接保存到内存中，只是持有数据流连接。只有当我们需要时，才会从服务器获取数据并返回。
//                         * 同时，考虑到应用重复读取数据的可能性很小，所以将其设计为一次性流(one-shot)，读取后即 '关闭并释放资源'。
//                         */
//                        try {
//                            JsonObject jsonObject = JsonParser.parseString(responseBody.string()).getAsJsonObject();
//                            int state = jsonObject.get("state").getAsInt();
//                            if(state!=200)
//                            {
//                                new AlertDialog.Builder(WelcomeActivity.this)//基于activity，所以用getApplicationContext()无效
//                                        .setTitle("提示")
//                                        .setMessage("密码错误或未注册")
//                                        .setPositiveButton("重试", (dialog, which) -> {
//                                        })
//                                        .setNegativeButton("注册", (dialog, which) -> startActivity(new Intent(WelcomeActivity.this,RegisterActivity.class)))
//                                        .create()
//                                        .show();
//                                //收起软键盘
//                                InputMethodManager manager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//                                if (manager != null) manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//                            }
//                            else {
//                                String token = jsonObject.getAsJsonObject("data").get("token").getAsString();
//                                //保存或更新token、账号、密码
//                                SecuritySP.EncryptSP(getApplicationContext(),"token",token);
//                                SecuritySP.EncryptSP(getApplicationContext(),"ph",ph);
//                                SecuritySP.EncryptSP(getApplicationContext(),"pwd",pwd);
//                                Intent intent = new Intent();
//                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);//禁止返回上一页
//                                intent.setClass(WelcomeActivity.this, MainActivity.class);
//                                startActivity(intent);
//                            }
//                        } catch (IOException | GeneralSecurityException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//    }
}