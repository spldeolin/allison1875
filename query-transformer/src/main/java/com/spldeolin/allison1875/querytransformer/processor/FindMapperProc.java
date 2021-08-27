package com.spldeolin.allison1875.querytransformer.processor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;

/**
 * @author Deolin 2021-08-27
 */
@Singleton
public class FindMapperProc {

    public ClassOrInterfaceDeclaration findMapper(AstForest astForest, DesignMeta designMeta) {
        CompilationUnit cu = astForest.findCu(designMeta.getMapperQualifier());
        if (cu == null) {
            return null;
        }
        return cu.getPrimaryType().orElseThrow(RuntimeException::new).asClassOrInterfaceDeclaration();
    }

    public boolean isMapperMethodPresent(AstForest astForest, DesignMeta designMeta, ChainAnalysisDto chainAnalysis) {
        ClassOrInterfaceDeclaration mapper = findMapper(astForest, designMeta);
        if (mapper == null) {
            return false;
        }
        return mapper.getMethodsByName(chainAnalysis.getMethodName()).size() > 0;
    }

}