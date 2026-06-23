package com.spldeolin.allison1875.appgenerator.service.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.appgenerator.service.ControllerAuthAnnotateService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-06-21
 */
@Singleton
@Slf4j
public class ControllerAuthAnnotateServiceImpl implements ControllerAuthAnnotateService {

    @Override
    public void annotateControllers(List<FormDef> forms, Path backendOutputRoot, String namespace) {
        String namespacePath = namespace.replace('.', '/');

        for (FormDef form : forms) {
            String formName = form.getName();
            String upperSnake = toUpperSnake(formName);
            Path controllerFile = backendOutputRoot.resolve(
                    "src/main/java/" + namespacePath + "/controller/" + formName + "Controller.java");

            if (!Files.exists(controllerFile)) {
                log.warn("controller file not found, skipping @WebApiAuth annotation: {}", controllerFile);
                continue;
            }

            annotateController(controllerFile, upperSnake);
        }
    }

    private void annotateController(Path controllerFile, String upperSnake) {
        List<String> lines;
        try {
            lines = Files.readAllLines(controllerFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        List<String> result = new ArrayList<>();
        boolean importAdded = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            // Insert imports after the last existing import line
            if (!importAdded && i + 1 < lines.size() && lines.get(i).startsWith("import ") && !lines.get(i + 1)
                    .startsWith("import ")) {
                result.add(line);
                result.add("import " + extractNamespaceFromFile(lines) + ".annotation.WebApiAuth;");
                result.add("import " + extractNamespaceFromFile(lines) + ".enums.PermissionEnum;");
                importAdded = true;
                continue;
            }

            // Insert @WebApiAuth before @PostMapping lines
            if (line.contains("@PostMapping(\"create")) {
                String indent = extractIndent(line);
                result.add(indent + "@WebApiAuth(PermissionEnum.CREATE_" + upperSnake + ")");
            } else if (line.contains("@PostMapping(\"update")) {
                String indent = extractIndent(line);
                result.add(indent + "@WebApiAuth(PermissionEnum.UPDATE_" + upperSnake + ")");
            } else if (line.contains("@PostMapping(\"list")) {
                String indent = extractIndent(line);
                result.add(indent + "@WebApiAuth(PermissionEnum.LIST_" + upperSnake + ")");
            } else if (line.contains("@PostMapping(\"get") && line.contains("Detail")) {
                String indent = extractIndent(line);
                result.add(indent + "@WebApiAuth(PermissionEnum.LIST_" + upperSnake + ")");
            } else if (line.contains("@PostMapping(\"delete")) {
                String indent = extractIndent(line);
                result.add(indent + "@WebApiAuth(PermissionEnum.DELETE_" + upperSnake + ")");
            }

            result.add(line);
        }

        try {
            Files.writeString(controllerFile, String.join("\n", result) + "\n", StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        log.info("annotated @WebApiAuth on controller: {}", controllerFile.getFileName());
    }

    private String extractNamespaceFromFile(List<String> lines) {
        for (String line : lines) {
            if (line.startsWith("package ")) {
                // e.g. "package com.example.controller;" -> "com.example"
                String pkg = line.substring("package ".length(), line.length() - 1);
                // Remove the last segment (e.g. ".controller")
                int lastDot = pkg.lastIndexOf('.');
                return lastDot > 0 ? pkg.substring(0, lastDot) : pkg;
            }
        }
        return "";
    }

    private String extractIndent(String line) {
        StringBuilder indent = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == ' ' || c == '\t') {
                indent.append(c);
            } else {
                break;
            }
        }
        return indent.toString();
    }

    private String toUpperSnake(String upperCamelName) {
        return MoreStringUtils.camelToSnakeCase(upperCamelName).toUpperCase();
    }

}
