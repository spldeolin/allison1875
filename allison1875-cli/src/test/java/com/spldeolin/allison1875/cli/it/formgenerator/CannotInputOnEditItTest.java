package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * cannot-input-on-edit 集成测试。
 *
 * <p>验证 (canInputOnInit=true, canInputOnEdit=false) 字段：
 * <ul>
 *   <li>该字段出现在 SaveReq DTO 中</li>
 *   <li>由于 isNonVoid 校验改为分支内 if-throw，而非 ReqDTO 注解，所以该字段在 ReqDTO 中无 @NotBlank</li>
 *   <li>setter 仅出现在 if(toCreate) 内</li>
 *   <li>else/edit 分支与 common 节中均不出现该字段</li>
 *   <li>不再生成 // TODO 注释</li>
 * </ul>
 *
 * @author Deolin 2026-05-30
 */
public class CannotInputOnEditItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("cannot-input-on-edit");

        File saveReqFile = new File(basedir, "src/main/java/com/example/dto/req/SaveDocumentReq.java");
        assertTrue(saveReqFile.exists(), "SaveDocumentReq DTO should be generated");
        String saveReqContent = Files.readString(saveReqFile.toPath());
        assertTrue(saveReqContent.contains("authorName"),
                "SaveReq should contain authorName (canInputOnInit=true)");
        // 校验改为分支内 if-throw，ReqDTO 字段上不应含 @NotBlank
        assertFalse(saveReqContent.contains("@NotBlank String authorName")
                        || saveReqContent.contains("@NotBlank\n    String authorName"),
                "authorName in SaveReq should NOT have @NotBlank when canInputOnEdit=false");

        File saveFile = new File(basedir,
                "src/main/java/com/example/service/impl/SaveDocumentServiceImpl.java");
        assertTrue(saveFile.exists(), "SaveDocumentServiceImpl should be generated");
        String saveContent = Files.readString(saveFile.toPath());

        int toCreateStart = saveContent.indexOf("if (toCreate) {");
        int elseStart = saveContent.indexOf("} else {");
        int insertCall = saveContent.indexOf("documentMapper.insert");

        String toCreateSection = saveContent.substring(toCreateStart, elseStart);
        String elseSection = saveContent.substring(elseStart, insertCall);
        String commonSection = saveContent.substring(insertCall);

        // toCreate 分支：if-throw 校验 + setter
        assertTrue(toCreateSection.contains("StringUtils.isBlank(req.getAuthorName())"),
                "toCreate should contain isBlank validation for authorName");
        assertTrue(toCreateSection.contains("throw new IllegalArgumentException(\"作者不能为空\")"),
                "toCreate should throw IllegalArgumentException with title");
        assertTrue(toCreateSection.contains("document.setAuthorName("),
                "toCreate should set authorName");

        // else 分支：authorName 不应被设置
        assertFalse(elseSection.contains("document.setAuthorName"),
                "authorName should NOT be set in else branch");

        // common 节（insert 之后到 return 之前）：authorName 不应被设置
        assertFalse(commonSection.contains("document.setAuthorName"),
                "authorName should NOT be set in common section");

        // title / content 是 (true,true) 字段，应该在 common 节
        assertTrue(saveContent.contains("document.setTitle(req.getTitle())"),
                "title should be set (in common section)");
        assertTrue(saveContent.contains("document.setContent(req.getContent())"),
                "content should be set (in common section)");

        // 不再生成 TODO 行注释
        assertFalse(saveContent.contains("TODO 请补充"),
                "Generated code should not contain // TODO 请补充 comment");

        File apiDocsDir = new File(basedir, "api-docs");
        assertFalse(apiDocsDir.exists(), "api-docs should NOT be generated");
    }
}
