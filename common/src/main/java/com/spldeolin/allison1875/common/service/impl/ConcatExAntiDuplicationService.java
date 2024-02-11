package com.spldeolin.allison1875.common.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
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
            String extension = FilenameUtils.getExtension(path.toString());
            Path newPath = Paths.get(FilenameUtils.removeExtension(path.toString()) + "Ex" + "." + extension);
            log.info("Path of file [{}] is exist, hence '{}' is used", path, newPath);
            return getNewPathIfExist(newPath);
        } else {
            return path;
        }
    }

    @Override
    public String getNewMethodNameIfExist(String methodName, ClassOrInterfaceDeclaration coid) {
        if (CollectionUtils.isNotEmpty(coid.getMethodsByName(methodName))) {
            String newMethodName = methodName + "Ex";
            log.info("Method name [{}] is duplicate in {} [{}], hence '{}' is used", methodName,
                    coid.isInterface() ? "Interface" : "Class", coid.getName(), newMethodName);
            return getNewMethodNameIfExist(newMethodName, coid);
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
