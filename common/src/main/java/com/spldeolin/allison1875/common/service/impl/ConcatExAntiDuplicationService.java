package com.spldeolin.allison1875.common.service.impl;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-01-01
 */
@Singleton
@Slf4j
public class ConcatExAntiDuplicationService implements AntiDuplicationService {

    @Override
    public Path getNewPathIfExist(Path path) {
        if (path.toFile().exists()) {
            String name = path.getFileName().toString();
            int dot = name.lastIndexOf('.');
            String base = dot > 0 ? name.substring(0, dot) : name;
            String ext = dot > 0 ? "." + name.substring(dot + 1) : "";
            Path newPath = path.resolveSibling(base + "Ex" + ext);
            log.info("Path of file [{}] is exist, hence '{}' is used", path, newPath);
            return getNewPathIfExist(newPath);
        } else {
            return path;
        }
    }

    @Override
    public String getNewMethodNameIfExist(String methodName, ClassOrInterfaceDeclaration... coids) {
        if (Arrays.stream(coids).anyMatch(coid -> CollectionUtils.isNotEmpty(coid.getMethodsByName(methodName)))) {
            String newMethodName = methodName + "Ex";
            log.info("Method name [{}] is duplicate in {}, hence '{}' is used", methodName,
                    Arrays.stream(coids).map(coid -> "'" + coid.getNameAsString() + "'")
                            .collect(Collectors.joining(", ")), newMethodName);
            return getNewMethodNameIfExist(newMethodName, coids);
        } else {
            return methodName;
        }
    }

    @Override
    public String getNewElementIfExist(String element, List<String> list) {
        if (list.contains(element)) {
            String newElement = element + "Ex";
            return getNewElementIfExist(newElement, list);
        } else {
            return element;
        }
    }

}
