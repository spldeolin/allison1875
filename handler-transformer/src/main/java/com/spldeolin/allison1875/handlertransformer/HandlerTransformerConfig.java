package com.spldeolin.allison1875.handlertransformer;

import java.util.Collection;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.google.inject.Singleton;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Allison1875[handler-transformer]的配置
 *
 * @author Deolin 2020-08-25
 */
@Singleton
@Accessors(chain = true)
@Data
public class HandlerTransformerConfig {

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
    protected String serviceImplPackage;

    /**
     * handler 方法上的需要生成的注解
     */
    @NotNull
    protected Collection<@NotEmpty String> handlerAnnotations;

    /**
     * handler 方法签名的返回类型（使用%s占位符代替业务数据部分的泛型）
     */
    @NotEmpty
    protected String result;

    /**
     * handler 当不需要返回业务数据时，方法签名的返回值
     */
    @NotEmpty
    protected String resultVoid;

    /**
     * handler方法体的格式（使用%s占位符代替调用service的表达式）
     */
    @NotEmpty
    protected String handlerBodyPattern;

    /**
     * handler不需要返回ResponseBody的场景，handler方法体的格式（使用%s占位符代替调用service的表达式）
     */
    @NotEmpty(message = "不能为空，如果不需要返回值则指定为;")
    protected String handlerBodyPatternInNoResponseBodySituation;

    /**
     * controller需要确保存在的import
     */
    @NotEmpty
    protected Collection<@NotEmpty String> controllerImports;

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty
    protected String author;

}