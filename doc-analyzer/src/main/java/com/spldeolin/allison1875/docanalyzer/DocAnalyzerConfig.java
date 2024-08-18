package com.spldeolin.allison1875.docanalyzer;

import java.io.File;
import java.util.List;
import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.common.ancestor.Allison1875Config;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
import com.spldeolin.allison1875.common.util.ValidUtils;
import com.spldeolin.allison1875.docanalyzer.enums.FlushToEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Allison1875[doc-analyzer]的配置
 *
 * @author Deolin 2020-02-18
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocAnalyzerConfig extends Allison1875Config {

    /**
     * 目标项目handler方法签名所依赖的外部项目的源码路径（相对于basedir的相对路径 或 绝对路径 皆可）
     */
    @NotNull
    List<File> dependencyProjectDirs;

    /**
     * 全局URL前缀
     */
    @NotNull
    String globalUrlPrefix;

    /**
     * 文档保存到...
     */
    @NotNull(message = "must be 'LOCAL_MARKDOWN' or 'YAPI'")
    FlushToEnum flushTo;

    /**
     * 文档输出到YApi时，YApi请求URL
     */
    String yapiUrl;

    /**
     * 文档输出到YApi时，YApi项目的TOKEN
     */
    String yapiToken;

    /**
     * 文档输出到markdown时，Markdown文件的目录的路径（相对于basedir的相对路径 或 绝对路径 皆可）
     */
    File markdownDir;

    /**
     * 文档输出到markdown时，是否启用cURL命令的输出
     */
    Boolean enableCurl;

    /**
     * 文档输出到markdown时，是否启用Response Body示例的输出
     */
    Boolean enableResponseBodySample;

    @Override
    public List<InvalidDto> invalidSelf() {
        List<InvalidDto> invalids = super.invalidSelf();
        if (FlushToEnum.YAPI.equals(flushTo)) {
            if (yapiUrl == null) {
                invalids.add(new InvalidDto().setPath("yapiUrl").setValue(ValidUtils.formatValue(yapiUrl))
                        .setReason("must not be null"));
            }
            if (yapiToken == null) {
                invalids.add(new InvalidDto().setPath("yapiToken").setValue(ValidUtils.formatValue(yapiToken))
                        .setReason("must not be null"));
            }
        }
        if (FlushToEnum.LOCAL_MARKDOWN.equals(flushTo)) {
            if (markdownDir == null) {
                invalids.add(new InvalidDto().setPath("markdownDirectoryPath")
                        .setValue(ValidUtils.formatValue(markdownDir)).setReason("must not be null"));
            }
            if (enableCurl == null) {
                invalids.add(new InvalidDto().setPath("enableCurl").setValue(ValidUtils.formatValue(enableCurl))
                        .setReason("must not be null"));
            }
            if (enableResponseBodySample == null) {
                invalids.add(new InvalidDto().setPath("enableResponseBodySample")
                        .setValue(ValidUtils.formatValue(enableResponseBodySample)).setReason("must not be null"));
            }
        }
        return invalids;
    }

}
