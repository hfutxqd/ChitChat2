package com.room517.chitchat.model;

/**
 * Created by imxqd on 2016/6/8.
 * 朋友圈点赞的Model
 */
public class Like {
    String  explore_id, device_id;

    public Like(String explore_id, String device_id) {
        this.explore_id = explore_id;
        this.device_id = device_id;
    }
}
