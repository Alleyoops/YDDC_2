package com.example.yddc_2.myinterface;


import com.example.yddc_2.bean.DaySentence;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface APIService {
//    @GET("/getCodeImage")//获取验证码
//    Observable<ResponseBody> GetCodeImage();
    //要获取响应头里的cookie，就不能使用这种接口方法的方式，因为这里只能返回响应体以及转换器转换后的数据。
    //所以把它注释掉，采取okhttp方式
    @FormUrlEncoded
    @POST("/user/load")//登录
    Observable<ResponseBody> load(@Field("devId") String devId,@Field("pwd") String pwd,@Field("tel")String tel);

//    @FormUrlEncoded
//    @POST("/user/regist")//注册
//    Observable<ResponseBody> register(@Field("code") String code,@Field("pwd") String pwd,@Field("tel")String tel);
    //添加cookie,也不选择接口方式，用okhttp吧。
    //当然也可以使用Retrofit来获取和添加cookie
    //可以参考：https://www.jianshu.com/p/1caa92bf8079 和 https://blog.csdn.net/zhang___yong/article/details/76255872
    //Retrofit的cookie的保存和添加都可以用Interceptor来实现,而且可以每次请求中都被实现
    //但是登录后会使用Token，所以就不需要cookie了

    @GET("/user/getuserdetail")//获取用户详细信息
    Observable<ResponseBody> getUserDetail(@Header("token") String token);

    @FormUrlEncoded
    @POST("/user/getuserindex")//获取用户主页信息
    Observable<ResponseBody> getUserindex(@Field("token")String token,@Field("uId")String uid);

    @FormUrlEncoded
    @POST("/user/update")//修改用户信息，Map：birth,name,desc,email,grade
    Observable<ResponseBody> update(@Header("token") String token, @FieldMap Map<String,String> info);

    @GET("/everyday/index")//每日一句key = "506799470fbdf81a728b2f19905545f3"
    Observable<DaySentence> GetDaySentence(@Query("key") String key);

    @Multipart//修改头像
    @POST("user/{path}")
    Observable<ResponseBody> ChangeHead(@Path("path") String path, @Header("token") String token,@Part MultipartBody.Part file);

    @GET(" ")//获取头像或背景
    Observable<ResponseBody> GetHead_Back();

}
