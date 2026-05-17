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
public class DefaultMethodNameItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("default-method-name");

        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(mapperContent.contains("queryTOrder"), "Default method name for select should be queryTOrder");
        assertTrue(mapperContent.contains("updateTOrder"), "Default method name for update should be updateTOrder");
        assertTrue(mapperContent.contains("deleteTOrder"), "Default method name for delete should be deleteTOrder");

        File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java");
        String serviceContent = new String(Files.readAllBytes(serviceFile.toPath()), StandardCharsets.UTF_8);
        assertFalse(serviceContent.contains("TOrderDesign."));
        assertTrue(serviceContent.contains("TOrderMapper"));
        assertTrue(serviceContent.contains("tOrderMapper.queryTOrder"));
        assertTrue(serviceContent.contains("tOrderMapper.updateTOrder"));
        assertTrue(serviceContent.contains("tOrderMapper.deleteTOrder"));
    }
}