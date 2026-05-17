package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * edit-pattern-donot 集成测试。
 *
 * <p>验证 {@code editPattern=doNot} 字段在编辑时不被更新：
 * <ul>
 *   <li>该字段仍出现在 SaveReq DTO 中（initPattern=userInput，允许创建时输入）</li>
 *   <li>创建分支中正常设置该字段</li>
 *   <li>common 节中不设置该字段（editPattern != userInput）</li>
 *   <li>编辑分支中不设置该字段（editPattern=doNot）</li>
 * </ul>
 *
 * @author Deolin 2026-05-17
 */
public class EditPatternDoNotItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("edit-pattern-donot");

        // === SaveReq DTO 验证：doNot 字段仍出现（initPattern=userInput） ===
        File saveReqFile = new File(basedir, "src/main/java/com/example/dto/req/SaveDocumentReq.java");
        assertTrue(saveReqFile.exists(), "SaveDocumentReq DTO should be generated");
        String saveReqContent = new String(Files.readAllBytes(saveReqFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(saveReqContent.contains("authorName"),
                "SaveReq should contain authorName (initPattern=userInput)");

        // === Save ServiceImpl 验证 ===
        File saveFile = new File(basedir,
                "src/main/java/com/example/service/impl/SaveDocumentServiceImpl.java");
        assertTrue(saveFile.exists(), "SaveDocumentServiceImpl should be generated");
        String saveContent = new String(Files.readAllBytes(saveFile.toPath()), StandardCharsets.UTF_8);

        // 获取三个关键区域的索引
        int toCreateStart = saveContent.indexOf("if (toCreate) {");
        int elseStart = saveContent.indexOf("} else {");
        int insertCall = saveContent.indexOf("documentMapper.insert");

        // common 节（toCreate/else 之后、insert 之前）: authorName 不应出现
        String commonSection = saveContent.substring(elseStart, insertCall);
        assertFalse(commonSection.contains("document.setAuthorName"),
                "authorName (editPattern=doNot) should NOT be set in common section");

        // toCreate 分支：authorName 应出现
        String toCreateSection = saveContent.substring(toCreateStart, elseStart);
        assertTrue(toCreateSection.contains("document.setAuthorName("),
                "authorName should be set in toCreate branch (initPattern=userInput)");

        // else/edit 分支：authorName 不应出现
        String elseSection = saveContent.substring(elseStart, insertCall);
        assertFalse(elseSection.contains("document.setAuthorName"),
                "authorName should NOT be set in else/edit branch (editPattern=doNot)");

        // 对比：普通字段 title/content 应该在 common 节设置
        assertTrue(commonSection.contains("document.setTitle("),
                "title (normal userInput) should be set in common section");
        assertTrue(commonSection.contains("document.setContent("),
                "content (normal userInput) should be set in common section");

        // === 验证没有生成 api-docs ===
        File apiDocsDir = new File(basedir, "api-docs");
        assertFalse(apiDocsDir.exists(), "api-docs should NOT be generated");
    }

}
