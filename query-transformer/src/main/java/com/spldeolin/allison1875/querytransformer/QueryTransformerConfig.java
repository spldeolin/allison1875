package com.spldeolin.allison1875.querytransformer;

import com.spldeolin.allison1875.base.util.YamlUtils;
import lombok.Data;
import lombok.Getter;

/**
 * @author Deolin 2020-08-09
 */
@Data
public class QueryTransformerConfig {

    @Getter
    private static final QueryTransformerConfig instance = YamlUtils
            .toObject("query-transformer-config.yml", QueryTransformerConfig.class);

    private String sourceRoot;

    private String mapperXmlPath;

    private QueryTransformerConfig() {
    }

}