package com.room517.chitchat.model;

import java.io.Serializable;

/**
 * Created by imxqd on 2016/6/8.
 * 朋友圈评论的Model
 */
public class Comment implements Serializable{
    String explore_id, device_id, nickname, text, time;
    int color;

    public Comment(String explore_id, String device_id, String nickname, String text, String time, int color) {
        this.explore_id = explore_id;
        this.device_id = device_id;
        this.nickname = nickname;
        this.text = text;
        this.time = time;
        this.color = color;
    }

    public Comment() {
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getExplore_id() {
        return explore_id;
    }

    public void setExplore_id(String explore_id) {
        this.explore_id = explore_id;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
