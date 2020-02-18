package com.spldeolin.allison1875.da;

import com.spldeolin.allison1875.base.BaseConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 【doc-analyzer】的全局配置
 *
 * @author Deolin 2020-02-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class DocAnalyzerConfig extends BaseConfig {

    /**
     * 分页包装类的全限定名
     */
    private String commonPageTypeQualifier;

    public static final DocAnalyzerConfig CONFIG = new DocAnalyzerConfig();

    private DocAnalyzerConfig() {
        super();
        this.initLoad();
    }

    private void initLoad() {
        commonPageTypeQualifier = rawData.get("commonPageTypeQualifier");
    }

}
