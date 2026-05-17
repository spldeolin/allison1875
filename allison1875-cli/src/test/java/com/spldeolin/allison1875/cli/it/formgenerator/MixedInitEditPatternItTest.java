package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * mixed-init-edit-pattern 集成测试。
 *
 * <p>验证 {@code initPattern=userInput + editPattern=doNot} 组合：
 * <ul>
 *   <li>该字段出现在 SaveReq 中（initPattern=userInput）</li>
 *   <li>该字段 setter 仅出现在 {@code if (toCreate)} 分支内</li>
 *   <li>common 节和 else/edit 分支中均不出现该字段</li>
 *   <li>同一表单中普通字段（userInput+userInput）不受影响</li>
 * </ul>
 *
 * @author Deolin 2026-05-17
 */
public class MixedInitEditPatternItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("mixed-init-edit-pattern");

        // === SaveReq DTO 验证：所有 userInput 字段均应出现 ===
        File saveReqFile = new File(basedir, "src/main/java/com/example/dto/req/SaveProductReq.java");
        assertTrue(saveReqFile.exists(), "SaveProductReq DTO should be generated");
        String saveReqContent = new String(Files.readAllBytes(saveReqFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(saveReqContent.contains("productName"), "SaveReq should contain productName");
        assertTrue(saveReqContent.contains("skuCode"), "SaveReq should contain skuCode (initPattern=userInput)");
        assertTrue(saveReqContent.contains("remark"), "SaveReq should contain remark");

        // === Save ServiceImpl 验证 ===
        File saveFile = new File(basedir,
                "src/main/java/com/example/service/impl/SaveProductServiceImpl.java");
        assertTrue(saveFile.exists(), "SaveProductServiceImpl should be generated");
        String saveContent = new String(Files.readAllBytes(saveFile.toPath()), StandardCharsets.UTF_8);

        int toCreateStart = saveContent.indexOf("if (toCreate) {");
        int elseStart = saveContent.indexOf("} else {");
        int insertCall = saveContent.indexOf("productMapper.insert");

        // toCreate 分支：skuCode 应出现（initPattern=userInput && editPattern!=userInput）
        String toCreateSection = saveContent.substring(toCreateStart, elseStart);
        assertTrue(toCreateSection.contains("product.setSkuCode("),
                "skuCode should be set in toCreate branch (userInput+doNot → init matched, edit not)");

        // common 节（else 之后、insert 之前）：skuCode 不应出现
        String commonSection = saveContent.substring(elseStart, insertCall);
        assertFalse(commonSection.contains("product.setSkuCode"),
                "skuCode should NOT be set in common section (editPattern != userInput)");

        // else/edit 分支：skuCode 不应出现
        assertFalse(commonSection.contains("product.setSkuCode"),
                "skuCode should NOT be set in else/edit branch (editPattern=doNot)");

        // 对比：普通字段 productName 和 remark 应在 common 节设置
        assertTrue(commonSection.contains("product.setProductName("),
                "productName (normal) should be set in common section");
        assertTrue(commonSection.contains("product.setRemark("),
                "remark (normal) should be set in common section");

        // 验证 skuCode 的 setter 仅出现一次（只在 toCreate 分支）
        int firstSkuSet = saveContent.indexOf("product.setSkuCode(");
        int lastSkuSet = saveContent.lastIndexOf("product.setSkuCode(");
        assertTrue(firstSkuSet == lastSkuSet,
                "skuCode setter should appear only once (only in toCreate branch)");

        // === 验证没有生成 api-docs ===
        File apiDocsDir = new File(basedir, "api-docs");
        assertFalse(apiDocsDir.exists(), "api-docs should NOT be generated");
    }

}
