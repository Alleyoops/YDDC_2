package com.example.yddc_2.navigation.word;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.yddc_2.bean.DaySentence;
import com.example.yddc_2.bean.WordList;
import com.example.yddc_2.myinterface.APIService;
import com.example.yddc_2.utils.GetNetService;
import com.example.yddc_2.utils.SecuritySP;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.security.GeneralSecurityException;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class FirstViewModel extends ViewModel {
    // TODO: Implement the ViewModel

}