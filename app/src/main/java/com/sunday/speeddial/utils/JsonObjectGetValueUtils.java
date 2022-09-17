package com.sunday.speeddial.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonObjectGetValueUtils {

    /**
     * 获取的对象直接转换成字符串
     * @param jsonElement
     * @param defaultValue
     * @return
     */
    public static String get(JsonElement jsonElement, String defaultValue) {

        if(jsonElement == null){
            return defaultValue;
        }
        return jsonElement.toString();

    }

    /**
     * 获取value，默认值
     * @param jsonObject
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getString(JsonObject jsonObject , String key, String defaultValue){

        if(jsonObject == null){
            return defaultValue;
        }

        if(jsonObject.has(key) && !jsonObject.get(key).isJsonNull()){
            return jsonObject.get(key).getAsString();
        }
        return defaultValue;
    }

    /**
     * 获取value，默认值
     * @param jsonObject
     * @param key
     * @param defaultValue
     * @return
     */
    public static boolean getBoolean(JsonObject jsonObject, String key, boolean defaultValue){

        if(jsonObject == null){
            return defaultValue;
        }

        if(jsonObject.has(key) && !jsonObject.get(key).isJsonNull()){
            return jsonObject.get(key).getAsBoolean();
        }

        return defaultValue;

    }

    /**
     * 获取整型数字
     * @param jsonObject
     * @param key
     * @param defaultValue
     * @return
     */
    public static int getInteger(JsonObject jsonObject, String key, int defaultValue){
        if(jsonObject == null){
            return defaultValue;
        }

        if(jsonObject.has(key) && !jsonObject.get(key).isJsonNull()){
            return jsonObject.get(key).getAsInt();
        }

        return defaultValue;

    }

    /**
     * 获取long型值
     * @param jsonObject
     * @param key
     * @param defaultValue
     * @return
     */
    public static long getLong(JsonObject jsonObject, String key, long defaultValue){

        if(jsonObject == null){
            return defaultValue;
        }

        if(jsonObject.has(key) && !jsonObject.get(key).isJsonNull()){
            return jsonObject.get(key).getAsLong();
        }

        return defaultValue;

    }

    /**
     *
     * @param jsonObject
     * @param key
     * @return
     */
    public static JsonObject getJsonObject(JsonObject jsonObject, String key){

        if(jsonObject == null){
            return new JsonObject();
        }

        if(jsonObject.has(key) && !jsonObject.get(key).isJsonNull()){
            return jsonObject.get(key).getAsJsonObject();
        }

        return new JsonObject();

    }

    public static JsonArray getJsonArray(JsonObject jsonObject, String key){

        if(jsonObject == null){
            return new JsonArray();
        }

        if(jsonObject.has(key) && !jsonObject.get(key).isJsonNull()){
            return jsonObject.get(key).getAsJsonObject().getAsJsonArray();
        }

        return new JsonArray();
    }

}
