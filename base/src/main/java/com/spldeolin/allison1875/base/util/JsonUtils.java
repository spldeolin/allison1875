package com.spldeolin.allison1875.base.util;

import java.io.IOException;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
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

    private static final ObjectMapper om = ObjectMapperUtils.initDefault(new ObjectMapper());

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
     * <strong>除非JSON对应数据结构在运行时是变化的，否则不建议使这个方法</strong>
     *
     * @throws JsonException 任何原因转化失败时，抛出这个异常，如果需要补偿处理，可以进行捕获
     */
    public static JsonNode toTree(String json) {
        return toTree(json, om);
    }

    /**
     * JSON -> JsonNode对象
     *
     * <strong>除非JSON对应数据结构在运行时是变化的，否则不建议使这个方法</strong>
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