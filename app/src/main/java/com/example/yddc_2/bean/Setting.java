package com.example.yddc_2.bean;

import java.io.Serializable;

public class Setting {
    private Integer state;

    public Setting(Integer state, String message, DataDTO data) {
        this.state = state;
        this.message = message;
        this.data = data;
    }

    private String message;
    private DataDTO data;

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataDTO getData() {
        return data;
    }

    public void setData(DataDTO data) {
        this.data = data;
    }

    public static class DataDTO {
        private String id;
        private String tag;
        private Integer phoRem;
        private Integer watRem;
        private Integer dayTime;
        private Integer list;
        private Integer numOfList;
        private Integer circWay;
        private String uid;

        public DataDTO(String id, String tag, Integer phoRem, Integer watRem, Integer dayTime, Integer list, Integer numOfList, Integer circWay, String uid) {
            this.id = id;
            this.tag = tag;
            this.phoRem = phoRem;
            this.watRem = watRem;
            this.dayTime = dayTime;
            this.list = list;
            this.numOfList = numOfList;
            this.circWay = circWay;
            this.uid = uid;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public Integer getPhoRem() {
            return phoRem;
        }

        public void setPhoRem(Integer phoRem) {
            this.phoRem = phoRem;
        }

        public Integer getWatRem() {
            return watRem;
        }

        public void setWatRem(Integer watRem) {
            this.watRem = watRem;
        }

        public Integer getDayTime() {
            return dayTime;
        }

        public void setDayTime(Integer dayTime) {
            this.dayTime = dayTime;
        }

        public Integer getList() {
            return list;
        }

        public void setList(Integer list) {
            this.list = list;
        }

        public Integer getNumOfList() {
            return numOfList;
        }

        public void setNumOfList(Integer numOfList) {
            this.numOfList = numOfList;
        }

        public Integer getCircWay() {
            return circWay;
        }

        public void setCircWay(Integer circWay) {
            this.circWay = circWay;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }
    }
}
