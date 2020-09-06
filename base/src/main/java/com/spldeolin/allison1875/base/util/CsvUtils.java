package com.spldeolin.allison1875.base.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
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

    public static final CsvMapper cm = createCsvMapper();

    private CsvUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static CsvMapper createCsvMapper() {
        CsvMapper result = new CsvMapper();
        // 忽略csv中存在，但Javabean中不存在的属性
        result.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Java8 time
        result.registerModule(JsonUtils.java8TimeModule());

        // java.util.Date
        result.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        // 反序列化时，对每个String进行trim
        result.registerModule(JsonUtils.stringTrimModule());

        // 时区
        result.setTimeZone(TimeZone.getDefault());

        // 序列化时，不按属性名排序
        result.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        return result;
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
    public static <T> String writeCsv(Collection<T> data, Class<T> clazz) {
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
