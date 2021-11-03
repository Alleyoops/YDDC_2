package com.example.yddc_2.myinterface;


import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

public interface APIService {
    @GET("/getCodeImage")//获取验证码
    Observable<ResponseBody> GetCodeImage();

    @FormUrlEncoded
    @POST("/user/load")//登录
    Observable<ResponseBody> load(@Field("tel")String tel,@Field("pwd") String pwd,@Field("devId") String devId);
}
