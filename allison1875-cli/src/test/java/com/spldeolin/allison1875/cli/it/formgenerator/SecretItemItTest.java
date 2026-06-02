package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * secret-item 集成测试。
 *
 * <p>验证 secret 类型字段的特殊处理：
 * <ul>
 *   <li>DDL 中列类型为 {@code VARCHAR(255)}</li>
 *   <li>Save API 的 Req 中包含该字段（"String apiKeySecret" + @NotEmpty）</li>
 *   <li>Save ServiceImpl 中正常执行 {@code entity.setApiKeySecret(req.getApiKeySecret())}</li>
 *   <li>GetDetail API 的 Resp 中<b>不包含</b>该字段（secret 不应返回）</li>
 *   <li>List API 的 Resp 中<b>不包含</b>该字段（secret 不在列表中展示）</li>
 *   <li>List API 的 Req 过滤条件中<b>不包含</b>该字段</li>
 * </ul>
 *
 * @author Deolin 2026-05-17
 */
public class SecretItemItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("secret-item");

        // === DDL 验证 ===
        File ddlFile = new File(basedir, "sql/ddl.sql");
        assertTrue(ddlFile.exists(), "DDL file should be generated at sql/ddl.sql");
        String ddl = new String(Files.readAllBytes(ddlFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(ddl.contains("CREATE TABLE"), "DDL should contain CREATE TABLE");
        // secret 字段在 DDL 中为 VARCHAR(255)
        assertTrue(ddl.contains("`api_key_secret` VARCHAR(255) NOT NULL"),
                "apiKeySecret (secret+nonVoid) should be VARCHAR(255) NOT NULL");

        // === Entity 验证 ===
        File entityFile = new File(basedir, "src/main/java/com/example/entity/ApiKeyEntity.java");
        assertTrue(entityFile.exists(), "ApiKeyEntity file should be generated");
        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(entityContent.contains("class ApiKeyEntity"), "Entity should declare class ApiKeyEntity");
        // Entity 中 secret 字段存储为 String
        assertTrue(entityContent.contains("String apiKeySecret"),
                "Entity should contain String apiKeySecret field");

        // === SaveReq DTO 验证：secret 字段应包含 ===
        File saveReqFile = new File(basedir, "src/main/java/com/example/dto/req/SaveApiKeyReq.java");
        assertTrue(saveReqFile.exists(), "SaveApiKeyReq DTO should be generated");
        String saveReqContent = new String(Files.readAllBytes(saveReqFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(saveReqContent.contains("class SaveApiKeyReq"), "Should contain class SaveApiKeyReq");
        // secret 字段作为用户输入出现在 SaveReq
        assertTrue(saveReqContent.contains("String apiKeySecret"),
                "SaveReq should contain String apiKeySecret (user input)");
        // isNonVoid=true → @NotEmpty
        assertTrue(
                saveReqContent.contains("@NotEmpty")
                        || saveReqContent.contains("@javax.validation.constraints.NotEmpty"),
                "nonVoid secret field should have @NotEmpty");

        // === Save ServiceImpl 验证：secret 字段正常设置 ===
        File saveServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/SaveApiKeyServiceImpl.java");
        assertTrue(saveServiceImplFile.exists(), "SaveApiKeyServiceImpl should be generated");
        String saveServiceImplContent = new String(Files.readAllBytes(saveServiceImplFile.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(saveServiceImplContent.contains("apiKey.setApiKeySecret(req.getApiKeySecret())"),
                "Save service should set apiKeySecret from req");

        // === GetDetailResp DTO 验证：secret 字段应不包含 ===
        File getDetailRespFile = new File(basedir,
                "src/main/java/com/example/dto/resp/GetApiKeyDetailResp.java");
        assertTrue(getDetailRespFile.exists(), "GetApiKeyDetailResp DTO should be generated");
        String getDetailRespContent = new String(Files.readAllBytes(getDetailRespFile.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(getDetailRespContent.contains("class GetApiKeyDetailResp"),
                "Should contain class GetApiKeyDetailResp");
        // secret 不应出现在 GetDetailResp
        assertFalse(getDetailRespContent.contains("apiKeySecret"),
                "GetDetailResp should NOT contain apiKeySecret (secret not returned)");
        // 但非 secret 字段应出现
        assertTrue(getDetailRespContent.contains("apiKeyName"),
                "GetDetailResp should contain apiKeyName (non-secret field)");

        // === ListReq DTO 验证：secret 字段不应作为过滤条件 ===
        File listReqFile = new File(basedir, "src/main/java/com/example/dto/req/ListApiKeysReq.java");
        assertTrue(listReqFile.exists(), "ListApiKeysReq DTO should be generated");
        String listReqContent = new String(Files.readAllBytes(listReqFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(listReqContent.contains("class ListApiKeysReq"), "Should contain class ListApiKeysReq");
        // secret 不应出现在 ListReq 过滤条件
        assertFalse(listReqContent.contains("apiKeySecret"),
                "ListReq should NOT contain apiKeySecret as filter");
        // 但非 secret 字段应出现作为过滤条件
        assertTrue(listReqContent.contains("apiKeyName"),
                "ListReq should contain apiKeyName for like filter");

        // === ListResp DTO 验证：secret 字段不应展示 ===
        File listRespFile = new File(basedir, "src/main/java/com/example/dto/resp/ListApiKeysResp.java");
        assertTrue(listRespFile.exists(), "ListApiKeysResp DTO should be generated");
        String listRespContent = new String(Files.readAllBytes(listRespFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(listRespContent.contains("class ListApiKeysResp"), "Should contain class ListApiKeysResp");
        // secret 不应出现在列表展示
        assertFalse(listRespContent.contains("apiKeySecret"),
                "ListResp should NOT contain apiKeySecret (secret not shown in list)");
        // 但非 secret 字段应出现
        assertTrue(listRespContent.contains("apiKeyName"),
                "ListResp should contain apiKeyName (non-secret field)");

        // === 验证生成了 api-docs 目录 ===
        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should be generated");
    }

}
