package com.spldeolin.allison1875.transformer;

import java.nio.file.Path;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ext.NioPathDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.spldeolin.allison1875.base.exception.ConfigLoadingException;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875[transformer]的配置
 *
 * transformer模块目前暂时没有专门的配置项，所以这个类暂时没有作用
 *
 * @author Deolin 2020-02-18
 */
@Data
@Log4j2
public final class TransformerConfig {

    private static final TransformerConfig instance;

    static {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        SimpleModule forNioPath = new SimpleModule();
        forNioPath.addDeserializer(Path.class, new NioPathDeserializer());
        mapper.registerModule(forNioPath);

        try {
            instance = mapper.readValue(ClassLoader.getSystemResourceAsStream("transformer-config.yml"),
                    TransformerConfig.class);
        } catch (Exception e) {
            log.error("TransformerConfig static block failed.", e);
            throw new ConfigLoadingException();
        }
    }

    private TransformerConfig() {
    }

    public static TransformerConfig getInstance() {
        return instance;
    }

}
