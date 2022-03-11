package com.example.yddc_2.navigation.word;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.yddc_2.bean.DaySentence;
import com.example.yddc_2.bean.ReciteRecord;
import com.example.yddc_2.bean.Setting;
import com.example.yddc_2.bean.WordList;
import com.example.yddc_2.myinterface.APIService;
import com.example.yddc_2.utils.GetNetService;
import com.example.yddc_2.utils.SecuritySP;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.security.GeneralSecurityException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class FirstViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    MutableLiveData<Setting> mSetting;
    public MutableLiveData<Setting> getmSetting(Context context) throws GeneralSecurityException, IOException {
        mSetting = new MutableLiveData<>();
        getSetting(context);
        return mSetting;
    };
    //获取setting
    private void getSetting(Context context) throws GeneralSecurityException, IOException {
        Observable<Setting> observable = GetNetService.GetApiService().getSetting(SecuritySP.DecryptSP(context,"token"));
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Setting>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(context, "getSetting: onError", Toast.LENGTH_SHORT).show();
                        Log.d("MainViewModel", "e:" + e);
                    }

                    @Override
                    public void onNext(Setting setting) {
                        if(setting.getState()!=200)
                        {
                            Toast.makeText(context, setting.getState().toString(), Toast.LENGTH_SHORT).show();
                        }
                        else mSetting.setValue(setting);
                    }
                });
    }

    //获取背诵数据
    MutableLiveData<ReciteRecord> mReciteData;
    public MutableLiveData<ReciteRecord> getmReciteData(Context context) throws GeneralSecurityException, IOException {
        mReciteData = new MutableLiveData<>();
        getReciteData(context);
        return mReciteData;
    }
    private void getReciteData(Context context) throws GeneralSecurityException, IOException {
        GetNetService.GetApiService().getRecord(SecuritySP.DecryptSP(context,"token"))
                .enqueue(new Callback<ReciteRecord>() {
                    @Override
                    public void onResponse(@NonNull Call<ReciteRecord> call, Response<ReciteRecord> response) {
                        String json = new Gson().toJson(response.body());
                        Log.d("FirstViewModel", json);
                        ReciteRecord reciteRecord = new Gson().fromJson(json,ReciteRecord.class);
                        mReciteData.setValue(reciteRecord);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ReciteRecord> call, Throwable t) {
                        Toast.makeText(context, "t:" + t, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
