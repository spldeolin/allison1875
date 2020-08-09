package com.spldeolin.allison1875.pqt;

import com.spldeolin.allison1875.base.util.YamlUtils;
import lombok.Data;

/**
 * @author Deolin 2020-08-09
 */
@Data
public class PersistenceQueryTransformerConfig {

    private static final PersistenceQueryTransformerConfig instance = YamlUtils
            .toObject("persistence-query-transformer-config.yml", PersistenceQueryTransformerConfig.class);

    private String sourceRoot;

    private String mapperXmlPath;

    private PersistenceQueryTransformerConfig() {
    }

}