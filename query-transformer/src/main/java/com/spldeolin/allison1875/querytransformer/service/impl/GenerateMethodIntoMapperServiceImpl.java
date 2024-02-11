package com.spldeolin.allison1875.querytransformer.service.impl;

import java.util.Optional;
import java.util.stream.Collectors;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.exception.CuAbsentException;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.service.ImportService;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.QueryTransformerConfig;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParamGenerationDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultGenerationDto;
import com.spldeolin.allison1875.querytransformer.service.FindMapperService;
import com.spldeolin.allison1875.querytransformer.service.GenerateMethodIntoMapperService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-10-10
 */
@Singleton
@Slf4j
public class GenerateMethodIntoMapperServiceImpl implements GenerateMethodIntoMapperService {

    @Inject
    private FindMapperService findMapperService;

    @Inject
    private QueryTransformerConfig config;

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Inject
    private ImportService importService;

    @Override
    public Optional<FileFlush> generate(AstForest astForest, DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ParamGenerationDto paramGeneration, ResultGenerationDto resultGeneration) {
        ClassOrInterfaceDeclaration mapper = findMapperService.findMapper(astForest, designMeta);
        if (mapper == null) {
            return Optional.empty();
        }

        String methodName = chainAnalysis.getMethodName();
        methodName = antiDuplicationService.getNewMethodNameIfExist(methodName, mapper);
        log.info(
                "anti duplication worked completed, new method name '{}' update to ChainAnalysisDto.methodName, old={}",
                methodName, chainAnalysis.getMethodName());
        chainAnalysis.setMethodName(methodName);

        MethodDeclaration method = new MethodDeclaration();
        if (config.getEnableLotNoAnnounce()) {
            method.setJavadocComment(chainAnalysis.getLotNo());
        }
        method.setType(resultGeneration.getResultType().clone());
        method.setName(methodName);
        method.setParameters(new NodeList<>(
                paramGeneration.getParameters().stream().map(Parameter::clone).collect(Collectors.toList())));
        method.setBody(null);
        mapper.getMembers().add(method);

        CompilationUnit cu = mapper.findCompilationUnit().orElseThrow(() -> new CuAbsentException(mapper));
        importService.extractQualifiedTypeToImport(cu);
        return Optional.of(FileFlush.build(cu));
    }

}