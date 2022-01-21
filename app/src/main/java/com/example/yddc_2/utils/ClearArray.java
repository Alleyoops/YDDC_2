package com.example.yddc_2.utils;

//把int[]数组全部置为0
public class ClearArray {
    public static int[] toBeZero(int[] arr){
        for (int i:arr
             ) {
            arr[i] = 0;
        }
        return arr;
    }
}
