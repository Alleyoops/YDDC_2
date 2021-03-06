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
import com.example.yddc_2.utils.DeviceIdUtil;
import com.example.yddc_2.utils.HideBar;
import com.example.yddc_2.utils.SecuritySP;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Calendar;
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
        //?????????????????????
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
                //??????RXjava???????????????runOnUIThread????????????
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = BitmapFactory.decodeStream(responseBody.byteStream());
                        //????????????
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
                    new AlertDialog.Builder(RegisterActivity.this)//??????activity????????????getApplicationContext()??????
                            .setTitle("??????")
                            .setMessage("??????????????????????????????")
                            .setPositiveButton("??????", (dialog, which) -> {
                            })
                            .create()
                            .show();
                }
                else
                {
                    //??????
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
                            //??????RXjava???????????????runOnUIThread????????????
                            try {
                                int state = JsonParser.parseString(responseBody.string()).getAsJsonObject().get("state").getAsInt();
                                if(state==200)
                                {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            new AlertDialog.Builder(RegisterActivity.this)//??????activity????????????getApplicationContext()??????
                                                    .setTitle("??????")
                                                    .setMessage("????????????")
                                                    .setPositiveButton("????????????", new DialogInterface.OnClickListener() {
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
                                else if(state==402)
                                {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(RegisterActivity.this,"????????????????????????", Toast.LENGTH_SHORT).show();
                                            InitCode();
                                        }
                                    });
                                }
                                else if(state==412)
                                {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(RegisterActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                                            InitCode();
                                        }
                                    });
                                }
                                else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(RegisterActivity.this, "state:" + state, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                }
            }
        });
    }
    private void LoginByPwd(String ph,String pwd){
        //?????????????????????????????????????????????1?????????
        String devId = 1+ DeviceIdUtil.getDeviceId(this);
        Observable<ResponseBody> observable = GetApiService().load(devId,pwd,ph);
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
                                new AlertDialog.Builder(RegisterActivity.this)//??????activity????????????getApplicationContext()??????
                                        .setTitle("??????")
                                        .setMessage("????????????????????????")
                                        .setPositiveButton("??????", (dialog, which) -> startActivity(new Intent(RegisterActivity.this,LoginActivity.class)))
                                        .create()
                                        .show();
                                //???????????????
                                InputMethodManager manager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (manager != null) manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                            }
                            else {
                                //???????????????token
                                String token = jsonObject.getAsJsonObject("data").get("token").getAsString();
                                //???????????????token??????????????????
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