package com.spldeolin.allison1875.base.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.exception.CsvException;
import lombok.extern.log4j.Log4j2;

/**
 * CSV工具类
 *
 * @author Deolin 2019-01-14
 */
@Log4j2
public class CsvUtils {

    private static final CsvMapper cm = createCsvMapper();

    public static CsvMapper createCsvMapper() {
        CsvMapper cm = new CsvMapper();

        // 反序列化时，忽略Javabean中不存在的属性，而不是抛出异常
        cm.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 使用所在操作系统的时区
        cm.setTimeZone(TimeZone.getDefault());

        // 配置Java8的LocalDateTime、LocalDate、LocalTime的pattern（yyyy-MM-dd HH:mm:ss、yyyy-MM-dd、HH:mm:ss）
        cm.registerModule(java8timeSimplePattern());

        // 配置java.util.Date的pattern
        cm.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        // 序列化时，不按属性名排序
        cm.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);

        return cm;
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

    private CsvUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * 读取csv
     */
    public static <T> List<T> readCsv(String csvContent, Class<T> clazz) throws CsvException {
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        ObjectReader reader = cm.readerFor(clazz).with(schema);

        try {
            return Lists.newArrayList(reader.readValues(csvContent));
        } catch (IOException e) {
            log.error("csvContent={}, clazz={}", csvContent, clazz, e);
            throw new CsvException(e);
        }
    }

    /**
     * 生成csv
     */
    public static <T> String writeCsv(Collection<T> data, Class<T> clazz) throws CsvException {
        CsvSchema schema = cm.schemaFor(clazz).withHeader();
        ObjectWriter writer = cm.writer(schema);

        try {
            return writer.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("data={}, clazz={}", data, clazz, e);
            throw new CsvException(e);
        }
    }

}
