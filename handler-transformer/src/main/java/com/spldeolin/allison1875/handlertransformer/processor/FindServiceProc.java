package com.spldeolin.allison1875.handlertransformer.processor;

import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.handlertransformer.javabean.ServicePairDto;

/**
 * @author Deolin 2021-03-04
 */
@Singleton
public class FindServiceProc {

    public ServicePairDto findServiceWithServiceImpls(AstForest astForest, String presentServiceQualifier,
            Map<String, ServicePairDto> qualifier2Pair) {
        if (qualifier2Pair.get(presentServiceQualifier) != null) {
            return qualifier2Pair.get(presentServiceQualifier);
        }

        ClassOrInterfaceDeclaration service = null;
        Collection<ClassOrInterfaceDeclaration> serviceImpls = Lists.newArrayList();
        boolean caught = false;
        for (CompilationUnit cu2 : astForest.clone()) {
            if (cu2.getPrimaryType().filter(TypeDeclaration::isClassOrInterfaceDeclaration).isPresent()) {
                ClassOrInterfaceDeclaration coid = cu2.getPrimaryType().get().asClassOrInterfaceDeclaration();
                if (coid.getFullyQualifiedName().isPresent() && coid.isInterface()) {
                    String qualifier = coid.getFullyQualifiedName().get();
                    if (qualifier.equals(presentServiceQualifier)) {
                        service = coid.asClassOrInterfaceDeclaration();
                        caught = true;
                    }
                }
                if (coid.getImplementedTypes().stream()
                        .anyMatch(implType -> implType.resolve().describe().equals(presentServiceQualifier))) {
                    serviceImpls.add(coid);
                }
            }
        }

        ServicePairDto result = new ServicePairDto().setService(service).setServiceImpls(serviceImpls);

        if (caught) {
            qualifier2Pair.put(presentServiceQualifier, result);
        }

        return result;
    }

}