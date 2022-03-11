package com.example.yddc_2.bean;

import com.google.gson.annotations.SerializedName;

public class ReciteRecord {
    private DataDTO data;
    private int state;
    private String message;

    public DataDTO getData() {
        return data;
    }

    public void setData(DataDTO data) {
        this.data = data;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class DataDTO {
        @SerializedName("A")
        private int a;
        @SerializedName("B")
        private int b;
        @SerializedName("C")
        private int c;
        @SerializedName("D")
        private int d;
        @SerializedName("E")
        private int e;
        @SerializedName("F")
        private int f;
        @SerializedName("G")
        private int g;
        @SerializedName("H")
        private int h;
        @SerializedName("I")
        private int i;
        @SerializedName("J")
        private int j;
        @SerializedName("K")
        private int k;
        @SerializedName("L")
        private int l;
        @SerializedName("AverageTime")
        private int averageTime;
        @SerializedName("AverageWords")
        private int averageWords;
        @SerializedName("Efficiency")
        private int efficiency;

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }

        public int getC() {
            return c;
        }

        public void setC(int c) {
            this.c = c;
        }

        public int getD() {
            return d;
        }

        public void setD(int d) {
            this.d = d;
        }

        public int getE() {
            return e;
        }

        public void setE(int e) {
            this.e = e;
        }

        public int getF() {
            return f;
        }

        public void setF(int f) {
            this.f = f;
        }

        public int getG() {
            return g;
        }

        public void setG(int g) {
            this.g = g;
        }

        public int getH() {
            return h;
        }

        public void setH(int h) {
            this.h = h;
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public int getJ() {
            return j;
        }

        public void setJ(int j) {
            this.j = j;
        }

        public int getK() {
            return k;
        }

        public void setK(int k) {
            this.k = k;
        }

        public int getL() {
            return l;
        }

        public void setL(int l) {
            this.l = l;
        }

        public int getAverageTime() {
            return averageTime;
        }

        public void setAverageTime(int averageTime) {
            this.averageTime = averageTime;
        }

        public int getAverageWords() {
            return averageWords;
        }

        public void setAverageWords(int averageWords) {
            this.averageWords = averageWords;
        }

        public int getEfficiency() {
            return efficiency;
        }

        public void setEfficiency(int efficiency) {
            this.efficiency = efficiency;
        }
    }
}
