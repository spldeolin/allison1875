package com.spldeolin.allison1875.common.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2024-01-01
 */
@Singleton
@Log4j2
public class AntiDuplicationServiceImpl implements AntiDuplicationService {

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
        if (!coid.getMethodsByName(methodName).isEmpty()) {
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
        return getNewElementIfExist(element, list, 1);
    }

    private String getNewElementIfExist(String element, List<String> list, int n) {
        if (list.contains(element)) {
            n += 1;
            String newElement = element + n;
            return getNewElementIfExist(newElement, list, n);
        } else {
            return element;
        }
    }

}
