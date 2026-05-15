package com.spldeolin.allison1875.cli.it.docanalyzer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 * no-handler-detected 集成测试。
 *
 * <p>验证项目中没有任何 @RequestMapping handler 时，工具正常结束（不抛异常），
 * 输出空文档或不输出文档。
 *
 * @author Deolin 2026-05-15
 */
public class NoHandlerDetectedItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        // 不应抛异常
        runDocAnalyzer("no-handler-detected");

        // api-docs 目录不应存在（没有 handler 就没有文档输出）
        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(!apiDocsDir.exists() || (apiDocsDir.isDirectory() && apiDocsDir.list().length == 0),
                "api-docs directory should not exist or be empty when no handler detected");
    }

}
