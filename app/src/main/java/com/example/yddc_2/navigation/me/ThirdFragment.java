package com.example.yddc_2.navigation.me;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.yddc_2.MainActivity;
import com.example.yddc_2.R;
import com.example.yddc_2.bean.User;
import com.example.yddc_2.databinding.ThirdFragmentBinding;
import com.example.yddc_2.myinterface.APIService;
import com.example.yddc_2.utils.BitmapUtil;
import com.example.yddc_2.utils.DateUtil;
import com.example.yddc_2.utils.GetNetService;
import com.example.yddc_2.utils.GlideEngine;
import com.example.yddc_2.utils.MyHandler;
import com.example.yddc_2.utils.SecuritySP;
import com.example.yddc_2.utils.StatusBarUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.luck.picture.lib.tools.PictureFileUtils;


import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class ThirdFragment extends Fragment{
    private ThirdFragmentBinding binding;
    public static ThirdFragment newInstance() {
        return new ThirdFragment();
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = ThirdFragmentBinding.inflate(inflater);
        binding.setHandler(new MyHandler());
        updateHead_Back();
        return binding.getRoot();
    }

//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        ThirdViewModel mViewModel = new ViewModelProvider(this).get(ThirdViewModel.class);
//        // TODO: Use the ViewModel
//        try {
//            mViewModel.getUser(getContext()).observe(getViewLifecycleOwner(), new Observer<User>() {
//                @SuppressLint("SetTextI18n")
//                @Override
//                public void onChanged(User user) {
//                    //显示姓名和简介
//                    binding.tvName.setText("\"gg"+user.getName()+" ");
//                    binding.desc.setText(user.getDesc());
//                    //显示头像和背景
//                    downHead_Back(binding.ivHead,user.getHUrl(),"/img_head.PNG");
//                    downHead_Back(binding.back,user.getBUrl(),"/img_back.PNG");
//                    //加载完后隐藏ProgressBar
//                    binding.pBar.setVisibility(View.INVISIBLE);
//                }
//            });
//        } catch (GeneralSecurityException | IOException e) {
//            e.printStackTrace();
//        }
//    }

    //下载头像或背景
    private  void downHead_Back(ImageView iv,String url,String name)
    {

        if(!url.equals(""))//url不为空，说明有上传图片，可以选择是下载还是加载本地图片
        {
            Bitmap bitmap = BitmapUtil.openBitmap(requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()+name);
            //如果本地不存在，则下载
            if(bitmap==null)
            {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://youdian.asedrfa.top"+url+"/")
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                Observable<ResponseBody> observable = retrofit.create(APIService.class).getHead_Back();
                observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new rx.Observer<ResponseBody>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(getContext(), "downHead_Back: onError", Toast.LENGTH_SHORT).show();
                                Log.d("ThirdFragment", "e:" + e);
                            }

                            @Override
                            public void onNext(ResponseBody responseBody) {
                                Bitmap _bitmap = BitmapFactory.decodeStream(responseBody.byteStream());
                                iv.setImageBitmap(_bitmap);
                                //获取应用私有空间的存储路径
                                String privatePath = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
                                //下载后把图片保存到本地
                                BitmapUtil.saveImage(privatePath+name,_bitmap);

                            }
                        });
            }
            else
            {
                iv.setImageBitmap(bitmap);
            }

        }
    }


    @Override
    public void onResume() {
        super.onResume();
        ThirdViewModel mViewModel = new ViewModelProvider(this).get(ThirdViewModel.class);
        // TODO: Use the ViewModel
        try {
            mViewModel.getUser(getContext()).observe(getViewLifecycleOwner(), new Observer<User>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onChanged(User user) {
                    //显示姓名和简介
                    binding.tvName.setText(" "+user.getName()+" ");
                    binding.desc.setText(user.getDesc());
                    //显示头像和背景
                    downHead_Back(binding.ivHead,user.getHUrl(),"/img_head.PNG");
                    downHead_Back(binding.back,user.getBUrl(),"/img_back.PNG");
                    binding.pBar.setVisibility(View.INVISIBLE);
                }
            });
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    //上传头像或背景
    private void updateHead_Back()
    {
        binding.back.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                LayoutInflater  inflater = LayoutInflater.from(v.getContext());
                View view = inflater.inflate(R.layout.popupwindow_menu,null);
                builder.setView(view);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.shape_2);
                alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                TextView tv_chd = (TextView)alertDialog.getWindow().findViewById(R.id.tv_chd);
                TextView tv_cbk = (TextView)alertDialog.getWindow().findViewById(R.id.tv_cbk);
                tv_chd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //从相册选择头像
                        PictureSelector.create(ThirdFragment.this)
                                .openGallery(PictureMimeType.ofAll())
                                .selectionMode(PictureConfig.SINGLE)
                                .isEnableCrop(true)
                                .isCompress(true)//压缩
                                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                                .withAspectRatio(1, 1)
                                .imageEngine(GlideEngine.createGlideEngine())
                                .forResult(new OnResultCallbackListener<LocalMedia>() {
                                    @Override
                                    public void onResult(List<LocalMedia> list) {
                                        String img_path = list.get(0).getCompressPath();//获取剪切压缩后的图片路径
                                        File file = new File(img_path);
                                        RequestBody requestBody=RequestBody.create(MediaType.parse("image/png"),file);
                                        MultipartBody.Part multipartBody=MultipartBody.Part.createFormData("file",file.getName(),requestBody);
//                                        //转为位图
//                                        Bitmap bitmap = BitmapFactory.decodeFile(path);
                                        //上传
                                        try {
                                            uploadPic(getContext(),multipartBody,"changehead");
                                        } catch (GeneralSecurityException | IOException e) {
                                            e.printStackTrace();
                                        }
                                        alertDialog.dismiss();
                                    }

                                    @Override
                                    public void onCancel() {
                                        alertDialog.dismiss();
                                    }
                                });
                    }
                });
                tv_cbk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //从相册选择背景
                        PictureSelector.create(ThirdFragment.this)
                                .openGallery(PictureMimeType.ofAll())
                                .selectionMode(PictureConfig.SINGLE)
                                .isEnableCrop(true)
                                .isCompress(true)//压缩
                                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                                .withAspectRatio(16, 9)
                                .imageEngine(GlideEngine.createGlideEngine())
                                .forResult(new OnResultCallbackListener<LocalMedia>() {
                                    @Override
                                    public void onResult(List<LocalMedia> list) {
                                        String img_path = list.get(0).getCompressPath();
                                        File file = new File(img_path);
                                        RequestBody requestBody=RequestBody.create(MediaType.parse("image/png"),file);
                                        MultipartBody.Part multipartBody=MultipartBody.Part.createFormData("file",file.getName(),requestBody);
                                        //上传
                                        try {
                                            uploadPic(getContext(),multipartBody,"changeback");
                                        } catch (GeneralSecurityException | IOException e) {
                                            e.printStackTrace();
                                        }
                                        alertDialog.dismiss();
                                    }
                                    @Override
                                    public void onCancel() {
                                        alertDialog.dismiss();
                                    }
                                });
                    }
                });
                return false;
            }
        });
    }
    //上传图片
    private void uploadPic(Context context,MultipartBody.Part part,String path) throws GeneralSecurityException, IOException {
        Observable<ResponseBody> observable = GetNetService.GetApiService().changeHead(path,SecuritySP.DecryptSP(context,"token"),part);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new rx.Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(context, "onError", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        JsonObject jsonObject = null;
                        try {
                            jsonObject = JsonParser.parseString(responseBody.string()).getAsJsonObject();
                            int state = jsonObject.get("state").getAsInt();
                            if(state!=200)
                            {
                                Toast.makeText(context, "state:" + state, Toast.LENGTH_SHORT).show();
                            }
                            else
                            {   //上传后清除压缩缓存
                                PictureFileUtils.deleteCacheDirFile(requireContext(),PictureMimeType.ofImage());
                                //更新显示
                                onResume();
                                Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }

}