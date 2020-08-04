package com.spldeolin.allison1875.docanalyzer;


import java.io.InputStream;
import java.util.Objects;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.spldeolin.allison1875.base.exception.ConfigLoadingException;
import com.spldeolin.allison1875.base.util.JsonUtils;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875[doc-analyzer]的配置
 *
 * @author Deolin 2020-02-18
 */
@Data
@Log4j2
public final class DocAnalyzerConfig {

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

    @Getter
    private static final DocAnalyzerConfig instance = createInstance();

    private DocAnalyzerConfig() {
    }

    private static DocAnalyzerConfig createInstance() {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        JsonUtils.initObjectMapper(om);
        try {
            InputStream inputStream = Objects
                    .requireNonNull(ClassLoader.getSystemResourceAsStream("doc-analyzer-config.yml"),
                            "doc-analyzer-config.yml not exist.");
            return om.readValue(inputStream, DocAnalyzerConfig.class);
        } catch (Exception e) {
            log.error("读取配置文件失败：{}", e.getMessage());
            throw new ConfigLoadingException();
        }
    }

    // 暂时移除输出到本地的功能
//    static {
////        File docOutputDirectory = new File(instance.docOutputDirectoryPath);
////        if (!docOutputDirectory.exists()) {
////            if (!docOutputDirectory.mkdirs()) {
////                log.error("mkdirs [{}] failed.", docOutputDirectory);
////                throw new ConfigLoadingException();
////            }
////        }
////        try {
////            FileUtils.cleanDirectory(docOutputDirectory);
////        } catch (Exception e) {
////            log.error("FileUtils.cleanDirectory failed. {}", docOutputDirectory, e);
////            throw new ConfigLoadingException();
////        }
//    }
//
//    /**
//     * 文档输出路径
//     */
//    private String docOutputDirectoryPath;

}
