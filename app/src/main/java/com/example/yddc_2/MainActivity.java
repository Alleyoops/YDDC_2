package com.example.yddc_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yddc_2.adapter.ViewPagerAdapter;
import com.example.yddc_2.bean.RequestData;
import com.example.yddc_2.bean.Setting;
import com.example.yddc_2.bean.WordList;
import com.example.yddc_2.navigation.find.SecondFragment;
import com.example.yddc_2.navigation.me.ThirdFragment;
import com.example.yddc_2.navigation.word.FirstFragment;
import com.example.yddc_2.utils.GetNetService;
import com.example.yddc_2.utils.GetRandomNum;
import com.example.yddc_2.utils.HideBar;
import com.example.yddc_2.utils.PressAnimUtil;
import com.example.yddc_2.utils.SecuritySP;
import com.example.yddc_2.viewmodels.MainViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements  ActivityCompat.OnRequestPermissionsResultCallback {
    private int pos = 0;//记录哪一页
    private int flag = 0;//bottomSheet展开为1
    //以下三个list要理清关系
    public static List<WordList.DataDTO> totalList;//比如通过链接获取到的今天的50个单词，每次背诵完后需要提交数据（更新totalList中单词的tag值）（此外totalList只是在用户注册后设置每日任务单词数前默认是50个）
    public static List<WordList.DataDTO> tempTotalList;//tempTotalList的初始值是totalList中tag为0的所有单词，每生成一个recycleList，它就减去10个，recycleList每更新一个，它就减去一个
    public static List<WordList.DataDTO> recycleList;//大小为10,保存每次背诵时循环的十个单词
    //保存一个list中单词个数的随机数的数组(nol)
    public static Integer[] someOfTotal = new Integer[100];//max50
    //定义一个保存绿星和红星个数的数组
    public static int[] greStar = new int[100];//max100
    public static int[] redStar = new int[100];//max100
    //计时器
    public Chronometer tick;
    public static int time = 0;//秒数
    public static long tempTime = 0L;
    //提交数据
    public static RequestData data = new RequestData();
    public static List<RequestData.WordRecordDTO> wrdList = new ArrayList<>();
    public static List<RequestData.TimeRecordDTO> trdList = new ArrayList<>();
    //扫码
    public static final int CAMERA_REQ_CODE = 111;
    public static final int DECODE = 1;
    public static final int GENERATE = 2;
    private static final int REQUEST_CODE_SCAN_ONE = 0X01;
    public static final String RESULT = "SCAN_RESULT";
    //扫码，申请权限
    public void loadScanKitBtnClick(View view) {
        requestPermission();
    }
    //扫码申请权限
    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                MainActivity.CAMERA_REQ_CODE);
    }
    //扫码申请权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length < 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (requestCode == CAMERA_REQ_CODE) {
            ScanUtil.startScan(this, REQUEST_CODE_SCAN_ONE, new HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE).create());
        }
    }
    //扫码结果回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            return;
        }
//        if (requestCode == REQUEST_CODE_SCAN_ONE) {//打开网页
//            HmsScan obj = data.getParcelableExtra(ScanUtil.RESULT);
//            Uri uri = Uri.parse(obj.showResult);
//            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            startActivity(intent);
//        }
        if (requestCode == REQUEST_CODE_SCAN_ONE) {
            HmsScan obj = data.getParcelableExtra(ScanUtil.RESULT);
            if (obj != null) {
                Toast.makeText(this, obj.originalValue, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        HideBar.hideBar(this);//暂时不理想
    }

    // 按返回键不销毁当前Activity
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView() {
        tick = (Chronometer)findViewById(R.id.tick);
        BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_layout));
        List<Fragment> FragmentList = new ArrayList<>();
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        //向viewpager添加各个fragment页面
        FragmentList.add((new FirstFragment()));
        FragmentList.add((new SecondFragment()));
        FragmentList.add((new ThirdFragment()));
        //适配器
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), this, FragmentList);
        viewPager.setAdapter(adapter);viewPager.setOffscreenPageLimit(2);
        //导航栏点击事件和ViewPager滑动事件,让两个控件相互关联
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //这里设置为：当点击到某子项时，viewpager就滑动到对应位置
                switch (item.getItemId()) {
                    case R.id.word:
                        pos = 0;
                        viewPager.setCurrentItem(0, false);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        return true;
                    case R.id.find:
                        pos = 1;
                        viewPager.setCurrentItem(1, false);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        return true;
                    case R.id.me:
                        pos = 2;
                        viewPager.setCurrentItem(2, false);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        return true;
                    default:
                        break;
                }
                return false;
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                final float MIN_SCALE = 0.85f;
//                final float MIN_ALPHA = 0.5f;
//                int width = viewPager.getWidth();
                 final float MAX_ALPHA=0.5f;
                 final float MAX_SCALE=0.9f;
                viewPager.setPageTransformer(true, new ViewPager.PageTransformer() {
                    @Override
                    public void transformPage(@NonNull @NotNull View page, float position) {
//                        if (position<-1) {
//                        }else if(position<=0){
//                            page.setTranslationX(0);
//                            page.setAlpha(1);
//                            page.setScaleX(1 );
//                            page.setScaleY(1 );
//                            //page.setRotationY((1+position)*360);
//                        }else if(position<=1){
//                            float scaleFactor = MIN_SCALE
//                                    + (1 - MIN_SCALE) * (1 - Math.abs(position));
//                            page.setTranslationX(width*-position);
//                            page.setAlpha(1 - position);
//                            page.setScaleX(scaleFactor);
//                            page.setScaleY(scaleFactor);
//                            //page.setRotationY((1-position)*360);
//                        }

                        if(position<-1||position>1){
                            //不可见区域
                            page.setAlpha(MAX_ALPHA);
                            page.setScaleX(MAX_SCALE);
                            page.setScaleY(MAX_SCALE);
                        }else {
                            //可见区域，透明度效果
                            if(position<=0){
                                //pos区域[-1,0)
                                page.setAlpha(MAX_ALPHA+MAX_ALPHA*(1+position));
                            }else{
                                //pos区域[0,1]
                                page.setAlpha(MAX_ALPHA+MAX_ALPHA*(1-position));
                            }
                            //可见区域，缩放效果
                            //float scale=Math.max(MAX_SCALE,1-Math.abs(position));
                            float x = Math.abs(position)/10;
                            page.setScaleX(1-1.5f*x);
                            page.setScaleY(1-1.5f*x);
                        }

                    }
                });
            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                switch (position) {
                    case 0:
                        pos = 0;
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        //不知道为什么，必须要延迟执行，不然会有很丑的画面。。
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                bottomSheetBehavior.setHideable(false);
                            }
                        },100);
                        break;
                    case 1:
                        pos = 1;bottomSheetBehavior.setHideable(true);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        break;
                    case 2:
                        pos = 2;
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                bottomSheetBehavior.setHideable(false);
                            }
                        },100);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull @NotNull View bottomSheet, int newState) {
                int temp0 = Integer.parseInt(tick.getText().toString().split(":")[0]);//分
                int temp1 =Integer.parseInt(tick.getText().toString().split(":")[1]);//秒
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        //开始计时
                        if(flag == 0)//说明下拉框刚拉上来
                        {
                            flag = 1;
                            //开始计时
                            tick.setBase(SystemClock.elapsedRealtime()-(long) tempTime);
                            tick.start();
                        }
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        if(flag==1)//下拉框收起
                        {
                            flag = 0;
                            time=temp0*60+temp1;
                            tempTime = SystemClock.elapsedRealtime()-tick.getBase();
                            tick.stop();
                            try {
                                //提交背诵数据
                                data.setWord_record(wrdList);
                                // 获取当天零点零分零秒的时间戳
                                Calendar curentDay = Calendar.getInstance();
                                curentDay.setTime(new Date());
                                curentDay.set(Calendar.HOUR_OF_DAY, 0);
                                curentDay.set(Calendar.MINUTE, 0);
                                String day = String.valueOf(curentDay.getTime().getTime());
                                trdList.add(new RequestData.TimeRecordDTO(day,time));
                                data.setTime_record(trdList);
                                summit(data);
                                //提交后清空缓存
                                trdList.clear();
                                wrdList.clear();
                            } catch (GeneralSecurityException | IOException e) {
                                e.printStackTrace();
                            }
                            //设置手表时间
                            TextView watch = (TextView)findViewById(R.id.watch);
                            watch.setText(tick.getText());
                        }

                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        if (pos == 0)
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        if(flag==1)//跟下拉框收起时的处理一样
                        {
                            flag = 0;
                            //time=temp0*60+temp1;
                            tempTime = SystemClock.elapsedRealtime()-tick.getBase();
                            tick.stop();
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull @NotNull View bottomSheet, float slideOffset) {
                LinearLayout ll_tip = (LinearLayout)findViewById(R.id.ll_tip);
                ll_tip.setAlpha(1-slideOffset);
                if(pos!=1&&slideOffset>0)
                {
                    BottomNavigationView bnv = (BottomNavigationView)findViewById(R.id.bottomNavigationView);
                    bnv.setTranslationY(bnv.getHeight()*slideOffset);
                    bnv.setAlpha(1-slideOffset);
                }

            }
        });
    }

    //保存当天任务的单词到本地缓存和取出本地缓存
    public void iniTodayWords() throws GeneralSecurityException, IOException {
        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        Calendar calendar = Calendar.getInstance();
        String today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        if(!today.equals(SecuritySP.DecryptSP(MainActivity.this,"day")))
        {
            viewModel.getmWordList(this).observe(this, new Observer<WordList>() {
                @Override
                public void onChanged(WordList wordList) {
                    try {
                        //更新日期
                        Calendar calendar = Calendar.getInstance();
                        int dayOfWord = calendar.get(Calendar.DAY_OF_MONTH);
                        SecuritySP.EncryptSP(MainActivity.this,"day",String.valueOf(dayOfWord));
                        Gson gson = new Gson();
                        String jsonStr = gson.toJson(wordList);
                        SecuritySP.EncryptSP(MainActivity.this,"todayWords",jsonStr);
                        Toast.makeText(MainActivity.this, "词库更新", Toast.LENGTH_SHORT).show();
                        //放进一个可以方便增删的list
                        totalList = gson.fromJson(jsonStr,WordList.class).getData();
                        reciteOfWork();
                    } catch (GeneralSecurityException | IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        else
        {
            //从本地取出todayWords
            String jsonStr = SecuritySP.DecryptSP(MainActivity.this,"todayWords");
            Gson gson = new Gson();
            //放进一个可以方便增删的list
            totalList = gson.fromJson(jsonStr,WordList.class).getData();
            reciteOfWork();
            Toast.makeText(this, "读取缓存词库", Toast.LENGTH_SHORT).show();
        }
    }

    //背单词之任务模式（默认模式）
    private void reciteOfWork() throws GeneralSecurityException, IOException {
        //获取setting
        String str = SecuritySP.DecryptSP(MainActivity.this,"setting");
        Gson gson = new Gson();
        Setting setting = gson.fromJson(str,Setting.class);
        int nol = setting.getData().getNumOfList();
        //选取totalList里tag为生词本的单词加入tempTotalList供用户背诵
        tempTotalList = new ArrayList<>();
        for (WordList.DataDTO wd :totalList) {
            //0:生词本 ，1：无需再背 ，2.熟悉单词(已背)，3：三次全错，4:手动添加的收藏。（3是自动添加的收藏，收藏包括已复习和未复习）
            if (wd.getTag()==0) tempTotalList.add(wd);
            //
            //···假如totalList中单词的tag全为0呢（也就是背完了）
            //···那么应该给用户一个提示，还有避免闪退
            //···但是谁会测试到背完50个呢？所以先空着吧
            //
        }
        //从tempTotalList随机取nol个放进一个recycleList，（10个背完再背诵下一个recycleList），不满10个时就往里面依次添加totalList的元素
        someOfTotal = GetRandomNum.getIntegers(nol,tempTotalList.size());
        //
        //···这里当tempTotalList中的数目小于10时还需要写一个判决条件，否则估计会报错、闪退
        //···但是谁会测试到40个以上呢？所以先空着吧
        //
        recycleList = new ArrayList<>();
        for (int i = 0; i < nol; i++) {
            recycleList.add(tempTotalList.get(someOfTotal[i]));
        }
        for (int i = 0; i < nol; i++) {
            tempTotalList.remove(recycleList.get(i));//相当于把total移动十个到recycle
        }
//        Toast.makeText(this, "totalList.size():" + totalList.size(), Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, "recycleList.size():" + recycleList.size(), Toast.LENGTH_SHORT).show();
        reciteProcess();
    }

    //背诵流程
    public void reciteProcess() throws GeneralSecurityException, IOException {
        TextView spell = (TextView) findViewById(R.id.spell);
        TextView voice = (TextView) findViewById(R.id.voice);
        TextView item1 = (TextView) findViewById(R.id.item1);
        TextView item2 = (TextView) findViewById(R.id.item2);
        TextView item3 = (TextView) findViewById(R.id.item3);
        TextView item4 = (TextView) findViewById(R.id.item4);
        Log.d("MainActivity", "totalList.size():" + String.valueOf(totalList.size()));
        //从10个随机取四个
        Integer[] integers = new Integer[0];int rightIndex = 0;
        TextView[] items = new TextView[]{item1,item2,item3,item4};
        if(recycleList.size()>=4)
        {
            integers = GetRandomNum.getIntegers(4,recycleList.size());
            //四个选项随机选取一个作为正确答案
            rightIndex = GetRandomNum.getOneInt(4);
            //四个中的rightIndex作为spell
            spell.setText(recycleList.get(integers[rightIndex]).getWord().getSpell());
            //发音（英式美式等,不全）
            if(recycleList.get(integers[rightIndex]).getWord().getAudio().size()!=0)
                voice.setText(recycleList.get(integers[rightIndex]).getWord().getAudio().get(0).getTagDetail());
            else voice.setText("");
            //选项赋值
            for (int i = 0; i < 4; i++) {
                items[i].setText(recycleList.get(integers[i]).getWord().getClearfix().get(0).getClearfix());
            }
        }
        else {
                if(recycleList.size()==0)
                {
                    spell.setText("今日任务已完成");
                    summit(data);//提交数据
                    for (int i = 0; i < 4; i++) {
                        items[i].setText("");
                        items[i].setVisibility(View.INVISIBLE);
                    }
                    voice.setVisibility(View.INVISIBLE);
                    LinearLayout ll1 = (LinearLayout)findViewById(R.id.ll_1);
                    LinearLayout ll2 = (LinearLayout)findViewById(R.id.ll_2);
                    LinearLayout llStar = (LinearLayout)findViewById(R.id.ll_star);
                    ll1.setVisibility(View.INVISIBLE);
                    ll2.setVisibility(View.INVISIBLE);
                    llStar.setVisibility(View.INVISIBLE);
                    //停止计时
                    flag = 2;//不是1不是0，无论上下滑都不计时了
                    tempTime = SystemClock.elapsedRealtime()-tick.getBase();
                    tick.stop();
                    tick.setBase(SystemClock.elapsedRealtime());
                    tick.setVisibility(View.INVISIBLE);
                    return;
                }
                //recycle小于4的时候
                integers = GetRandomNum.getIntegers(recycleList.size(), recycleList.size());
                //size个选项随机选取一个作为正确答案
                rightIndex = GetRandomNum.getOneInt(recycleList.size());
                //四个中的rightIndex作为spell
                spell.setText(recycleList.get(integers[rightIndex]).getWord().getSpell());
                //发音（英式美式等,不全）
                if (recycleList.get(integers[rightIndex]).getWord().getAudio().size() != 0)
                    voice.setText(recycleList.get(integers[rightIndex]).getWord().getAudio().get(0).getTagDetail());
                else voice.setText("");
                //选项赋值
                for (int i = 0; i < recycleList.size(); i++) {
                    items[i].setText(recycleList.get(integers[i]).getWord().getClearfix().get(0).getClearfix());
                }
                for (int i = 0; i < 4 - recycleList.size(); i++) {
                    items[3 - i].setText("");
                }
        }
        //点之前刷新星星
        refreshStar(greStar[someOfTotal[integers[rightIndex]]],redStar[someOfTotal[integers[rightIndex]]]);
        //点击选项
        int finalRightIndex = rightIndex;
        Integer[] finalIntegers = integers;
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId()==items[finalRightIndex].getId())
                {
                    if(recycleList.size()==0)
                    {
                        spell.setText("今日任务已完成");
                        try {
                            summit(data);//提交数据
                        } catch (GeneralSecurityException | IOException e) {
                            e.printStackTrace();
                        }
                        for (int i = 0; i < 4; i++) {
                            items[i].setText("");
                            items[i].setVisibility(View.INVISIBLE);
                        }
                        LinearLayout ll1 = (LinearLayout)findViewById(R.id.ll_1);
                        LinearLayout ll2 = (LinearLayout)findViewById(R.id.ll_2);
                        LinearLayout llStar = (LinearLayout)findViewById(R.id.ll_star);
                        ll1.setVisibility(View.INVISIBLE);
                        ll2.setVisibility(View.INVISIBLE);
                        llStar.setVisibility(View.INVISIBLE);
                        //停止计时
                        flag = 2;//不是1不是0，无论上下滑都不计时了
                        tempTime = SystemClock.elapsedRealtime()-tick.getBase();
                        tick.stop();
                        tick.setBase(SystemClock.elapsedRealtime());
                        tick.setVisibility(View.INVISIBLE);
                        return;
                    }
                    else
                    {
                        v.setBackgroundResource(R.drawable.shape_9);
                        items[finalRightIndex].setTextColor(getResources().getColor(R.color.teal_200));
                        for (int i = 0; i < 4; i++) {
                            if(finalRightIndex !=i) items[i].setTextColor(0xFFD4D6D8);//灰色
                        }
                        //选对,绿星加一
                        if(greStar[someOfTotal[finalIntegers[finalRightIndex]]]+redStar[someOfTotal[finalIntegers[finalRightIndex]]]<5
                                &&greStar[someOfTotal[finalIntegers[finalRightIndex]]]<3)
                            greStar[someOfTotal[finalIntegers[finalRightIndex]]]+=1;

                    }

                }
                else
                {
                    switch (v.getId())
                    {
                        //收藏和无需再背
                        case R.id.yes:
                            items[finalRightIndex].setTextColor(getResources().getColor(R.color.teal_200));
                            //把totalList的该单词tag设置为1,无需再背
                            WordList.DataDTO ddt = totalList.get(someOfTotal[finalIntegers[finalRightIndex]]);
                            //构建提交数据
                            RequestData.WordRecordDTO wrd=new RequestData.WordRecordDTO(ddt.getId(),1,
                                    ddt.getTimesReview(),ddt.getDifficult(),String.valueOf(new Date().getTime()));
                            wrdList.add(wrd);
                            //已经掌握，就移除
                            if(tempTotalList.size()!=0)
                            {
                                //tempTotalList还有
                                recycleList.set(finalIntegers[finalRightIndex],tempTotalList.get(0));
                                greStar[someOfTotal[finalIntegers[finalRightIndex]]] = 0;redStar[someOfTotal[finalIntegers[finalRightIndex]]] = 0;
                                tempTotalList.remove(tempTotalList.get(0));
                            }
                            else
                            {
                                //tempTotalList已经全被加进Recycle了，就recycle-1
                                recycleList.remove(recycleList.get(finalIntegers[finalRightIndex]));
                                Toast.makeText(MainActivity.this, "recycleList.size():" + recycleList.size(), Toast.LENGTH_SHORT).show();
                            }

                            Toast.makeText(MainActivity.this, "掌握+1", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.no:
                            items[finalRightIndex].setTextColor(getResources().getColor(R.color.teal_200));
                            WordList.DataDTO ddt_ = totalList.get(someOfTotal[finalIntegers[finalRightIndex]]);
                            //构建提交数据
                            RequestData.WordRecordDTO wrd_=new RequestData.WordRecordDTO(ddt_.getId(),4,
                                    ddt_.getTimesReview(),ddt_.getDifficult(),String.valueOf(new Date().getTime()));
                            wrdList.add(wrd_);
                            //已经收藏，就移除
                            if(tempTotalList.size()!=0)
                            {
                                //tempTotalList还有
                                recycleList.set(finalIntegers[finalRightIndex],tempTotalList.get(0));
                                greStar[someOfTotal[finalIntegers[finalRightIndex]]] = 0;redStar[someOfTotal[finalIntegers[finalRightIndex]]] = 0;
                                tempTotalList.remove(tempTotalList.get(0));
                            }
                            else
                            {
                                //tempTotalList已经全被加进Recycle了，就recycle-1
                                recycleList.remove(recycleList.get(finalIntegers[finalRightIndex]));
                                Toast.makeText(MainActivity.this, "recycleList.size():" + recycleList.size(), Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(MainActivity.this, "收藏+1", Toast.LENGTH_SHORT).show();
                            break;
                        //其它三个选项
                        default:
                            if(recycleList.size()==0)
                            {
                                spell.setText("今日任务已完成");
                                try {
                                    summit(data);//提交数据
                                } catch (GeneralSecurityException | IOException e) {
                                    e.printStackTrace();
                                }
                                for (int i = 0; i < 4; i++) {
                                    items[i].setText("");
                                    items[i].setVisibility(View.INVISIBLE);
                                }
                                LinearLayout ll1 = (LinearLayout)findViewById(R.id.ll_1);
                                LinearLayout ll2 = (LinearLayout)findViewById(R.id.ll_2);
                                LinearLayout llStar = (LinearLayout)findViewById(R.id.ll_star);
                                ll1.setVisibility(View.INVISIBLE);
                                ll2.setVisibility(View.INVISIBLE);
                                llStar.setVisibility(View.INVISIBLE);
                                //停止计时
                                flag = 2;//不是1不是0，无论上下滑都不计时了
                                tempTime = SystemClock.elapsedRealtime()-tick.getBase();
                                tick.stop();
                                tick.setBase(SystemClock.elapsedRealtime());
                                tick.setVisibility(View.INVISIBLE);
                                return;
                            }
                            else
                            {
                                v.setBackgroundResource(R.drawable.shape_10);
                                items[finalRightIndex].setBackgroundResource(R.drawable.shape_9);
                                items[finalRightIndex].setTextColor(getResources().getColor(R.color.teal_200));
                                for (int i = 0; i < 4; i++) {
                                    if(finalRightIndex !=i) items[i].setTextColor(0xFFD4D6D8);//灰色
                                }
                                //选错,红星加一
                                if(greStar[someOfTotal[finalIntegers[finalRightIndex]]]+redStar[someOfTotal[finalIntegers[finalRightIndex]]]<5
                                        &&redStar[someOfTotal[finalIntegers[finalRightIndex]]]<3)
                                    redStar[someOfTotal[finalIntegers[finalRightIndex]]]+=1;
                            }

                            break;
                    }



                }
                //刷新星星
                refreshStar(greStar[someOfTotal[finalIntegers[finalRightIndex]]],redStar[someOfTotal[finalIntegers[finalRightIndex]]]);
                //保存星星数组到本地，每日更新
                //···
                //···这里由于星星数据还没写手机和手表同步的接口，代码搁置在这
                //···
                //满三颗星就刷新recycleList,从tempTotalList添加，从tempTotalList减一
                if(greStar[someOfTotal[finalIntegers[finalRightIndex]]]==3)
                {
                    //把totalList的该单词tag设置为2，相当于记住
                    WordList.DataDTO ddt = totalList.get(someOfTotal[finalIntegers[finalRightIndex]]);
                    //构建提交数据
                    RequestData.WordRecordDTO wrd=new RequestData.WordRecordDTO(ddt.getId(),2,
                            ddt.getTimesReview(),ddt.getDifficult(),String.valueOf(new Date().getTime()));
                    wrdList.add(wrd);
                        //从tempTotalList拿一个生词替换recycleList中这个位置的单词，并把这个单词星星数归0
                    if(tempTotalList.size()!=0)
                    {
                        //tempTotalList还有
                        recycleList.set(finalIntegers[finalRightIndex],tempTotalList.get(0));
                        greStar[someOfTotal[finalIntegers[finalRightIndex]]] = 0;redStar[someOfTotal[finalIntegers[finalRightIndex]]] = 0;
                        tempTotalList.remove(tempTotalList.get(0));
                    }
                    else
                    {
                        //tempTotalList已经全被加进Recycle了，就recycle-1
                        recycleList.remove(recycleList.get(finalIntegers[finalRightIndex]));
                        Toast.makeText(MainActivity.this, "recycleList.size():" + recycleList.size(), Toast.LENGTH_SHORT).show();
                    }

                }
                if(redStar[someOfTotal[finalIntegers[finalRightIndex]]]==3)
                {
                    //把totalList的该单词tag设置为3，相当于加入(自动)收藏
                    WordList.DataDTO ddt = totalList.get(someOfTotal[finalIntegers[finalRightIndex]]);
                    //构建提交数据
                    RequestData.WordRecordDTO wrd=new RequestData.WordRecordDTO(ddt.getId(),3,
                            ddt.getTimesReview(),ddt.getDifficult(),String.valueOf(new Date().getTime()));
                    wrdList.add(wrd);
                    //从tempTotalList拿一个生词替换recycleList中这个位置的单词
                    if(tempTotalList.size()!=0)
                    {
                        //tempTotalList还有
                        recycleList.set(finalIntegers[finalRightIndex],tempTotalList.get(0));
                        greStar[someOfTotal[finalIntegers[finalRightIndex]]] = 0;redStar[someOfTotal[finalIntegers[finalRightIndex]]] = 0;
                        tempTotalList.remove(tempTotalList.get(0));
                    }
                    else
                    {
                        //tempTotalList已经全被加进Recycle了，就recycle-1
                        recycleList.remove(recycleList.get(finalIntegers[finalRightIndex]));
                        Toast.makeText(MainActivity.this, "recycleList.size():" + recycleList.size(), Toast.LENGTH_SHORT).show();
                    }
                }

                //点击一秒(供用户查看)后刷新选项,这个期间要禁止点击
                item1.setEnabled(false);item2.setEnabled(false);
                item3.setEnabled(false);item4.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //一秒后刷新选项(注意排除当前选项),递归recite()
                        item1.setEnabled(true);item1.setBackgroundResource(R.drawable.shape_7);//设置回默认背景
                        item2.setEnabled(true);item2.setBackgroundResource(R.drawable.shape_7);
                        item3.setEnabled(true);item3.setBackgroundResource(R.drawable.shape_7);
                        item4.setEnabled(true);item4.setBackgroundResource(R.drawable.shape_7);
                        item1.setTextColor(0xFF717274);item2.setTextColor(0xFF717274);//设置回系统默认颜色
                        item3.setTextColor(0xFF717274);item4.setTextColor(0xFF717274);
                        try {
                            reciteProcess();
                        } catch (GeneralSecurityException | IOException e) {
                            e.printStackTrace();
                        }
                        Log.d("MainActivity", "tempTotalList.size():" + String.valueOf(tempTotalList.size()));
                    }
                },1500);
            }
        };
        //设置监听器
        PressAnimUtil.addScaleAnimition(item1,listener,0.8f);
        PressAnimUtil.addScaleAnimition(item2,listener,0.8f);
        PressAnimUtil.addScaleAnimition(item3,listener,0.8f);
        PressAnimUtil.addScaleAnimition(item4,listener,0.8f);
        PressAnimUtil.addScaleAnimition((ImageView)findViewById(R.id.yes),listener,0.1f);
        PressAnimUtil.addScaleAnimition((ImageView)findViewById(R.id.no),listener,0.1f);
    }

    //显示和刷新星星数目
    private void refreshStar(int greStarNum,int redStarNum)
    {
        ImageView[] ivStars = new ImageView[5];
        ivStars[0] = (ImageView)findViewById(R.id.star1);
        ivStars[1] = (ImageView)findViewById(R.id.star2);
        ivStars[2] = (ImageView)findViewById(R.id.star3);
        ivStars[3] = (ImageView)findViewById(R.id.star4);
        ivStars[4] = (ImageView)findViewById(R.id.star5);
        //设置绿星显示
        for (int i = 0; i < greStarNum; i++) {
            if(greStarNum<=3) ivStars[i].setImageResource(R.drawable.star_gre);
        }
        //设置红星显示
        for (int i = 0; i < redStarNum; i++) {
            if(redStarNum<=3) ivStars[4-i].setImageResource(R.drawable.star_red);
        }
        //设置灰星显示
        for (int i = 0; i < (5-redStarNum-greStarNum); i++) {
            ivStars[greStarNum+i].setImageResource(R.drawable.star_gray);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();//后台暂停计时，后期再加个通知栏显示
        //如果正在背单词才暂停计时,即bottomSheetBehavior为展开状态
        BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_layout));
        if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED) {
            tempTime = SystemClock.elapsedRealtime() - tick.getBase();
            tick.stop();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();//重回app，开始计时
        //如果正在背单词才继续计时,即bottomSheetBehavior为展开状态
        BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_layout));
        if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED)
        {
            tick.setBase(SystemClock.elapsedRealtime()-tempTime);
            tick.start();
        }
    }

    //结束背单词，提交背诵数据
    private void summit(RequestData data) throws GeneralSecurityException, IOException {
        Gson gson = new Gson();
        String jsonStr= gson.toJson(data);
        String token = SecuritySP.DecryptSP(this,"token");
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonStr);
        GetNetService.GetApiService().summitData(token,requestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new rx.Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this, "summit onError", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            JsonObject jsonObject = JsonParser.parseString(responseBody.string()).getAsJsonObject();
                            int state = jsonObject.get("state").getAsInt();
//                            Toast.makeText(MainActivity.this, "state:" + state, Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}