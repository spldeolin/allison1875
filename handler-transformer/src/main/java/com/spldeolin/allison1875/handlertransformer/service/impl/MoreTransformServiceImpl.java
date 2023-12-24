package com.spldeolin.allison1875.handlertransformer.service.impl;

import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.HandlerCreation;
import com.spldeolin.allison1875.handlertransformer.service.MoreTransformService;

/**
 * @author Deolin 2021-04-07
 */
@Singleton
public class MoreTransformServiceImpl implements MoreTransformService {

    @Override
    public Map<String, Object> parseMoreFromFirstLine(VariableDeclarator vd) {
        return null;
    }

    @Override
    public Collection<CompilationUnit> transform(AstForest clonedAstForest, FirstLineDto firstLine,
            HandlerCreation handlerCreation, Collection<String> dtoQualifiers) {
        return Lists.newArrayList();
    }

}