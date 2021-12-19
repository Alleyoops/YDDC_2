package com.example.yddc_2.bean;

import java.util.List;

public class RequestData {
    private List<WordRecordDTO> word_record;
    private List<TimeRecordDTO> time_record;

    public List<WordRecordDTO> getWord_record() {
        return word_record;
    }

    public void setWord_record(List<WordRecordDTO> word_record) {
        this.word_record = word_record;
    }

    public List<TimeRecordDTO> getTime_record() {
        return time_record;
    }

    public void setTime_record(List<TimeRecordDTO> time_record) {
        this.time_record = time_record;
    }

    public static class WordRecordDTO {
        private String id;
        private Integer tag;

        public WordRecordDTO(String id, Integer tag, Integer timesReview, Integer difficult, String cDate) {
            this.id = id;
            this.tag = tag;
            this.timesReview = timesReview;
            this.difficult = difficult;
            this.cDate = cDate;
        }

        private Integer timesReview;
        private Integer difficult;
        private String cDate;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getTag() {
            return tag;
        }

        public void setTag(Integer tag) {
            this.tag = tag;
        }

        public Integer getTimesReview() {
            return timesReview;
        }

        public void setTimesReview(Integer timesReview) {
            this.timesReview = timesReview;
        }

        public Integer getDifficult() {
            return difficult;
        }

        public void setDifficult(Integer difficult) {
            this.difficult = difficult;
        }

        public String getCDate() {
            return cDate;
        }

        public void setCDate(String cDate) {
            this.cDate = cDate;
        }
    }

    public static class TimeRecordDTO {
        public TimeRecordDTO(String day, Integer sumTime) {
            this.day = day;
            this.sumTime = sumTime;
        }

        private String day;
        private Integer sumTime;

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public Integer getSumTime() {
            return sumTime;
        }

        public void setSumTime(Integer sumTime) {
            this.sumTime = sumTime;
        }
    }
}
