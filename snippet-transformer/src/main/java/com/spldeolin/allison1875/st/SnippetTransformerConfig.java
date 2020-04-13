package com.spldeolin.allison1875.st;

import lombok.Data;

/**
 * Allison1875[snippet-transformer]的配置
 *
 * snippet-transformer模块目前暂时没有专门的配置项，所以这个类暂时没有作用
 *
 * @author Deolin 2020-02-18
 */
@Data
public final class SnippetTransformerConfig {

    private static final SnippetTransformerConfig instance = new SnippetTransformerConfig();

    private SnippetTransformerConfig() {
        this.initLoad();
    }

    private void initLoad() {
    }

    public static SnippetTransformerConfig getInstance() {
        return instance;
    }

}
