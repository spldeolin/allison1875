package com.spldeolin.allison1875.querytransformer.service.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.QueryTransformerConfig;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParamGenerationDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultGenerationDto;
import com.spldeolin.allison1875.querytransformer.service.FindMapperService;
import com.spldeolin.allison1875.querytransformer.service.GenerateMethodIntoMapperService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-10-10
 */
@Singleton
@Log4j2
public class GenerateMethodIntoMapperServiceImpl implements GenerateMethodIntoMapperService {

    @Inject
    private FindMapperService findMapperService;

    @Inject
    private QueryTransformerConfig queryTransformerConfig;

    @Override
    public CompilationUnit generate(AstForest astForest, DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ParamGenerationDto paramGeneration, ResultGenerationDto resultGeneration) {
        ClassOrInterfaceDeclaration mapper = findMapperService.findMapper(astForest, designMeta);
        if (mapper == null) {
            return null;
        }

        for (String anImport : resultGeneration.getImports()) {
            mapper.findCompilationUnit().orElseThrow(CuAbsentException::new).addImport(anImport);
        }
        for (String anImport : paramGeneration.getImports()) {
            mapper.findCompilationUnit().orElseThrow(CuAbsentException::new).addImport(anImport);
        }

        MethodDeclaration method = new MethodDeclaration();
        if (queryTransformerConfig.getEnableLotNoAnnounce()) {
            method.setJavadocComment(chainAnalysis.getLotNo());
        }
        method.setType(resultGeneration.getResultType());
        method.setName(chainAnalysis.getMethodName());
        method.setParameters(new NodeList<>(paramGeneration.getParameters()));
        method.setBody(null);
        mapper.getMembers().add(method);
        return mapper.findCompilationUnit().orElseThrow(CuAbsentException::new);
    }

}