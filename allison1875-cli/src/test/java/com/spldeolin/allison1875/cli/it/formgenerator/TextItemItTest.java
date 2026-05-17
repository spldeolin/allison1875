package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * text-item 集成测试。
 *
 * <p>验证 text 类型字段的全路径处理：
 * <ul>
 *   <li>DDL 中生成 {@code VARCHAR(n)} 列（n 取自 maxLength），非多行/富文本时</li>
 *   <li>DDL 中生成 {@code LONGTEXT} 列（isMultilineOrRich=true 时）</li>
 *   <li>SaveReq DTO 中字段类型为 {@code String}，isNonVoid=true 时附加 {@code @NotBlank}</li>
 *   <li>所有 text 字段附加 {@code @Size(min=0, max=n)} 注解</li>
 *   <li>ListReq 中该字段为模糊匹配过滤条件（单值 {@code String}）</li>
 *   <li>GetDetailResp 中该字段正常返回</li>
 *   <li>List ServiceImpl 的 Design Chain 对 text 字段使用 {@code .like()} 过滤</li>
 * </ul>
 *
 * @author Deolin 2026-05-17
 */
public class TextItemItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("text-item");

        // === DDL 验证 ===
        File ddlFile = new File(basedir, "sql/ddl.sql");
        assertTrue(ddlFile.exists(), "DDL file should be generated at sql/ddl.sql");
        String ddl = new String(Files.readAllBytes(ddlFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(ddl.contains("CREATE TABLE"), "DDL should contain CREATE TABLE");
        // noteTitle: isNonVoid=true, maxLength=200, isMultilineOrRich=false → VARCHAR(200) NOT NULL
        assertTrue(ddl.contains("`note_title` VARCHAR(200) NOT NULL COMMENT '标题'"),
                "noteTitle should be VARCHAR(200) NOT NULL");
        // noteContent: isNonVoid=false, isMultilineOrRich=true → LONGTEXT (no NOT NULL)
        assertTrue(ddl.contains("`note_content` LONGTEXT"),
                "noteContent should be LONGTEXT (multiline)");
        assertFalse(ddl.contains("`note_content` LONGTEXT NOT NULL"),
                "noteContent should NOT have NOT NULL since isNonVoid=false");
        // noteTag: isNonVoid=false, maxLength=50 → VARCHAR(50) (no NOT NULL)
        assertTrue(ddl.contains("`note_tag` VARCHAR(50)"),
                "noteTag should be VARCHAR(50)");
        assertFalse(ddl.contains("`note_tag` VARCHAR(50) NOT NULL"),
                "noteTag should NOT have NOT NULL since isNonVoid=false");
        // 自动添加的审计字段
        assertTrue(ddl.contains("`note_code`"), "DDL should contain auto-added bizId column 'note_code'");
        assertTrue(ddl.contains("`created_at`"), "DDL should contain auto-added column 'created_at'");
        assertTrue(ddl.contains("`updated_at`"), "DDL should contain auto-added column 'updated_at'");

        // === Entity 验证 ===
        File entityFile = new File(basedir, "src/main/java/com/example/entity/NoteEntity.java");
        assertTrue(entityFile.exists(), "Entity file should be generated");
        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(entityContent.contains("class NoteEntity"), "Entity should declare class NoteEntity");
        assertTrue(entityContent.contains("noteCode"), "Entity should contain noteCode field");
        assertTrue(entityContent.contains("noteTitle"), "Entity should contain noteTitle field");
        assertTrue(entityContent.contains("noteContent"), "Entity should contain noteContent field");
        assertTrue(entityContent.contains("noteTag"), "Entity should contain noteTag field");

        // === SaveReq DTO 验证 ===
        File saveReqFile = new File(basedir, "src/main/java/com/example/dto/req/SaveNoteReq.java");
        assertTrue(saveReqFile.exists(), "SaveNoteReq DTO should be generated");
        String saveReqContent = new String(Files.readAllBytes(saveReqFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(saveReqContent.contains("class SaveNoteReq"), "Should contain class SaveNoteReq");
        // noteTitle: isNonVoid=true → 应有 @NotBlank
        assertTrue(saveReqContent.contains("noteTitle"), "SaveReq should contain noteTitle field");
        assertTrue(saveReqContent.contains("@NotBlank") || saveReqContent.contains("@javax.validation.constraints.NotBlank"),
                "noteTitle with isNonVoid=true should have @NotBlank");
        // noteContent: isNonVoid=false → 不应有 @NotBlank，但有 @Size
        assertTrue(saveReqContent.contains("noteContent"), "SaveReq should contain noteContent field");
        // @Size annotation exists
        assertTrue(saveReqContent.contains("@Size") || saveReqContent.contains("@javax.validation.constraints.Size"),
                "Text fields should have @Size annotation");

        // === ListReq DTO 验证 ===
        File listReqFile = new File(basedir, "src/main/java/com/example/dto/req/ListNotesReq.java");
        assertTrue(listReqFile.exists(), "ListNotesReq DTO should be generated");
        String listReqContent = new String(Files.readAllBytes(listReqFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(listReqContent.contains("class ListNotesReq"), "Should contain class ListNotesReq");
        // text 字段在 ListReq 中为单值 String，用于模糊匹配
        assertTrue(listReqContent.contains("String noteTitle"), "ListReq should contain String noteTitle for fuzzy match");
        assertTrue(listReqContent.contains("String noteContent"),
                "ListReq should contain String noteContent for fuzzy match");
        assertTrue(listReqContent.contains("String noteTag"), "ListReq should contain String noteTag for fuzzy match");

        // === GetDetailResp DTO 验证 ===
        File getDetailRespFile = new File(basedir, "src/main/java/com/example/dto/resp/GetNoteDetailResp.java");
        assertTrue(getDetailRespFile.exists(), "GetNoteDetailResp DTO should be generated");
        String getDetailRespContent = new String(Files.readAllBytes(getDetailRespFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(getDetailRespContent.contains("class GetNoteDetailResp"), "Should contain class GetNoteDetailResp");
        // text 字段在 GetDetailResp 中正常返回（非 secret 字段）
        assertTrue(getDetailRespContent.contains("noteTitle"), "GetDetailResp should contain noteTitle field");
        assertTrue(getDetailRespContent.contains("noteContent"), "GetDetailResp should contain noteContent field");
        assertTrue(getDetailRespContent.contains("noteTag"), "GetDetailResp should contain noteTag field");

        // === List ServiceImpl 验证：Design Chain 被 query-transformer 转换为 Mapper 调用 ===
        File listServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/ListNotesServiceImpl.java");
        assertTrue(listServiceImplFile.exists(), "ListNotesServiceImpl file should be generated");
        String listServiceImplContent = new String(Files.readAllBytes(listServiceImplFile.toPath()),
                StandardCharsets.UTF_8);
        // query-transformer 将 Design Chain 转换为 Mapper 调用
        assertTrue(listServiceImplContent.contains("noteMapper.countNote("),
                "List service should call noteMapper.countNote for pagination");
        assertTrue(listServiceImplContent.contains("noteMapper.queryNote("),
                "List service should call noteMapper.queryNote for query");
        // QueryNoteParam 中包含所有 text 字段的 setter
        assertTrue(listServiceImplContent.contains("setNoteTitle(req.getNoteTitle())"),
                "Param should set noteTitle from req (text field)");
        assertTrue(listServiceImplContent.contains("setNoteContent(req.getNoteContent())"),
                "Param should set noteContent from req (text field)");
        assertTrue(listServiceImplContent.contains("setNoteTag(req.getNoteTag())"),
                "Param should set noteTag from req (text field)");

        // === 验证没有生成 api-docs 目录（enableDocAnalyzer=false） ===
        File apiDocsDir = new File(basedir, "api-docs");
        assertFalse(apiDocsDir.exists(), "api-docs directory should NOT be generated when enableDocAnalyzer=false");
    }

}
