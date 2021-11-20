package com.example.yddc_2.bean;

import java.util.List;

public class WordList {

    private List<DataDTO> data;
    private Integer state;
    private String message;

    public List<DataDTO> getData() {
        return data;
    }

    public void setData(List<DataDTO> data) {
        this.data = data;
    }

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

    public static class DataDTO {
        private String id;
        private Integer tag;
        private Integer difficult;
        private WordDTO word;
        private String wId;
        private Integer timesReview;

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

        public Integer getDifficult() {
            return difficult;
        }

        public void setDifficult(Integer difficult) {
            this.difficult = difficult;
        }

        public WordDTO getWord() {
            return word;
        }

        public void setWord(WordDTO word) {
            this.word = word;
        }

        public String getWId() {
            return wId;
        }

        public void setWId(String wId) {
            this.wId = wId;
        }

        public Integer getTimesReview() {
            return timesReview;
        }

        public void setTimesReview(Integer timesReview) {
            this.timesReview = timesReview;
        }

        public static class WordDTO {
            private String spell;
            private String tag;
            private String href;
            private List<AudioDTO> audio;
            private List<ClearfixDTO> clearfix;
            private List<?> deformation;
            private List<?> phrase;
            private List<SentencesDTO> sentences;
            private List<SimilarsDTO> similars;
            private String wId;

            public String getSpell() {
                return spell;
            }

            public void setSpell(String spell) {
                this.spell = spell;
            }

            public String getTag() {
                return tag;
            }

            public void setTag(String tag) {
                this.tag = tag;
            }

            public String getHref() {
                return href;
            }

            public void setHref(String href) {
                this.href = href;
            }

            public List<AudioDTO> getAudio() {
                return audio;
            }

            public void setAudio(List<AudioDTO> audio) {
                this.audio = audio;
            }

            public List<ClearfixDTO> getClearfix() {
                return clearfix;
            }

            public void setClearfix(List<ClearfixDTO> clearfix) {
                this.clearfix = clearfix;
            }

            public List<?> getDeformation() {
                return deformation;
            }

            public void setDeformation(List<?> deformation) {
                this.deformation = deformation;
            }

            public List<?> getPhrase() {
                return phrase;
            }

            public void setPhrase(List<?> phrase) {
                this.phrase = phrase;
            }

            public List<SentencesDTO> getSentences() {
                return sentences;
            }

            public void setSentences(List<SentencesDTO> sentences) {
                this.sentences = sentences;
            }

            public List<SimilarsDTO> getSimilars() {
                return similars;
            }

            public void setSimilars(List<SimilarsDTO> similars) {
                this.similars = similars;
            }

            public String getWId() {
                return wId;
            }

            public void setWId(String wId) {
                this.wId = wId;
            }

            public static class AudioDTO {
                private String tag;
                private String tagDetail;
                private String url;

                public String getTag() {
                    return tag;
                }

                public void setTag(String tag) {
                    this.tag = tag;
                }

                public String getTagDetail() {
                    return tagDetail;
                }

                public void setTagDetail(String tagDetail) {
                    this.tagDetail = tagDetail;
                }

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }
            }

            public static class ClearfixDTO {
                private String clearfix;

                public String getClearfix() {
                    return clearfix;
                }

                public void setClearfix(String clearfix) {
                    this.clearfix = clearfix;
                }
            }

            public static class SentencesDTO {
                private String key;
                private String value;

                public String getKey() {
                    return key;
                }

                public void setKey(String key) {
                    this.key = key;
                }

                public String getValue() {
                    return value;
                }

                public void setValue(String value) {
                    this.value = value;
                }
            }

            public static class SimilarsDTO {
                private String tag;
                private String tagList;

                public String getTag() {
                    return tag;
                }

                public void setTag(String tag) {
                    this.tag = tag;
                }

                public String getTagList() {
                    return tagList;
                }

                public void setTagList(String tagList) {
                    this.tagList = tagList;
                }
            }
        }
    }
}
