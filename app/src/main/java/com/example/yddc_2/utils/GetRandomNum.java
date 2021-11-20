package com.example.yddc_2.utils;

public class GetRandomNum {
    //获取0~num-1的随机数
    public static int getInt(int num){
        int index = (int) (Math.random() * num);
        return index;
    }
}
