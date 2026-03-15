package com.spldeolin.allison1875.handlertransformer.service.impl;

import java.util.Collections;
import java.util.List;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.spldeolin.allison1875.handlertransformer.dto.BuildServiceImplMethodBodyRetval;
import com.spldeolin.allison1875.handlertransformer.dto.InitDecAnalysisDTO;
import com.spldeolin.allison1875.handlertransformer.service.ServiceLayerExpansionService;

/**
 * @author Deolin 2025-08-14
 */
public class HandlerTransformerServiceLayerExpansionServiceImpl implements ServiceLayerExpansionService {

    @Override
    public List<AnnotationExpr> buildAnnotationsFormServiceImplMethod(InitDecAnalysisDTO initDecAnalysis) {
        return Collections.emptyList();
    }

    @Override
    public BuildServiceImplMethodBodyRetval buildServiceImplMethodBody(InitDecAnalysisDTO initDecAnalysis,
            String reqBodyDTOType, List<VariableDeclarator> reqParams, String respBodyDTOType) {
        BlockStmt body = new BlockStmt();
        if (respBodyDTOType != null) {
            body.addStatement(parseStatement("return null;"));
        }
        return new BuildServiceImplMethodBodyRetval().setBody(body);
    }

    @Override
    public List<FieldDeclaration> buildFieldsForServiceImpl(ClassOrInterfaceDeclaration serviceImpl,
            InitDecAnalysisDTO initDecAnalysis) {
        return Collections.emptyList();
    }

}
