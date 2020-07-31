package com.spldeolin.allison1875.base.util;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.spldeolin.allison1875.base.util.exception.JsonException;
import lombok.extern.log4j.Log4j2;

/**
 * JSON工具类
 *
 * <pre>
 * 支持JSON与对象间的互相转换
 * </pre>
 *
 * @author Deolin 2018-04-02
 */
@Log4j2
public class JsonUtils {

    private static final ObjectMapper om = initObjectMapper(new ObjectMapper());

    private JsonUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static ObjectMapper initObjectMapper(ObjectMapper om) {
        // Java8时间类型的全局格式
        om.registerModule(timeModule());

        // 反序列化时，忽略json中存在，但Javabean中不存在的属性
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 时区
        om.setTimeZone(TimeZone.getDefault());

        // 只有类属性可见，类的getter、setter、构造方法里的字段不会被当作JSON的字段
        om.setVisibility(om.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY).withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        return om;
    }

    public static SimpleModule timeModule() {
        SimpleModule javaTimeModule = new JavaTimeModule();
        DateTimeFormatter date = TimeUtils.DEFAULT_DATE_FORMATTER;
        DateTimeFormatter time = TimeUtils.DEFAULT_TIME_FORMATTER;
        DateTimeFormatter dateTime = TimeUtils.DEFAULT_DATE_TIME_FORMATTER;
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(date))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(date))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(time))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(time))
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTime))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTime));
        return javaTimeModule;
    }

    /**
     * 将对象转化为JSON
     */
    public static String toJson(Object object) {
        return toJson(object, om);
    }

    /**
     * 将对象转化为JSON
     */
    public static String toJson(Object object, ObjectMapper om) {
        try {
            return om.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("object={}", object, e);
            throw new JsonException(e);
        }
    }

    /**
     * 将对象转化为JSON，结果是美化的
     */
    public static String toJsonPrettily(Object object) {
        return toJsonPrettily(object, om);
    }

    /**
     * 将对象转化为JSON，结果是美化的
     */
    public static String toJsonPrettily(Object object, ObjectMapper om) {
        try {
            return om.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("object={}", object, e);
            throw new JsonException("转化JSON失败");
        }
    }

    /**
     * 将JSON转化为对象
     *
     * @throws JsonException 转化失败时，抛出这个Runtime异常，如果需要补偿处理，可以捕获这个异常
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        return toObject(json, clazz, om);
    }

    /**
     * 将JSON转化为对象
     *
     * @throws JsonException 转化失败时，抛出这个Runtime异常，如果需要补偿处理，可以捕获这个异常
     */
    public static <T> T toObject(String json, Class<T> clazz, ObjectMapper om) {
        try {
            return om.readValue(json, clazz);
        } catch (IOException e) {
            log.error("json={}, clazz={}", json, clazz, e);
            throw new JsonException(e);
        }
    }

    /**
     * 将JSON转化为对象列表
     *
     * @throws JsonException 转化失败时，抛出这个Runtime异常，如果需要补偿处理，可以捕获这个异常
     */
    public static <T> List<T> toListOfObject(String json, Class<T> clazz) {
        return toListOfObject(json, clazz, om);
    }

    /**
     * 将JSON转化为对象列表
     *
     * @throws JsonException 转化失败时，抛出这个Runtime异常，如果需要补偿处理，可以捕获这个异常
     */
    public static <T> List<T> toListOfObject(String json, Class<T> clazz, ObjectMapper om) {
        try {
            return om.readValue(json, new TypeReference<List<T>>() {
            });
        } catch (IOException e) {
            log.error("json={}, clazz={}", json, clazz, e);
            throw new JsonException(e);
        }
    }

    /**
     * JSON -> 参数化的对象
     *
     * 示例： Collection<<User<UserAddress>> users = JsonUtils.toParameterizedObject(text);
     *
     * @throws JsonException 转化失败时，抛出这个Runtime异常，如果需要补偿处理，可以捕获这个异常
     */
    public static <T> T toParameterizedObject(String json) {
        try {
            return om.readValue(json, new TypeReference<T>() {
            });
        } catch (IOException e) {
            log.error("json={}", json, e);
            throw new JsonException(e);
        }
    }

}