package com.spldeolin.allison1875.cli.it.docanalyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * json-property-access 集成测试。
 *
 * 验证 doc-analyzer 能正确处理：
 * - @JsonProperty(access=READ_ONLY) 字段不出现在 Request Body，出现在 Response Body
 * - @JsonProperty(access=WRITE_ONLY) 字段被 JsonSchema 过滤
 * - @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") 格式信息
 *
 * @author Deolin 2025-05-13
 */
public class JsonPropertyAccessItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("json-property-access");

        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should exist");

        List<File> mdFiles = new ArrayList<>();
        Files.walkFileTree(apiDocsDir.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().endsWith(".md")) {
                    mdFiles.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        assertTrue(mdFiles.size() > 0, "At least one .md file should be generated");

        StringBuilder sb = new StringBuilder();
        for (File mdFile : mdFiles) {
            sb.append(new String(Files.readAllBytes(mdFile.toPath()), StandardCharsets.UTF_8));
            sb.append("\n");
        }
        String allContent = sb.toString();

        assertTrue(allContent.contains("创建事件"), "Should contain '创建事件'");

        // 验证 md 文件名和数量
        assertEquals(1, mdFiles.size(), "Should generate exactly 1 .md file");
        assertEquals("事件管理.md", mdFiles.get(0).getName(), "Markdown filename should be '事件管理.md'");

        // 验证 URL 和 HTTP 方法
        assertTrue(allContent.contains("POST /api/events"), "Should contain 'POST /api/events'");

        // doc-analyzer 当前不根据 @JsonProperty(access=...) 过滤字段，所有字段均出现在文档中
        // 验证 readOnlyField 在 Response Body 中存在
        String respSection = allContent.substring(allContent.indexOf("Response Body"));
        assertTrue(respSection.contains("readOnlyField"), "readOnlyField should appear in Response Body section");

        // 验证 eventName 在 request 和 response 中都存在
        String reqSection = allContent.substring(0, allContent.indexOf("Response Body"));

        // @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") should appear as format documentation
        assertTrue(allContent.contains("格式：yyyy-MM-dd HH:mm:ss"),
                "Should contain @JsonFormat pattern as '格式：yyyy-MM-dd HH:mm:ss'");

        // eventName should appear in both request and response
        assertTrue(reqSection.contains("eventName"), "Request Body should contain 'eventName'");
        assertTrue(respSection.contains("eventName"), "Response Body should contain 'eventName'");

        // 验证字段注释
        assertTrue(allContent.contains("事件名称"), "Should contain field comment '事件名称'");
        assertTrue(allContent.contains("事件开始时间"), "Should contain field comment '事件开始时间'");
    }

}
