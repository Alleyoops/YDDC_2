package com.example.yddc_2.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yddc_2.R;
import com.example.yddc_2.bean.User;
import com.example.yddc_2.myinterface.APIService;
import com.example.yddc_2.utils.DateUtil;
import com.example.yddc_2.utils.SecuritySP;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shawnlin.numberpicker.NumberPicker;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Timestamp;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RecyclerView_1_Adapter extends RecyclerView.Adapter<RecyclerView_1_Adapter.RvViewHolder> {
    private final int layoutId;
    private final Context context;
    private final String[] strings= {"姓名","简介","邮箱","年级","生日"};
    public RecyclerView_1_Adapter(int layoutId,Context context) {
        this.layoutId = layoutId;
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public RvViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(layoutId,null);
        return new RvViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull @NotNull RvViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.tv.setText(strings[position]);
        try {
            setUserInfo(context,holder,position);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {

                switch (position)
                {
                    case 0://姓名
                        Window window;
                        window = popWindow(v,R.layout.popupwindow);
                        TextView tvTitle = (TextView)window.findViewById(R.id.tv_title);
                        TextView tv = (TextView)window.findViewById(R.id.tv);
                        EditText et = (EditText)window.findViewById(R.id.et);
                        et.setText(holder.tvDetail.getText());
                        tvTitle.setText("修改"+strings[position]);
                        tv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(et.getText().toString().equals("")||et.getText().toString().length()>8)
                                {
                                    Toast.makeText(context, "长度不得超过8位", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    try {
                                        Map<String,String> map = new HashMap<>();
                                        map.put("name",et.getText().toString());
                                        update(map,holder);
                                    } catch (GeneralSecurityException | IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                        break;
                    case 1://简介
                        Window window1;
                        window1 = popWindow(v,R.layout.popupwindow);
                        TextView tvTitle1 = (TextView)window1.findViewById(R.id.tv_title);
                        TextView tv1 = (TextView)window1.findViewById(R.id.tv);
                        EditText et1 = (EditText)window1.findViewById(R.id.et);
                        et1.setText(holder.tvDetail.getText());
                        tvTitle1.setText("修改"+strings[position]);
                        tv1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(et1.getText().toString().length()>40)
                                {
                                    Toast.makeText(context, "字数不得超过40", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    try {
                                        Map<String,String> map = new HashMap<>();
                                        map.put("desc",et1.getText().toString());
                                        update(map,holder);
                                    } catch (GeneralSecurityException | IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                        break;
                    case 2://邮箱
                        Window window2;
                        window2 = popWindow(v,R.layout.popupwindow);
                        TextView tvTitle2 = (TextView)window2.findViewById(R.id.tv_title);
                        TextView tv2 = (TextView)window2.findViewById(R.id.tv);
                        EditText et2 = (EditText)window2.findViewById(R.id.et);
                        et2.setText(holder.tvDetail.getText());
                        et2.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                        tvTitle2.setText("修改"+strings[position]);
                        tv2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                    try {
                                        Map<String,String> map = new HashMap<>();
                                        map.put("email",et2.getText().toString());
                                        update(map,holder);
                                    } catch (GeneralSecurityException | IOException e) {
                                        e.printStackTrace();
                                    }
                            }
                        });
                        break;
                    case 3://年级
                        Window window3;
                        window3 = popWindow(v,R.layout.popupwindow2);
                        TextView tvTitle3 = (TextView)window3.findViewById(R.id.tv_title);
                        TextView tv3 = (TextView)window3.findViewById(R.id.tv);
                        NumberPicker picker1 = (NumberPicker)window3.findViewById(R.id.numPicker1);
                        NumberPicker picker2 = (NumberPicker)window3.findViewById(R.id.numPicker2);
                        String[] str1 = new String[]{"大学","高中","初中","硕士"};
                        String[] str2 = new String[]{"一年级","二年级","三年级","四年级"};
                        // IMPORTANT! setMinValue to 1 and call setDisplayedValues after setMinValue and setMaxValue
                        //下面三行顺序不能变，否则bug
                        picker1.setMinValue(1);//下标是从1算起的
                        picker1.setMaxValue(str1.length);
                        picker1.setDisplayedValues(str1);
                        picker2.setMinValue(1);
                        picker2.setMaxValue(str2.length);
                        picker2.setDisplayedValues(str2);
                        picker1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                            @Override
                            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                                if(picker1.getValue()==1)
                                {//大学有四个年级
                                    picker2.setMaxValue(str2.length);
                                }
                                else
                                {
                                    picker2.setMaxValue(str2.length-1);
                                }
                            }
                        });
                        tvTitle3.setText("修改"+strings[position]);
                        tv3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                NumPicker_update(picker1,picker2,holder,str1,str2);
                            }
                        });
                        break;
                    case 4://生日
                        //获取日历的一个实例，里面包含了当前的年月日
                        Calendar calendar = Calendar.getInstance();
                        //构建一个日期对话框，该对话框已经集成了日期选择器
                        //DatePickerDialog的第二个构造参数指定了日期监听器
                        DatePickerDialog dialog=new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                //把选择的日期上传
                                month+=1;//月份有点特殊
                                String str = year+"/"+month+"/"+dayOfMonth;
                                //Log.d("RecyclerView_1_Adapter", str);
                                try {
                                    Map<String,String> map = new HashMap<>();
                                    map.put("birth",str);
                                    update(map,holder);
                                } catch (GeneralSecurityException | IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                        //把日期对话框显示在界面上
                        dialog.show();
                        break;
                }
            }
        });
    }

    public void NumPicker_update(NumberPicker picker1, NumberPicker picker2,RvViewHolder holder
    ,String[] str1,String[] str2)
    {
        try {
            Map<String,String> map = new HashMap<>();
            map.put("grade",str1[picker1.getValue()-1]+str2[picker2.getValue()-1]);
            update(map,holder);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    //获取不同popupWindow的window布局
    private Window popWindow(View v,int layout){
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        LayoutInflater  inflater = LayoutInflater.from(v.getContext());
        View view = inflater.inflate(layout,null);
        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.shape_8);
        alertDialog.show();
        return alertDialog.getWindow();
    }

    private void update(Map<String,String> info,RvViewHolder holder) throws GeneralSecurityException, IOException {
        Log.d("RecyclerView_1_Adapter", info.get(info.keySet().iterator().next()));
        //上传修改后的用户信息
        Observable<ResponseBody> observable = GetApiService().update(SecuritySP.DecryptSP(context,"token"),info);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(context, "onError：修改失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            JsonObject jsonObject = JsonParser.parseString(responseBody.string()).getAsJsonObject();
                            int state = jsonObject.get("state").getAsInt();
                            if(state==200)
                            {
                                Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show();
                                //把修改后的信息显示到页面
                                String value = info.get(info.keySet().iterator().next());
                                holder.tvDetail.setText(value);
                            }
                            else Toast.makeText(context, "state:" + state, Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    @Override
    public int getItemCount() {
        return strings.length;
    }


    static class RvViewHolder extends RecyclerView.ViewHolder{
        public TextView tv;
        public TextView tvDetail;
        public LinearLayout ll;
        public RvViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tv = (TextView)itemView.findViewById(R.id.tv);
            tvDetail = (TextView)itemView.findViewById(R.id.tv_detail);
            ll = (LinearLayout)itemView.findViewById(R.id.ll);
        }
    }
    private static APIService GetApiService(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://youdian.asedrfa.top")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(APIService.class);
    }
    private void setUserInfo(Context context,RvViewHolder holder,int position) throws GeneralSecurityException, IOException {
        Observable<ResponseBody> observable = GetApiService().getUserDetail(SecuritySP.DecryptSP(context,"token"));
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("RVAdapter: ", "getUserDetail onError");
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            String[] strDetail;
                            JsonObject jsonObject = JsonParser.parseString(responseBody.string()).getAsJsonObject();
                            int state = jsonObject.get("state").getAsInt();
                            Gson gson = new Gson();
                            User user;
                            if(state != 200)
                            {
                                //给一个默认user属性
                                user = gson.fromJson(context.getResources().getString(R.string.user),User.class);
                            }
                            else
                            {
                                user = gson.fromJson(jsonObject.get("data").toString(),User.class);
                            }
                            //把user的birth时间戳改为年月日字符串
                            String birth = user.getBirth();
                            user.setBirth(DateUtil.transForDateNYR(Long.parseLong(birth)));
                            strDetail= new String[]{user.getName(),user.getDesc(),user.getEmail(),user.getGrade(),user.getBirth()};
                            holder.tvDetail.setText(strDetail[position]);
                        } catch (IOException | ParseException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
