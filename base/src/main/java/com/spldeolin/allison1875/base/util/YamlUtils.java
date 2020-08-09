package com.spldeolin.allison1875.base.util;

import java.io.IOException;
import java.io.InputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.spldeolin.allison1875.base.util.exception.JsonException;
import com.spldeolin.allison1875.base.util.exception.YamlException;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-08-09
 */
@Log4j2
public class YamlUtils {

    private static final ObjectMapper om = JsonUtils.initObjectMapper(new ObjectMapper(new YAMLFactory()));

    public static <T> T toObject(String yamlPath, Class<T> clazz) {
        InputStream is = ClassLoader.getSystemResourceAsStream(yamlPath);
        if (is == null) {
            throw new YamlException("资源文件[" + yamlPath + "]不存在");
        }

        try {
            return om.readValue(is, clazz);
        } catch (IOException e) {
            log.error("yamlPath={}, clazz={}", yamlPath, clazz, e);
            throw new JsonException(e);
        }
    }

}