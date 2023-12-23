package com.spldeolin.allison1875.docanalyzer;


import java.util.Collection;
import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.base.valid.annotation.IsDirectory;
import com.spldeolin.allison1875.docanalyzer.enums.OutputToEnum;
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
     * 文档输出到...
     */
    @NotNull
    private OutputToEnum outputTo;

    /**
     * 文档输出到YApi时，YApi请求URL
     */
    private String yapiUrl;

    /**
     * 文档输出到YApi时，YApi项目的TOKEN
     */
    private String yapiToken;

    /**
     * 文档输出到markdown时，Markdown文件的目录的路径
     */
    private String markdownDirectoryPath;

    /**
     * 文档输出到markdown时，是否启用cURL命令的输出
     */
    private Boolean enableCurl;

    /**
     * 文档输出到markdown时，是否启用Response Body示例的输出
     */
    private Boolean enableResponseBodySample;

}
