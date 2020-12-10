package com.spldeolin.allison1875.querytransformer;

import java.util.Map;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import lombok.Data;

/**
 * @author Deolin 2020-08-09
 */
@Singleton
@Data
public final class QueryTransformerConfig extends AbstractModule {

    /**
     * mapper.xml所在目录的相对路径（根据目标工程的情况填写）
     */
    @NotEmpty
    protected String mapperXmlDirectoryPath;

    /**
     * Entity通用属性的类型
     */
    @NotNull
    protected Map<String, String> entityCommonPropertyTypes;

    @Override
    protected void configure() {
        bind(QueryTransformerConfig.class).toInstance(this);
    }

}