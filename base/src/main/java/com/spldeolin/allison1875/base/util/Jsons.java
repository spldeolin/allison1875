package com.spldeolin.allison1875.base.util;

import java.io.IOException;
import java.util.List;
import java.util.TimeZone;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Jsons {

    private Jsons() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    private static final ObjectMapper defaultObjectMapper;

    static {
        defaultObjectMapper = newDefaultObjectMapper();
    }

    public static ObjectMapper newDefaultObjectMapper() {
        ObjectMapper om = new ObjectMapper();

        // json -> object时，忽略json中不认识的属性名
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 时区
        om.setTimeZone(TimeZone.getDefault());
        return om;
    }

    /**
     * 美化JSON
     * <pre>
     * e.g.:
     * {
     *   "name" : "Deolin",
     *   "age" : 18,
     *   "isVip" : true
     * }
     * </pre>
     */
    public static String beautify(Object object) {
        try {
            return defaultObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("转化JSON失败", e);
            throw new RuntimeException("转化JSON失败");
        }
    }

    /**
     * 压缩JSON
     * <pre>
     * e.g.:
     * {"name":"Deolin","age":18,"isVip":true}
     * </pre>
     */
    public static String compress(String json) {
        return json.replace(" ", "").replace("\r", "").replace("\n", "").replace("\t", "");
    }

    /**
     * 将对象转化为JSON
     */
    public static String toJson(Object object) {
        return toJson(object, defaultObjectMapper);
    }

    /**
     * 将对象转化为JSON，支持自定义ObjectMapper
     */
    public static String toJson(Object object, ObjectMapper om) {
        try {
            return om.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("转化JSON失败", e);
            throw new RuntimeException("转化JSON失败");
        }
    }

    /**
     * 将JSON转化为对象
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        return toObject(json, clazz, defaultObjectMapper);
    }

    /**
     * 将JSON转化为对象，支持自定义ObjectMapper
     */
    public static <T> T toObject(String json, Class<T> clazz, ObjectMapper om) {
        try {
            return om.readValue(json, clazz);
        } catch (IOException e) {
            log.error("转化对象失败", e);
            throw new RuntimeException("转化对象失败");
        }
    }

    /**
     * 将JSON转化为对象列表
     */
    public static <T> List<T> toListOfObjects(String json, Class<T> clazz) {
        return toListOfObjects(json, clazz, defaultObjectMapper);
    }

    /**
     * 将JSON转化为对象列表，支持自定义ObjectMapper
     */
    public static <T> List<T> toListOfObjects(String json, Class<T> clazz, ObjectMapper om) {
        // ObjectMapper.TypeFactory没有线程隔离，所以需要new一个默认ObjectMapper
        JavaType javaType = newDefaultObjectMapper().getTypeFactory().constructParametricType(List.class, clazz);
        try {
            return om.readValue(json, javaType);
        } catch (IOException e) {
            log.error("转化对象列表失败", e);
            throw new RuntimeException("转化对象失败");
        }

    }

}