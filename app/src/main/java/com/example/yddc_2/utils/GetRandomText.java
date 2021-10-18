package com.example.yddc_2.utils;

public class GetRandomText {
    public static String getText(){
        String[] RandomStr = {"你好","姐们儿 冲！","你可以的","没得问题","向前 向前","哈哈","啵一个","酷","卧槽"};
        int index = (int) (Math.random() * RandomStr.length);
        return RandomStr[index];
    }
}
