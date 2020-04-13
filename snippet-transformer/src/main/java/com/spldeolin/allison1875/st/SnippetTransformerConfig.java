package com.spldeolin.allison1875.st;

import lombok.Data;

/**
 * 【snippet-transformer】的全局配置
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
