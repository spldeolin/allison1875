package com.spldeolin.allison1875.handlertransformer.service;

import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.spldeolin.allison1875.handlertransformer.dto.BuildServiceImplMethodBodyRetval;
import com.spldeolin.allison1875.handlertransformer.dto.InitDecAnalysisDTO;

/**
 * @author Deolin 2025-08-14
 */
public interface ServiceLayerExpansionService {

    List<AnnotationExpr> buildAnnotationsFormServiceImplMethod(InitDecAnalysisDTO initDecAnalysis);

    BuildServiceImplMethodBodyRetval buildServiceImplMethodBody(InitDecAnalysisDTO initDecAnalysis,
            String reqBodyDTOType, List<VariableDeclarator> reqParams, String respBodyDTOType);

    List<FieldDeclaration> buildFieldsForServiceImpl(ClassOrInterfaceDeclaration serviceImpl,
            InitDecAnalysisDTO initDecAnalysis);

}
