package com.example.yddc_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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
import com.example.yddc_2.bean.WordList;
import com.example.yddc_2.navigation.find.SecondFragment;
import com.example.yddc_2.navigation.me.ThirdFragment;
import com.example.yddc_2.navigation.word.FirstFragment;
import com.example.yddc_2.utils.GetRandomNum;
import com.example.yddc_2.utils.HideBar;
import com.example.yddc_2.utils.MyHandler;
import com.example.yddc_2.utils.PressAnimUtil;
import com.example.yddc_2.utils.SecuritySP;
import com.example.yddc_2.viewmodels.MainViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private int pos = 0;//记录哪一页
    private int flag = 0;//bottomSheet展开为1
    public  static  List<WordList.DataDTO> totalList;
    //定义一个保存绿星和红星个数的数组
    public static int[] greStar = new int[50];
    public static int[] redStar = new int[50];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBottomNavigationView();
        try {
            iniWords();
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
                        recite();
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
    public void iniWords() throws GeneralSecurityException, IOException {
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
            WordList temp = gson.fromJson(jsonStr,WordList.class);
            //放进一个可以方便增删的list
            totalList = temp.getData();
        }
    }

    //BottomSheet里面背单词
    private void recite(){//调用MainViewModel
        if(flag == 0)//说明下拉框刚拉上来
        {
            flag = 1;
            TextView spell = (TextView) findViewById(R.id.spell);
            TextView item1 = (TextView) findViewById(R.id.item1);
            TextView item2 = (TextView) findViewById(R.id.item2);
            TextView item3 = (TextView) findViewById(R.id.item3);
            TextView item4 = (TextView) findViewById(R.id.item4);
            //从totalList随机取10个放进一个recycleList，（10个背完再背诵下一个recycleList），不满10个时就往里面依次添加totalList的元素
            List<WordList.DataDTO> tempTotalList = new ArrayList<>(totalList);
            Integer[] tenOfFifty = GetRandomNum.getIntegers(10,tempTotalList.size());
            List<WordList.DataDTO> recycleList = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                recycleList.add(tempTotalList.get(tenOfFifty[i]));
            }
            for (int i = 0; i < 10; i++) {
                tempTotalList.remove(recycleList.get(i));//相当于把total移动十个到recycle
            }
            Log.d("MainActivity", "tempTotalList.size():" + String.valueOf(tempTotalList.size()));
            Log.d("MainActivity", "totalList.size():" + String.valueOf(totalList.size()));
            //从10个随机取四个
            Integer[] integers = GetRandomNum.getIntegers(4,10);
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
            refreshStar(greStar[tenOfFifty[integers[rightIndex]]],redStar[tenOfFifty[integers[rightIndex]]]);
            //点击选项
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //得到被选中的选项的下标
//                            int chosenIndex = 0;
//                            for (int i = 0; i < 4; i++) {
//                                if(items[i].getId()==v.getId()) chosenIndex = i;
//                            }
                    if(v.getId()==items[rightIndex].getId())
                    {
                        v.setBackgroundResource(R.drawable.shape_9);
                        //选对,绿星加一
                        if(greStar[tenOfFifty[integers[rightIndex]]]+redStar[tenOfFifty[integers[rightIndex]]]<5
                                &&greStar[tenOfFifty[integers[rightIndex]]]<3)
                            greStar[tenOfFifty[integers[rightIndex]]]+=1;

                    }
                    else
                    {
                        v.setBackgroundResource(R.drawable.shape_10);
                        items[rightIndex].setBackgroundResource(R.drawable.shape_9);
                        //选错,红星加一
                        if(greStar[tenOfFifty[integers[rightIndex]]]+redStar[tenOfFifty[integers[rightIndex]]]<5
                                &&redStar[tenOfFifty[integers[rightIndex]]]<3)
                            redStar[tenOfFifty[integers[rightIndex]]]+=1;
                    }
                    //刷新星星
                    refreshStar(greStar[tenOfFifty[integers[rightIndex]]],redStar[tenOfFifty[integers[rightIndex]]]);
                    //保存星星数组到本地，每日更新
                    //慢三颗星就刷新recycleList,从tempTotalList添加，从tempTotalList减一
                    if(greStar[tenOfFifty[integers[rightIndex]]]>3)
                    {
                    }
                    if(redStar[tenOfFifty[integers[rightIndex]]]>3)
                    {
                    }
                    //点击一秒后刷新选项

                }
            };
            //设置监听器
            PressAnimUtil.addScaleAnimition(item1,listener);
            PressAnimUtil.addScaleAnimition(item2,listener);
            PressAnimUtil.addScaleAnimition(item3,listener);
            PressAnimUtil.addScaleAnimition(item4,listener);

        }
    }
    //显示星星
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
        //设置绿星
        for (int i = 0; i < greStarNum; i++) {
            if(greStarNum<=3) ivStars[i].setImageResource(R.drawable.star_gre);
        }
        //设置红星
        for (int i = 0; i < redStarNum; i++) {
            if(redStarNum<=3) ivStars[4-i].setImageResource(R.drawable.star_red);
        }
        //设置灰星
        for (int i = 0; i < (5-redStarNum-greStarNum); i++) {
            ivStars[greStarNum+i].setImageResource(R.drawable.star_gray);
        }
    }

    //结束背单词，提交背诵数据
    private void endRecite()
    {
        if(flag==1)//收起
        {
            flag = 0;
            Toast.makeText(this, "end", Toast.LENGTH_SHORT).show();
        }

    }

}