package com.example.yddc_2.utils;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@SuppressLint("SimpleDateFormat")
public class DateUtil {

    public static String getNowDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date());
    }

    public static String getNowDateTimeFull() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return sdf.format(new Date());
    }

    public static String getNowDateTimeFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    public static String getNowTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    //12:30:23或07:30形式的字符串转换秒数
    public static int toSeconds(String s){
        String[] t = s.split(":");
        int ret = 0;
        if (t.length==2) ret = Integer.parseInt(t[0])*60+Integer.parseInt(t[1]);
        else if (t.length==3)
            ret = Integer.parseInt(t[0])*60*60
                    +Integer.parseInt(t[1])*60
                    +Integer.parseInt(t[2]);
        return ret;
    }

    //时间戳转换成年月日
    public static String transForDateNYR(Long ms) throws ParseException {
        if(ms==null){
            ms=0L;
        }
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd");
        String temp=null;
        String str=sdf.format(ms);
        temp=sdf.format(Objects.requireNonNull(sdf.parse(str)));
        return temp;
    }

}
