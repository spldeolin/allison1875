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

    @Override
    public void generatePermissionEnum(List<FormDef> allForms, Path backendOutputRoot, String namespace) {
        String namespacePath = namespace.replace('.', '/');
        Path targetFile = backendOutputRoot.resolve("src/main/java/" + namespacePath + "/enums/PermissionEnum.java");

        String sourceCode = buildSourceCode(allForms, namespace);

        try {
            Files.createDirectories(targetFile.getParent());
            Files.writeString(targetFile, sourceCode, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        log.info("generated PermissionEnum with {} permission points for {} forms",
                allForms.size() * 4, allForms.size());
    }

    private String buildSourceCode(List<FormDef> allForms, String namespace) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(namespace).append(".enums;\n\n");
        sb.append("import lombok.AllArgsConstructor;\n");
        sb.append("import lombok.Getter;\n\n");
        sb.append("@Getter\n");
        sb.append("@AllArgsConstructor\n");
        sb.append("public enum PermissionEnum {\n\n");

        for (int i = 0; i < allForms.size(); i++) {
            FormDef form = allForms.get(i);
            String upperSnake = toUpperSnake(form.getName());
            String title = form.getTitle();
            String groupRef = "Group." + upperSnake;

            sb.append("    LIST_").append(upperSnake).append("(\"LIST_").append(upperSnake)
                    .append("\", \"查看").append(title).append("\", ").append(groupRef).append(", null),\n");
            sb.append("    CREATE_").append(upperSnake).append("(\"CREATE_").append(upperSnake)
                    .append("\", \"创建").append(title).append("\", ").append(groupRef)
                    .append(", LIST_").append(upperSnake).append("),\n");
            sb.append("    UPDATE_").append(upperSnake).append("(\"UPDATE_").append(upperSnake)
                    .append("\", \"编辑").append(title).append("\", ").append(groupRef)
                    .append(", LIST_").append(upperSnake).append("),\n");
            sb.append("    DELETE_").append(upperSnake).append("(\"DELETE_").append(upperSnake)
                    .append("\", \"删除").append(title).append("\", ").append(groupRef)
                    .append(", LIST_").append(upperSnake).append("),\n");

            if (i < allForms.size() - 1) {
                sb.append("\n");
            }
        }

        sb.append("    ;\n\n");
        sb.append("    private final String code;\n");
        sb.append("    private final String title;\n");
        sb.append("    private final Group group;\n");
        sb.append("    private final PermissionEnum baseOn;\n\n");

        sb.append("    @Getter\n");
        sb.append("    @AllArgsConstructor\n");
        sb.append("    public enum Group {\n");

        String groupEntries = allForms.stream()
                .map(form -> {
                    String upperSnake = toUpperSnake(form.getName());
                    return "        " + upperSnake + "(\"" + upperSnake + "\", \"" + form.getTitle() + "管理\")";
                })
                .collect(Collectors.joining(",\n"));
        sb.append(groupEntries).append(",\n");

        sb.append("        ;\n\n");
        sb.append("        private final String code;\n");
        sb.append("        private final String title;\n");
        sb.append("    }\n\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String toUpperSnake(String upperCamelName) {
        return MoreStringUtils.camelToSnakeCase(upperCamelName).toUpperCase();
    }

}
