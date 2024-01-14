package com.spldeolin.allison1875.docanalyzer;


import java.io.File;
import java.util.List;
import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.common.ancestor.Allison1875Config;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
import com.spldeolin.allison1875.common.util.ValidUtils;
import com.spldeolin.allison1875.docanalyzer.enums.OutputToEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * Allison1875[doc-analyzer]的配置
 *
 * @author Deolin 2020-02-18
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class DocAnalyzerConfig extends Allison1875Config {

    /**
     * 目标项目handler方法签名所依赖的项目的源码路径，相对路径、绝对路径皆可
     */
    @NotNull List<@NotNull File> dependencyProjectDirectories;

    /**
     * 全局URL前缀
     */
    @NotNull String globalUrlPrefix;

    /**
     * 文档输出到...
     */
    @NotNull OutputToEnum outputTo;

    /**
     * 文档输出到YApi时，YApi请求URL
     */
    String yapiUrl;

    /**
     * 文档输出到YApi时，YApi项目的TOKEN
     */
    String yapiToken;

    /**
     * 文档输出到markdown时，Markdown文件的目录的路径
     */
    String markdownDirectoryPath;

    /**
     * 文档输出到markdown时，是否启用cURL命令的输出
     */
    Boolean enableCurl;

    /**
     * 文档输出到markdown时，是否启用Response Body示例的输出
     */
    Boolean enableResponseBodySample;

    /**
     * 是否在该生成的地方生成 Any modifications may be overwritten by future code generations. 声明
     */
    @NotNull Boolean enableNoModifyAnnounce;

    /**
     * 是否在该生成的地方生成诸如 Allison 1875 Lot No: DA1000S-967D9357 的声明
     */
    @NotNull Boolean enableLotNoAnnounce;

    @Override
    public List<InvalidDto> invalidSelf() {
        List<InvalidDto> invalids = super.invalidSelf();
        for (File dependencyProjectDirectory : dependencyProjectDirectories) {
            if (!dependencyProjectDirectory.exists() || !dependencyProjectDirectory.isDirectory()) {
                invalids.add(new InvalidDto().setPath("dependencyProjectDirectories")
                        .setValue(ValidUtils.formatValue(dependencyProjectDirectory))
                        .setReason("must exist and be a directory"));
            }
        }
        if (outputTo == OutputToEnum.YAPI) {
            if (yapiUrl == null) {
                invalids.add(new InvalidDto().setPath("yapiUrl").setValue(ValidUtils.formatValue(yapiUrl))
                        .setReason("must not be null"));
            }
            if (yapiToken == null) {
                invalids.add(new InvalidDto().setPath("yapiToken").setValue(ValidUtils.formatValue(yapiToken))
                        .setReason("must not be null"));
            }
        }
        if (outputTo == OutputToEnum.LOCAL_MARKDOWN) {
            if (markdownDirectoryPath == null) {
                invalids.add(new InvalidDto().setPath("markdownDirectoryPath")
                        .setValue(ValidUtils.formatValue(markdownDirectoryPath)).setReason("must not be null"));
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
