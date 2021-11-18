package com.example.yddc_2.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    Context context;
    List<Fragment> listFragment;

    public ViewPagerAdapter(FragmentManager fm, Context context, List<Fragment> listFragment) {
        super(fm);
        this.context = context;
        this.listFragment = listFragment;
    }

    @Override
    public Fragment getItem(int position) {
        return listFragment.get(position);
    }

    @Override
    public int getCount() {
        return listFragment.size();
    }

    @Override
    public void destroyItem(@NonNull @NotNull ViewGroup container, int position, @NonNull @NotNull Object object) {
        //super.destroyItem(container, position, object);
    }
}