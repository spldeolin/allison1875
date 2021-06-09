package com.spldeolin.allison1875.querytransformer.processor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-06-09
 */
@Log4j2
@Singleton
public class TransformMethodCallProc {

    public String process(DesignMeta designMeta, ChainAnalysisDto chainAnalysis) {
        MethodCallExpr callMapperMethod = StaticJavaParser.parseExpression(
                MoreStringUtils.lowerFirstLetter(designMeta.getMapperName()) + "." + chainAnalysis.getMethodName()
                        + "()").asMethodCallExpr();
        return callMapperMethod.toString();
    }

}