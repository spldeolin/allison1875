package com.spldeolin.allison1875.querytransformer.service.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.service.FindMapperService;

/**
 * @author Deolin 2021-08-27
 */
@Singleton
public class FindMapperServiceImpl implements FindMapperService {

    @Override
    public ClassOrInterfaceDeclaration findMapper(AstForest astForest, DesignMeta designMeta) {
        CompilationUnit cu = astForest.findCu(designMeta.getMapperQualifier());
        if (cu == null) {
            return null;
        }
        return cu.getPrimaryType().orElseThrow(RuntimeException::new).asClassOrInterfaceDeclaration();
    }

    @Override
    public boolean isMapperMethodPresent(AstForest astForest, DesignMeta designMeta, ChainAnalysisDto chainAnalysis) {
        ClassOrInterfaceDeclaration mapper = findMapper(astForest, designMeta);
        if (mapper == null) {
            return false;
        }
        return mapper.getMethodsByName(chainAnalysis.getMethodName()).size() > 0;
    }

}