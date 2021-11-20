package com.example.yddc_2.navigation.find;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.yddc_2.bean.DaySentence;
import com.example.yddc_2.myinterface.APIService;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SecondViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    MutableLiveData<DaySentence> mds;
    public MutableLiveData<DaySentence> getMds(Context context)
    {
        mds = new MutableLiveData<>();
        getDaySentence(context);
        return mds;
    }
    //获取每日一句
    private void getDaySentence(Context context)
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.tianapi.com")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Observable<DaySentence> observable = retrofit
                .create(APIService.class)
                .getDaySentence("506799470fbdf81a728b2f19905545f3");
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<DaySentence>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(context, "onError", Toast.LENGTH_SHORT).show();
                        Log.d("FirstViewModel", "e:" + e);
                    }

                    @Override
                    public void onNext(DaySentence daySentence) {
                        if (daySentence.getCode()!=200)
                        {
                            Toast.makeText(context, daySentence.getCode().toString(), Toast.LENGTH_SHORT).show();
                        }
                        else {
                            mds.setValue(daySentence);
                        }

                    }
                });
    }
}