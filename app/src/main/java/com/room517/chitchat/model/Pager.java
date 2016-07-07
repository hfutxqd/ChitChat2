package com.room517.chitchat.model;

import java.io.Serializable;

/**
 * Created by imxqd on 2016/7/7.
 * Explore列表的Pager类
 */
public class Pager implements Serializable {
    long total_page = 1, next_page = 1, current_page = 1;

    public long getTotal_page() {
        return total_page;
    }

    public void setTotal_page(long total_page) {
        this.total_page = total_page;
    }

    public long getNext_page() {
        return next_page;
    }

    public void setNext_page(long next_page) {
        this.next_page = next_page;
    }

    public long getCurrent_page() {
        return current_page;
    }

    public void setCurrent_page(long current_page) {
        this.current_page = current_page;
    }
}
