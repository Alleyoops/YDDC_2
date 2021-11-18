package com.example.yddc_2.bean;


import java.util.List;

public class DaySentence {


    private Integer code;
    private String msg;
    private List<NewslistDTO> newslist;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<NewslistDTO> getNewslist() {
        return newslist;
    }

    public void setNewslist(List<NewslistDTO> newslist) {
        this.newslist = newslist;
    }

    public static class NewslistDTO {
        private Integer id;
        private String content;
        private String source;
        private String note;
        private String tts;
        private String imgurl;
        private String date;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public String getTts() {
            return tts;
        }

        public void setTts(String tts) {
            this.tts = tts;
        }

        public String getImgurl() {
            return imgurl;
        }

        public void setImgurl(String imgurl) {
            this.imgurl = imgurl;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }
}
