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

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
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
import com.example.yddc_2.viewmodels.MainViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private int pos = 0;//记录哪一页

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBottomNavigationView();
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
                        ll_content.setVisibility(View.VISIBLE);
                        NavBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        try {
                            recite();
                        } catch (GeneralSecurityException | IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        ll_tip.setVisibility(View.VISIBLE);
                        ll_content.setVisibility(View.GONE);
                        NavBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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

    //BottomSheet里面背单词
    private void recite() throws GeneralSecurityException, IOException {//调用MainViewModel
        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getmWordList(this).observe(this, new Observer<WordList>() {
            @Override
            public void onChanged(WordList wordList) {
                TextView test = (TextView) findViewById(R.id.spell);
                TextView item1 = (TextView) findViewById(R.id.item1);
                TextView item2 = (TextView) findViewById(R.id.item2);
                TextView item3 = (TextView) findViewById(R.id.item3);
                TextView item4 = (TextView) findViewById(R.id.item4);
                test.setText(wordList.getData().get(GetRandomNum.getInt(50)).getWord().getSpell());
                PressAnimUtil.addScaleAnimition(item1,null);//按压动画
                PressAnimUtil.addScaleAnimition(item2,null);
                PressAnimUtil.addScaleAnimition(item3,null);
                PressAnimUtil.addScaleAnimition(item4,null);
                int index1 = GetRandomNum.getInt(50);
                int index2 = GetRandomNum.getInt(50);
                int index3 = GetRandomNum.getInt(50);
                int index4 = GetRandomNum.getInt(50);
                item1.setText(wordList.getData().get(index1).getWord().getClearfix().get(0).getClearfix());
                item2.setText(wordList.getData().get(index2).getWord().getClearfix().get(0).getClearfix());
                item3.setText(wordList.getData().get(index3).getWord().getClearfix().get(0).getClearfix());
                item4.setText(wordList.getData().get(index4).getWord().getClearfix().get(0).getClearfix());
            }
        });
    }

}