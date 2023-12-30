package com.spldeolin.allison1875.handlertransformer.service;

import java.util.List;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.handlertransformer.enums.JavabeanTypeEnum;
import com.spldeolin.allison1875.handlertransformer.service.impl.FieldServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(FieldServiceImpl.class)
public interface FieldService {

    List<ImportDeclaration> resolveTimeType(FieldDeclaration field, JavabeanTypeEnum javabeanType);

    List<ImportDeclaration> resolveLongType(FieldDeclaration field, JavabeanTypeEnum javabeanType);

}