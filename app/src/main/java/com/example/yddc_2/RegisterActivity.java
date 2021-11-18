package com.example.yddc_2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yddc_2.databinding.ActivityRegisterBinding;
import com.example.yddc_2.myinterface.APIService;
import com.example.yddc_2.utils.HideBar;
import com.example.yddc_2.utils.SecuritySP;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RegisterActivity extends AppCompatActivity{
    private ActivityRegisterBinding rBinding;
    private static String cookie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_register);
        rBinding = DataBindingUtil.setContentView(this,R.layout.activity_register);
        HideBar.hideBar(this);
        InitCode();
        rBinding.ivCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InitCode();
            }
        });
    }

    private static APIService GetApiService(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://youdian.asedrfa.top")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        APIService apiService = retrofit.create(APIService.class);
        return apiService;
    }
    private void InitCode(){
//        //请求注册验证码
//        Observable<ResponseBody> observable = GetApiService().GetCodeImage();
//        observable.subscribeOn(Schedulers.io())//在子线程中进行http访问
//                .observeOn(AndroidSchedulers.mainThread())//UI线程处理返回接口
//                .subscribe(new Observer<ResponseBody>() {//订阅
//                    @Override
//                    public void onCompleted() {
//                        /*事件队列完结。RxJava 不仅把每个事件单独处理，还会把它们看做一个队列。
//                         *RxJava 规定，当不会再有新的 onNext() 发出时，需要触发 onCompleted() 方法作为标志
//                         */
//                        //Toast.makeText(LoginActivity.this, "ok", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        /*事件队列异常。在事件处理过程中出异常时，onError() 会被触发，同时队列自动终止，不允许再有事件发出。
//                         */
//                        Log.d("LoginActivity", "e:" + e);
//                        Toast.makeText(RegisterActivity.this, "rxjava:error", Toast.LENGTH_SHORT).show();
//                    }
//                    @Override
//                    public void onNext(ResponseBody responseBody) {//RxJava的事件回调方法，针对普通事件。
//                        Bitmap bitmap = BitmapFactory.decodeStream(responseBody.byteStream());
//                        //获取布局
//                        ImageView iv = rBinding.ivCode;
//                        iv.setImageBitmap(bitmap);
//
//                        Register();//注册
//                        //Toast.makeText(LoginActivity.this, "responseBody:" + responseBody, Toast.LENGTH_SHORT).show();
//                    }
//                });
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://youdian.asedrfa.top/getCodeImage")
                .get()
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RegisterActivity.this, "OKHttp:error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ResponseBody responseBody = response.body();
                //不像RXjava，所有必须runOnUIThread了，无奈
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = BitmapFactory.decodeStream(responseBody.byteStream());
                        //获取布局
                        ImageView iv = rBinding.ivCode;
                        iv.setImageBitmap(bitmap);
                        cookie = response.header("Set-Cookie");
                        //Toast.makeText(RegisterActivity.this, cookie, Toast.LENGTH_SHORT).show();
                        Register();
                    }
                });
            }
        });
    }
    private void Register(){
        rBinding.tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ph = rBinding.et1.getText().toString();
                String pwd = rBinding.et2.getText().toString();
                String code = rBinding.et3.getText().toString();
                if(ph.length()!=11||pwd.length()<6)
                {
                    new AlertDialog.Builder(RegisterActivity.this)//基于activity，所以用getApplicationContext()无效
                            .setTitle("提示")
                            .setMessage("手机号或密码格式错误")
                            .setPositiveButton("确定", (dialog, which) -> {
                            })
                            .create()
                            .show();
                }
                else
                {
                    //注册
//                    Observable<ResponseBody> observable = GetApiService().register(code,pwd,ph);
//                    observable.subscribeOn(Schedulers.io())//在子线程中进行http访问
//                            .observeOn(AndroidSchedulers.mainThread())//UI线程处理返回接口
//                            .subscribe(new Observer<ResponseBody>() {
//                                @Override
//                                public void onCompleted() {
//
//                                }
//
//                                @Override
//                                public void onError(Throwable e) {
//                                    Toast.makeText(RegisterActivity.this, "onError", Toast.LENGTH_SHORT).show();
//                                }
//
//                                @Override
//                                public void onNext(ResponseBody responseBody) {
//                                    try {
//                                        int state = JsonParser.parseString(responseBody.string()).getAsJsonObject().get("state").getAsInt();
//                                        if(state!=200){
//                                            new AlertDialog.Builder(RegisterActivity.this)//基于activity，所以用getApplicationContext()无效
//                                                    .setTitle("提示")
//                                                    .setMessage("注册失败")
//                                                    .setPositiveButton("确定", (dialog, which) -> {
//                                                    })
//                                                    .create()
//                                                    .show();
//                                        }
//                                        else{
//                                            new AlertDialog.Builder(RegisterActivity.this)//基于activity，所以用getApplicationContext()无效
//                                                    .setTitle("提示")
//                                                    .setMessage("注册成功")
//                                                    .setPositiveButton("直接登录", new DialogInterface.OnClickListener() {
//                                                        @Override
//                                                        public void onClick(DialogInterface dialog, int which) {
//                                                            LoginByPwd(ph,pwd);
//                                                        }
//                                                    })
//                                                    .create()
//                                                    .show();
//                                        }
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            });
                    OkHttpClient client = new OkHttpClient();
                    FormBody formBody = new FormBody.Builder()
                            .add("code",code)
                            .add("pwd",pwd)
                            .add("tel",ph)
                            .build();
                    Request request = new Request.Builder()
                            .url("http://youdian.asedrfa.top/user/regist")
                            .post(formBody)
                            .addHeader("Cookie",cookie)
                            .build();
                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RegisterActivity.this, "fail", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            ResponseBody responseBody = response.body();
                            //不像RXjava，所有必须runOnUIThread了，无奈
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    try {
//                                        int state = JsonParser.parseString(responseBody.string()).getAsJsonObject().get("state").getAsInt();
//                                        Toast.makeText(RegisterActivity.this, String.valueOf(state), Toast.LENGTH_SHORT).show();
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
                                       new AlertDialog.Builder(RegisterActivity.this)//基于activity，所以用getApplicationContext()无效
                                                    .setTitle("提示")
                                                    .setMessage("注册成功")
                                                    .setPositiveButton("直接登录", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            LoginByPwd(ph,pwd);
                                                        }
                                                    })
                                                    .create()
                                                    .show();

                                }
                            });
                        }
                    });


                }
            }
        });
    }
    private void LoginByPwd(String ph,String pwd){
        Observable<ResponseBody> observable = GetApiService().load("1",pwd,ph);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            JsonObject jsonObject = JsonParser.parseString(responseBody.string()).getAsJsonObject();
                            int state = jsonObject.get("state").getAsInt();
                            if(state!=200)
                            {
                                new AlertDialog.Builder(RegisterActivity.this)//基于activity，所以用getApplicationContext()无效
                                        .setTitle("提示")
                                        .setMessage("密码错误或未注册")
                                        .setPositiveButton("返回", (dialog, which) -> startActivity(new Intent(RegisterActivity.this,LoginActivity.class)))
                                        .create()
                                        .show();
                                //收起软键盘
                                InputMethodManager manager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (manager != null) manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                            }
                            else {
                                //保存或更新token
                                String token = jsonObject.getAsJsonObject("data").get("token").getAsString();
                                //保存或更新token、账号、密码
                                SecuritySP.EncryptSP(getApplicationContext(),"token",token);
                                SecuritySP.EncryptSP(getApplicationContext(),"ph",ph);
                                SecuritySP.EncryptSP(getApplicationContext(),"pwd",pwd);
                                startActivity(new Intent(RegisterActivity.this,MainActivity.class));
                            }
                        } catch (IOException | GeneralSecurityException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}