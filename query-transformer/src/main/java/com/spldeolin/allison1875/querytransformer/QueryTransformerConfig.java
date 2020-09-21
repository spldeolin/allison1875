package com.spldeolin.allison1875.querytransformer;

import javax.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.Getter;

/**
 * @author Deolin 2020-08-09
 */
@Data
public class QueryTransformerConfig {

    @Getter
    private static final QueryTransformerConfig instance = new QueryTransformerConfig();

    /**
     * mapper.xml所在目录的相对路径（根据目标工程的情况填写）
     */
    @NotEmpty
    private String mapperXmlDirectoryPath;

    private QueryTransformerConfig() {
    }

}