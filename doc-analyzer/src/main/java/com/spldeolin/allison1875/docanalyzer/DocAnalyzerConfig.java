package com.spldeolin.allison1875.docanalyzer;


import com.spldeolin.allison1875.base.util.YamlUtils;
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

    @Getter
    private static final DocAnalyzerConfig instance = YamlUtils
            .toObject("doc-analyzer-config.yml", DocAnalyzerConfig.class);

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

}
