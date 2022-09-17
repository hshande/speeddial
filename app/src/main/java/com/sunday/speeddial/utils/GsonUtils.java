package com.sunday.speeddial.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GsonUtils {

    private static final String TAG = "GsonUtils";

    // 默认的编码
    private static final String DEFAULT_CHARSET = "UTF-8";

    // 响应格式
    private static final String MIME = "text/html;charset=" + DEFAULT_CHARSET;

    /**
     * 把对象转换成JSON对象 object 为对象，一般是BEAN对象 JsonConfig 为JOSN配置对象
     *
     * @param obj bean对象 Object
     * @return json对象 JsonObject
     */
    public static JsonObject objectToJSON(Object obj) {
        Gson gson = new Gson();
        return (JsonObject) gson.toJsonTree(obj);
    }

    /**
     * 合并对象
     *
     * @param sourceObject 源对象
     * @param destObject   目标对象
     * @return 合并的结果
     */
    @SuppressWarnings("unchecked")
    public static <T> T mergeObject(T sourceObject, T destObject) {

        JsonObject sourceJson = null;
        JsonObject destJson = null;
        if (sourceObject instanceof JsonObject) {
            sourceJson = (JsonObject) sourceObject;
            destJson = (JsonObject) destObject;
        } else {
            Gson gson = new Gson();
            sourceJson = gson.toJsonTree(sourceObject).getAsJsonObject();
            destJson = gson.toJsonTree(destObject).getAsJsonObject();
            gson = null;
        }
        Iterator<Entry<String, JsonElement>> iter = destJson.entrySet().iterator();
        String fieldName;
        Set<Entry<String, JsonElement>> set = sourceJson.entrySet();
        Entry<String, JsonElement> item;
        while (iter.hasNext()) {
            item = iter.next();
            fieldName = item.getKey();
            JsonElement obj = item.getValue();
            if (set.contains(fieldName) && null != obj) {
                sourceJson.add(fieldName, obj);
            }
        }
        Class<T> cls = (Class<T>) sourceObject.getClass();
        return GsonUtils.jsonToBean(sourceJson, cls);
    }

    /**
     * 移除JSON不需要的属性
     *
     * @param jsonObj  需要移除的JSON对象
     * @param excludes 移除的名称集
     * @return JSON对象
     */
    public static JsonObject excludesProperty(JsonObject jsonObj, String[] excludes) {
        for (String excludeName : excludes) {
            if (jsonObj.has(excludeName)) {
                jsonObj.remove(excludeName);
            }
        }
        return jsonObj;
    }

    /**
     * 把Json对象中的值，根据属性相等，设置到OldObject中进行合并，返回一新的JSON对象
     *
     * @param newJson   json对象 JsonObject
     * @param oldObject bean对象 Object
     * @return json对象 JsonObject
     */
    public static JsonObject getCombineJson(JsonObject newJson, Object oldObject) {
        Gson gson = new Gson();
        JsonObject oldJson = (JsonObject) gson.toJsonTree(oldObject);
        gson = null;
        Iterator<Entry<String, JsonElement>> iter = newJson.entrySet().iterator();
        String fieldName;
        Set<Entry<String, JsonElement>> set = oldJson.entrySet();
        Entry<String, JsonElement> item;
        while (iter.hasNext()) {
            item = iter.next();
            fieldName = item.getKey();
            JsonElement obj = item.getValue();
            if (set.contains(fieldName)) {
                oldJson.add(fieldName, obj);
            }
        }
        return oldJson;
    }

    /**
     * 获取JsonObject key的数组，如果为null，返回null
     *
     * @param json json对象 JsonObject
     * @return 字符串列表     List<String>
     */
    public static List<String> getKeys(JsonObject json) {
        if (null == json) return null;
        List<String> keyList = new ArrayList<String>();
        Iterator<Entry<String, JsonElement>> it = json.entrySet().iterator();
        while (it.hasNext()) {
            keyList.add(it.next().getKey());
        }
        return keyList;
    }

    /**
     * JSON转Bean
     *
     * @param sourceJson 源JSON字符串
     * @param type       类对象的类型
     * @return 类对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T jsonToBean(String sourceJson, Type type) {
        Gson gson = new Gson();
        JsonParser jsonParser = new JsonParser();
        JsonElement jse = jsonParser.parse(sourceJson);
        T obj = (T) gson.fromJson(jse, type);
        gson = null;
        return obj;
    }

    /**
     * JSON转Bean
     *
     * @param sourceJson 源JSON字符串
     * @param type       类对象的类型
     * @return 类对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T jsonToBean(JsonElement sourceJson, Type type) {
        Gson gson = new Gson();
        T obj = (T) gson.fromJson(sourceJson, type);
        gson = null;
        return obj;
    }

    /**
     * JSON转Bean
     *
     * @param sourceJson  源JSON字符串
     * @param sourceClass 需要转的类对象
     * @return 类对象
     */
    public static <T> T jsonToBean(String sourceJson, Class<T> sourceClass) {
        Gson gson = new Gson();
        T obj = gson.fromJson(sourceJson, sourceClass);
        gson = null;
        return obj;
    }

    /**
     * JSON转Bean
     *
     * @param sourceJson  源JSON字符串
     * @param sourceClass 需要转的类对象
     * @return 类对象
     */
    public static <T> T jsonToBean(JsonElement sourceJson, Class<T> sourceClass) {
        Gson gson = new Gson();
        T obj = gson.fromJson(sourceJson, sourceClass);
        gson = null;
        return obj;
    }

    /**
     * JSON转Bean
     *
     * @param jsonStr     源JSON字符串
     * @param sourceClass 需要转的类对象
     * @param dateFormat  日期时间的格式化对象
     * @return 类对象
     */
    public static <T> T jsonToBean(String jsonStr, Class<T> sourceClass, String dateFormat) {
        if (dateFormat == null || dateFormat.length() == 0) {
            return jsonToBean(jsonStr, sourceClass);
        }
        Gson gson = new GsonBuilder()
                .setDateFormat(dateFormat)
                .create();
        T obj = gson.fromJson(jsonStr, sourceClass);
        gson = null;
        return obj;
    }

    /**
     * JSON转Bean
     *
     * @param sourceJson  源JSON对象
     * @param sourceClass 需要转的类对象
     * @param dateFormat  日期时间的格式化对象
     * @return 类对象
     */
    public static <T> T jsonToBean(JsonElement sourceJson, Class<T> sourceClass, String dateFormat) {
        if (dateFormat == null || dateFormat.length() == 0) {
            return jsonToBean(sourceJson, sourceClass);
        }
        Gson gson = new GsonBuilder()
                .setDateFormat(dateFormat)
                .create();
        T obj = gson.fromJson(sourceJson, sourceClass);
        gson = null;
        return obj;
    }


    /**
     * 获取Json的字符串
     *
     * @param obj 对象
     * @return json字符串
     */
    public static String getJsonStr(Object obj) {
        Gson gson = new Gson();
        String resultStr = gson.toJson(obj);
        gson = null;
        return resultStr;
    }

    /**
     * 获取JsonObject对象
     *
     * @param jsonStr json的字符串
     * @return JsonObject对象
     */
    public static JsonObject getJsonObject(String jsonStr) {
        JsonParser parser = new JsonParser();
        return parser.parse(jsonStr).getAsJsonObject();
    }

    /**
     * 获取JsonArray对象
     *
     * @param jsonStr json的字符串
     * @return JsonArray对象
     */
    public static JsonArray getJsonArray(String jsonStr) {
        JsonParser parser = new JsonParser();
        return parser.parse(jsonStr).getAsJsonArray();
    }

    /**
     * 获取驼峰转下划线规则的JsonObject
     *
     * @param sourceJson 源JSON
     * @return 转换后的JSON
     */
    public static JsonObject getHumpToUnderlineJsonObject(JsonObject sourceJson) {
        JsonObject destJson = getHumpToUnderlineJsonElement(sourceJson).getAsJsonObject();
        return destJson;
    }

    /**
     * 获取下划线转驼峰规则的JsonObject
     *
     * @param sourceJson 源JSON
     * @return 转换后的JSON
     */
    public static JsonObject getUnderlineToHumpJsonObject(JsonObject sourceJson) {
        JsonObject destJson = getUnderlineToHumpJsonElement(sourceJson).getAsJsonObject();
        return destJson;
    }

    /**
     * 获取驼峰转下划线规则的JsonArray
     *
     * @param sourceJson 源JSON
     * @return 转换后的JSON
     */
    public static JsonArray getHumpToUnderlineJsonArray(JsonArray sourceJson) {
        JsonArray destJson = getHumpToUnderlineJsonElement(sourceJson).getAsJsonArray();
        return destJson;
    }

    /**
     * 取驼峰转下划线规则的JsonArray
     *
     * @param sourceJson 源JSON
     * @return 转换后的JSON
     */
    public static JsonArray getUnderlineToHumpJsonArray(JsonArray sourceJson) {
        JsonArray destJson = getUnderlineToHumpJsonElement(sourceJson).getAsJsonArray();
        return destJson;
    }

    /**
     * 通过字符串获取驼峰转下划线的JsonElement对象
     *
     * @param sourceJsonStr 源Json的字符串
     * @return JsonElement对象
     */
    public static JsonElement getUnderlineToHumpJsonElement(String sourceJsonStr) {
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(sourceJsonStr);
        return getUnderlineToHumpJsonElement(jsonElement);
    }

    /**
     * 获取下划线转换为驼峰后的JsonElement对象
     *
     * @param jsonElement json对象
     * @return 转换后的JsonElement对象
     */
    public static JsonElement getUnderlineToHumpJsonElement(JsonElement jsonElement) {
        return getTranslateJsonElement(jsonElement, TranslateType.UNDERLINE_TO_HUMP);
    }

    /**
     * 获取下划线转换为驼峰后的JsonElement对象
     *
     * @param sourceJsonStr 源Json的字符串
     * @return JsonElement对象
     */
    public static JsonElement getHumpToUnderlineJsonElement(String sourceJsonStr) {
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(sourceJsonStr);
        return getHumpToUnderlineJsonElement(jsonElement);
    }

    /**
     * 获取驼峰转换为下划线后的JsonElement对象
     *
     * @param jsonElement json对象
     * @return 转换后的JsonElement对象
     */
    public static JsonElement getHumpToUnderlineJsonElement(JsonElement jsonElement) {
        return getTranslateJsonElement(jsonElement, TranslateType.HUMP_TO_UNDERLINE);
    }

    /**
     * 获取转换后的JsonElement对象
     *
     * @param jsonElement JsonElement对象
     * @param type        转换的类型
     * @return 转换的结果
     */
    private static JsonElement getTranslateJsonElement(JsonElement jsonElement, TranslateType type) {
        if (jsonElement instanceof JsonArray) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            JsonArray newJsonArray = new JsonArray();

            // 替换成新的JsonArray
            for (JsonElement item : jsonArray) {
                newJsonArray.add(getTranslateJsonElement(item, type));
            }

            // 清空旧的数据，添加新数据
            int oldJsonArraySize = jsonArray.size();
            for (int i = oldJsonArraySize - 1; i >= 0; i--) {
                jsonArray.remove(i);
            }
            jsonArray.addAll(newJsonArray);
        } else if (jsonElement instanceof JsonObject) {
            return translate(jsonElement.getAsJsonObject(), type);
        }
        return jsonElement;
    }

    /**
     * 驼峰转下划线
     *
     * @param sourceJson    源
     * @param translateType 转换类型
     * @return 目标
     */
    private static JsonObject translate(JsonObject sourceJson, TranslateType translateType) {
        JsonObject destJson = new JsonObject();
        Iterator<Entry<String, JsonElement>> iter = sourceJson.entrySet().iterator();
        String fieldName;
        JsonElement fieldValue;
        Entry<String, JsonElement> jsonItem;

        // 遍历JSON所有的属性
        while (iter.hasNext()) {
            jsonItem = iter.next();
            fieldName = jsonItem.getKey();
            fieldValue = jsonItem.getValue();

            // 子对象是JsonObject，递归转换
            if (fieldValue instanceof JsonObject) {
                fieldValue = translate((JsonObject) fieldValue, translateType);
            } else if (fieldValue instanceof JsonArray) {// JsonArray是JsonArray，子对象递归转换
                JsonArray valueArray = (JsonArray) fieldValue;
                fieldValue = translateJsonArray(valueArray, translateType);
            }

            // 转换类型
            switch (translateType) {
                case UNDERLINE_TO_HUMP:// 下划线转驼峰
                    fieldName = underLineToHumpField(fieldName);
                    break;
                case HUMP_TO_UNDERLINE:// 驼峰转下划线
                    fieldName = humpToUderlineField(fieldName);
                    break;
                default:
                    break;
            }

            destJson.add(fieldName, fieldValue);
        }
        sourceJson = null;
        return destJson;
    }

    /**
     * JsonArray转换
     *
     * @param sourceJsonArray 源JSONArray对象
     * @param translateType   转换的类型
     * @return 转换后的JsonArray对象
     */
    private static JsonArray translateJsonArray(JsonArray sourceJsonArray, TranslateType translateType) {
        JsonArray destJsonArray = new JsonArray();
        for (JsonElement valueItem : sourceJsonArray) {
            if (valueItem instanceof JsonObject) {
                destJsonArray.add(translate(valueItem.getAsJsonObject(), translateType));
            } else if (valueItem instanceof JsonArray) {
                destJsonArray.add(translateJsonArray(valueItem.getAsJsonArray(), translateType));
            } else {
                destJsonArray.add(valueItem);
            }
        }
        return destJsonArray;
    }

    /**
     * 字段驼峰类型转换为下划线类型
     *
     * @param fieldName 字段名称
     * @return 下划线类型的字符串
     */
    private static String humpToUderlineField(String fieldName) {
        Matcher matcher = HUMP_PATTERN.matcher(fieldName);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String g = matcher.group();
            matcher.appendReplacement(sb, "_" + g.toLowerCase());
        }
        matcher.appendTail(sb);

        String tmpFieldName = sb.toString();

        // Margin的转换
        matcher = UNDERLINE_MARGIN_PATTERN.matcher(tmpFieldName);
        tmpFieldName = matcher.replaceAll("$1-$3");

        // 背景的转换
        matcher = UNDERLINE_BACKGROUND_PATTERN.matcher(tmpFieldName);
        tmpFieldName = matcher.replaceAll("$1-$3");

        return tmpFieldName;
    }

    /**
     * 字段下划线类型转换为驼峰类型
     *
     * @param fieldName 字段名称
     * @return 下划线类型的字符串
     */
    private static String underLineToHumpField(String fieldName) {

        String tmpFieldName = fieldName;

        // Margin的转换
        Matcher matcher = MINUS_MARGIN_PATTERN.matcher(tmpFieldName);
        tmpFieldName = matcher.replaceAll("$1_$3");

        // 背景的转换
        matcher = MINUS_BACKGROUND_PATTERN.matcher(tmpFieldName);
        tmpFieldName = matcher.replaceAll("$1_$3");

        matcher = UNDERLINE_PATTERN.matcher(tmpFieldName);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String g = matcher.group();
            if (g.length() == 2) {
                matcher.appendReplacement(sb, g.substring(1).toUpperCase());
            }
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 转换类型
     */
    enum TranslateType {

        // 下划线转驼峰
        UNDERLINE_TO_HUMP,

        // 驼峰转下划线
        HUMP_TO_UNDERLINE
    }

    /**
     * 驼峰规则
     */
    private static final Pattern HUMP_PATTERN = Pattern.compile("[A-Z]");

    /**
     * 下划线规则
     */
    private static final Pattern UNDERLINE_PATTERN = Pattern.compile("_[a-z]");

    /**
     * 下划线的间距规则
     */
    private static final Pattern UNDERLINE_MARGIN_PATTERN = Pattern.compile("(margin)(_)(left|right|top|bottom)");

    /**
     * 减号的间距规则
     */
    private static final Pattern MINUS_MARGIN_PATTERN = Pattern.compile("(margin)(-)(left|right|top|bottom)");

    /**
     * 下划线的背景规则
     */
    private static final Pattern UNDERLINE_BACKGROUND_PATTERN = Pattern.compile("(background)(_)(image|size|repeat|color)");

    /**
     * 减号的背景规则
     */
    private static final Pattern MINUS_BACKGROUND_PATTERN = Pattern.compile("(background)(-)(image|size|repeat|color)");
}
