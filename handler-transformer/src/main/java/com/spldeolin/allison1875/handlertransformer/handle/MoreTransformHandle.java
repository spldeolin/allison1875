package com.spldeolin.allison1875.handlertransformer.handle;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;

/**
 * @author Deolin 2021-04-07
 */
public interface MoreTransformHandle {

    Collection<CompilationUnit> transform(AstForest astForest, FirstLineDto firstLine);

}
