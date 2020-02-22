package com.spldeolin.allison1875.base.util;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;

/**
 * CSV工具类
 *
 * @author Deolin 2019-01-14
 */
@Log4j2
public class Csvs {

    public static final CsvMapper defaultCsvMapper;

    static {
        defaultCsvMapper = new CsvMapper();

        // csv -> object时，忽略json中不认识的属性名
        defaultCsvMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 时区
        defaultCsvMapper.setTimeZone(TimeZone.getDefault());
    }

    /**
     * 读取csv
     */
    public static <T> List<T> readCsv(String csvContent, Class<T> clazz) {
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        ObjectReader reader = defaultCsvMapper.readerFor(clazz).with(schema);

        try {
            return Lists.newArrayList(reader.readValues(csvContent));
        } catch (IOException e) {
            log.error("转化List失败", e);
            throw new RuntimeException("转化List失败");
        }
    }

    /**
     * 生成csv
     */
    public static <T> String writeCsv(Collection<T> data, Class<T> clazz) {
        CsvSchema schema = defaultCsvMapper.schemaFor(clazz).withHeader();
        ObjectWriter writer = defaultCsvMapper.writer(schema);

        try {
            return writer.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("转化CSV失败", e);
            throw new RuntimeException("转化CSV失败");
        }
    }

}
