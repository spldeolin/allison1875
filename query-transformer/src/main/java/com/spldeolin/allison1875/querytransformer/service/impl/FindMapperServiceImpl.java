package com.spldeolin.allison1875.querytransformer.service.impl;

import java.util.Optional;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.service.FindMapperService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-08-27
 */
@Singleton
@Slf4j
public class FindMapperServiceImpl implements FindMapperService {

    @Override
    public ClassOrInterfaceDeclaration findMapper(AstForest astForest, DesignMeta designMeta) {
        Optional<CompilationUnit> cu = astForest.findCu(designMeta.getMapperQualifier());
        if (!cu.isPresent()) {
            return null;
        }
        Optional<TypeDeclaration<?>> pt = cu.get().getPrimaryType();
        if (!pt.isPresent()) {
            return null;
        }
        if (!pt.get().isClassOrInterfaceDeclaration()) {
            return null;
        }
        return pt.get().asClassOrInterfaceDeclaration();
    }

}