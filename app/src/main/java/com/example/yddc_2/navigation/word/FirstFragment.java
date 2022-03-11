package com.example.yddc_2.navigation.word;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yddc_2.MainActivity;
import com.example.yddc_2.R;
import com.example.yddc_2.adapter.RecyclerView_1_Adapter;
import com.example.yddc_2.adapter.RecyclerView_wordList_Adapter;
import com.example.yddc_2.bean.DayPlan;
import com.example.yddc_2.bean.ReciteRecord;
import com.example.yddc_2.bean.Setting;
import com.example.yddc_2.bean.WordList;
import com.example.yddc_2.databinding.FirstFragmentBinding;
import com.example.yddc_2.utils.DateUtil;
import com.example.yddc_2.utils.GetNetService;
import com.example.yddc_2.utils.PressAnimUtil;
import com.example.yddc_2.utils.SecuritySP;
import com.example.yddc_2.viewmodels.MainViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shawnlin.numberpicker.NumberPicker;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class FirstFragment extends Fragment {
    private FirstFragmentBinding binding;
    public static int num = 0;//单词本单词数
    public static int numOfDay = 0;//每天单词数，即：numOfDay = list x nol
    public static Setting set;
    private LineChartView lineChart;
    String[] date = {"0-2","2-4","4-6","6-8","8-10","10-12","12-14","14-16","16-18","18-20","20-22","22-24"};//X轴的标注
    int[] score = new int[12];//图表的数据点
    private List<PointValue> mPointValues = new ArrayList<>();
    private List<AxisValue> mAxisXValues = new ArrayList<>();
    private LocalBroadcastManager broadcastManager;//定义一个广播管理器

    public static FirstFragment newInstance() {
        return new FirstFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FirstFragmentBinding.inflate(inflater);
        //注册广播
        registerReceiver();
        try {
            //初始化设置
            initSettings();
            //initView();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return binding.getRoot();
    }

    //定义一个广播接收器
    private final BroadcastReceiver mAdDownLoadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String change = intent.getStringExtra("change");
            if ("yes".equals(change)) {
                // 这地方只能在主线程中刷新UI,子线程中无效，因此用Handler来实现
                new Handler().post(new Runnable() {
                    public void run() {
                        //在这里来写你需要刷新的地方
                        try {
                            initSettings();//在ThirdFragment更新单词本后执行
                            //Log.d("FirstFragment", "通知到位");
                        } catch (GeneralSecurityException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    };
    //注册广播接收器
    private void registerReceiver() {
        broadcastManager = LocalBroadcastManager.getInstance(requireActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("initSetting");//说白了都是些暗号罢了
        broadcastManager.registerReceiver(mAdDownLoadReceiver, intentFilter);
    }
    //Detach时注销广播
    @Override
    public void onDetach() {
        super.onDetach();
        broadcastManager.unregisterReceiver(mAdDownLoadReceiver);
    }

    //FirstFragment里initSetting的同时加载词库iniTodayWords()
    private void initSettings() throws GeneralSecurityException, IOException {
        com.shawnlin.numberpicker.NumberPicker list = binding.list;
        com.shawnlin.numberpicker.NumberPicker nl = binding.nl;
        com.shawnlin.numberpicker.NumberPicker day = binding.day;
        list.setMaxValue(20);
        nl.setMinValue(5);//必须大于4
        nl.setMaxValue(20);
        day.setMaxValue(200);//会出现大于200的情况
        FirstViewModel viewModel = new ViewModelProvider(this).get(FirstViewModel.class);
        viewModel.getmSetting(getContext()).observe(getViewLifecycleOwner(), new Observer<Setting>() {
            @Override
            public void onChanged(Setting setting) {
                set = setting;
                String tag = setting.getData().getTag();//单词本tag
                Gson gson = new Gson();
                String jsonStr = gson.toJson(set);
                try {
                    //把setting保存到本地
                    SecuritySP.EncryptSP(getContext(),"setting",jsonStr);
                    //根据ThirdFragment的reciteWay加载相应记忆模式的词库
                    String[] res_ = getResources().getStringArray(R.array.recite_way);
                    String value = SecuritySP.DecryptSP(getContext(),"reciteWay");
                    MainActivity mainActivity = (MainActivity)getActivity();
                    assert mainActivity != null;
                    if(value.equals("")){//说明第一次登陆，默认设置为第一个模式，即“任务模式”
                        value = res_[0];
                        SecuritySP.EncryptSP(getContext(),"reciteWay",value);//保存在本地
                    }
                    if (value.equals(res_[0]))//任务模式
                    {

                        mainActivity.iniTodayWords();
                        //Log.d("FirstFragment", "here");
                    }
                    else if (value.equals(res_[1])){//收藏模式
                        mainActivity.iniMyWords();;
                        //Log.d("FirstFragment", "there");
                    }

                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
                //获取单词本的单词总数
                GetNetService.GetApiService().getNumOfBook(tag)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new rx.Observer<ResponseBody>() {
                            @Override
                            public void onCompleted() {
                            }
                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(getContext(), "getNumOfBook onError", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onNext(ResponseBody responseBody) {
                                try {
                                    JsonObject jsonObject = JsonParser.parseString(responseBody.string()).getAsJsonObject();
                                    int state = jsonObject.get("state").getAsInt();
                                    if (state==200)
                                    {
                                        list.setValue(setting.getData().getList());
                                        nl.setValue(setting.getData().getNumOfList());
                                        num = jsonObject.get("data").getAsJsonObject().get("num").getAsInt();
                                        numOfDay = setting.getData().getList()*setting.getData().getNumOfList();
                                        int d = (int)(num/(setting.getData().getList()*setting.getData().getNumOfList()));
                                        day.setMaxValue(Math.max(d, 200));
                                        day.setValue(d);
                                        //更新"完成日期"的显示
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTime(new Date());
                                        calendar.add(Calendar.DATE,d);
                                        binding.overtime.setText(DateUtil.transForDateNYR(calendar.getTime().getTime()));
                                        //更新bottom上的当前词汇tag显示
                                        TextView tag = (TextView) requireActivity().findViewById(R.id.tag);
                                        tag.setText(set.getData().getTag());
                                    }
                                    else Toast.makeText(getContext(), "getNumOfBook:state:" + state, Toast.LENGTH_SHORT).show();
                                    initView();
                                } catch (IOException | ParseException | GeneralSecurityException e) {
                                    e.printStackTrace();
                                }

                            }});
            }
        });
        //监听器
        list.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                binding.check.setVisibility(View.VISIBLE);
                //它俩乘积最大为100，后端只返回最多100条
                if(list.getValue()*nl.getValue()>=100) nl.setMaxValue(100/list.getValue());
                else nl.setMaxValue(20);
                int d = (int)(num/(list.getValue()*nl.getValue()));
                day.setMaxValue(Math.max(d, 200));
                day.setValue(d);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        list.setValue(set.getData().getList());
                        binding.check.setVisibility(View.INVISIBLE);
                    }
                },30000);
            }
        });
        nl.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                binding.check.setVisibility(View.VISIBLE);
                //它俩乘积最大为100，后端只返回最多100条
                if(list.getValue()*nl.getValue()>=100) list.setMaxValue(100/nl.getValue());
                else list.setMaxValue(20);
                int d = (int)(num/(list.getValue()*nl.getValue()));
                day.setMaxValue(Math.max(d, 200));
                day.setValue(d);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        nl.setValue(set.getData().getNumOfList());
                        binding.check.setVisibility(View.INVISIBLE);
                    }
                },30000);
            }
        });
        day.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                binding.check.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        day.setValue((int)(num/(set.getData().getList()*set.getData().getNumOfList())));
                        binding.check.setVisibility(View.INVISIBLE);
                    }
                },30000);
            }
        });
        //点击提交
        binding.check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    updateSetting(set,list.getValue(),nl.getValue(),day.getValue());
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void updateSetting(Setting set,int list,int nol,int days) throws GeneralSecurityException, IOException {
        String token = SecuritySP.DecryptSP(getContext(),"token");
        Map<String,Object> settingMap = new HashMap<>();
        if (set==null)
        {
            //给个默认初始值，防止闪退
            Setting.DataDTO dataDTO = new Setting.DataDTO("0","0",0,0,0,5,10,0,"null");
            set = new Setting(0,"null",dataDTO);
        }
        settingMap.put("tag",set.getData().getTag());
        settingMap.put("watRem",set.getData().getWatRem());
        settingMap.put("phoRem",set.getData().getPhoRem());
        settingMap.put("circWay", 0);
        settingMap.put("list",list);
        settingMap.put("numOfList",nol);
        settingMap.put("dayTime",days);
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
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            JsonObject jsonObject = JsonParser.parseString(responseBody.string()).getAsJsonObject();
                            int state = jsonObject.get("state").getAsInt();
                            if (state==200)
                            {
//                                //暂时无用
//                                Calendar calendar = Calendar.getInstance();
//                                int dayOfWord = calendar.get(Calendar.DAY_OF_MONTH);
//                                SecuritySP.EncryptSP(getContext(),"day",String.valueOf(dayOfWord-1));
                                Toast.makeText(getContext(), "提交成功", Toast.LENGTH_SHORT).show();
                                //重新iniSetting和iniTodayWords
                                initSettings();
                            }
                            else
                            {
                                Toast.makeText(getContext(), "保存失败 State："+state, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    private void initView() throws GeneralSecurityException, IOException {
        //展示今日计划
        String token = null;
        try {
            token = SecuritySP.DecryptSP(getContext(),"token");
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        GetNetService.GetApiService().getPlan(token)
                .enqueue(new Callback<DayPlan>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(Call<DayPlan> call, Response<DayPlan> response) {
                        String json = new Gson().toJson(response.body());
                        DayPlan dayPlan = new Gson().fromJson(json,DayPlan.class);
                        if (dayPlan.getState()==200){
                            binding.plan1.setText(String.valueOf(dayPlan.getData().getToday()));
                            binding.plan2.setText(String.valueOf(dayPlan.getData().getToReviewed()));
                            binding.plan3.setText(String.valueOf(dayPlan.getData().getTodayed()));
                            binding.plan4.setText(String.valueOf(dayPlan.getData().getEfficiency())+"/h");
                            TextView wordNum = (TextView)requireActivity().findViewById(R.id.word_num);
                            wordNum.setText(String.valueOf(dayPlan.getData().getSumWord())+"个");
                            TextView wordNum2 = (TextView) requireActivity().findViewById(R.id.total_num);
                            wordNum2.setText(String.valueOf(dayPlan.getData().getSumWord()));
                            TextView totalTime = (TextView) requireActivity().findViewById(R.id.total_time);
                            totalTime.setText(DateUtil.TransSecondsToHMS(dayPlan.getData().getSumTime()));
                        }

                    }

                    @Override
                    public void onFailure(Call<DayPlan> call, Throwable t) {
                        Toast.makeText(getContext(), "t:" + t, Toast.LENGTH_SHORT).show();
                    }
                });

        //展示背诵数据
        FirstViewModel viewModel = new ViewModelProvider(this).get(FirstViewModel.class);
        viewModel.getmReciteData(getContext()).observe(getViewLifecycleOwner(), new Observer<ReciteRecord>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(ReciteRecord reciteRecord) {
                if (reciteRecord!=null)
                {
                    ReciteRecord.DataDTO dataDTO = reciteRecord.getData();
                    if (dataDTO.getEfficiency()>0)
                        binding.avEfficient.setText("+"+dataDTO.getEfficiency()+"%");
                    else binding.avEfficient.setText(dataDTO.getEfficiency()+"%");
                    binding.avNum.setText(dataDTO.getAverageWords()+"个");
                    binding.avTime.setText(DateUtil.TransSecondsToHMS(dataDTO.getAverageTime()));
                    //如果上面两行的数据为0，说明刚注册，未使用，不是先数据卡片
                    if(dataDTO.getAverageWords()==0&&dataDTO.getAverageTime()==0)
                    {
                        binding.llData1.setVisibility(View.GONE);
                    }
                    else binding.llData1.setVisibility(View.VISIBLE);
                    float eff = ((float)dataDTO.getAverageWords())/3600;//_个/s
                    if (eff!=0){
                        float daytime = ((float)numOfDay)/eff;//_s
                        binding.daytime.setText("预计每天"+DateUtil.TransSecondsToHMS(Math.round(daytime)));
                        //binding.daytime.setText("预计每天"+numOfDay);
                    }
                    else {
                        //eff为0，不显示预估时间
                        binding.daytime.setText("");
                    }

                    score = new int[]{dataDTO.getA(), dataDTO.getB(), dataDTO.getC(), dataDTO.getD(),
                            dataDTO.getE(), dataDTO.getF(), dataDTO.getG(), dataDTO.getH(),
                            dataDTO.getI(), dataDTO.getJ(), dataDTO.getK(), dataDTO.getL()};
                    //选取最大时间段并显示
                    int index = 0;
                    int max = 0;
                    for (int i=0;i<score.length;i++)
                    {
                        if (max<score[i]) {
                            max = score[i];
                            index = i;
                        }
                    }
                    //转化一下显示的格式
                    String s1 = date[index];
                    String[] str = s1.split("-");
                    String s2 = str[0]+":00~"+str[1]+":00";
                    binding.tv1.setText("您背单词效率较高的时间集中在"+s2+"\n最近一周，您平均:");
                    lineChart = binding.chart;
                    mPointValues.clear();
                    mAxisXValues.clear();
                    getAxisXLables();//获取x轴的标注
                    getAxisPoints();//获取坐标点
                    initLineChart();
                }

            }
        });

        //今日任务和我的收藏的单词的展示
        View.OnClickListener listener1 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取单词列表
                String token = null;
                try {
                    token = SecuritySP.DecryptSP(getContext(),"token");
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
                GetNetService.GetApiService().getMyWordList(token,1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new rx.Observer<WordList>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(getContext(), "error_1", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onNext(WordList wordList) {
                                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
                                bottomSheetDialog.setContentView(R.layout.word_list);
                                TextView tv_title = bottomSheetDialog.findViewById(R.id.tv_title);
                                assert tv_title != null;
                                if (wordList.getData().size()!=0)
                                {

                                    tv_title.setText("我已掌握");
                                    //初始化RecyclerView
                                    RecyclerView recyclerView = bottomSheetDialog.findViewById(R.id.listView);
                                    assert recyclerView != null;
                                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                    recyclerView.setAdapter(new RecyclerView_wordList_Adapter(R.layout.list_item,getContext(),wordList,0));


                                }
                                else
                                {
                                    tv_title.setText("还没有掌握哦~");
                                }
                                bottomSheetDialog.show();
                            }
                        });
            }
        };
        View.OnClickListener listener2 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取单词列表
                String token = null;
                try {
                    token = SecuritySP.DecryptSP(getContext(),"token");
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
                GetNetService.GetApiService().getMyWordList(token,4)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new rx.Observer<WordList>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(getContext(), "error_2", Toast.LENGTH_SHORT).show();
                            }

                            @SuppressLint("UseCompatLoadingForDrawables")
                            @Override
                            public void onNext(WordList wordList) {
                                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
                                bottomSheetDialog.setContentView(R.layout.word_list);
                                TextView tv_title = bottomSheetDialog.findViewById(R.id.tv_title);
                                assert tv_title != null;
                                if (wordList.getData().size()!=0)
                                {

                                    tv_title.setText("我的收藏");
                                    tv_title.setTextColor(getResources().getColor(R.color.item));
                                    //初始化RecyclerView
                                    RecyclerView recyclerView = bottomSheetDialog.findViewById(R.id.listView);
                                    assert recyclerView != null;
                                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                    recyclerView.setAdapter(new RecyclerView_wordList_Adapter(R.layout.list_item,getContext(),wordList,1));

                                }
                                else {
                                    tv_title.setText("还没有收藏哦~");
                                    tv_title.setTextColor(getResources().getColor(R.color.item));
                                }
                                bottomSheetDialog.show();

                            }
                        });
            }};
        PressAnimUtil.addScaleAnimition(binding.task1,listener1,0.8f);
        PressAnimUtil.addScaleAnimition(binding.task2,listener2,0.8f);

    }


    private void initLineChart(){
            Line line = new Line(mPointValues).setColor(Color.parseColor("#FFCD41"));  //折线的颜色（橙色）
            List<Line> lines = new ArrayList<Line>();
            line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
            line.setCubic(true);//曲线是否平滑，即是曲线还是折线
            line.setFilled(true);//是否填充曲线的面积
//            line.setHasLabels(true);//曲线的数据坐标是否加上备注
      line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
            line.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
            line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点
            lines.add(line);
            LineChartData data = new LineChartData();

            data.setLines(lines);
            //坐标轴
            Axis axisX = new Axis(); //X轴
            axisX.setName("时间周期");
            axisX.setAutoGenerated(true);
            axisX.setHasSeparationLine(true);
            axisX.setHasTiltedLabels(false);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
            axisX.setTextColor(Color.GRAY);  //设置字体颜色
            axisX.setTextSize(10);//设置字体大小
            axisX.setMaxLabelChars(12); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数
            axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
            data.setAxisXBottom(axisX); //x 轴在底部
            axisX.setHasLines(true); //x 轴分割线
            Axis axisY = new Axis();  //Y轴
            axisY.setName("单词个数");//y轴标注
            axisY.setTextSize(10);//设置字体大小
        axisY.setTextColor(Color.GRAY);
            data.setAxisYLeft(axisY);  //Y轴设置在左边
        data.setValueLabelBackgroundEnabled(true);
//            设置行为属性，支持缩放、滑动以及平移
            lineChart.setInteractive(true);
            lineChart.setZoomType(ZoomType.VERTICAL);
            lineChart.setMaxZoom((float) 2);//最大方法比例
            lineChart.setContainerScrollEnabled(false, ContainerScrollType.VERTICAL);
            lineChart.setLineChartData(data);
            lineChart.setVisibility(View.VISIBLE);
        }
    /**
     * 设置X 轴的显示
     */
    private void getAxisXLables(){
        for (int i = 0; i < date.length; i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(date[i]));
        }
    }
    /**
     * 图表的每个点的显示
     */
    private void getAxisPoints(){
        for (int i = 0; i < score.length; i++) {
            mPointValues.add(new PointValue(i, score[i]));
        }
    }
    /**
     * **************************************************************************
     */

}