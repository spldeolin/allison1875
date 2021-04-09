package com.spldeolin.allison1875.handlertransformer.handle;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;

/**
 * @author Deolin 2021-04-07
 */
@Singleton
public class MoreTransformHandle {

    public Collection<CompilationUnit> transform(AstForest resetAstForest, FirstLineDto firstLine) {
        return Lists.newArrayList();
    }

}