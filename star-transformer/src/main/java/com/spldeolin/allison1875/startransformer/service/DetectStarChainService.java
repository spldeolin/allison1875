package com.spldeolin.allison1875.startransformer.service;

import java.util.List;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.startransformer.service.impl.DetectStarChainServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(DetectStarChainServiceImpl.class)
public interface DetectStarChainService {

    List<MethodCallExpr> process(Node cu);

}