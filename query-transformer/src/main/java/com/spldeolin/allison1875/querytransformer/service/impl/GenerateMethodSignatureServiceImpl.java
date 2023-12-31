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
import com.spldeolin.allison1875.querytransformer.javabean.ParameterTransformationDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultTransformationDto;
import com.spldeolin.allison1875.querytransformer.service.FindMapperService;
import com.spldeolin.allison1875.querytransformer.service.GenerateMethodSignatureService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-10-10
 */
@Singleton
@Log4j2
public class GenerateMethodSignatureServiceImpl implements GenerateMethodSignatureService {

    @Inject
    private FindMapperService findMapperService;

    @Inject
    private QueryTransformerConfig queryTransformerConfig;

    @Override
    public CompilationUnit process(AstForest astForest, DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ParameterTransformationDto parameterTransformation, ResultTransformationDto resultTransformation) {
        ClassOrInterfaceDeclaration mapper = findMapperService.findMapper(astForest, designMeta);
        if (mapper == null) {
            return null;
        }

        for (String anImport : resultTransformation.getImports()) {
            mapper.findCompilationUnit().orElseThrow(CuAbsentException::new).addImport(anImport);
        }
        if (parameterTransformation != null) {
            for (String anImport : parameterTransformation.getImports()) {
                mapper.findCompilationUnit().orElseThrow(CuAbsentException::new).addImport(anImport);
            }
        }

        MethodDeclaration method = new MethodDeclaration();
        if (queryTransformerConfig.getEnableLotNoAnnounce()) {
            method.setJavadocComment(chainAnalysis.getLotNo());
        }
        method.setType(resultTransformation.getResultType());
        method.setName(chainAnalysis.getMethodName());
        if (parameterTransformation != null) {
            method.setParameters(new NodeList<>(parameterTransformation.getParameters()));
        }
        method.setBody(null);
        mapper.getMembers().add(method);
        return mapper.findCompilationUnit().orElseThrow(CuAbsentException::new);
    }

}