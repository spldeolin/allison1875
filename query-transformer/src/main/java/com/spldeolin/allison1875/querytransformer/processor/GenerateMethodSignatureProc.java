package com.spldeolin.allison1875.querytransformer.processor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParameterTransformationDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultTransformationDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-10-10
 */
@Singleton
@Log4j2
public class GenerateMethodSignatureProc {

    @Inject
    private FindMapperProc findMapperProc;

    public CompilationUnit process(AstForest astForest, DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ParameterTransformationDto parameterTransformation, ResultTransformationDto resultTransformation) {
        ClassOrInterfaceDeclaration mapper = findMapperProc.findMapper(astForest, designMeta);
        if (mapper == null) {
            return null;
        }

        for (String anImport : resultTransformation.getImports()) {
            Imports.ensureImported(mapper, anImport);
        }
        if (parameterTransformation != null) {
            for (String anImport : parameterTransformation.getImports()) {
                Imports.ensureImported(mapper, anImport);
            }
        }

        MethodDeclaration method = new MethodDeclaration();
        method.setJavadocComment(chainAnalysis.getLotNo().asJavadocDescription());
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