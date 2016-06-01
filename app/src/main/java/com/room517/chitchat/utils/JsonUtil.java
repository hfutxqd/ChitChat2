package com.room517.chitchat.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Created by ywwynm on 2016/5/19.
 * 与Json相关的一些工具
 */
public class JsonUtil {

    public static JsonElement getParam(String json, String param) {
        Gson gson = new Gson();
        JsonObject jObj = gson.fromJson(json, JsonObject.class);
        return jObj.get(param);
    }

}
