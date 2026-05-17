package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * onoff-item 集成测试。
 *
 * <p>验证 onOff 类型字段的处理：
 * <ul>
 *   <li>DDL 中列类型为 {@code TINYINT(1)}</li>
 *   <li>DTO 中字段类型为 {@code Boolean}</li>
 *   <li>List API 的 Req 中该字段为 {@code List<Boolean>} 列表过滤</li>
 *   <li>SaveReq / GetDetailResp / ListResp 中该字段正常出现（非 secret）</li>
 * </ul>
 *
 * @author Deolin 2026-05-17
 */
public class OnOffItemItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("onoff-item");

        // === DDL 验证 ===
        File ddlFile = new File(basedir, "sql/ddl.sql");
        assertTrue(ddlFile.exists(), "DDL file should be generated at sql/ddl.sql");
        String ddl = new String(Files.readAllBytes(ddlFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(ddl.contains("CREATE TABLE"), "DDL should contain CREATE TABLE");
        // onOff 字段在 DDL 中为 TINYINT(1)
        assertTrue(ddl.contains("`is_enabled` TINYINT(1) NOT NULL"),
                "isEnabled (onOff+nonVoid) should be TINYINT(1) NOT NULL");

        // === Entity 验证 ===
        File entityFile = new File(basedir, "src/main/java/com/example/entity/FeatureEntity.java");
        assertTrue(entityFile.exists(), "FeatureEntity file should be generated");
        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(entityContent.contains("class FeatureEntity"), "Entity should declare class FeatureEntity");
        // Entity 中 onOff 字段存储为 Boolean
        assertTrue(entityContent.contains("Boolean isEnabled"),
                "Entity should contain Boolean isEnabled field");

        // === SaveReq DTO 验证 ===
        File saveReqFile = new File(basedir, "src/main/java/com/example/dto/req/SaveFeatureReq.java");
        assertTrue(saveReqFile.exists(), "SaveFeatureReq DTO should be generated");
        String saveReqContent = new String(Files.readAllBytes(saveReqFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(saveReqContent.contains("class SaveFeatureReq"), "Should contain class SaveFeatureReq");
        // onOff 字段类型为 Boolean
        assertTrue(saveReqContent.contains("Boolean isEnabled"),
                "SaveReq should contain Boolean isEnabled");
        // isNonVoid=true → @NotNull
        assertTrue(
                saveReqContent.contains("@NotNull")
                        || saveReqContent.contains("@javax.validation.constraints.NotNull"),
                "nonVoid onOff field should have @NotNull");

        // === Save ServiceImpl 验证 ===
        File saveServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/SaveFeatureServiceImpl.java");
        assertTrue(saveServiceImplFile.exists(), "SaveFeatureServiceImpl should be generated");
        String saveServiceImplContent = new String(Files.readAllBytes(saveServiceImplFile.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(saveServiceImplContent.contains("feature.setIsEnabled(req.getIsEnabled())"),
                "Save service should set isEnabled from req");

        // === GetDetailResp DTO 验证 ===
        File getDetailRespFile = new File(basedir,
                "src/main/java/com/example/dto/resp/GetFeatureDetailResp.java");
        assertTrue(getDetailRespFile.exists(), "GetFeatureDetailResp DTO should be generated");
        String getDetailRespContent = new String(Files.readAllBytes(getDetailRespFile.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(getDetailRespContent.contains("class GetFeatureDetailResp"),
                "Should contain class GetFeatureDetailResp");
        assertTrue(getDetailRespContent.contains("Boolean isEnabled"),
                "GetDetailResp should contain Boolean isEnabled");

        // === ListReq DTO 验证 ===
        File listReqFile = new File(basedir, "src/main/java/com/example/dto/req/ListFeaturesReq.java");
        assertTrue(listReqFile.exists(), "ListFeaturesReq DTO should be generated");
        String listReqContent = new String(Files.readAllBytes(listReqFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(listReqContent.contains("class ListFeaturesReq"), "Should contain class ListFeaturesReq");
        // onOff 字段在 ListReq 中为 List<Boolean> 列表过滤（与 NUMBER 共用 IN 路径）
        assertTrue(listReqContent.contains("List<Boolean> isEnabled"),
                "ListReq should contain List<Boolean> isEnabled for in-filter");

        // === ListResp DTO 验证 ===
        File listRespFile = new File(basedir, "src/main/java/com/example/dto/resp/ListFeaturesResp.java");
        assertTrue(listRespFile.exists(), "ListFeaturesResp DTO should be generated");
        String listRespContent = new String(Files.readAllBytes(listRespFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(listRespContent.contains("class ListFeaturesResp"), "Should contain class ListFeaturesResp");
        assertTrue(listRespContent.contains("Boolean isEnabled"),
                "ListResp should contain Boolean isEnabled");

        // === 验证没有生成 api-docs 目录（enableDocAnalyzer=false） ===
        File apiDocsDir = new File(basedir, "api-docs");
        assertFalse(apiDocsDir.exists(), "api-docs directory should NOT be generated when enableDocAnalyzer=false");
    }

}
