package com.spldeolin.allison1875.handlertransformer.service;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.handlertransformer.enums.JavabeanTypeEnum;
import com.spldeolin.allison1875.handlertransformer.javabean.BeforeJavabeanCuBuildResult;
import com.spldeolin.allison1875.handlertransformer.service.impl.FieldServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(FieldServiceImpl.class)
public interface FieldService {

    BeforeJavabeanCuBuildResult beforeJavabeanCuBuild(FieldDeclaration field, JavabeanTypeEnum javabeanType);

}