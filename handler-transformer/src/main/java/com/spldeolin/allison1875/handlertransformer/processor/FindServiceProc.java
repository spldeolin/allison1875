package com.spldeolin.allison1875.handlertransformer.processor;

import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.javabean.ServicePairDto;

/**
 * @author Deolin 2021-03-04
 */
@Singleton
public class FindServiceProc {

    @Inject
    private HandlerTransformerConfig handlerTransformerConfig;

    public ServicePairDto findPresent(AstForest astForest, String presentServiceQualifier,
            Map<String, ServicePairDto> qualifier2Pair) {
        if (qualifier2Pair.get(presentServiceQualifier) != null) {
            return qualifier2Pair.get(presentServiceQualifier);
        }

        ClassOrInterfaceDeclaration service = null;
        Collection<ClassOrInterfaceDeclaration> serviceImpls = Lists.newArrayList();
        boolean caught = false;
        for (CompilationUnit cu : astForest.clone()) {
            if (cu.getPrimaryType().filter(TypeDeclaration::isClassOrInterfaceDeclaration).isPresent()) {
                ClassOrInterfaceDeclaration coid = cu.getPrimaryType().get().asClassOrInterfaceDeclaration();
                if (coid.getFullyQualifiedName().isPresent() && coid.isInterface()) {
                    String qualifier = coid.getFullyQualifiedName().get();
                    if (qualifier.equals(presentServiceQualifier)) {
                        service = coid;
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

    public ServicePairDto findGenerated(AstForest astForest, String serviceName,
            Map<String, ServicePairDto> name2Pair) {
        if (name2Pair.get(serviceName) != null) {
            return name2Pair.get(serviceName);
        }

        ClassOrInterfaceDeclaration service = null;
        Collection<ClassOrInterfaceDeclaration> serviceImpls = Lists.newArrayList();
        boolean caught = false;
        for (CompilationUnit cu : astForest.clone()) {
            if (cu.getPrimaryType().filter(TypeDeclaration::isClassOrInterfaceDeclaration).isPresent()) {
                ClassOrInterfaceDeclaration coid = cu.getPrimaryType().get().asClassOrInterfaceDeclaration();
                if (coid.getNameAsString().equals(serviceName) && coid.isInterface()) {
                    service = coid;
                    caught = true;
                }
                if (coid.getImplementedTypes().stream()
                        .anyMatch(implType -> implType.getNameAsString().equals(serviceName))) {
                    serviceImpls.add(coid);
                }
            }
        }

        ServicePairDto result = new ServicePairDto().setService(service).setServiceImpls(serviceImpls);

        if (caught) {
            // 去除implement Service中 Service名一致，但Service Qualifier不一致的 Service Impl
//            serviceImpls = qualifierMatch(service, serviceImpls);
//            result.setServiceImpls(serviceImpls);
            name2Pair.put(serviceName, result);
        }

        return result;
    }

    private Collection<ClassOrInterfaceDeclaration> qualifierMatch(ClassOrInterfaceDeclaration service,
            Collection<ClassOrInterfaceDeclaration> serviceImpls) {
        String serviceQualifier = service.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);
        Collection<ClassOrInterfaceDeclaration> temp = Lists.newArrayList();
        for (ClassOrInterfaceDeclaration serviceImpl : serviceImpls) {
            if (serviceImpl.getImplementedTypes().stream()
                    .anyMatch(implType -> implType.resolve().describe().equals(serviceQualifier))) {
                temp.add(serviceImpl);
            }
        }
        return temp;
    }

}