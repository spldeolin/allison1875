package com.spldeolin.allison1875.base.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
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
import com.spldeolin.allison1875.base.json.IgnoreCollectionNullElementDeserializeModule;

/**
 * 对Jackson的核心 ObjectMapper 进行配置的工具类
 *
 * @author Deolin 2020-09-19
 */
public class ObjectMapperUtils {

    /**
     * 对 ObjectMapper 进行缺省化配置
     *
     * <pre>
     * 1. 时间相关的pattern缺省为yyyy-MM-dd HH:mm:ss、yyyy-MM-dd、HH:mm:ss，时区缺省为系统时区
     * 2. 发现并注册所有 jackson-datatype-* 依赖
     * 3. 反序列化时，忽略Javabean中不存在的属性，而不是抛出异常
     * 4. 反序列化时，忽略Javabean中Collection属性对应JSON Array中的为null的元素
     * </pre>
     *
     * @see ObjectMapperUtils#findAndRegister
     * @see ObjectMapperUtils#ignoreCollectionNullElement
     * @see ObjectMapperUtils#ignoreUnknownProperties
     * @see ObjectMapperUtils#setJavaUtilDateZone
     * @see ObjectMapperUtils#setJavaTimePattern
     * @see ObjectMapperUtils#setJavaUtilDatePattern
     */
    public static <T extends ObjectMapper> T initDefault(T om) {
        ObjectMapperUtils.findAndRegister(om);
        ObjectMapperUtils.ignoreCollectionNullElement(om);
        ObjectMapperUtils.ignoreUnknownProperties(om);
        ObjectMapperUtils.setJavaUtilDateZone(om, null);
        ObjectMapperUtils.setJavaTimePattern(om, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "HH:mm:ss");
        ObjectMapperUtils.setJavaUtilDatePattern(om, "yyyy-MM-dd HH:mm:ss");
        return om;
    }

    /**
     * 使 ObjectMapper 自动发现和注册 jackson-datatype-* 所提供的Module
     */
    public static void findAndRegister(ObjectMapper om) {
        om.findAndRegisterModules();
    }

    /**
     * 使 ObjectMapper 在反序列化时，忽略Javabean中不存在的属性，而不是抛出异常
     */
    public static void ignoreUnknownProperties(ObjectMapper om) {
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 使 ObjectMapper 在反序列化时，忽略Javabean中Collection属性对应JSON Array中的为null的元素
     */
    public static void ignoreCollectionNullElement(ObjectMapper om) {
        om.registerModule(new IgnoreCollectionNullElementDeserializeModule());
    }

    /**
     * 为 ObjectMapper 配置时区
     *
     * @param timeZone 为null时缺省为TimeZone.getDefault()
     */
    public static void setJavaUtilDateZone(ObjectMapper om, TimeZone timeZone) {
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }
        om.setTimeZone(timeZone);
    }

    /**
     * 为 ObjectMapper 配置java.util.Date的全局pattern
     */
    public static void setJavaUtilDatePattern(ObjectMapper om, String pattern) {
        om.setDateFormat(new SimpleDateFormat(pattern));
    }

    /**
     * 为 ObjectMapper 分别配置（java.time.）LocalDateTime、LocalDate、LocalTime的全局pattern
     */
    public static void setJavaTimePattern(ObjectMapper om, String localDateTimePattern, String localDatePattern,
            String localTimePattern) {
        SimpleModule module = new JavaTimeModule();
        DateTimeFormatter ldtFormatter = DateTimeFormatter.ofPattern(localDateTimePattern);
        DateTimeFormatter ldFormatter = DateTimeFormatter.ofPattern(localDatePattern);
        DateTimeFormatter ltFormattter = DateTimeFormatter.ofPattern(localTimePattern);
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(ldtFormatter))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(ldtFormatter))
                .addSerializer(LocalDate.class, new LocalDateSerializer(ldFormatter))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(ldFormatter))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(ltFormattter))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(ltFormattter));
        om.registerModule(module);
    }

}