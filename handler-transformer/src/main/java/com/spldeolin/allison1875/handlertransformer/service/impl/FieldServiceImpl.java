package com.spldeolin.allison1875.handlertransformer.service.impl;

import java.util.List;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.handlertransformer.enums.JavabeanTypeEnum;
import com.spldeolin.allison1875.handlertransformer.service.FieldService;

/**
 * @author Deolin 2021-01-29
 */
@Singleton
public class FieldServiceImpl implements FieldService {

    @Override
    public List<ImportDeclaration> resolveTimeType(FieldDeclaration field, JavabeanTypeEnum javabeanType) {
        return Lists.newArrayList();
    }

    @Override
    public List<ImportDeclaration> resolveLongType(FieldDeclaration field, JavabeanTypeEnum javabeanType) {
        return Lists.newArrayList();
    }

}