package com.spldeolin.allison1875.handlertransformer.handle;

import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.handlertransformer.handle.javabean.HandlerCreation;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;

/**
 * @author Deolin 2021-04-07
 */
@Singleton
public class MoreTransformHandle {

    public Map<String, Object> parseMoreFromFirstLine(VariableDeclarator vd) {
        return null;
    }

    public Collection<CompilationUnit> transform(AstForest clonedAstForest, FirstLineDto firstLine,
            HandlerCreation handlerCreation, Collection<String> dtoQualifiers) {
        return Lists.newArrayList();
    }

}