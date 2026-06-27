package com.spldeolin.allison1875.appgenerator.service.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.appgenerator.service.AuditOperationTypeEnumGenerateService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-06-27
 */
@Singleton
@Slf4j
public class AuditOperationTypeEnumGenerateServiceImpl implements AuditOperationTypeEnumGenerateService {

    private static final String ENUM_MARKER = "    // === 以下枚举项由 app-generator 生成，勿手动修改 ===\n    ;";

    @Override
    public void generateAuditOperationTypeEnum(List<FormDef> allForms, Path backendOutputRoot, String namespace) {
        List<FormDef> forms = allForms.stream()
                .filter(f -> !"AuditLog".equals(f.getName()))
                .collect(Collectors.toList());

        String namespacePath = namespace.replace('.', '/');
        Path targetFile = backendOutputRoot.resolve(
                "src/main/java/" + namespacePath + "/enums/AuditOperationTypeEnum.java");

        try {
            String content = Files.readString(targetFile, StandardCharsets.UTF_8);
            content = injectEnumEntries(content, forms);
            Files.writeString(targetFile, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        log.info("generated AuditOperationTypeEnum with {} entries for {} forms", forms.size() * 3, forms.size());
    }

    private String injectEnumEntries(String content, List<FormDef> forms) {
        if (forms.isEmpty()) {
            return content.replace(ENUM_MARKER, "    ;");
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < forms.size(); i++) {
            FormDef form = forms.get(i);
            String upperSnake = toUpperSnake(form.getName());

            sb.append("    CREATE_").append(upperSnake).append("(\"create").append(form.getName())
                    .append("\", \"创建").append(form.getTitle()).append("\"),\n");
            sb.append("    UPDATE_").append(upperSnake).append("(\"update").append(form.getName())
                    .append("\", \"编辑").append(form.getTitle()).append("\"),\n");
            sb.append("    DELETE_").append(upperSnake).append("(\"delete").append(form.getName())
                    .append("\", \"删除").append(form.getTitle()).append("\"),\n");

            if (i < forms.size() - 1) {
                sb.append("\n");
            }
        }
        sb.append("    ;");

        return content.replace(ENUM_MARKER, sb.toString());
    }

    private String toUpperSnake(String upperCamelName) {
        return MoreStringUtils.camelToSnakeCase(upperCamelName).toUpperCase();
    }

}
