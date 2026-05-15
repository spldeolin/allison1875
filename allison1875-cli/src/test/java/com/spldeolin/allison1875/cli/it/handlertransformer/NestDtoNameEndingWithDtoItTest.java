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
 * nest-dto-name-ending-with-dto 集成测试。
 *
 * <p>验证嵌套类名已以 DTO 结尾时（如 AddressDTO），生成的文件名不会追加 DTO 后缀。
 *
 * @author Deolin 2026-05-15
 */
public class NestDtoNameEndingWithDtoItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("nest-dto-name-ending-with-dto");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/EndingDtoController.java");
        assertTrue(controllerFile.exists(), "EndingDtoController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        assertFalse(controllerContent.contains("class Req"), "inner class Req should be removed");
        assertFalse(controllerContent.contains("class AddressDTO"), "nested class AddressDTO should be removed");
        assertTrue(controllerContent.contains("/create-record"), "Should contain URL");

        // ========== 2. 验证嵌套 DTO 文件名为 AddressDTO.java（而非 AddressDTODTO.java） ==========
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        assertTrue(reqDtoDir.exists(), "req DTO directory should exist");

        // 应存在 AddressDTO.java
        File addressDtoFile = new File(reqDtoDir, "AddressDTO.java");
        assertTrue(addressDtoFile.exists(),
                "Should generate 'AddressDTO.java' (not 'AddressDTODTO.java' or 'AddressDtoDTO.java')");

        // 不应存在 AddressDTODTO.java
        File addressDtoDtoFile = new File(reqDtoDir, "AddressDTODTO.java");
        assertFalse(addressDtoDtoFile.exists(), "Should NOT generate 'AddressDTODTO.java'");

        // 内容验证
        String addressContent = new String(Files.readAllBytes(addressDtoFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(addressContent.contains("province"), "AddressDTO should contain 'province'");
        assertTrue(addressContent.contains("city"), "AddressDTO should contain 'city'");

        // ========== 3. 验证 Req 主 DTO 中嵌套字段名 ==========
        File[] reqFiles = reqDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("CreateRecord") && name.contains("Req"));
        assertTrue(reqFiles != null && reqFiles.length == 1, "Should generate CreateRecordReq DTO");

        String reqContent = new String(Files.readAllBytes(reqFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(reqContent.contains("recordName"), "Req should contain 'recordName'");
        // 嵌套字段名：AddressDTO → address（去掉DTO后缀转为lowerCamel）
        assertTrue(reqContent.contains("address"), "Req should reference AddressDTO as field 'address'");
    }

}
