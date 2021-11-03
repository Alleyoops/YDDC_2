package com.example.yddc_2.myinterface;

import android.view.View;
import android.widget.Toast;

public interface ClickInterface {

    /*
     * default关键字修饰的方法就是初始化的抽象方法。或者说是一个已经实现了的抽象方法，不需要再在其他implement接口位置进行实现。
     *
     */
    //实现dataBinding的onclick方法
    default void test(View view){
        Toast.makeText(view.getContext(), "HI", Toast.LENGTH_SHORT).show();
    }
    void test2();//这种没关键字的是需要继承并实现的
}
