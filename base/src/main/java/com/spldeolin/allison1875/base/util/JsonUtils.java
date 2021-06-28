package com.spldeolin.allison1875.base.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.json.IgnoreNullElementDeserializeModule;
import com.spldeolin.allison1875.base.util.exception.JsonException;
import lombok.extern.log4j.Log4j2;

/**
 * JSON工具类
 *
 * <pre>
 * 序列化与反序列化策略参考{@link ObjectMapperUtils#initDefault}
 * </pre>
 *
 * @author Deolin 2018-04-02
 */
@Log4j2
public class JsonUtils {

    private static final ObjectMapper om = createObjectMapper();

    public static ObjectMapper createObjectMapper() {
        ObjectMapper om = new ObjectMapper();

        // 反序列化时，忽略Javabean中Collection属性对应JSON Array中的为null的元素
        om.registerModule(new IgnoreNullElementDeserializeModule());

        // 反序列化时，忽略Javabean中不存在的属性，而不是抛出异常
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 使用所在操作系统的时区
        om.setTimeZone(TimeZone.getDefault());

        // 配置Java8的LocalDateTime、LocalDate、LocalTime的pattern（yyyy-MM-dd HH:mm:ss、yyyy-MM-dd、HH:mm:ss）
        om.registerModule(java8timeSimplePattern());

        // 配置java.util.Date的pattern
        om.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        return om;
    }

    private static SimpleModule java8timeSimplePattern() {
        SimpleModule module = new JavaTimeModule();
        DateTimeFormatter ldtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter ldFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter ltFormattter = DateTimeFormatter.ofPattern("HH:mm:ss");
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(ldtFormatter))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(ldtFormatter))
                .addSerializer(LocalDate.class, new LocalDateSerializer(ldFormatter))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(ldFormatter))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(ltFormattter))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(ltFormattter));
        return module;
    }

    private JsonUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
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
     * 将对象转化为美化后的JSON
     */
    public static String toJsonPrettily(Object object) {
        return toJsonPrettily(object, om);
    }

    /**
     * 将对象转化为美化后的JSON
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
     * 压缩JSON（去除美化JSON中多余的换行与空格，如果参数字符串不是一个JSON，则无事发生）
     */
    public static String compressJson(String json) {
        try {
            Map<?, ?> map = om.readValue(json, Map.class);
            return toJson(map);
        } catch (JsonProcessingException e) {
            // is not a json
            return json;
        }
    }

    /**
     * 将JSON转化为对象
     */
    public static <T> T toObject(String json, Class<T> clazz) throws JsonException {
        return toObject(json, clazz, om);
    }

    /**
     * 将JSON转化为对象
     */
    public static <T> T toObject(String json, Class<T> clazz, ObjectMapper om) throws JsonException {
        try {
            return om.readValue(json, clazz);
        } catch (IOException e) {
            log.error("json={}, clazz={}", json, clazz, e);
            throw new JsonException(e);
        }
    }

    /**
     * 将JSON转化为对象列表
     */
    public static <T> List<T> toListOfObject(String json, Class<T> clazz) throws JsonException {
        return toListOfObject(json, clazz, om);
    }

    /**
     * 将JSON转化为对象列表
     */
    public static <T> List<T> toListOfObject(String json, Class<T> clazz, ObjectMapper om) throws JsonException {
        try {
            @SuppressWarnings("unchecked") Class<T[]> arrayClass = (Class<T[]>) Class
                    .forName("[L" + clazz.getName() + ";");
            return Lists.newArrayList(om.readValue(json, arrayClass));
        } catch (IOException | ClassNotFoundException e) {
            log.error("json={}, clazz={}", json, clazz, e);
            throw new JsonException(e);
        }
    }

    /**
     * JSON -> 参数化的对象
     */
    public static <T> T toParameterizedObject(String json, TypeReference<T> typeReference) throws JsonException {
        return toParameterizedObject(json, typeReference, om);
    }

    /**
     * JSON -> 参数化的对象
     */
    public static <T> T toParameterizedObject(String json, TypeReference<T> typeReference, ObjectMapper om)
            throws JsonException {
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
     * <strong>除非JSON对应数据结构在运行时是变化的，否则不建议使这个方法</strong>
     */
    public static JsonNode toTree(String json) throws JsonException {
        return toTree(json, om);
    }

    /**
     * JSON -> JsonNode对象
     *
     * <strong>除非JSON对应数据结构在运行时是变化的，否则不建议使这个方法</strong>
     */
    public static JsonNode toTree(String json, ObjectMapper om) throws JsonException {
        try {
            return om.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("json={}", json, e);
            throw new JsonException(e);
        }
    }

}