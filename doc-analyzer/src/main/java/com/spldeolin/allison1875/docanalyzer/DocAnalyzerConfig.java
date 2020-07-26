package com.spldeolin.allison1875.docanalyzer;

import java.io.File;
import org.apache.commons.io.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.spldeolin.allison1875.base.exception.ConfigLoadingException;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875[doc-analyzer]的配置
 *
 * @author Deolin 2020-02-18
 */
@Data
@Log4j2
public final class DocAnalyzerConfig {

    private static final DocAnalyzerConfig instance;

    static {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try {
            instance = mapper.readValue(ClassLoader.getSystemResourceAsStream("doc-analyzer-config.yml"),
                    DocAnalyzerConfig.class);
        } catch (Exception e) {
            log.error("DocAnalyzerConfig static block failed.", e);
            throw new ConfigLoadingException();
        }

        File docOutputDirectory = new File(instance.docOutputDirectoryPath);
        if (!docOutputDirectory.exists()) {
            if (!docOutputDirectory.mkdirs()) {
                log.error("mkdirs [{}] failed.", docOutputDirectory);
                throw new ConfigLoadingException();
            }
        }
        try {
            FileUtils.cleanDirectory(docOutputDirectory);
        } catch (Exception e) {
            log.error("FileUtils.cleanDirectory failed. {}", docOutputDirectory, e);
            throw new ConfigLoadingException();
        }
    }

    /**
     * 文档输出路径
     */
    private String docOutputDirectoryPath;

    /**
     * 根据作者名过滤
     */
    private String filterByAuthorName;

    /**
     * 全局URL前缀
     */
    private String globalUrlPrefix;

    /**
     * YApi请求URL
     */
    private String yapiUrl;

    /**
     * YApi项目的TOKEN
     */
    private String yapiToken;

    private DocAnalyzerConfig() {
    }

    public static DocAnalyzerConfig getInstance() {
        return instance;
    }

}
