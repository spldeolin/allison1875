package com.spldeolin.allison1875.cli.it.querytransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * @author Deolin 2026-05-17
 */
public class SelectOnePropertyMapItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("select-one-property-map");

        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(mapperContent.contains("mapByOrderNo"));
        // 单属性 map → 生成 Record DTO（含单属性）
        assertTrue(mapperContent.contains("Map<Long, MapByOrderNoRecord>"),
                "Map value type should be MapByOrderNoRecord for single property");

        File recordDir = new File(basedir, "src/main/java/com/example/dto/record");
        assertTrue(recordDir.exists());

        File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java");
        String serviceContent = new String(Files.readAllBytes(serviceFile.toPath()), StandardCharsets.UTF_8);
        assertFalse(serviceContent.contains("TOrderDesign."));
        assertTrue(serviceContent.contains("TOrderMapper"));
    }
}