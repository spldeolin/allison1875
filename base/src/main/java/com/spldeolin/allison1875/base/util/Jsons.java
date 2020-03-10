package com.spldeolin.allison1875.base.util;

import java.io.IOException;
import java.util.List;
import java.util.TimeZone;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spldeolin.allison1875.base.util.exception.JsonsException;
import lombok.extern.log4j.Log4j2;

/**
 * JSON工具类
 * <pre>
 * 支持JSON 与对象间、与对象列表间 的互相转换。
 * </pre>
 *
 * @author Deolin
 */
@Log4j2
public class Jsons {

    private static final ObjectMapper om = initObjectMapper(new ObjectMapper());

    private Jsons() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static ObjectMapper initObjectMapper(ObjectMapper om) {
        // json -> object时，忽略json中不认识的属性名
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 只有类属性可见，类的getter、setter、构造方法里的字段不会被当作JSON的字段
        om.setVisibility(om.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY).withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

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
            return om.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("object={}", object, e);
            throw new JsonsException("转化JSON失败");
        }
    }

    /**
     * 将对象转化为JSON
     */
    public static String toJson(Object object) {
        return toJson(object, om);
    }

    /**
     * 将对象转化为JSON，支持自定义ObjectMapper
     */
    public static String toJson(Object object, ObjectMapper om) {
        try {
            return om.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("object={}", object, e);
            throw new JsonsException("转化JSON失败");
        }
    }

    /**
     * 将JSON转化为对象
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        return toObject(json, clazz, om);
    }

    /**
     * 将JSON转化为对象，支持自定义ObjectMapper
     */
    public static <T> T toObject(String json, Class<T> clazz, ObjectMapper om) {
        try {
            return om.readValue(json, clazz);
        } catch (IOException e) {
            log.error("json={}, clazz={}", json, clazz, e);
            throw new JsonsException("转化对象失败");
        }
    }

    /**
     * 将JSON转化为对象列表
     */
    public static <T> List<T> toListOfObjects(String json, Class<T> clazz) {
        return toListOfObjects(json, clazz, om);
    }

    /**
     * 将JSON转化为对象列表，支持自定义ObjectMapper
     */
    public static <T> List<T> toListOfObjects(String json, Class<T> clazz, ObjectMapper om) {
        // ObjectMapper.TypeFactory没有线程隔离，所以需要new一个默认ObjectMapper
        TypeFactory typeFactory = initObjectMapper(new ObjectMapper()).getTypeFactory();
        JavaType javaType = typeFactory.constructParametricType(List.class, clazz);
        try {
            return om.readValue(json, javaType);
        } catch (IOException e) {
            log.error("json={}, clazz={}", json, clazz, e);
            throw new JsonsException("转化对象失败");
        }
    }

}