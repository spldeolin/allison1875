package com.spldeolin.allison1875.cli.it.handlertransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import com.spldeolin.allison1875.cli.it.docanalyzer.HandlerTransformerItBaseTest;

/**
 * list-annotation 集成测试。
 *
 * 验证 Resp 标注 @L 注解时，handler 返回类型为 List&lt;XxxResp&gt;。
 *
 * @author Deolin 2026-05-13
 */
public class ListAnnotationItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("list-annotation");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/TagController.java");
        assertTrue(controllerFile.exists(), "TagController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块移除
        assertFalse(controllerContent.contains("class Resp"), "inner class Resp should be removed");

        // handler 生成
        assertTrue(controllerContent.contains("/list-tags"), "Should contain URL '/list-tags'");
        assertTrue(controllerContent.contains("listTags"), "Should contain method 'listTags'");

        // 返回类型应该是 List（因为 @L 注解）
        assertTrue(controllerContent.contains("List"),
                "Handler return type should contain 'List' due to @L annotation");

        // ========== 2. 验证 Resp DTO 生成 ==========
        File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp");
        assertTrue(respDtoDir.exists(), "resp DTO directory should exist");

        File[] respFiles = respDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("ListTags") && name.contains("Resp"));
        assertTrue(respFiles != null && respFiles.length == 1, "Should generate 1 Resp DTO file");

        String respContent = new String(Files.readAllBytes(respFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(respContent.contains("tagId"), "Resp DTO should contain 'tagId'");
        assertTrue(respContent.contains("tagName"), "Resp DTO should contain 'tagName'");

        // ========== 3. 验证 Service 方法返回类型也应包含 List ==========
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        assertTrue(serviceDir.exists(), "service directory should exist");
        File[] serviceFiles = serviceDir.listFiles((dir, name) -> name.endsWith(".java") && name.contains("ListTags"));
        assertTrue(serviceFiles != null && serviceFiles.length == 1, "Should generate 1 Service file");
        String serviceContent = new String(Files.readAllBytes(serviceFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(serviceContent.contains("List"), "Service method return type should contain 'List'");
    }

}