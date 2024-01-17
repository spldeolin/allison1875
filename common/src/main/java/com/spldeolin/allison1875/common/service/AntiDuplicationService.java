package com.spldeolin.allison1875.common.service;

import java.nio.file.Path;
import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.service.impl.ConcatExAntiDuplicationService;

/**
 * @author Deolin 2024-01-01
 */
@ImplementedBy(ConcatExAntiDuplicationService.class)
public interface AntiDuplicationService {

    Path getNewPathIfExist(Path path);

    String getNewMethodNameIfExist(String methodName, ClassOrInterfaceDeclaration coid);

    String getNewElementIfExist(String element, List<String> list);

}
