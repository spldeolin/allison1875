package com.spldeolin.allison1875.cli.it.querytransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * query-transformer param-threshold 集成测试。
 *
 * <p>验证当 where 条件超过 3 个时自动生成 ParamDTO：
 * 1. ParamDTO 文件生成在 dto/param/ 下，包含所有 where 条件字段
 * 2. Mapper 接口方法参数为单个 ParamDTO（无 @Param）
 * 3. Mapper XML 中引用 ParamDTO 字段
 * 4. Service 中包含 ParamDTO 构建代码
 *
 * @author Deolin 2026-05-15
 */
public class ParamThresholdItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("param-threshold");

        // ========== 1. ParamDTO 文件验证 ==========
        File paramDir = new File(basedir, "src/main/java/com/example/dto/param");
        assertTrue(paramDir.exists(), "paramDTO package directory should exist");

        File[] paramFiles = paramDir.listFiles((dir, name) -> name.endsWith(".java") && name.contains("Param"));
        assertTrue(paramFiles != null && paramFiles.length > 0, "At least one ParamDTO java file should be generated");

        String paramContent = new String(Files.readAllBytes(paramFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(paramContent.contains("orderNo"), "ParamDTO should contain orderNo field");
        assertTrue(paramContent.contains("userId"), "ParamDTO should contain userId field");
        assertTrue(paramContent.contains("status"), "ParamDTO should contain status field");
        assertTrue(paramContent.contains("amount"), "ParamDTO should contain amount field");

        // ========== 2. Mapper 接口验证 ==========
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        assertTrue(mapperFile.exists(), "TOrderMapper.java should exist");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("listByMultipleConditions"),
                "Mapper should contain listByMultipleConditions method");

        // 验证 listByMultipleConditions 方法签名：参数应为 ParamDTO 类型，无 @Param
        // 查找 listByMultipleConditions 所在行
        String[] mapperLines = mapperContent.split("\n");
        String methodLine = "";
        for (String line : mapperLines) {
            if (line.contains("listByMultipleConditions")) {
                methodLine = line;
                break;
            }
        }
        assertFalse(methodLine.isEmpty(), "listByMultipleConditions method line should exist");
        assertFalse(methodLine.contains("@Param"), "ParamDTO method should NOT have @Param in its signature");
        assertTrue(methodLine.contains("ListByMultipleConditionsParam"), "Method param should be the ParamDTO type");

        // ========== 3. Mapper XML 验证 ==========
        File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml");
        assertTrue(mapperXmlFile.exists(), "TOrderMapper.xml should exist");
        String xmlContent = new String(Files.readAllBytes(mapperXmlFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(xmlContent.contains("id='listByMultipleConditions'") || xmlContent.contains(
                "id=\"listByMultipleConditions\""), "XML should contain listByMultipleConditions");
        assertTrue(xmlContent.contains("#{orderNo}"), "XML should reference #{orderNo}");
        assertTrue(xmlContent.contains("#{userId}"), "XML should reference #{userId}");
        assertTrue(xmlContent.contains("#{status}"), "XML should reference #{status}");
        assertTrue(xmlContent.contains("#{amount}"), "XML should reference #{amount}");

        // ========== 4. Service 文件验证 ==========
        File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java");
        assertTrue(serviceFile.exists(), "OrderService.java should exist");
        String serviceContent = new String(Files.readAllBytes(serviceFile.toPath()), StandardCharsets.UTF_8);

        // Design 链已被替换
        assertFalse(serviceContent.contains("TOrderDesign."), "Design chain should be replaced");

        // Mapper 被注入
        assertTrue(serviceContent.contains("TOrderMapper"), "Mapper should be injected");
        assertTrue(serviceContent.contains("tOrderMapper"), "Mapper field should exist");

        // 包含 ParamDTO 构建代码
        assertTrue(serviceContent.contains("Param"), "Service should reference ParamDTO type");
        assertTrue(serviceContent.contains(".setOrderNo("), "Service should set orderNo on ParamDTO");
        assertTrue(serviceContent.contains(".setUserId("), "Service should set userId on ParamDTO");
        assertTrue(serviceContent.contains(".setStatus("), "Service should set status on ParamDTO");
        assertTrue(serviceContent.contains(".setAmount("), "Service should set amount on ParamDTO");
    }

}