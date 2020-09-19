package com.spldeolin.allison1875.base.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spldeolin.allison1875.base.util.exception.YamlAbsentException;
import com.spldeolin.allison1875.base.util.exception.YamlException;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-08-09
 */
@Log4j2
public class YamlUtils {

    private static final ObjectMapper om = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        // 缺省配置
        ObjectMapper om = ObjectMapperUtils.initDefault(new ObjectMapper());

        return om;
    }

    public static <T> T toObject(String yamlPath, Class<T> clazz) {
        InputStream is = ClassLoader.getSystemResourceAsStream(yamlPath);
        if (is == null) {
            throw new YamlAbsentException("配置文件[" + yamlPath + "]不存在");
        }

        try {
            return om.readValue(is, clazz);
        } catch (IOException e) {
            log.error("yamlPath={}, clazz={}", yamlPath, clazz, e);
            throw new YamlException(e);
        }
    }

    public static <T> T toObjectAndThen(String yamlPath, Class<T> clazz, Consumer<T> andThen) {
        T result = toObject(yamlPath, clazz);
        andThen.accept(result);
        return result;
    }

}