package com.spldeolin.allison1875.base.util;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.exception.CsvException;

/**
 * CSV工具类
 *
 * @author Deolin 2019-01-14
 */
public class CsvUtils {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(CsvUtils.class);

    public static final CsvMapper cm = createCsvMapper();

    private CsvUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static CsvMapper createCsvMapper() {
        // 缺省配置
        CsvMapper cm = ObjectMapperUtils.initDefault(new CsvMapper());

        // 序列化时，不按属性名排序
        cm.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);

        return cm;
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
