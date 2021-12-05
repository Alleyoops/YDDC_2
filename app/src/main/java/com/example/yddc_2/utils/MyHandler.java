package com.example.yddc_2.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.example.yddc_2.DetailActivity;
import com.example.yddc_2.MainActivity;
import com.example.yddc_2.SettingsActivity;

public class MyHandler {
    public void onClickView1(View view){
        Toast.makeText(view.getContext(), "暂不支持第三方登录", Toast.LENGTH_SHORT).show();
    };
    public void onClickView2(View view)
    {
        view.getContext().startActivity(new Intent().setClass(view.getContext(), DetailActivity.class));
    }
    public void onClickView3(View view)
    {
        view.getContext().startActivity(new Intent().setClass(view.getContext(), SettingsActivity.class));
    }
}
