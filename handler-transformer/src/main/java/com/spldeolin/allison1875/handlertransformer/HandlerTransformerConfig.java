package com.spldeolin.allison1875.handlertransformer;

import javax.validation.constraints.NotEmpty;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import lombok.Data;

/**
 * Allison1875[handler-transformer]的配置
 *
 * @author Deolin 2020-08-25
 */
@Singleton
@Data
public final class HandlerTransformerConfig extends AbstractModule {

    /**
     * 控制层 @RequestBody类型所在包的包名
     */
    @NotEmpty
    protected String reqDtoPackage;

    /**
     * 控制层 @ResponseBody业务数据部分类型所在包的包名
     */
    @NotEmpty
    protected String respDtoPackage;

    /**
     * 业务层 Service接口所在包的包名
     */
    @NotEmpty
    protected String servicePackage;

    /**
     * 业务 ServiceImpl类所在包的包名
     */
    @NotEmpty
    private String serviceImplPackage;

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty
    private String author;

    /**
     * 分页对象的全限定名
     */
    private String pageTypeQualifier;

    @Override
    protected void configure() {
        bind(HandlerTransformerConfig.class).toInstance(this);
    }

}