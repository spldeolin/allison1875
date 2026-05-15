package com.spldeolin.allison1875.cli.it.handlertransformer;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import org.junit.jupiter.api.Test;
import com.spldeolin.allison1875.cli.it.docanalyzer.HandlerTransformerItBaseTest;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;

/**
 * invalid-init-body-three-coid 集成测试。
 *
 * <p>验证 init 块中包含 3 个以上内部类时，工具抛出 Allison1875Exception（cause 为 IllegalArgumentException）。
 *
 * @author Deolin 2026-05-15
 */
public class InvalidInitBodyMoreThanTwoCoidItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() {
        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> runHandlerTransformer("invalid-init-body-three-coid"),
                "Should throw Allison1875Exception when init block has more than 2 inner classes");

        assertInstanceOf(IllegalArgumentException.class, ex.getCause(),
                "Root cause should be IllegalArgumentException");
        assertTrue(ex.getCause().getMessage().contains("only 2 Coid"),
                "Exception message should mention 'only 2 Coid'");

        // 验证不生成任何文件（除了Controller本身不变）
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        if (basedir != null && reqDtoDir.exists()) {
            File[] dtoFiles = reqDtoDir.listFiles((dir, name) -> name.endsWith(".java"));
            assertTrue(dtoFiles == null || dtoFiles.length == 0,
                    "No DTO files should be generated on validation error");
        }
    }

}
