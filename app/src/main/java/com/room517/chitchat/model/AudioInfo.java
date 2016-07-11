package com.room517.chitchat.model;

import com.google.gson.Gson;

/**
 * Created by ywwynm on 2016/7/11.
 * 保存了音频的一些信息，比如URI、时长、平均分贝、最大分贝等
 */
public class AudioInfo {

    private String uri;
    private int duration;
    private int maxDecibel;
    private int averageDecibel;

    public AudioInfo(String uri, int duration, int maxDecibel, int averageDecibel) {
        this.uri            = uri;
        this.duration       = duration;
        this.maxDecibel     = maxDecibel;
        this.averageDecibel = averageDecibel;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getMaxDecibel() {
        return maxDecibel;
    }

    public void setMaxDecibel(int maxDecibel) {
        this.maxDecibel = maxDecibel;
    }

    public int getAverageDecibel() {
        return averageDecibel;
    }

    public void setAverageDecibel(int averageDecibel) {
        this.averageDecibel = averageDecibel;
    }

    public static AudioInfo fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, AudioInfo.class);
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
