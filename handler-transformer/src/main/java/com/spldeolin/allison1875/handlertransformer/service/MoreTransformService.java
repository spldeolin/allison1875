package com.spldeolin.allison1875.handlertransformer.service;

import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.HandlerCreation;
import com.spldeolin.allison1875.handlertransformer.service.impl.MoreTransformServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(MoreTransformServiceImpl.class)
public interface MoreTransformService {

    Map<String, Object> parseMoreFromFirstLine(VariableDeclarator vd);

    Collection<CompilationUnit> transform(AstForest clonedAstForest, FirstLineDto firstLine,
            HandlerCreation handlerCreation, Collection<String> dtoQualifiers);

}