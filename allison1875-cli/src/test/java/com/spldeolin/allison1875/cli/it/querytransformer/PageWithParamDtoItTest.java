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
public class PageWithParamDtoItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("page-with-param-dto");

        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(mapperContent.contains("pageByStatusAndAmount"));

        // ParamDTO 生成
        File paramDir = new File(basedir, "src/main/java/com/example/dto/param");
        assertTrue(paramDir.exists(), "ParamDTO directory should exist for page+2 conditions");
        File[] paramFiles = paramDir.listFiles();
        boolean hasParamDto = paramFiles != null && paramFiles.length > 0;
        assertTrue(hasParamDto, "ParamDTO should be generated");

        File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java");
        String serviceContent = new String(Files.readAllBytes(serviceFile.toPath()), StandardCharsets.UTF_8);
        assertFalse(serviceContent.contains("TOrderDesign."));
        assertTrue(serviceContent.contains("TOrderMapper"));
    }
}