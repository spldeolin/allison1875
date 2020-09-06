package com.spldeolin.allison1875.base.util;

import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.spldeolin.allison1875.base.json.CollectionIgnoreNullElementDeserializerModule;
import com.spldeolin.allison1875.base.json.NumberToStringMightJsonSerializer;
import com.spldeolin.allison1875.base.json.StringTrimDeserializer;
import com.spldeolin.allison1875.base.util.exception.JsonException;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON工具类
 *
 * <pre>
 * 特性：
 * 1. 支持Guava的数据结构，如Multimap、Table等
 * 2. 反系列化时，忽略JSON中提供了而Javabean中不存在的属性，不抛出异常
 * 3. 序列化时，支持将Long、long、BigInteger类型转化为String，作用范围可指定
 *    （可通过@JsonSerializer覆盖这个特性）
 * 4. 支持Java8 time包下的LocalDate、LocalTime、LocalDateTime，缺省pattern分别为"yyyy-MM-dd"、"HH:mm:ss"、"yyyy-MM-dd HH:mm:ss"
 *    （可通过@JsonFormat覆盖这个特性）
 * 5. java.util.Date的缺省pattern为yyyy-MM-dd HH:mm:ss
 *    （可通过@JsonFormat覆盖这个特性）
 * 6. 时区默认为东8区
 *    （可通过@JsonFormat覆盖这个特性）
 * 7. 反序列化时，忽略Collection中为null的元素，不add(null)到容器对象中
 * 8. 反序列化时，对每个String进行trim
 *    （可通过@JsonDeserializer覆盖这个特性）
 * </pre>
 *
 * @author Deolin 2018-04-02
 */
@Slf4j
public class JsonUtils {

    private static final ObjectMapper om = createObjectMapper();

    private JsonUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static ObjectMapper createObjectMapper() {
        return createObjectMapper(new NumberToStringMightJsonSerializer());
    }

    public static ObjectMapper createObjectMapper(NumberToStringMightJsonSerializer numberToStringMightJsonSerializer) {
        // Guava的数据结构
        om.registerModule(new GuavaModule());

        // 忽略json中存在，但Javabean中不存在的属性
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Long to String
        om.registerModule(toStringModule(numberToStringMightJsonSerializer));

        // Java8 time
        om.registerModule(java8TimeModule());

        // java.util.Date
        om.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        // 反序列化时，忽略Collection中为null的元素
        om.registerModule(new CollectionIgnoreNullElementDeserializerModule());

        // 反序列化时，对每个String进行trim
        om.registerModule(stringTrimModule());

        // 时区
        om.setTimeZone(TimeZone.getDefault());
        return om;
    }

    public static SimpleModule stringTrimModule() {
        SimpleModule result = new SimpleModule();
        result.addDeserializer(String.class, new StringTrimDeserializer());
        return result;
    }

    public static SimpleModule java8TimeModule() {
        SimpleModule result = new JavaTimeModule();
        DateTimeFormatter date = TimeUtils.DEFAULT_DATE_FORMATTER;
        DateTimeFormatter time = TimeUtils.DEFAULT_TIME_FORMATTER;
        DateTimeFormatter dateTime = TimeUtils.DEFAULT_DATE_TIME_FORMATTER;
        result.addSerializer(LocalDate.class, new LocalDateSerializer(date))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(date))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(time))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(time))
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTime))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTime));
        return result;
    }

    private static SimpleModule toStringModule(NumberToStringMightJsonSerializer numberToStringMightJsonSerializer) {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(BigInteger.class, numberToStringMightJsonSerializer);
        simpleModule.addSerializer(Long.class, numberToStringMightJsonSerializer);
        simpleModule.addSerializer(Long.TYPE, numberToStringMightJsonSerializer);
        return simpleModule;
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
     * @throws JsonException 任何原因转化失败时，抛出这个异常，如果需要补偿处理，可以进行捕获
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        return toObject(json, clazz, om);
    }

    /**
     * 将JSON转化为对象
     *
     * @throws JsonException 任何原因转化失败时，抛出这个异常，如果需要补偿处理，可以进行捕获
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
     * @throws JsonException 任何原因转化失败时，抛出这个异常，如果需要补偿处理，可以进行捕获
     */
    public static <T> List<T> toListOfObject(String json, Class<T> clazz) {
        return toListOfObject(json, clazz, om);
    }

    /**
     * 将JSON转化为对象列表
     *
     * @throws JsonException 任何原因转化失败时，抛出这个异常，如果需要补偿处理，可以进行捕获
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
     * @throws JsonException 任何原因转化失败时，抛出这个异常，如果需要补偿处理，可以进行捕获
     */
    public static <T> T toParameterizedObject(String json, TypeReference<T> typeReference) {
        return toParameterizedObject(json, typeReference, om);
    }

    /**
     * JSON -> 参数化的对象
     *
     * 示例： Collection<<User<UserAddress>> users = JsonUtils.toParameterizedObject(text);
     *
     * @throws JsonException 任何原因转化失败时，抛出这个异常，如果需要补偿处理，可以进行捕获
     */
    public static <T> T toParameterizedObject(String json, TypeReference<T> typeReference, ObjectMapper om) {
        try {
            return om.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("json={}, typeReference={}", json, typeReference, e);
            throw new JsonException(e);
        }
    }

    /**
     * JSON -> JsonNode对象
     *
     * <strong>不建议使用</strong>
     *
     * @throws JsonException 任何原因转化失败时，抛出这个异常，如果需要补偿处理，可以进行捕获
     */
    public static JsonNode toTree(String json) {
        return toTree(json, om);
    }

    /**
     * JSON -> JsonNode对象
     *
     * <strong>不建议使用</strong>
     *
     * @throws JsonException 任何原因转化失败时，抛出这个异常，如果需要补偿处理，可以进行捕获
     */
    public static JsonNode toTree(String json, ObjectMapper om) {
        try {
            return om.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("json={}", json, e);
            throw new JsonException(e);
        }
    }

}