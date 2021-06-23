package com.spldeolin.allison1875.docanalyzer;


import java.util.Collection;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.base.valid.annotation.IsDirectory;
import lombok.Data;

/**
 * Allison1875[doc-analyzer]的配置
 *
 * @author Deolin 2020-02-18
 */
@Data
public final class DocAnalyzerConfig {

    /**
     * 目标项目handler方法签名所依赖的项目的源码路径，相对路径、绝对路径皆可
     */
    @NotNull
    private Collection<@NotNull @IsDirectory String> dependencyProjectPaths;

    /**
     * 全局URL前缀
     */
    @NotNull
    private String globalUrlPrefix;

    /**
     * YApi请求URL
     */
    @NotEmpty
    private String yapiUrl;

    /**
     * YApi项目的TOKEN
     */
    @NotEmpty
    private String yapiToken;

}
