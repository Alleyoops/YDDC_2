package com.example.yddc_2.navigation.me;

import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yddc_2.MainActivity;
import com.example.yddc_2.R;
import com.example.yddc_2.bean.Setting;
import com.example.yddc_2.bean.User;
import com.example.yddc_2.databinding.ThirdFragmentBinding;
import com.example.yddc_2.myinterface.APIService;
import com.example.yddc_2.navigation.word.FirstFragment;
import com.example.yddc_2.navigation.word.FirstViewModel;
import com.example.yddc_2.utils.BitmapUtil;
import com.example.yddc_2.utils.GetNetService;
import com.example.yddc_2.utils.GlideEngine;
import com.example.yddc_2.utils.MyHandler;
import com.example.yddc_2.utils.NetUtil;
import com.example.yddc_2.utils.PressAnimUtil;
import com.example.yddc_2.utils.SecuritySP;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.luck.picture.lib.tools.PictureFileUtils;


import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    public static String tag = "";
    public static int watch = 0;
    public static int phone = 0;
    public static ThirdFragment newInstance() {
        return new ThirdFragment();
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = ThirdFragmentBinding.inflate(inflater);
        binding.setHandler(new MyHandler());
        //intiView
        PressAnimUtil.addScaleAnimition(binding.llData,null,0.8f);
        try {
            initSetting();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        scan();
        updateHead_Back();
        return binding.getRoot();
    }

    private void initSetting() throws GeneralSecurityException, IOException {
        //initSetting
        String[] res1 = getResources().getStringArray(R.array.word_book);
        String[] res2 = getResources().getStringArray(R.array.recite_way);
        List<String> data1 = new LinkedList<>(Arrays.asList(res1));//用于确定下标顺序
        List<String> data2 = new LinkedList<>(Arrays.asList(res2));
        //词典：CET4/CET6/高中/考研/GRE/TOEFL/IELTS
        NiceSpinner spWordBook = binding.SpWordBook;
        spWordBook.attachDataSource(data1);
        //默认模式：任务模式、收藏模式（本地修改）
        NiceSpinner spReciteWay = binding.SpReciteWay;
        spReciteWay.attachDataSource(data2);
        //手表提醒
        SwitchCompat sbWatch = binding.sbWatch;
        //手机提醒
        SwitchCompat sbPhone = binding.sbPhone;
        //FirstViewModel代码复用
        FirstViewModel viewModel = new ViewModelProvider(this).get(FirstViewModel.class);
        viewModel.getmSetting(getContext()).observe(getViewLifecycleOwner(), new Observer<Setting>() {
            @Override
            public void onChanged(Setting setting) {
                if (setting==null)
                {
                    //给个初始值，防止闪退
                    Setting.DataDTO dataDTO = new Setting.DataDTO("0","0",0,0,0,5,10,0,"null");
                    setting = new Setting(0,"null",dataDTO);
                }
                tag = setting.getData().getTag();//我的词典
                watch = setting.getData().getWatRem();
                phone = setting.getData().getPhoRem();
//                List<String> list1 = new ArrayList<>(Arrays.asList(res1));
//                List<String> list2 = new ArrayList<>(Arrays.asList(res2));
                //我的词典
                spWordBook.setSelectedIndex(data1.indexOf(tag));//勾选相应选项,这里注意：setSelectedIndex并不会触发监听器！！！
                //记忆模式
                try {
                    String value = SecuritySP.DecryptSP(getContext(),"reciteWay");//从本地读取该设置
                    if(value.equals("")){//说明第一次登陆，默认设置为第一个模式，即“任务模式”
                        value = data2.get(0);
                        spReciteWay.setSelectedIndex(data2.indexOf(value));//设置选项
                        TextView tv_way = (TextView) requireActivity().findViewById(R.id.tv_way);//设置底部栏"记忆模式"的显示
                        tv_way.setText(value);
                    }
                    else
                    {
                        spReciteWay.setSelectedIndex(data2.indexOf(value));
                        TextView tv_way = (TextView) requireActivity().findViewById(R.id.tv_way);//设置底部栏"记忆模式"的显示
                        tv_way.setText(value);
                    }
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
                spReciteWay.getSelectedItem();
                //手表提醒
                if(watch==1) sbWatch.setChecked(true);
                sbWatch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) watch = 1;
                        else watch = 0;
                        try {
                            updateSetting(tag,watch,phone);
                        } catch (GeneralSecurityException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                //手机提醒
                if(phone==1) sbPhone.setChecked(true);
                sbPhone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) phone = 1;
                        else phone = 0;
                        try {
                            updateSetting(tag,watch,phone);
                        } catch (GeneralSecurityException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        spWordBook.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                tag = data1.get(position);
                try {
                    //initWord
                    updateSetting(tag,watch,phone);
                    //FirstFragment的initSetting,从而显示新的DAYS
                    //这里实现的方法是用广播通知对方再次实行initSetting方法来刷新days
                    Intent intent = new Intent("initSetting");
                    intent.putExtra("change", "yes");//用来判断，可有可无
                    LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent);
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
            }
        });

        spReciteWay.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                if (NetUtil.getNetWorkStart(requireContext())!=1) {
                try {
                    //保存到本地
                    SecuritySP.EncryptSP(getContext(),"reciteWay",data2.get(position));
                    //设置底部栏“记忆模式”的显示
                    TextView way = (TextView) requireActivity().findViewById(R.id.tv_way);
                    way.setText(data2.get(position));
                    //重新加载相应的单词库
                    MainActivity mainActivity = (MainActivity)getActivity();
                    assert mainActivity != null;
                    if (position==0) mainActivity.iniTodayWords();//任务模式
                    else if (position==1)mainActivity.iniMyWords();//收藏模式
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(requireActivity(), data2.get(position), Toast.LENGTH_SHORT).show();
            }}
        });
    }

    private void updateSetting(String tag, int watch, int phone) throws GeneralSecurityException, IOException {
        String token = SecuritySP.DecryptSP(getContext(),"token");
        Map<String,Object> settingMap = new HashMap<>();
        settingMap.put("tag",tag);
        settingMap.put("watRem",watch);
        settingMap.put("phoRem",phone);
        settingMap.put("circWay", 0);
        //Log.d("ThirdFragment", "settingMap:" + settingMap);
        GetNetService.GetApiService().updateSetting(token,settingMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new rx.Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                            Toast.makeText(getContext(), "updateSetting onError", Toast.LENGTH_SHORT).show();
                            //Log.d("ThirdFragment", "e:" + e);
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            JsonObject jsonObject = JsonParser.parseString(responseBody.string()).getAsJsonObject();
                            int state = jsonObject.get("state").getAsInt();
                            if (state==200) Toast.makeText(getContext(), "设置更新", Toast.LENGTH_SHORT).show();
                            else {
                                Toast.makeText(getContext(), "设置失败 State:"+state, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
    }


    //下载头像或背景
    private void downHead_Back(ImageView iv,String url,String name)
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
                                //Toast.makeText(requireActivity(), privatePath, Toast.LENGTH_SHORT).show();
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
        View.OnLongClickListener listener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                LayoutInflater  inflater = LayoutInflater.from(v.getContext());
                View view = inflater.inflate(R.layout.popupwindow_menu,null);
                builder.setView(view);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.shape_8);
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
        };
        binding.back.setOnLongClickListener(listener);
        binding.ivHead.setOnLongClickListener(listener);
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
    private void scan()
    {
        binding.ivScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity)getActivity();
                assert activity != null;
                activity.loadScanKitBtnClick(v);
            }
        });
    }

}