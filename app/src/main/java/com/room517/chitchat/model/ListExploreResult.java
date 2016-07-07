package com.room517.chitchat.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by imxqd on 2016/7/7.
 * ListExploreByPager的结果类
 */
public class ListExploreResult implements Serializable {
    Pager pager;
    ArrayList<Explore> data;

    public Pager getPager() {
        return pager;
    }

    public void setPager(Pager pager) {
        this.pager = pager;
    }

    public ArrayList<Explore> getData() {
        return data;
    }

    public void setData(ArrayList<Explore> data) {
        this.data = data;
    }
}
