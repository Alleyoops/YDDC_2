package com.example.yddc_2.navigation.word;

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
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.yddc_2.MainActivity;
import com.example.yddc_2.R;
import com.example.yddc_2.bean.Setting;
import com.example.yddc_2.bean.WordList;
import com.example.yddc_2.databinding.FirstFragmentBinding;
import com.example.yddc_2.utils.GetNetService;
import com.example.yddc_2.utils.SecuritySP;
import com.example.yddc_2.viewmodels.MainViewModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shawnlin.numberpicker.NumberPicker;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
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
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class FirstFragment extends Fragment {
    private FirstFragmentBinding binding;
    private FirstViewModel mViewModel;
    public static int num = 0;//单词本单词数
    public static Setting set;
    private LineChartView lineChart;
    String[] date = {"10-22","11-22","12-22","1-22","6-22","5-23","5-22","6-22","5-23","5-22"};//X轴的标注
    int[] score= {50,42,90,33,10,74,22,18,79,20};//图表的数据点
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
            initSettings();
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
                //把setting保存到本地
                Gson gson = new Gson();
                String jsonStr = gson.toJson(set);
                try {
                    SecuritySP.EncryptSP(getContext(),"setting",jsonStr);
                    MainActivity mainActivity = (MainActivity)getActivity();
                    assert mainActivity != null;
                    mainActivity.iniTodayWords();
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
                //获取单词本的单词总数
                String tag = setting.getData().getTag();
                if(!tag.equals(""))//tag不为空
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
                                            int d = (int)(num/(setting.getData().getList()*setting.getData().getNumOfList()));
                                            if(d>=200) day.setMaxValue(d);
                                            day.setValue(d);
                                        }
                                        else Toast.makeText(getContext(), "getNumOfBook:state:" + state, Toast.LENGTH_SHORT).show();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
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
                day.setValue((int)(num/(list.getValue()*nl.getValue())));
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
                day.setValue((int)(num/(list.getValue()*nl.getValue())));
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
                            if (state!=200) Toast.makeText(getContext(), "保存失败 State："+state, Toast.LENGTH_SHORT).show();
                            else
                            {
                                //一个小技巧，把时间改为不同步，再刷新MainActivity，达到刷新单词列表的目的
                                Calendar calendar = Calendar.getInstance();
                                int dayOfWord = calendar.get(Calendar.DAY_OF_MONTH);
                                SecuritySP.EncryptSP(getContext(),"day",String.valueOf(dayOfWord-1));
                                Toast.makeText(getContext(), "提交成功", Toast.LENGTH_SHORT).show();
                                //重新iniSetting和iniTodayWords
                                initSettings();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
    }






    //Detach时注销广播
    @Override
    public void onDetach() {
        super.onDetach();
        broadcastManager.unregisterReceiver(mAdDownLoadReceiver);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(FirstViewModel.class);
        // TODO: Use the ViewModel
        lineChart = binding.chart;
        getAxisXLables();//获取x轴的标注
        getAxisPoints();//获取坐标点
        initLineChart();
        /**
         * ********************************************
         */


    }
    private void initLineChart(){

            Line line = new Line(mPointValues).setColor(Color.parseColor("#FFCD41"));  //折线的颜色（橙色）
            List<Line> lines = new ArrayList<Line>();
            line.setShape(ValueShape.DIAMOND);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
            line.setCubic(false);//曲线是否平滑，即是曲线还是折线
            line.setFilled(false);//是否填充曲线的面积
            line.setHasLabels(true);//曲线的数据坐标是否加上备注
//      line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
            line.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
            line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
            lines.add(line);
            LineChartData data = new LineChartData();
            data.setLines(lines);
            //坐标轴
            Axis axisX = new Axis(); //X轴
            axisX.setHasTiltedLabels(false);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
            axisX.setTextColor(Color.BLUE);  //设置字体颜色
            //axisX.setName("date");  //x轴名称
            axisX.setTextSize(10);//设置字体大小
            axisX.setMaxLabelChars(8); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
            axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
            data.setAxisXBottom(axisX); //x 轴在底部
            //data.setAxisXTop(axisX);  //x 轴在顶部
            axisX.setHasLines(true); //x 轴分割线
            // Y轴是根据数据的大小自动设置Y轴上限(在下面我会给出固定Y轴数据个数的解决方案)
            Axis axisY = new Axis();  //Y轴
            axisY.setName("test");//y轴标注
            axisY.setTextSize(10);//设置字体大小
            data.setAxisYLeft(axisY);  //Y轴设置在左边
            //data.setAxisYRight(axisY);  //y轴设置在右边
            //设置行为属性，支持缩放、滑动以及平移
            lineChart.setInteractive(true);
            lineChart.setZoomType(ZoomType.VERTICAL);
            lineChart.setMaxZoom((float) 2);//最大方法比例
            lineChart.setContainerScrollEnabled(false, ContainerScrollType.HORIZONTAL);
            lineChart.setLineChartData(data);
            lineChart.setVisibility(View.VISIBLE);
            /**注：下面的7，10只是代表一个数字去类比而已
             * 当时是为了解决X轴固定数据个数。见（http://forum.xda-developers.com/tools/programming/library-hellocharts-charting-library-t2904456/page2）;
             */
            Viewport v = new Viewport(lineChart.getMaximumViewport());
            v.left = 0;
            v.right= 7;
            lineChart.setCurrentViewport(v);
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