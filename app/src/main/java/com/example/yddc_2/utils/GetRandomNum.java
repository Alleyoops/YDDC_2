package com.example.yddc_2.utils;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

public class GetRandomNum {
    //获取0~num-1的随机数
    public static int getOneInt(int num){
        return (int) (Math.random() * num);
    }

    /**
     * @param max:随机范围
     * @param size:个数
     */
    public static Integer[] getIntegers(int size,int max){
        Random random= new Random();
        Set<Integer> set= new HashSet<Integer>();
        Integer[] integers = new Integer[size];
        while (set.size() < size)
        {
            //Random.nextInt(int num)随机返回一个值在[0,num)的int类型的整数,包括0不包括num
            int temp = set.size();
            Integer next = random.nextInt(max);
            set.add(next);
            if(set.size()>temp)//说明这个随机数没有重复，就把他添加到List里
            {
                integers[temp] = next;
            }
        }
        return integers;
    }
}
