package com.spldeolin.allison1875.da;

import java.io.File;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ext.NioPathDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
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

    private static DocAnalyzerConfig instace;

    /**
     * 文档输出路径
     */
    private Path docOutputDirectoryPath;

    /**
     * showdoc的api_key
     */
    private String showdocApiKey;

    /**
     * showdoc的api_token
     */
    private String showdocApiToken;

    private DocAnalyzerConfig() {
    }

    public static DocAnalyzerConfig getInstace() {
        if (instace != null) {
            return instace;
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        SimpleModule forNioPath = new SimpleModule();
        forNioPath.addDeserializer(Path.class, new NioPathDeserializer());
        mapper.registerModule(forNioPath);

        try {
            instace = mapper.readValue(ClassLoader.getSystemResourceAsStream("doc-analyzer-config.yml"),
                    DocAnalyzerConfig.class);
        } catch (Exception e) {
            log.error("DocAnalyzerConfig.getInstance failed.", e);
            throw new ConfigLoadingException();
        }

        File docOutputDirectory = instace.docOutputDirectoryPath.toFile();
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

        return instace;
    }

}
