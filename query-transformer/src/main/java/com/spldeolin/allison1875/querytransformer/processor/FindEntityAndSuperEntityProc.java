package com.spldeolin.allison1875.querytransformer.processor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.QueryTransformerConfig;
import com.spldeolin.allison1875.querytransformer.javabean.EntityAndSuperEntityDto;

/**
 * @author Deolin 2021-06-05
 */
@Singleton
public class FindEntityAndSuperEntityProc {

    @Inject
    private QueryTransformerConfig queryTransformerConfig;

    public EntityAndSuperEntityDto find(AstForest astForest, DesignMeta designMeta) {
        CompilationUnit cu = astForest.findCu(designMeta.getEntityName());
        if (cu == null) {
            throw new IllegalStateException("cannot find Entity [" + designMeta.getEntityQualifier() + "]");
        }
        ClassOrInterfaceDeclaration entity = cu.getPrimaryType().orElseThrow(RuntimeException::new)
                .asClassOrInterfaceDeclaration();
        EntityAndSuperEntityDto result = new EntityAndSuperEntityDto().setEntity(entity);

        cu = astForest.findCu(queryTransformerConfig.getSuperEntityQualifier());
        if (cu != null) {
            result.setSuperEntity(
                    cu.getPrimaryType().orElseThrow(RuntimeException::new).asClassOrInterfaceDeclaration());
        }

        return result;
    }

}