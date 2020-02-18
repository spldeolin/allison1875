package com.spldeolin.allison1875.st;

import com.spldeolin.allison1875.base.BaseConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 【snippet-transformer】的全局配置
 *
 * @author Deolin 2020-02-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class SnippetTransformerConfig extends BaseConfig {

    public static final SnippetTransformerConfig CONFIG = new SnippetTransformerConfig();

    private SnippetTransformerConfig() {
        super();
        this.initLoad();
    }

    private void initLoad() {
    }

}
