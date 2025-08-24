package com.spldeolin.allison1875.handlertransformer.service.impl;

import java.util.List;
import java.util.Optional;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.handlertransformer.dto.InitDecAnalysisDTO;
import com.spldeolin.allison1875.handlertransformer.service.HandlerTransformerServiceLayerExpansionServiceImpl;
import com.spldeolin.allison1875.handlertransformer.service.ServiceLayerExpansionService;

/**
 * @author Deolin 2025-08-14
 */
@Singleton
public class ServiceLayerExpansionServiceImplManager implements ServiceLayerExpansionService {

    private volatile ServiceLayerExpansionService currentImpl =
            new HandlerTransformerServiceLayerExpansionServiceImpl();

    public <T extends ServiceLayerExpansionService> void setCurrentImpl(T impl) {
        this.currentImpl = impl;
    }

    @Override
    public BlockStmt buildServiceImplMethodBody(InitDecAnalysisDTO initDecAnalysis, String reqBodyDTOType,
            List<VariableDeclarator> reqParams, String respBodyDTOType) {
        return currentImpl.buildServiceImplMethodBody(initDecAnalysis, reqBodyDTOType, reqParams, respBodyDTOType);
    }

    @Override
    public Optional<FieldDeclaration> buildFieldForServiceImpl(ClassOrInterfaceDeclaration serviceImpl,
            InitDecAnalysisDTO initDecAnalysis) {
        return currentImpl.buildFieldForServiceImpl(serviceImpl, initDecAnalysis);
    }

}
