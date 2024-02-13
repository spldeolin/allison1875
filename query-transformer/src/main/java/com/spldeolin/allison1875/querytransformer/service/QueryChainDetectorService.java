package com.spldeolin.allison1875.querytransformer.service;

import java.util.List;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.querytransformer.service.impl.QueryChainDetectorServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(QueryChainDetectorServiceImpl.class)
public interface QueryChainDetectorService {

    List<MethodCallExpr> detectQueryChains(Node node);

}