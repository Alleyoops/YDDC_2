package com.example.yddc_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.example.yddc_2.adapter.ViewPagerAdapter;
import com.example.yddc_2.databinding.ActivityMainBinding;
import com.example.yddc_2.navigation.find.SecondFragment;
import com.example.yddc_2.navigation.me.ThirdFragment;
import com.example.yddc_2.navigation.word.FirstFragment;
import com.example.yddc_2.utils.HideBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private int pos ;//记录哪一页
    //private ActivityMainBinding mBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mBinding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        initBottomNavigationView();
        HideBar.hideBar(this);//暂时不理想
    }

    private void initBottomNavigationView(){
        BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_layout));
        BottomSheetBehavior<BottomNavigationView> NavBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomNavigationView));
        List<Fragment> FragmentList = new ArrayList<>();
        ViewPager viewPager = (ViewPager)findViewById(R.id.view_pager);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        //向viewpager添加各个fragment页面
        FragmentList.add((new FirstFragment()));
        FragmentList.add((new SecondFragment()));
        FragmentList.add((new ThirdFragment()));
        //适配器
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(),this,FragmentList);
        viewPager.setAdapter(adapter);
        //导航栏点击事件和ViewPager滑动事件,让两个控件相互关联
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //这里设置为：当点击到某子项时，viewpager就滑动到对应位置
                switch (item.getItemId()){
                    case R.id.word:
                        pos = 0;
                        viewPager.setCurrentItem(0,false);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        return true;
                    case R.id.find:
                        pos = 1;
                        viewPager.setCurrentItem(1,false);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        return true;
                    case R.id.me:
                        pos = 2;
                        viewPager.setCurrentItem(2,false);
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
                if(position == 1){
                    pos = 1;
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }else {
                    pos = 2;//或者pos=0
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });



        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull @NotNull View bottomSheet, int newState) {
                //bottomSheet状态改变
                switch (newState){
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        viewPager.setVisibility(View.VISIBLE);
                        NavBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        //视图从脱离手指自由滑动到最终停下的这一小段时间
                        break;
                    //收起状态
                    case BottomSheetBehavior.STATE_DRAGGING:
                        //拖动时
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        //全部展开状态
                        viewPager.setVisibility(View.GONE);
                        NavBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        //隐藏状态
                        //如果是find页面则不弹出，否则弹出
                        if(pos==1){
                            //bottomSheetBehavior什么都不做，保持状态,但是NavBehavior要升起来
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        }else {
                            //恢复状态
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        //半展开状态
                        viewPager.setVisibility(View.VISIBLE);
                        NavBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull @NotNull View bottomSheet, float slideOffset) {
                //bottomSheet滑动拖拽改变slideOffset做动画
            }
        });
    }
}