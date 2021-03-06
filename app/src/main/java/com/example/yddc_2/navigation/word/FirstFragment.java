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
    public static int num = 0;//??????????????????
    public static int numOfDay = 0;//????????????????????????numOfDay = list x nol
    public static Setting set;
    private LineChartView lineChart;
    String[] date = {"0-2","2-4","4-6","6-8","8-10","10-12","12-14","14-16","16-18","18-20","20-22","22-24"};//X????????????
    int[] score = new int[12];//??????????????????
    private List<PointValue> mPointValues = new ArrayList<>();
    private List<AxisValue> mAxisXValues = new ArrayList<>();
    private LocalBroadcastManager broadcastManager;//???????????????????????????

    public static FirstFragment newInstance() {
        return new FirstFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FirstFragmentBinding.inflate(inflater);
        //????????????
        registerReceiver();
        try {
            //???????????????
            initSettings();
            //initView();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return binding.getRoot();
    }

    //???????????????????????????
    private final BroadcastReceiver mAdDownLoadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String change = intent.getStringExtra("change");
            if ("yes".equals(change)) {
                // ????????????????????????????????????UI,??????????????????????????????Handler?????????
                new Handler().post(new Runnable() {
                    public void run() {
                        //???????????????????????????????????????
                        try {
                            initSettings();//???ThirdFragment????????????????????????
                            //Log.d("FirstFragment", "????????????");
                        } catch (GeneralSecurityException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    };
    //?????????????????????
    private void registerReceiver() {
        broadcastManager = LocalBroadcastManager.getInstance(requireActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("initSetting");//??????????????????????????????
        broadcastManager.registerReceiver(mAdDownLoadReceiver, intentFilter);
    }
    //Detach???????????????
    @Override
    public void onDetach() {
        super.onDetach();
        broadcastManager.unregisterReceiver(mAdDownLoadReceiver);
    }

    //FirstFragment???initSetting?????????????????????iniTodayWords()
    private void initSettings() throws GeneralSecurityException, IOException {
        com.shawnlin.numberpicker.NumberPicker list = binding.list;
        com.shawnlin.numberpicker.NumberPicker nl = binding.nl;
        com.shawnlin.numberpicker.NumberPicker day = binding.day;
        list.setMaxValue(20);
        nl.setMinValue(5);//????????????4
        nl.setMaxValue(20);
        day.setMaxValue(200);//???????????????200?????????
        FirstViewModel viewModel = new ViewModelProvider(this).get(FirstViewModel.class);
        viewModel.getmSetting(getContext()).observe(getViewLifecycleOwner(), new Observer<Setting>() {
            @Override
            public void onChanged(Setting setting) {
                set = setting;
                String tag = setting.getData().getTag();//?????????tag
                Gson gson = new Gson();
                String jsonStr = gson.toJson(set);
                try {
                    //???setting???????????????
                    SecuritySP.EncryptSP(getContext(),"setting",jsonStr);
                    //??????ThirdFragment???reciteWay?????????????????????????????????
                    String[] res_ = getResources().getStringArray(R.array.recite_way);
                    String value = SecuritySP.DecryptSP(getContext(),"reciteWay");
                    MainActivity mainActivity = (MainActivity)getActivity();
                    assert mainActivity != null;
                    if(value.equals("")){//??????????????????????????????????????????????????????????????????????????????
                        value = res_[0];
                        SecuritySP.EncryptSP(getContext(),"reciteWay",value);//???????????????
                    }
                    if (value.equals(res_[0]))//????????????
                    {

                        mainActivity.iniTodayWords();
                        //Log.d("FirstFragment", "here");
                    }
                    else if (value.equals(res_[1])){//????????????
                        mainActivity.iniMyWords();;
                        //Log.d("FirstFragment", "there");
                    }

                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
                //??????????????????????????????
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
                                        //??????"????????????"?????????
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTime(new Date());
                                        calendar.add(Calendar.DATE,d);
                                        binding.overtime.setText(DateUtil.transForDateNYR(calendar.getTime().getTime()));
                                        //??????bottom??????????????????tag??????
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
        //?????????
        list.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                binding.check.setVisibility(View.VISIBLE);
                //?????????????????????100????????????????????????100???
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
                //?????????????????????100????????????????????????100???
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
        //????????????
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
            //????????????????????????????????????
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
//                                //????????????
//                                Calendar calendar = Calendar.getInstance();
//                                int dayOfWord = calendar.get(Calendar.DAY_OF_MONTH);
//                                SecuritySP.EncryptSP(getContext(),"day",String.valueOf(dayOfWord-1));
                                Toast.makeText(getContext(), "????????????", Toast.LENGTH_SHORT).show();
                                //??????iniSetting???iniTodayWords
                                initSettings();
                            }
                            else
                            {
                                Toast.makeText(getContext(), "???????????? State???"+state, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    private void initView() throws GeneralSecurityException, IOException {
        //??????????????????
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
                            wordNum.setText(String.valueOf(dayPlan.getData().getSumWord())+"???");
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

        //??????????????????
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
                    binding.avNum.setText(dataDTO.getAverageWords()+"???");
                    binding.avTime.setText(DateUtil.TransSecondsToHMS(dataDTO.getAverageTime()));
                    //??????????????????????????????0??????????????????????????????????????????????????????
                    if(dataDTO.getAverageWords()==0&&dataDTO.getAverageTime()==0)
                    {
                        binding.llData1.setVisibility(View.GONE);
                    }
                    else binding.llData1.setVisibility(View.VISIBLE);
                    float eff = ((float)dataDTO.getAverageWords())/3600;//_???/s
                    if (eff!=0){
                        float daytime = ((float)numOfDay)/eff;//_s
                        binding.daytime.setText("????????????"+DateUtil.TransSecondsToHMS(Math.round(daytime)));
                        //binding.daytime.setText("????????????"+numOfDay);
                    }
                    else {
                        //eff???0????????????????????????
                        binding.daytime.setText("");
                    }

                    score = new int[]{dataDTO.getA(), dataDTO.getB(), dataDTO.getC(), dataDTO.getD(),
                            dataDTO.getE(), dataDTO.getF(), dataDTO.getG(), dataDTO.getH(),
                            dataDTO.getI(), dataDTO.getJ(), dataDTO.getK(), dataDTO.getL()};
                    //??????????????????????????????
                    int index = 0;
                    int max = 0;
                    for (int i=0;i<score.length;i++)
                    {
                        if (max<score[i]) {
                            max = score[i];
                            index = i;
                        }
                    }
                    //???????????????????????????
                    String s1 = date[index];
                    String[] str = s1.split("-");
                    String s2 = str[0]+":00~"+str[1]+":00";
                    binding.tv1.setText("??????????????????????????????????????????"+s2+"\n????????????????????????:");
                    lineChart = binding.chart;
                    mPointValues.clear();
                    mAxisXValues.clear();
                    getAxisXLables();//??????x????????????
                    getAxisPoints();//???????????????
                    initLineChart();
                }

            }
        });

        //?????????????????????????????????????????????
        View.OnClickListener listener1 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //??????????????????
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

                                    tv_title.setText("????????????");
                                    //?????????RecyclerView
                                    RecyclerView recyclerView = bottomSheetDialog.findViewById(R.id.listView);
                                    assert recyclerView != null;
                                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                    recyclerView.setAdapter(new RecyclerView_wordList_Adapter(R.layout.list_item,getContext(),wordList,0));


                                }
                                else
                                {
                                    tv_title.setText("??????????????????~");
                                }
                                bottomSheetDialog.show();
                            }
                        });
            }
        };
        View.OnClickListener listener2 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //??????????????????
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

                                    tv_title.setText("????????????");
                                    tv_title.setTextColor(getResources().getColor(R.color.item));
                                    //?????????RecyclerView
                                    RecyclerView recyclerView = bottomSheetDialog.findViewById(R.id.listView);
                                    assert recyclerView != null;
                                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                    recyclerView.setAdapter(new RecyclerView_wordList_Adapter(R.layout.list_item,getContext(),wordList,1));

                                }
                                else {
                                    tv_title.setText("??????????????????~");
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
            Line line = new Line(mPointValues).setColor(Color.parseColor("#FFCD41"));  //???????????????????????????
            List<Line> lines = new ArrayList<Line>();
            line.setShape(ValueShape.CIRCLE);//????????????????????????????????????  ??????????????? ???????????? ???ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND???
            line.setCubic(true);//?????????????????????????????????????????????
            line.setFilled(true);//???????????????????????????
//            line.setHasLabels(true);//???????????????????????????????????????
      line.setHasLabelsOnlyForSelected(true);//????????????????????????????????????????????????line.setHasLabels(true);????????????
            line.setHasLines(true);//??????????????????????????????false ??????????????????????????????
            line.setHasPoints(true);//?????????????????? ?????????false ?????????????????????????????????????????????????????????????????????
            lines.add(line);
            LineChartData data = new LineChartData();

            data.setLines(lines);
            //?????????
            Axis axisX = new Axis(); //X???
            axisX.setName("????????????");
            axisX.setAutoGenerated(true);
            axisX.setHasSeparationLine(true);
            axisX.setHasTiltedLabels(false);  //X?????????????????????????????????????????????true???????????????
            axisX.setTextColor(Color.GRAY);  //??????????????????
            axisX.setTextSize(10);//??????????????????
            axisX.setMaxLabelChars(12); //????????????X???????????????????????????????????????X?????????????????????
            axisX.setValues(mAxisXValues);  //??????X??????????????????
            data.setAxisXBottom(axisX); //x ????????????
            axisX.setHasLines(true); //x ????????????
            Axis axisY = new Axis();  //Y???
            axisY.setName("????????????");//y?????????
            axisY.setTextSize(10);//??????????????????
        axisY.setTextColor(Color.GRAY);
            data.setAxisYLeft(axisY);  //Y??????????????????
        data.setValueLabelBackgroundEnabled(true);
//            ??????????????????????????????????????????????????????
            lineChart.setInteractive(true);
            lineChart.setZoomType(ZoomType.VERTICAL);
            lineChart.setMaxZoom((float) 2);//??????????????????
            lineChart.setContainerScrollEnabled(false, ContainerScrollType.VERTICAL);
            lineChart.setLineChartData(data);
            lineChart.setVisibility(View.VISIBLE);
        }
    /**
     * ??????X ????????????
     */
    private void getAxisXLables(){
        for (int i = 0; i < date.length; i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(date[i]));
        }
    }
    /**
     * ???????????????????????????
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