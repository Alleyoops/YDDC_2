package com.example.yddc_2.viewmodels;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.yddc_2.bean.Setting;
import com.example.yddc_2.bean.WordList;
import com.example.yddc_2.myinterface.APIService;
import com.example.yddc_2.utils.GetNetService;
import com.example.yddc_2.utils.SecuritySP;

import java.io.IOException;
import java.security.GeneralSecurityException;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainViewModel extends ViewModel {
    MutableLiveData<WordList> mWordList;
    public MutableLiveData<WordList> getmWordList(Context context) throws GeneralSecurityException, IOException {
        mWordList = new MutableLiveData<>();
        getWordList(context);
        return mWordList;
    }
    //获取背单词的列表
    private void getWordList(Context context) throws GeneralSecurityException, IOException {
        Observable<WordList> observable = GetNetService.GetApiService().getWordList(SecuritySP.DecryptSP(context,"token"));
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<WordList>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(context, "getWordList: onError", Toast.LENGTH_SHORT).show();
                        //Log.d("MainViewModel", "e:" + e);
                    }

                    @Override
                    public void onNext(WordList wordList) {
                        if(wordList.getState()!=200)
                        {
                            Toast.makeText(context, "getWordList->"+"wordList.getState():" + wordList.getState().toString(), Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Log.d("MainViewModel", "获取一次单词列表");
                            mWordList.setValue(wordList);
                        }
                    }
                });
    }

//    MutableLiveData<WordList> mMyWordList;
//    public MutableLiveData<WordList> getmMyWordList(Context context,int[] tags) throws GeneralSecurityException, IOException {
//        mMyWordList = new MutableLiveData<>();
//        //getMyWordList(context,tags);
//        return mMyWordList;
//    }
//    //获取用户单词列表
//    private void getMyWordList(Context context,int tag) throws GeneralSecurityException, IOException {
//        Observable<WordList> observable = GetNetService.GetApiService()
//                .getMyWordList(SecuritySP.DecryptSP(context,"token"),tag);
//        observable.subscribeOn(Schedulers.io())
//                .observeOn()
//                .subscribe(new Observer<WordList>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Toast.makeText(context, "getMyWordList: onError", Toast.LENGTH_SHORT).show();
//                        Log.d("MainActivity", "e:" + e);
//                    }
//
//                    @Override
//                    public void onNext(WordList wordList) {
//                        if(wordList.getState()!=200)
//                        {
//                            Toast.makeText(context, "getMyWordList->"+"wordList.getState():" + wordList.getState().toString(), Toast.LENGTH_SHORT).show();
//                        }
//                        else mMyWordList.setValue(wordList);
//                    }
//                });
//    }



}
