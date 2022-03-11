package com.example.yddc_2.bean;

public class DayPlan {
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
        private double efficiency;
        private int sumTime;
        private int today;
        private int sumWord;
        private int todayed;
        private int toReviewed;

        public double getEfficiency() {
            return efficiency;
        }

        public void setEfficiency(double efficiency) {
            this.efficiency = efficiency;
        }

        public int getSumTime() {
            return sumTime;
        }

        public void setSumTime(int sumTime) {
            this.sumTime = sumTime;
        }

        public int getToday() {
            return today;
        }

        public void setToday(int today) {
            this.today = today;
        }

        public int getSumWord() {
            return sumWord;
        }

        public void setSumWord(int sumWord) {
            this.sumWord = sumWord;
        }

        public int getTodayed() {
            return todayed;
        }

        public void setTodayed(int todayed) {
            this.todayed = todayed;
        }

        public int getToReviewed() {
            return toReviewed;
        }

        public void setToReviewed(int toReviewed) {
            this.toReviewed = toReviewed;
        }
    }
}
