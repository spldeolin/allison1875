package com.spldeolin.allison1875.handlertransformer.service.impl;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.handlertransformer.enums.JavabeanTypeEnum;
import com.spldeolin.allison1875.handlertransformer.javabean.BeforeJavabeanCuBuildResult;
import com.spldeolin.allison1875.handlertransformer.service.FieldService;

/**
 * @author Deolin 2021-01-29
 */
@Singleton
public class FieldServiceImpl implements FieldService {

    @Override
    public BeforeJavabeanCuBuildResult beforeJavabeanCuBuild(FieldDeclaration field, JavabeanTypeEnum javabeanType) {
        return new BeforeJavabeanCuBuildResult().setField(field);
    }

}