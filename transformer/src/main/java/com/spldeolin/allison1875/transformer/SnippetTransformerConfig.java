package com.spldeolin.allison1875.transformer;

import java.io.InputStream;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import com.spldeolin.allison1875.base.exception.ConfigLoadingException;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875[snippet-transformer]的配置
 *
 * snippet-transformer模块目前暂时没有专门的配置项，所以这个类暂时没有作用
 *
 * @author Deolin 2020-02-18
 */
@Data
@Log4j2
public final class SnippetTransformerConfig {

    private static final SnippetTransformerConfig instance = new SnippetTransformerConfig();

    private SnippetTransformerConfig() {
        this.initLoad();
    }

    private void initLoad() {
        Yaml yaml = new Yaml();
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("base-config.yml")) {
            Map<String, String> rawData = yaml.load(is);
            log.info(rawData);
        } catch (Exception e) {
            log.error("SnippetTransformerConfig.initLoad failed.", e);
            throw new ConfigLoadingException();
        }
    }

    public static SnippetTransformerConfig getInstance() {
        return instance;
    }

}
