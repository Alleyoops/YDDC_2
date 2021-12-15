package com.example.yddc_2.navigation.me;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.yddc_2.R;
import com.example.yddc_2.bean.User;
import com.example.yddc_2.myinterface.APIService;
import com.example.yddc_2.utils.SecuritySP;
import com.google.gson.Gson;
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

public class ThirdViewModel extends ViewModel {
    private User user;
    MutableLiveData<User> mUser;
    public MutableLiveData<User> getUser(Context context) throws GeneralSecurityException, IOException {
        mUser = new MutableLiveData<>();
        getUserDetail(SecuritySP.DecryptSP(context,"token"),context);
        return mUser;
    }


    //init APIService
    private static APIService GetApiService(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://youdian.asedrfa.top")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(APIService.class);
    }
    //获取userDetail
    private void getUserDetail(String Token,Context context)
    {
        Observable<ResponseBody> observable = GetApiService().getUserDetail(Token);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("ThirdViewModel", "e:" + e);
                        Toast.makeText(context, "e:" + e, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            JsonObject jsonObject = JsonParser.parseString(responseBody.string()).getAsJsonObject();
                            int state = jsonObject.get("state").getAsInt();
                            Gson gson = new Gson();
                            if(state != 200)
                            {
                                //给一个默认user属性
                                user = gson.fromJson(context.getResources().getString(R.string.user),User.class);
                            }
                            else
                            {
                                user = gson.fromJson(jsonObject.get("data").toString(),User.class);
                            }
                            mUser.setValue(user);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}