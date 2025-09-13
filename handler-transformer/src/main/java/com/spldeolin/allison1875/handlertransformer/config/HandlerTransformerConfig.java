package com.spldeolin.allison1875.handlertransformer.config;

import java.io.File;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Allison1875[handler-transformer]的配置
 *
 * @author Deolin 2020-08-25
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HandlerTransformerConfig {

    /**
     * 分页对象的全限定名
     */
    @NotEmpty
    String pageTypeQualifier;

    /**
     * 启用「一个Controller均调用同一个Service」的模式
     */
    @NotNull
    Boolean enableOneService = false;

    /**
     * Service接口所在的SourcePath（相对于pom所在basedir的相对路径 或 绝对路径 皆可）
     */
    File serviceSourcePath;

    /**
     * ServiceImpl类所在的SourcePath（相对于pom所在basedir的相对路径 或 绝对路径 皆可）
     */
    File serviceImplSourcePath;

    /**
     * DTO类所在的SourcePath（相对于pom所在basedir的相对路径 或 绝对路径 皆可）
     */
    File dtoSourcePath;

}