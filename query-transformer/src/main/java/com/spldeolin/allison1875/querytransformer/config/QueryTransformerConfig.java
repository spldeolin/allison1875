package com.spldeolin.allison1875.querytransformer.config;

import java.io.File;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-08-09
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QueryTransformerConfig {

    /**
     * 持久层所在的SourcePath（相对于pom所在basedir的相对路径 或 绝对路径 皆可）
     */
    File persistenceSourcePath;

}