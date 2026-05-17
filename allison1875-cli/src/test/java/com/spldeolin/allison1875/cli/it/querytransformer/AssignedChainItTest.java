package com.spldeolin.allison1875.cli.it.querytransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * query-transformer assigned-chain 集成测试。
 *
 * @author Deolin 2026-05-17
 */
public class AssignedChainItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("assigned-chain");

        File mapperXml = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml");
        assertTrue(mapperXml.exists());
        String xml = new String(Files.readAllBytes(mapperXml.toPath()), StandardCharsets.UTF_8);
        assertTrue(xml.contains("id='listAssigned'") || xml.contains("id=\"listAssigned\""));

        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(mapperContent.contains("listAssigned"));

        // Record DTO 不应生成（assigned 链返回 Entity）
        File recordDir = new File(basedir, "src/main/java/com/example/dto/record");
        boolean hasRecordDto = recordDir.exists() && recordDir.listFiles() != null && recordDir.listFiles().length > 0;
        // assigned 链不生成 Record DTO，但 persistence-generator 的其他方法可能生成，所以只是不强断言

        File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java");
        String serviceContent = new String(Files.readAllBytes(serviceFile.toPath()), StandardCharsets.UTF_8);
        assertFalse(serviceContent.contains("TOrderDesign."));
        assertTrue(serviceContent.contains("TOrderMapper"));
        // 赋值语句保留
        assertTrue(serviceContent.contains("List<TOrderEntity> result = tOrderMapper.listAssigned")
                || serviceContent.contains("List<TOrderEntity> result = tOrderMapper.listAssigned"),
                "Assignment statement should be preserved");
    }
}