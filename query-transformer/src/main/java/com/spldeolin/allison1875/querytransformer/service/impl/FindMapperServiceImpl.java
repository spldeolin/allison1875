package com.spldeolin.allison1875.querytransformer.service.impl;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.exception.PrimaryTypeAbsentException;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.service.FindMapperService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-08-27
 */
@Singleton
@Log4j2
public class FindMapperServiceImpl implements FindMapperService {

    @Override
    public ClassOrInterfaceDeclaration findMapper(AstForest astForest, DesignMeta designMeta) {
        return astForest.findCu(designMeta.getMapperQualifier())
                .map(cu -> cu.getPrimaryType().orElseThrow(PrimaryTypeAbsentException::new)
                        .asClassOrInterfaceDeclaration()).orElse(null);
    }

}