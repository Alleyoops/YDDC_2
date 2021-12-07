package com.example.yddc_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yddc_2.adapter.ViewPagerAdapter;
import com.example.yddc_2.bean.WordList;
import com.example.yddc_2.navigation.find.SecondFragment;
import com.example.yddc_2.navigation.me.ThirdFragment;
import com.example.yddc_2.navigation.word.FirstFragment;
import com.example.yddc_2.utils.GetRandomNum;
import com.example.yddc_2.utils.HideBar;
import com.example.yddc_2.utils.PressAnimUtil;
import com.example.yddc_2.utils.SecuritySP;
import com.example.yddc_2.viewmodels.MainViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private int pos = 0;//记录哪一页
    private int flag = 0;//bottomSheet展开为1
    //以下三个list要理清关系
    public  static List<WordList.DataDTO> totalList;//比如通过链接获取到的今天的50个单词，每次背诵完后需要提交数据（更新totalList中单词的tag值）（此外totalList只是在用户注册后设置每日任务单词数前默认是50个）
    public  static List<WordList.DataDTO> tempTotalList;//tempTotalList的初始值是totalList中tag为0的所有单词，每生成一个recycleList，它就减去10个，recycleList每更新一个，它就减去一个
    public  static List<WordList.DataDTO> recycleList;//大小为10,保存每次背诵时循环的十个单词
    //保存一个list中单词个数的随机数的数组
    public  static Integer[] someOfTotal = new Integer[100];
    //定义一个保存绿星和红星个数的数组
    public static int[] greStar = new int[100];
    public static int[] redStar = new int[100];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBottomNavigationView();
        try {
            iniTodayWords();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
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

    private void initBottomNavigationView() {
        BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_layout));
        BottomSheetBehavior<BottomNavigationView> NavBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomNavigationView));
        List<Fragment> FragmentList = new ArrayList<>();
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        //向viewpager添加各个fragment页面
        FragmentList.add((new FirstFragment()));
        FragmentList.add((new SecondFragment()));
        FragmentList.add((new ThirdFragment()));
        //适配器
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), this, FragmentList);
        viewPager.setAdapter(adapter);
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
            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                switch (position) {
                    case 0:
                        pos = 0;
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        break;
                    case 1:
                        pos = 1;
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        break;
                    case 2:
                        pos = 2;
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
                LinearLayout ll_tip = (LinearLayout)findViewById(R.id.ll_tip);
                LinearLayout ll_content = (LinearLayout)findViewById(R.id.ll_content);
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        ll_tip.setVisibility(View.GONE);
                        //ll_content.setVisibility(View.VISIBLE);
                        NavBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        reciteOfWork();
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        ll_tip.setVisibility(View.VISIBLE);
                        //ll_content.setVisibility(View.GONE);
                        NavBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        endRecite();
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        if (pos == 0)
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull @NotNull View bottomSheet, float slideOffset) {

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
                        WordList temp = gson.fromJson(jsonStr,WordList.class);
                        //放进一个可以方便增删的list
                        totalList = temp.getData();
                    } catch (GeneralSecurityException | IOException e) {
                        e.printStackTrace();
                    }
                }
            });Toast.makeText(this, "in", Toast.LENGTH_SHORT).show();
        }
        else
        {
            //从本地取出todayWords
            String jsonStr = SecuritySP.DecryptSP(MainActivity.this,"todayWords");
            Gson gson = new Gson();
            //放进一个可以方便增删的list
            totalList = gson.fromJson(jsonStr,WordList.class).getData();
        }
    }


    //背单词之任务模式（默认模式）
    private void reciteOfWork(){
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
        //从tempTotalList随机取10个放进一个recycleList，（10个背完再背诵下一个recycleList），不满10个时就往里面依次添加totalList的元素
        someOfTotal = GetRandomNum.getIntegers(10,tempTotalList.size());
        //
        //···这里当tempTotalList中的数目小于10时还需要写一个判决条件，否则估计会报错、闪退
        //···但是谁会测试到40个以上呢？所以先空着吧
        //
        recycleList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            recycleList.add(tempTotalList.get(someOfTotal[i]));
        }
        for (int i = 0; i < 10; i++) {
            tempTotalList.remove(recycleList.get(i));//相当于把total移动十个到recycle
        }

        if(flag == 0)//说明下拉框刚拉上来
        {
            flag = 1;
            reciteProcess();
        }
    }

    //背诵流程
    public void reciteProcess()
    {

        for (int i = 0; i < recycleList.size(); i++) {
            Log.d("MainActivity", "recycleList:" + recycleList.get(i).getWord().getSpell());
        }

        TextView spell = (TextView) findViewById(R.id.spell);
        TextView item1 = (TextView) findViewById(R.id.item1);
        TextView item2 = (TextView) findViewById(R.id.item2);
        TextView item3 = (TextView) findViewById(R.id.item3);
        TextView item4 = (TextView) findViewById(R.id.item4);

        Log.d("MainActivity", "totalList.size():" + String.valueOf(totalList.size()));
        //从10个随机取四个
        Integer[] integers = GetRandomNum.getIntegers(4,recycleList.size());
        //
        //···这里当tempTotalList中的数目小于10时还需要写一个判决条件，否则估计会报错、闪退
        //···但是谁会测试到40个以上呢？所以先空着吧
        //
        //四个选项随机选取一个作为正确答案
        int rightIndex = GetRandomNum.getOneInt(4);
        //四个中的第一个作为spell
        spell.setText(recycleList.get(integers[rightIndex]).getWord().getSpell());
        //选项赋值
        TextView[] items = new TextView[]{item1,item2,item3,item4};
        item1.setText(recycleList.get(integers[0]).getWord().getClearfix().get(0).getClearfix());
        item2.setText(recycleList.get(integers[1]).getWord().getClearfix().get(0).getClearfix());
        item3.setText(recycleList.get(integers[2]).getWord().getClearfix().get(0).getClearfix());
        item4.setText(recycleList.get(integers[3]).getWord().getClearfix().get(0).getClearfix());
        //点之前刷新星星
        ///替换、list顺序问题。omg，fuck，替换后还要把星星设置为0哦~~
        refreshStar(greStar[someOfTotal[integers[rightIndex]]],redStar[someOfTotal[integers[rightIndex]]]);
        //点击选项
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId()==items[rightIndex].getId())
                {
                    v.setBackgroundResource(R.drawable.shape_9);
                    items[rightIndex].setTextColor(getResources().getColor(R.color.teal_200));
                    for (int i = 0; i < 4; i++) {
                        if(rightIndex!=i) items[i].setTextColor(0xFFD4D6D8);//灰色
                    }
                    //选对,绿星加一
                    if(greStar[someOfTotal[integers[rightIndex]]]+redStar[someOfTotal[integers[rightIndex]]]<5
                            &&greStar[someOfTotal[integers[rightIndex]]]<3)
                        greStar[someOfTotal[integers[rightIndex]]]+=1;
                }
                else
                {
                    v.setBackgroundResource(R.drawable.shape_10);
                    items[rightIndex].setBackgroundResource(R.drawable.shape_9);
                    items[rightIndex].setTextColor(getResources().getColor(R.color.teal_200));
                    for (int i = 0; i < 4; i++) {
                        if(rightIndex!=i) items[i].setTextColor(0xFFD4D6D8);//灰色
                    }
                    //选错,红星加一
                    if(greStar[someOfTotal[integers[rightIndex]]]+redStar[someOfTotal[integers[rightIndex]]]<5
                            &&redStar[someOfTotal[integers[rightIndex]]]<3)
                        redStar[someOfTotal[integers[rightIndex]]]+=1;
                }
                //刷新星星
                refreshStar(greStar[someOfTotal[integers[rightIndex]]],redStar[someOfTotal[integers[rightIndex]]]);

                //保存星星数组到本地，每日更新
                //···
                //···这里由于星星数据还没写手机和手表同步的接口，代码搁置在这
                //···

                //满三颗星就刷新recycleList,从tempTotalList添加，从tempTotalList减一
                if(greStar[someOfTotal[integers[rightIndex]]]==3)
                {
                    //把totalList的该单词tag设置为2
                    totalList.get(someOfTotal[integers[rightIndex]]).setTag(2);
                    //从tempTotalList拿一个生词替换recycleList中这个位置的单词，并把这个单词星星数归0
                    recycleList.set(integers[rightIndex],tempTotalList.get(0));
                    greStar[someOfTotal[integers[rightIndex]]] = 0;redStar[someOfTotal[integers[rightIndex]]] = 0;
//                    recycleList.remove(recycleList.get(integers[rightIndex]));//移除这一项
//                    recycleList.add(tempTotalList.get(0));//从今日任务里依次添加进来一个新的单词
                    tempTotalList.remove(tempTotalList.get(0));
                }
                if(redStar[someOfTotal[integers[rightIndex]]]==3)
                {
                    //把把temp的该单词tag设置为2的该单词tag设置为3，相当于加入(自动)收藏
                    totalList.get(someOfTotal[integers[rightIndex]]).setTag(3);
                    //从tempTotalList拿一个生词替换recycleList中这个位置的单词
                    recycleList.set(integers[rightIndex],tempTotalList.get(0));
                    greStar[someOfTotal[integers[rightIndex]]] = 0;redStar[someOfTotal[integers[rightIndex]]] = 0;
//                    recycleList.remove(recycleList.get(integers[rightIndex]));//移除这一项
//                    recycleList.add(tempTotalList.get(0));//从今日任务里依次添加进来一个新的单词
                    tempTotalList.remove(tempTotalList.get(0));
                    Log.d("MainActivity", "here");
                }
//                    //更新本地的wordList
//                    Gson gson = new Gson();
//                    String jsonStr = gson.toJson(temp);
//                    try {
//                        SecuritySP.EncryptSP(MainActivity.this,"todayWords",jsonStr);
//
//                    } catch (GeneralSecurityException | IOException e) {
//                        e.printStackTrace();
//                    }
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
                        reciteProcess();
                        Log.d("MainActivity", "tempTotalList.size():" + String.valueOf(tempTotalList.size()));
                    }
                },1500);
            }
        };
        //设置监听器
        PressAnimUtil.addScaleAnimition(item1,listener);
        PressAnimUtil.addScaleAnimition(item2,listener);
        PressAnimUtil.addScaleAnimition(item3,listener);
        PressAnimUtil.addScaleAnimition(item4,listener);
    }

    //显示和刷新星星数目
    private void refreshStar(int greStarNum,int redStarNum)
    {
        Log.d("MainActivity", "greStarNum:" + greStarNum);
        Log.d("MainActivity", "redStarNum:" + redStarNum);
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

    //结束背单词，提交背诵数据
    private void endRecite()
    {
        if(flag==1)//下拉框收起
        {
            flag = 0;
            Toast.makeText(this, "end", Toast.LENGTH_SHORT).show();
        }

    }

}