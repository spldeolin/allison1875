package com.spldeolin.allison1875.querytransformer;

import javax.validation.constraints.NotEmpty;
import com.spldeolin.allison1875.base.util.Configs;
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
            .toObjectAndThen("query-transformer-config.yml", QueryTransformerConfig.class, Configs::validate);

    /**
     * mapper.xml所在目录的相对路径（根据目标工程的情况填写）
     */
    @NotEmpty
    private String mapperXmlDirectoryPath;

    private QueryTransformerConfig() {
    }

}