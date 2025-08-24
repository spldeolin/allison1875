package com.spldeolin.allison1875.handlertransformer.service;

import java.util.List;
import java.util.Optional;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.handlertransformer.dto.InitDecAnalysisDTO;
import com.spldeolin.allison1875.handlertransformer.service.impl.ServiceLayerExpansionServiceImplManager;

/**
 * @author Deolin 2025-08-14
 */
@ImplementedBy(ServiceLayerExpansionServiceImplManager.class)
public interface ServiceLayerExpansionService {

    BlockStmt buildServiceImplMethodBody(InitDecAnalysisDTO initDecAnalysis, String reqBodyDTOType,
            List<VariableDeclarator> reqParams, String respBodyDTOType);

    Optional<FieldDeclaration> buildFieldForServiceImpl(ClassOrInterfaceDeclaration serviceImpl,
            InitDecAnalysisDTO initDecAnalysis);

}
