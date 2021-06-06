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
     * Entity通用属性的类型
     */
    @NotNull
    private Map<String, String> entityCommonPropertyTypes;

    /**
     * Entity父类的全限定名
     */
    private String superEntityQualifier;

    /**
     * Mapper方法签名中Condition类的路径
     */
    @NotEmpty
    private String mapperConditionQualifier;

    /**
     * Mapper方法签名中Record类的路径
     */
    @NotEmpty
    private String mapperRecordQualifier;

    @Override
    protected void configure() {
        bind(QueryTransformerConfig.class).toInstance(this);
    }

}