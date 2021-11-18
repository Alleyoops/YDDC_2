package com.example.yddc_2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.example.yddc_2.adapter.RecyclerView_1_Adapter;
import com.example.yddc_2.utils.HideBar;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        HideBar.hideBar(this);
        back();
        initRv();
    }
    //使导航栏返回键可用
    private void back(){
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_result);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void initRv()
    {
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerView1);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new RecyclerView_1_Adapter(R.layout.item_recyclerview1_layout,getApplicationContext()));
    }

}