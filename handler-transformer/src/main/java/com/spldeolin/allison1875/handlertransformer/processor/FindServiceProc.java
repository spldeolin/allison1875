package com.spldeolin.allison1875.handlertransformer.processor;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.FileFindUtils;
import com.spldeolin.allison1875.base.util.ast.Locations;
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

    public ServicePairDto findGenerated(CompilationUnit controllerCu, String serviceName,
            Map<String, ServicePairDto> name2Pair) {
        if (name2Pair.get(serviceName) != null) {
            return name2Pair.get(serviceName);
        }

        ClassOrInterfaceDeclaration service = null;
        Collection<ClassOrInterfaceDeclaration> serviceImpls = Lists.newArrayList();
        boolean caught = false;
        Collection<CompilationUnit> serviceOrImplCus = getServiceOrImplCus(controllerCu);

        for (CompilationUnit cu : serviceOrImplCus) {
            if (cu.getPrimaryType().filter(TypeDeclaration::isClassOrInterfaceDeclaration).isPresent()) {
                ClassOrInterfaceDeclaration coid = cu.getPrimaryType().get().asClassOrInterfaceDeclaration();
                if (coid.getFullyQualifiedName().isPresent() && coid.isInterface()) {
                    if (coid.getNameAsString().equals(serviceName)) {
                        service = coid;
                        caught = true;
                    }
                }
            }
        }
        if (caught) {
            for (CompilationUnit cu : serviceOrImplCus) {
                if (cu.getPrimaryType().filter(TypeDeclaration::isClassOrInterfaceDeclaration).isPresent()) {
                    ClassOrInterfaceDeclaration coid = cu.getPrimaryType().get().asClassOrInterfaceDeclaration();
                    ClassOrInterfaceDeclaration finalTemp = service;
                    if (coid.getImplementedTypes().stream().anyMatch(implType -> implType.resolve().describe()
                            .equals(finalTemp.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new)))) {
                        serviceImpls.add(coid);
                    }
                }
            }
        }

        ServicePairDto result = new ServicePairDto().setService(service).setServiceImpls(serviceImpls);

        if (caught) {
            name2Pair.put(serviceName, result);
        }

        return result;
    }

    private Collection<CompilationUnit> getServiceOrImplCus(CompilationUnit cu) {
        Path servicePath = Locations.getStorage(cu).getSourceRoot()
                .resolve(CodeGenerationUtils.packageToPath(handlerTransformerConfig.getServicePackage()));
        Path serviceImplPath = Locations.getStorage(cu).getSourceRoot()
                .resolve(CodeGenerationUtils.packageToPath(handlerTransformerConfig.getServiceImplPackage()));
        Collection<CompilationUnit> serviceOrImplCu = Lists.newArrayList();
        FileFindUtils.recursively(servicePath, "java", java -> {
            try {
                serviceOrImplCu.add(StaticJavaParser.parse(java.toFile()));
            } catch (FileNotFoundException | ParseProblemException ignored) {
            }
        });
        FileFindUtils.recursively(serviceImplPath, "java", java -> {
            try {
                serviceOrImplCu.add(StaticJavaParser.parse(java.toFile()));
            } catch (FileNotFoundException | ParseProblemException ignored) {
            }
        });
        return serviceOrImplCu;
    }

}