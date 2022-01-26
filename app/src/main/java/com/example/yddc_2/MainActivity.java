package com.example.yddc_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.yddc_2.bean.Star;
import com.example.yddc_2.bean.WordList;
import com.example.yddc_2.myinterface.APIService;
import com.example.yddc_2.navigation.find.SecondFragment;
import com.example.yddc_2.navigation.me.ThirdFragment;
import com.example.yddc_2.navigation.word.FirstFragment;
import com.example.yddc_2.utils.DateUtil;
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

import org.angmarch.views.NiceSpinner;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
//0:生词本，1：无需再背，2.熟悉单词(已背)，3：三次全错，4:手动添加的收藏。（3是自动添加的收藏，收藏包括已复习和未复习）
public class MainActivity extends AppCompatActivity implements  ActivityCompat.OnRequestPermissionsResultCallback {
    private static int pos = 0;//记录哪一页
    private static int flag = 0;//bottomSheet展开为1
    //以下四个list要理清关系
    public static List<WordList.DataDTO> totalList_1;//比如通过链接获取到的今天的50个单词，每次背诵完后需要提交数据（更新totalList中单词的tag值）（此外totalList只是在用户注册后设置每日任务单词数前默认是50个）
    public static List<WordList.DataDTO> tempTotalList;//tempTotalList的初始值是totalList中tag为0的所有单词，每生成一个recycleList，它就减去10个，recycleList每更新一个，它就减去一个
    public static List<WordList.DataDTO> recycleList;//大小为10,保存每次背诵时循环的十个单词
    public static List<WordList.DataDTO> totalList_2;//任务模式的totalList
    //保存一个list中单词个数的随机数的数组(nol)
    public static Integer[] someOfTotal = new Integer[100];//max100
    //用来保存单词所对应的绿星和红星的数目
    public static Map<WordList.DataDTO, Star> map = new HashMap<WordList.DataDTO, Star>();
    //计时器
    public Chronometer tick;
    public static long tempTime = 0L;//退出桌面时再回到桌面继续计时
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
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        //开始计时
                        if(flag == 0)//说明下拉框刚拉上来
                        {
                            flag = 1;
                            //如果是收藏模式且收藏单词列表的数目为0，则不计时且不显示
                            try {
                                String[] res = getResources().getStringArray(R.array.recite_way);
                                String value = SecuritySP.DecryptSP(getApplicationContext(),"reciteWay");
                                if(tempTotalList.size()==0&&value.equals(res[1])){
                                    tick.setVisibility(View.INVISIBLE);
                                }
                                //开始计时
                                else {
                                    tick.setVisibility(View.VISIBLE);
                                    tick.setBase(SystemClock.elapsedRealtime());
                                    tick.start();
                                }
                            } catch (GeneralSecurityException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        if(flag==1)//下拉框收起
                        {
                            flag = 0;
                            //time=temp0*60+temp1;
                            tempTime = SystemClock.elapsedRealtime()-tick.getBase();
                            tick.stop();
                            try {
                                //提交背诵数据
                                data.setWord_record(wrdList);
                                // 获取当天零点零分零秒的时间戳
                                Calendar currentDay = Calendar.getInstance();
                                currentDay.setTime(new Date());
                                currentDay.set(Calendar.HOUR_OF_DAY, 0);
                                currentDay.set(Calendar.MINUTE, 0);
                                currentDay.set(Calendar.SECOND,0);
                                currentDay.set(Calendar.MILLISECOND,0);
                                String day = String.valueOf(currentDay.getTimeInMillis());
                                Log.d("MainActivity", "tempTime:" + tempTime);
                                trdList.add(new RequestData.TimeRecordDTO(day,(int)DateUtil.toSeconds(tick.getText().toString())));
                                data.setTime_record(trdList);
                                commit(data);
                                Log.d("MainActivity", new Gson().toJson(data));
                                //提交后清空缓存
                                trdList.clear();
                                wrdList.clear();
                            } catch (GeneralSecurityException | IOException e) {
                                e.printStackTrace();
                            }
                            //设置手表时间
                            TextView watch = (TextView)findViewById(R.id.watch);
                            watch.setText(tick.getText());
                            tick.setBase(SystemClock.elapsedRealtime());
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
                //显示渐变
                LinearLayout ll_tip = (LinearLayout)findViewById(R.id.ll_tip);
                ll_tip.setAlpha(1-slideOffset);
                tick.setAlpha(slideOffset);
                TextView spell = (TextView)findViewById(R.id.spell);
                spell.setAlpha(slideOffset);
                if(pos!=1&&slideOffset>0)
                {
                    BottomNavigationView bnv = (BottomNavigationView)findViewById(R.id.bottomNavigationView);
                    bnv.setTranslationY(bnv.getHeight()*slideOffset);
                    bnv.setAlpha(1-slideOffset);
                }

            }
        });
    }

    //加载收藏模式单词,tag=3,4
    public void iniMyWords() throws GeneralSecurityException, IOException {
        Log.d("MainActivity", "开始");
        flag = 0;
        //获取tag（此处tag为3和4，即错三次自动收藏的和手动收藏的）
        int tag1 = 1;//三次全错
        int tag2 = 4;//手动收藏
        int[] tags = {tag1,tag2};
        //totalList_2的初始化
        totalList_2 = new ArrayList<>();
        //得到实现相应接口的APIService
        APIService service = GetNetService.GetApiService();
        String token = SecuritySP.DecryptSP(this,"token");
        //map方式:
        Observable.just(tags[0],tags[1])
                .map(new Func1<Integer, WordList.DataDTO>() {
                    @Override
                    public WordList.DataDTO call(Integer integer) {
                        service.getMyWordList(token,integer).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<WordList>() {
                                    @Override
                                    public void call(WordList wordList) {
                                        totalList_2.addAll(wordList.getData());
                                        Log.d("MainActivity", wordList.getData().size()+"||"+new Gson().toJson(wordList));
                                        if (integer==4)//顺序轮到最后一个，说明需要的请求完毕了，可以开始下一步
                                        {
                                            // 判决条件要改
                                            // 默认CET4选择的地方也要改，包括两个Fragment






                                                Log.d("MainActivity", "totalList_2.size():" + totalList_2.size());
                                                //reciteOfWay(totalList_2);
                                        }
                                    }
                                });
                        return null;
                    }
                })
                .subscribe(new Action1<WordList.DataDTO>() {
                    @Override
                    public void call(WordList.DataDTO dataDTO) {
                    }
                });
        //flatmap方式:
//        Observable.just(tags[0],tags[1])
//                .flatMap(new Func1<Integer, Observable<WordList>>() {
//                    @Override
//                    public Observable<WordList> call(Integer integer) {
//                        return service.getMyWordList(token,integer);
//                    }
//                })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Action1<WordList>() {
//                    @Override
//                    public void call(WordList wordList) {
//                        Log.d("MainActivity", new Gson().toJson(wordList));
//                        totalList_2.addAll(wordList.getData());
//                    }
//                });
        }

    //加载任务模式单词,tag=0
    public void iniTodayWords() throws GeneralSecurityException, IOException {
        flag = 0;
        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
//        Calendar calendar = Calendar.getInstance();
//        String today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
//        if(!today.equals(SecuritySP.DecryptSP(MainActivity.this,"day")))
//        {
//            //更新日期(暂时无用好像)
//            int dayOfWord = calendar.get(Calendar.DAY_OF_MONTH);
//            SecuritySP.EncryptSP(MainActivity.this,"day",String.valueOf(dayOfWord));
//        }
        viewModel.getmWordList(this).observe(this, new Observer<WordList>() {
            @Override
            public void onChanged(WordList wordList) {
                try {
                    Toast.makeText(MainActivity.this, "任务模式：词库更新", Toast.LENGTH_SHORT).show();
                    //放进一个可以方便增删的list
                    totalList_1 = wordList.getData();
                    reciteOfWay(totalList_1);
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //背单词模式选择
    private void reciteOfWay(List<WordList.DataDTO> TotalList) throws GeneralSecurityException, IOException {
        //获取setting
        String str = SecuritySP.DecryptSP(MainActivity.this,"setting");
        Gson gson = new Gson();
        Setting setting = gson.fromJson(str,Setting.class);
        int nol = setting.getData().getNumOfList();
        //把totalList_1或totalList_1赋值给tempTotalList全局变量
        tempTotalList = TotalList;

        //如果单词list数目不够（即收藏单词数为0），则给出提示
        if (tempTotalList.size()==0)
        {
            //设置invisible
            TextView voice = (TextView) findViewById(R.id.voice);
            TextView item1 = (TextView) findViewById(R.id.item1);
            TextView item2 = (TextView) findViewById(R.id.item2);
            TextView item3 = (TextView) findViewById(R.id.item3);
            TextView item4 = (TextView) findViewById(R.id.item4);
            TextView[] items = new TextView[]{item1,item2,item3,item4};
            LinearLayout ll1 = (LinearLayout)findViewById(R.id.ll_1);
            LinearLayout ll2 = (LinearLayout)findViewById(R.id.ll_2);
            LinearLayout llStar = (LinearLayout)findViewById(R.id.ll_star);
            ll1.setVisibility(View.INVISIBLE);
            ll2.setVisibility(View.INVISIBLE);
            llStar.setVisibility(View.INVISIBLE);
            for (int i = 0; i < 4; i++) {
                items[i].setText("");
                items[i].setVisibility(View.INVISIBLE);
            }
            voice.setVisibility(View.INVISIBLE);
            //停止计时
            flag = 2;//不是1不是0，无论上下滑都不计时了
            tempTime = SystemClock.elapsedRealtime()-tick.getBase();
            tick.stop();
            tick.setVisibility(View.INVISIBLE);
            //设置提示操作
            TextView spell = (TextView) findViewById(R.id.spell);
            spell.setText("收藏单词列表为空");
            TextView continue_go = (TextView)findViewById(R.id.btn);
            continue_go.setText("切换任务模式");
            continue_go.setVisibility(View.VISIBLE);
            PressAnimUtil.addScaleAnimition(continue_go, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        //加载任务模式单词库
                        iniTodayWords();
                        //ThirdFragment处切换为任务模式
                        String[] res = getResources().getStringArray(R.array.recite_way);
                        NiceSpinner spReciteWay = (NiceSpinner)findViewById(R.id.Sp_reciteWay);
                        spReciteWay.setSelectedIndex(0);
                        SecuritySP.EncryptSP(getApplicationContext(),"reciteWay",res[0]);
                        //设置visible显示
                        for (int i = 0; i < 4; i++) {
                            items[i].setText("");
                            items[i].setVisibility(View.VISIBLE);
                        }
                        voice.setVisibility(View.VISIBLE);
                        ll1.setVisibility(View.VISIBLE);
                        ll2.setVisibility(View.VISIBLE);
                        llStar.setVisibility(View.VISIBLE);
                        continue_go.setVisibility(View.INVISIBLE);
                        tick.setVisibility(View.VISIBLE);
                        //开始计时
                        flag = 1;
                        tick.setBase(SystemClock.elapsedRealtime());
                        tick.start();
                    } catch (GeneralSecurityException | IOException e) {
                        e.printStackTrace();
                    }
                }
            },0.8f);
        }
        else {
            //从tempTotalList随机取nol个放进一个recycleList，（10个背完再背诵下一个recycleList），不满10个时就往里面依次添加totalList的元素
            someOfTotal = GetRandomNum.getIntegers(nol,tempTotalList.size());
            recycleList = new ArrayList<>();
            for (int i = 0; i < nol; i++) {
                recycleList.add(tempTotalList.get(someOfTotal[i]));
                //添加的同时初始化该单词所对应的星星数，并置为0，Key代表单词，value是储存星星数目的类
                map.put(tempTotalList.get(someOfTotal[i]),new Star(0,0));
            }
            for (int i = 0; i < nol; i++) {
                tempTotalList.remove(recycleList.get(i));//相当于把total移动十个到recycle
            }
            reciteProcess();
        }
    }

    //背诵流程
    public void reciteProcess() throws GeneralSecurityException, IOException {
        TextView spell = (TextView) findViewById(R.id.spell);
        TextView voice = (TextView) findViewById(R.id.voice);
        TextView item1 = (TextView) findViewById(R.id.item1);
        TextView item2 = (TextView) findViewById(R.id.item2);
        TextView item3 = (TextView) findViewById(R.id.item3);
        TextView item4 = (TextView) findViewById(R.id.item4);
        TextView[] items = new TextView[]{item1,item2,item3,item4};
        LinearLayout ll1 = (LinearLayout)findViewById(R.id.ll_1);
        LinearLayout ll2 = (LinearLayout)findViewById(R.id.ll_2);
        LinearLayout llStar = (LinearLayout)findViewById(R.id.ll_star);
        TextView continue_go = (TextView)findViewById(R.id.btn);
        //假设是刚背完一轮，很多组件已经是invisible，所以需要先设置visible
        voice.setVisibility(View.VISIBLE);
        tick.setVisibility(View.VISIBLE);
        for (int i = 0; i < 4; i++) {
            items[i].setVisibility(View.VISIBLE);
        }
        ll2.setVisibility(View.VISIBLE);
        ll1.setVisibility(View.VISIBLE);
        llStar.setVisibility(View.VISIBLE);
        continue_go.setVisibility(View.INVISIBLE);

        //从10个随机取四个
        Integer[] integers = new Integer[0];int rightIndex = 0;

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
                //recycle等于0的时候，即背诵任务完成
                if(recycleList.size()==0)
                {
                    spell.setText("今日任务已完成");
                    continue_go.setText("继续挑战");
                    continue_go.setVisibility(View.VISIBLE);

                    //提交背诵数据
                    data.setWord_record(wrdList);
                    // 获取当天零点零分零秒的时间戳
                    Calendar currentDay = Calendar.getInstance();
                    currentDay.setTime(new Date());
                    currentDay.set(Calendar.HOUR_OF_DAY, 0);
                    currentDay.set(Calendar.MINUTE, 0);
                    currentDay.set(Calendar.SECOND,0);
                    currentDay.set(Calendar.MILLISECOND,0);
                    String day = String.valueOf(currentDay.getTimeInMillis());
                    trdList.add(new RequestData.TimeRecordDTO(day,DateUtil.toSeconds(tick.getText().toString())));
                    data.setTime_record(trdList);
                    tempTime = SystemClock.elapsedRealtime()-tick.getBase();
                    commit(data);
                    //提交后清空缓存
                    trdList.clear();
                    wrdList.clear();

                    for (int i = 0; i < 4; i++) {
                        items[i].setText("");
                        items[i].setVisibility(View.INVISIBLE);
                    }
                    voice.setVisibility(View.INVISIBLE);
                    ll1.setVisibility(View.INVISIBLE);
                    ll2.setVisibility(View.INVISIBLE);
                    llStar.setVisibility(View.INVISIBLE);
                    //停止计时
                    flag = 2;//不是1不是0，无论上下滑都不计时了
                    tempTime = SystemClock.elapsedRealtime()-tick.getBase();
                    tick.stop();
                    tick.setVisibility(View.INVISIBLE);
                    //设置手表时间
                    TextView watch = (TextView)findViewById(R.id.watch);
                    watch.setText(tick.getText());

                    PressAnimUtil.addScaleAnimition(continue_go, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                iniTodayWords();
                                for (int i = 0; i < 4; i++) {
                                    items[i].setText("");
                                    items[i].setVisibility(View.VISIBLE);
                                }
                                voice.setVisibility(View.VISIBLE);
                                ll1.setVisibility(View.VISIBLE);
                                ll2.setVisibility(View.VISIBLE);
                                llStar.setVisibility(View.VISIBLE);
                                tick.setVisibility(View.VISIBLE);
                                //开始计时
                                flag = 1;
                                tick.setBase(SystemClock.elapsedRealtime());
                                tick.start();
                                continue_go.setVisibility(View.INVISIBLE);
                            } catch (GeneralSecurityException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    },0.8f);
                    return;
                }
                //recycle小于4的时候
                integers = GetRandomNum.getIntegers(recycleList.size(), recycleList.size());
                //size个选项随机选取一个作为正确答案
                rightIndex = GetRandomNum.getOneInt(recycleList.size());
                //size个中的rightIndex作为spell
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
                    items[3 - i].setEnabled(false);
                }
        }
        //点击选项之前刷新星星，显示新的单词选项所对应的星星数目
        refreshStar(Objects.requireNonNull(map.get(recycleList.get(integers[rightIndex]))).getGre(),
                Objects.requireNonNull(map.get(recycleList.get(integers[rightIndex]))).getRed());
        //点击选项
        int finalRightIndex = rightIndex;
        Integer[] finalIntegers = integers;
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(v.getId()==items[finalRightIndex].getId())
                {
                        v.setBackgroundResource(R.drawable.shape_9);
                        items[finalRightIndex].setTextColor(getResources().getColor(R.color.teal_200));
                        for (int i = 0; i < 4; i++) {
                            if(finalRightIndex !=i) items[i].setTextColor(0xFFD4D6D8);//灰色
                        }
                        //选对,绿星加一
                        if(Objects.requireNonNull(map.get(recycleList.get(finalIntegers[finalRightIndex]))).getGre()<3)
                        {
                            Objects.requireNonNull(map.get(recycleList.get(finalIntegers[finalRightIndex])))
                                    .setGre(Objects.requireNonNull(map.get(recycleList.get(finalIntegers[finalRightIndex]))).getGre()+1);
                            //刷新星星
                            refreshStar(Objects.requireNonNull(map.get(recycleList.get(finalIntegers[finalRightIndex]))).getGre(),
                                    Objects.requireNonNull(map.get(recycleList.get(finalIntegers[finalRightIndex]))).getRed());
                        }
                    //满三颗星就刷新recycleList,从tempTotalList添加，从tempTotalList减一
                    if(Objects.requireNonNull(map.get(recycleList.get(finalIntegers[finalRightIndex]))).getGre()==3)
                    {
                        //把该单词tag设置为2，相当于记住
                        WordList.DataDTO ddt = recycleList.get(finalIntegers[finalRightIndex]);
                        //构建提交数据
                        //先根据
                        RequestData.WordRecordDTO wrd=new RequestData.WordRecordDTO(ddt.getId(),2,
                                ddt.getTimesReview(),ddt.getDifficult(),String.valueOf(new Date().getTime()));
                        wrdList.add(wrd);
                        //从tempTotalList拿一个生词替换recycleList中这个位置的单词，并把这个单词星星数归0
                        if(tempTotalList.size()!=0)
                        {
                            //tempTotalList里还有单词
                            recycleList.set(finalIntegers[finalRightIndex],tempTotalList.get(0));
                            map.put(tempTotalList.get(0),new Star(0,0));//
                            tempTotalList.remove(tempTotalList.get(0));
                        }
                        else
                        {
                            //tempTotalList已经全被加进Recycle了，就recycle-1
                            recycleList.remove(recycleList.get(finalIntegers[finalRightIndex]));
                        }

                    }
                }
                else
                {
                    switch (v.getId())
                    {
                        //无需再背
                        case R.id.yes:
                            items[finalRightIndex].setTextColor(getResources().getColor(R.color.teal_200));
                            //把该单词tag设置为1,无需再背
                            WordList.DataDTO ddt = recycleList.get(finalIntegers[finalRightIndex]);
                            //构建提交数据
                            RequestData.WordRecordDTO wrd=new RequestData.WordRecordDTO(ddt.getId(),1,
                                    ddt.getTimesReview(),ddt.getDifficult(),String.valueOf(new Date().getTime()));
                            wrdList.add(wrd);
                            //已经掌握，就移除
                            if(tempTotalList.size()!=0)
                            {
                                //tempTotalList里还有单词
                                recycleList.set(finalIntegers[finalRightIndex],tempTotalList.get(0));
                                map.put(tempTotalList.get(0),new Star(0,0));//
                                tempTotalList.remove(tempTotalList.get(0));
                            }
                            else
                            {
                                //tempTotalList已经全被加进Recycle了，就recycle-1
                                recycleList.remove(recycleList.get(finalIntegers[finalRightIndex]));
                            }
                            //把星星全部显示绿色，起到一个提示操作的作用
                            refreshStar(5,0);
                            break;
                        //收藏
                        case R.id.no:
                            items[finalRightIndex].setTextColor(getResources().getColor(R.color.teal_200));
                            WordList.DataDTO ddt_ = recycleList.get(finalIntegers[finalRightIndex]);
                            //构建提交数据
                            RequestData.WordRecordDTO wrd_=new RequestData.WordRecordDTO(ddt_.getId(),4,
                                    ddt_.getTimesReview(),ddt_.getDifficult(),String.valueOf(new Date().getTime()));
                            wrdList.add(wrd_);
                            //已经收藏，就移除
                            if(tempTotalList.size()!=0)
                            {
                                //tempTotalList还有
                                recycleList.set(finalIntegers[finalRightIndex],tempTotalList.get(0));
                                map.put(tempTotalList.get(0),new Star(0,0));//
                                tempTotalList.remove(tempTotalList.get(0));
                            }
                            else
                            {
                                //tempTotalList已经全被加进Recycle了，就recycle-1
                                recycleList.remove(recycleList.get(finalIntegers[finalRightIndex]));
                            }
                            //把星星全部显示绿色，起到一个提示操作的作用
                            refreshStar(0,5);
                            break;
                        //其它三个选项(选错)
                        default:

                                v.setBackgroundResource(R.drawable.shape_10);
                                items[finalRightIndex].setBackgroundResource(R.drawable.shape_9);
                                items[finalRightIndex].setTextColor(getResources().getColor(R.color.teal_200));
                                for (int i = 0; i < 4; i++) {
                                    if(finalRightIndex !=i) items[i].setTextColor(0xFFD4D6D8);//灰色
                                }
                                //选错,红星加一
                            if(Objects.requireNonNull(map.get(recycleList.get(finalIntegers[finalRightIndex]))).getRed()<3)
                            {
                                Objects.requireNonNull(map.get(recycleList.get(finalIntegers[finalRightIndex])))
                                        .setRed(Objects.requireNonNull(map.get(recycleList.get(finalIntegers[finalRightIndex]))).getRed()+1);
                                //刷新星星
                                refreshStar(Objects.requireNonNull(map.get(recycleList.get(finalIntegers[finalRightIndex]))).getGre(),
                                        Objects.requireNonNull(map.get(recycleList.get(finalIntegers[finalRightIndex]))).getRed());
                            }
                            //满三颗星就刷新recycleList,从tempTotalList添加，从tempTotalList减一
                            if(Objects.requireNonNull(map.get(recycleList.get(finalIntegers[finalRightIndex]))).getRed()==3)
                            {
                                //把totalList的该单词tag设置为3，相当于加入(自动)收藏
                                WordList.DataDTO ddt__ = recycleList.get(finalIntegers[finalRightIndex]);
                                //构建提交数据
                                RequestData.WordRecordDTO wrd__=new RequestData.WordRecordDTO(ddt__.getId(),3,
                                        ddt__.getTimesReview(),ddt__.getDifficult(),String.valueOf(new Date().getTime()));
                                wrdList.add(wrd__);
                                //从tempTotalList拿一个生词替换recycleList中这个位置的单词
                                if(tempTotalList.size()!=0)
                                {
                                    //tempTotalList还有
                                    recycleList.set(finalIntegers[finalRightIndex],tempTotalList.get(0));
                                    map.put(tempTotalList.get(0),new Star(0,0));//
                                    tempTotalList.remove(tempTotalList.get(0));
                                }
                                else
                                {
                                    //tempTotalList已经全被加进Recycle了，就recycle-1
                                    recycleList.remove(recycleList.get(finalIntegers[finalRightIndex]));
                                    Toast.makeText(MainActivity.this, "recycleList.size():" + recycleList.size(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            break;
                    }
                }

                //保存星星数组到本地，每日更新
                //···
                //···这里由于星星数据还没写手机和手表同步的接口，代码搁置在这
                //···

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
            ivStars[i].setImageResource(R.drawable.star_gre);
        }
        //设置红星显示
        for (int i = 0; i < redStarNum; i++) {
            ivStars[4-i].setImageResource(R.drawable.star_red);
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
    private void commit(RequestData data) throws GeneralSecurityException, IOException {
        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("上传数据中···");
        progressDialog.setCancelable(false);

        if (data.getWord_record().size()!=0)//需要提交的单词数为0，则不显示进度条
        {
            progressDialog.show();
        }
        if (data.getWord_record().size()==0&&tempTime<1000)//需要提交的单词数为0且时间也为0，就没有提交的价值，结束方法
        {
            return;
        }
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
                            Toast.makeText(MainActivity.this, "state:" + state, Toast.LENGTH_SHORT).show();
                            Log.d("MainActivity", new Gson().toJson(jsonObject));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        },2000);
    }
}