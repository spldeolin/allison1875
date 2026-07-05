package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * secret-edit-echo 集成测试。
 *
 * <p>验证 canInputOnEdit=true 的 secret 字段编辑语义：
 * <ul>
 *   <li>EditReqDTO 中 secret 字段不带 @NotEmpty（null 表示不修改）</li>
 *   <li>CreateReqDTO 中 secret 字段仍带 @NotEmpty（创建必填）</li>
 *   <li>Update 方法体对 secret 生成 null-skip 块；isNonVoid=true 版含空串校验</li>
 * </ul>
 *
 * @author Deolin 2026-07-05
 */
public class SecretEditEchoItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("secret-edit-echo");

        // CreateReq: secret 字段仍带 @NotEmpty
        File createReqFile = new File(basedir, "src/main/java/com/example/dto/req/CreateCredentialReq.java");
        assertTrue(createReqFile.exists(), "CreateCredentialReq DTO should be generated");
        String createReq = Files.readString(createReqFile.toPath());
        assertTrue(createReq.contains("apiKey"), "CreateReq should contain apiKey");
        assertTrue(createReq.contains("@NotEmpty"), "CreateReq secret should keep @NotEmpty");

        // UpdateReq: secret 字段无 @NotEmpty
        File updateReqFile = new File(basedir, "src/main/java/com/example/dto/req/UpdateCredentialReq.java");
        assertTrue(updateReqFile.exists(), "UpdateCredentialReq DTO should be generated");
        String updateReq = Files.readString(updateReqFile.toPath());
        assertTrue(updateReq.contains("apiKey"), "UpdateReq should contain apiKey");
        assertTrue(updateReq.contains("backupToken"), "UpdateReq should contain backupToken");
        assertFalse(updateReq.contains("@NotEmpty"),
                "UpdateReq secret fields should NOT carry @NotEmpty");

        // UpdateServiceImpl: null-skip 块
        File updateFile = new File(basedir,
                "src/main/java/com/example/service/impl/UpdateCredentialServiceImpl.java");
        assertTrue(updateFile.exists(), "UpdateCredentialServiceImpl should be generated");
        String update = Files.readString(updateFile.toPath());
        assertTrue(update.contains("if (req.getApiKey() != null)"),
                "apiKey update should be null-guarded");
        assertTrue(update.contains("req.getApiKey().isEmpty()"),
                "apiKey update should reject empty string (isNonVoid=true)");
        assertTrue(update.contains("credential.setApiKey(req.getApiKey())"),
                "apiKey should be set inside guard");
        assertTrue(update.contains("if (req.getBackupToken() != null)"),
                "backupToken update should be null-guarded");
        assertFalse(update.contains("req.getBackupToken().isEmpty()"),
                "backupToken (isNonVoid=false) should NOT reject empty string");
        assertTrue(update.contains("credential.setBackupToken(req.getBackupToken())"),
                "backupToken should be set inside guard");

        // GetDetail 仍排除 secret（安全要求，回显根因）
        File detailRespFile = new File(basedir,
                "src/main/java/com/example/dto/resp/GetCredentialDetailResp.java");
        assertTrue(detailRespFile.exists(), "GetCredentialDetailResp should be generated");
        String detailResp = Files.readString(detailRespFile.toPath());
        assertFalse(detailResp.contains("apiKey"), "detail resp should exclude secret apiKey");
        assertFalse(detailResp.contains("backupToken"), "detail resp should exclude secret backupToken");
    }
}
