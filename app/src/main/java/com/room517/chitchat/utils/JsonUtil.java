package com.room517.chitchat.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ywwynm on 2016/5/19.
 * 与Json相关的一些工具
 */
public class JsonUtil {

    public static JsonElement getParam(String json, String param) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        return jsonObject.get(param);
    }

    public static JsonElement getParam(JsonElement jsonElement, String param) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(jsonElement, JsonObject.class);
        return jsonObject.get(param);
    }

    public static List<JsonElement> getJsonElements(String arrayJson) {
        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(arrayJson, JsonArray.class);
        List<JsonElement> ret = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray) {
            ret.add(jsonElement);
        }
        return ret;
    }

    public static <T> T getObject(JsonElement jsonElement, Class<T> tClass) {
        Gson gson = new Gson();
        return gson.fromJson(jsonElement, tClass);
    }

}
