package com.spldeolin.allison1875.da;

import java.io.File;
import java.nio.file.Path;
import com.spldeolin.allison1875.base.BaseConfig;
import com.spldeolin.allison1875.base.exception.ConfigLoadingException;
import lombok.Data;

/**
 * 【doc-analyzer】的全局配置
 *
 * @author Deolin 2020-02-18
 */
@Data
public final class DocAnalyzerConfig {

    /**
     * Maven打包的命令行
     */
    private String mavenPackageCommandLine;

    /**
     * 分页包装类的全限定名
     */
    private String commonPageTypeQualifier;

    /**
     * 文档输出路径
     */
    private Path docOutputDirectoryPath;

    public static final DocAnalyzerConfig CONFIG = new DocAnalyzerConfig();

    private DocAnalyzerConfig() {
        super();
        this.initLoad();
    }

    private void initLoad() {
        mavenPackageCommandLine = BaseConfig.getInstace().getRawData().get("mavenPackageCommandLine");

        commonPageTypeQualifier = BaseConfig.getInstace().getRawData().get("commonPageTypeQualifier");

        File docOutputDirectory = new File(BaseConfig.getInstace().getRawData().get("docOutputDirectoryPath"));
        if (!docOutputDirectory.exists()) {
            if (!docOutputDirectory.mkdirs()) {
                throw new ConfigLoadingException("文件" + docOutputDirectory + "创建失败");
            }
        }
        docOutputDirectoryPath = docOutputDirectory.toPath();
    }

}
