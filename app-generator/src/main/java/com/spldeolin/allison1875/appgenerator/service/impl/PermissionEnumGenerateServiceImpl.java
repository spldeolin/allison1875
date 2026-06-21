package com.spldeolin.allison1875.appgenerator.service.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.appgenerator.service.PermissionEnumGenerateService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-06-20
 */
@Singleton
@Slf4j
public class PermissionEnumGenerateServiceImpl implements PermissionEnumGenerateService {

    private static final String ENUM_MARKER = "    // === 以下枚举项由 app-generator 生成，勿手动修改 ===\n    ;";

    private static final String GROUP_MARKER = "        // === 由 app-generator 生成 ===\n        ;";

    @Override
    public void generatePermissionEnum(List<FormDef> allForms, Path backendOutputRoot, String namespace) {
        String namespacePath = namespace.replace('.', '/');
        Path targetFile = backendOutputRoot.resolve("src/main/java/" + namespacePath + "/enums/PermissionEnum.java");

        try {
            String content = Files.readString(targetFile, StandardCharsets.UTF_8);
            content = injectEnumEntries(content, allForms);
            content = injectGroupEntries(content, allForms);
            Files.writeString(targetFile, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        log.info("generated PermissionEnum with {} permission points for {} forms", allForms.size() * 4,
                allForms.size());
    }

    private String injectEnumEntries(String content, List<FormDef> allForms) {
        if (allForms.isEmpty()) {
            return content.replace(ENUM_MARKER, "    ;");
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < allForms.size(); i++) {
            FormDef form = allForms.get(i);
            String upperSnake = toUpperSnake(form.getName());
            String title = form.getTitle();
            String groupRef = "Group." + upperSnake;

            sb.append("    LIST_").append(upperSnake).append("(\"LIST_").append(upperSnake)
                    .append("\", \"查看").append(title).append("\", ").append(groupRef).append(", null),\n");
            sb.append("    CREATE_").append(upperSnake).append("(\"CREATE_").append(upperSnake).append("\", \"创建")
                    .append(title).append("\", ").append(groupRef).append(", Lists.newArrayList(LIST_")
                    .append(upperSnake).append(")),\n");
            sb.append("    UPDATE_").append(upperSnake).append("(\"UPDATE_").append(upperSnake).append("\", \"编辑")
                    .append(title).append("\", ").append(groupRef).append(", Lists.newArrayList(LIST_")
                    .append(upperSnake).append(")),\n");
            sb.append("    DELETE_").append(upperSnake).append("(\"DELETE_").append(upperSnake).append("\", \"删除")
                    .append(title).append("\", ").append(groupRef).append(", Lists.newArrayList(LIST_")
                    .append(upperSnake).append(")),\n");

            if (i < allForms.size() - 1) {
                sb.append("\n");
            }
        }
        sb.append("    ;");

        return content.replace(ENUM_MARKER, sb.toString());
    }

    private String injectGroupEntries(String content, List<FormDef> allForms) {
        if (allForms.isEmpty()) {
            return content.replace(GROUP_MARKER, "        ;");
        }

        String groupEntries = allForms.stream()
                .map(form -> {
                    String upperSnake = toUpperSnake(form.getName());
                    return "        " + upperSnake + "(\"" + upperSnake + "\", \"" + form.getTitle() + "管理\")";
                })
                .collect(Collectors.joining(",\n"));

        return content.replace(GROUP_MARKER, groupEntries + ",\n        ;");
    }

    private String toUpperSnake(String upperCamelName) {
        return MoreStringUtils.camelToSnakeCase(upperCamelName).toUpperCase();
    }

}
