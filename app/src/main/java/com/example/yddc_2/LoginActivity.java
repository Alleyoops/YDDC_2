package com.example.yddc_2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.viewbinding.BuildConfig;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.yddc_2.databinding.ActivityLoginBinding;
import com.example.yddc_2.myinterface.APIService;
import com.example.yddc_2.utils.HideBar;
import com.example.yddc_2.utils.MyApplication;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityLoginBinding mBinding;
    private int LoginWay=0;//0表示验证码登录，1表示密码登录
    private boolean change=false;//用于TextView8的标识
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_login);有下行的代码，就可以省略这行代码，下行代码已经达到连接xml和activity的作用了
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_login);
        initView();
    }
    private void initView(){
        HideBar.hideBar(this);
        mBinding.textView9.setVisibility(View.INVISIBLE);
        mBinding.et2.setVisibility(View.GONE);
        mBinding.textView7.setOnClickListener(this);
        mBinding.textView8.setOnClickListener(this);
        mBinding.textView11.setOnClickListener(this);
        ChangeLoginKeyBack();
    }

    @Override
    public void onClick(View v) {
        if (v==mBinding.textView8)
        {
            if(!change)
            {   //进入密码登录页面
                LoginWay=1;
                mBinding.et2.setVisibility(View.VISIBLE);
                mBinding.textView6.setVisibility(View.GONE);
                mBinding.et2.setTransformationMethod(PasswordTransformationMethod.getInstance());
                mBinding.textView7.setText("登录");
                mBinding.textView8.setText("验证码登录");
                change=true;//
            }else {
                //返回初始登录页面
                LoginWay=0;
                mBinding.et2.setVisibility(View.GONE);
                mBinding.textView8.setText("密码登录");
                mBinding.textView6.setVisibility(View.VISIBLE);
                mBinding.textView7.setText("获取短信验证码");
                change=false;
            }
            ChangeLoginKeyBack();

        }
        if(v==mBinding.textView7)
        {//点击登录按钮
            switch (LoginWay)
            {
                case 0:
                    if(mBinding.et1.getText().toString().length()==11&&mBinding.et1.getText().toString().charAt(0)=='1')
                        Toast.makeText(this, "还不支持短信验证码登录", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(this, "请输入正确手机号", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    if(mBinding.et1.getText().toString().length()==11&&mBinding.et1.getText().toString().charAt(0)=='1'&&
                    mBinding.et2.getText().toString().length()>=6)//登录
                        LoginByPwd(mBinding.et1.getText().toString(),mBinding.et2.getText().toString());
                    else Toast.makeText(this, "手机号格式错误或密码位数小于6位", Toast.LENGTH_SHORT).show();
                    break;
                default:break;
            }
        }
        if(v==mBinding.textView11)
        {//点击注册按钮
            pupWindowForRegister();
        }

    }
    private void ChangeLoginKeyBack(){
        EditText ph = mBinding.et1;
       ph.addTextChangedListener(new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) {
               if(ph.getText().toString().length()==11)
               {
                   mBinding.textView7.setBackgroundResource(R.drawable.back_select_1);
               }else mBinding.textView7.setBackgroundResource(R.drawable.shape_6);
           }
           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {
               if(ph.getText().toString().length()==11)
               {
                   mBinding.textView7.setBackgroundResource(R.drawable.back_select_1);
               }else mBinding.textView7.setBackgroundResource(R.drawable.shape_6);

           }
           @Override
           public void afterTextChanged(Editable s) {
               if(ph.getText().toString().length()==11)
               {
                   mBinding.textView7.setBackgroundResource(R.drawable.back_select_1);
               }else mBinding.textView7.setBackgroundResource(R.drawable.shape_6);
           }
       });
    }
    private void LoginByPwd(String ph,String pwd){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://youdian.asedrfa.top")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        APIService apiService = retrofit.create(APIService.class);
        Observable<ResponseBody> observable = apiService.load(ph,pwd,"1");
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
                        /*
                         * 这里有个坑：
                         * 当我们第一次调用 response.body().string() 时，OkHttp 将响应体的缓冲资源返回的同时，调用 closeQuietly() 方法默默释放了资源。
                         * 响应主体 ResponseBody 持有的资源可能会很大，所以 OkHttp 并不会将其直接保存到内存中，只是持有数据流连接。只有当我们需要时，才会从服务器获取数据并返回。
                         * 同时，考虑到应用重复读取数据的可能性很小，所以将其设计为一次性流(one-shot)，读取后即 '关闭并释放资源'。
                         */
                        try {
                            int state = JsonParser.parseString(responseBody.string()).getAsJsonObject().get("state").getAsInt();
                            if(state!=200)
                            {
                                new AlertDialog.Builder(LoginActivity.this)//基于activity，所以用getApplicationContext()无效
                                        .setTitle("提示")
                                        .setMessage("密码错误或未注册")
                                        .setPositiveButton("重试", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .setNegativeButton("注册", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                pupWindowForRegister();
                                            }
                                        })
                                        .create()
                                        .show();
                                //收起软键盘
                                InputMethodManager manager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (manager != null) manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                            }
                            else {
                                Toast.makeText(LoginActivity.this, "suc", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }
    @SuppressLint("UseCompatLoadingForDrawables")
    private void pupWindowForRegister(){
        //初始化popUpWindow
        //设置contentView
        @SuppressLint("InflateParams")
        View v = LayoutInflater.from(LoginActivity.this).inflate(R.layout.register_window,null);
        PopupWindow popupWindow =new PopupWindow(v, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);//获得焦点
        popupWindow.setTouchable(true);//可触摸
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.color.bar));
        popupWindow.showAsDropDown(mBinding.cl,0,0,Gravity.BOTTOM);

        //请求注册验证码
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://youdian.asedrfa.top")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        APIService apiService = retrofit.create(APIService.class);
        Observable<ResponseBody> observable = apiService.GetCodeImage();
        observable.subscribeOn(Schedulers.io())//在子线程中进行http访问
                .observeOn(AndroidSchedulers.mainThread())//UI线程处理返回接口
                .subscribe(new Observer<ResponseBody>() {//订阅
                    @Override
                    public void onCompleted() {
                        /*事件队列完结。RxJava 不仅把每个事件单独处理，还会把它们看做一个队列。
                         *RxJava 规定，当不会再有新的 onNext() 发出时，需要触发 onCompleted() 方法作为标志
                         */
                        //Toast.makeText(LoginActivity.this, "ok", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        /*事件队列异常。在事件处理过程中出异常时，onError() 会被触发，同时队列自动终止，不允许再有事件发出。
                         */
                        Log.d("LoginActivity", "e:" + e);
                        Toast.makeText(LoginActivity.this, "rxjava:error", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onNext(ResponseBody responseBody) {//RxJava的事件回调方法，针对普通事件。
                        Bitmap bitmap = BitmapFactory.decodeStream(responseBody.byteStream());
                        //获取布局
                        @SuppressLint("InflateParams")
                        //ImageView iv = (ImageView) LayoutInflater.from(LoginActivity.this).inflate(R.layout.register_window,null).findViewById(R.id.iv_code);
                        ImageView iv = (ImageView)popupWindow.getContentView().findViewById(R.id.iv_code);//上面一行的代码不能更新UI
                        iv.setImageBitmap(bitmap);
                        //Toast.makeText(LoginActivity.this, "responseBody:" + responseBody, Toast.LENGTH_SHORT).show();
                    }
                });
    }

}