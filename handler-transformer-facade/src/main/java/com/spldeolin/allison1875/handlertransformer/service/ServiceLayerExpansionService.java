package com.spldeolin.allison1875.handlertransformer.service;

import java.util.List;
import java.util.Optional;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.spldeolin.allison1875.handlertransformer.dto.BuildServiceImplMethodBodyRetval;
import com.spldeolin.allison1875.handlertransformer.dto.InitDecAnalysisDTO;

/**
 * @author Deolin 2025-08-14
 */
public interface ServiceLayerExpansionService {

    BuildServiceImplMethodBodyRetval buildServiceImplMethodBody(InitDecAnalysisDTO initDecAnalysis,
            String reqBodyDTOType,
            List<VariableDeclarator> reqParams, String respBodyDTOType);

    Optional<FieldDeclaration> buildFieldForServiceImpl(ClassOrInterfaceDeclaration serviceImpl,
            InitDecAnalysisDTO initDecAnalysis);

}
