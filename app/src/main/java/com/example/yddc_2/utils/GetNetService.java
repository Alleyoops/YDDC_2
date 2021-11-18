package com.example.yddc_2.utils;

import com.example.yddc_2.myinterface.APIService;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class GetNetService {
    public static APIService GetApiService(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://youdian.asedrfa.top")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(APIService.class);
    }
}
