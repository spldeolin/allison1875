package com.spldeolin.allison1875.handlertransformer.service.impl;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.exception.QualifierAbsentException;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.javabean.ServicePairDto;
import com.spldeolin.allison1875.handlertransformer.service.FindServiceService;

/**
 * @author Deolin 2021-03-04
 */
@Singleton
public class FindServiceServiceImpl implements FindServiceService {

    @Inject
    private HandlerTransformerConfig config;

    @Override
    public ServicePairDto findPresent(AstForest astForest, String presentServiceQualifier,
            Map<String, ServicePairDto> qualifier2Pair) {
        if (qualifier2Pair.get(presentServiceQualifier) != null) {
            return qualifier2Pair.get(presentServiceQualifier);
        }

        ClassOrInterfaceDeclaration service = null;
        List<ClassOrInterfaceDeclaration> serviceImpls = Lists.newArrayList();
        boolean caught = false;
        for (CompilationUnit cu : astForest.cloneWithResetting()) {
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

    @Override
    public ServicePairDto findGenerated(String serviceName, Map<String, ServicePairDto> name2Pair,
            AstForest astForest) {
        if (name2Pair.get(serviceName) != null) {
            return name2Pair.get(serviceName);
        }

        ClassOrInterfaceDeclaration service = null;
        List<ClassOrInterfaceDeclaration> serviceImpls = Lists.newArrayList();
        boolean caught = false;
        List<CompilationUnit> serviceOrImplCus = getServiceOrImplCus(astForest);

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

    private List<CompilationUnit> getServiceOrImplCus(AstForest astForest) {
        File servicePackage = astForest.getAstForestRoot()
                .resolve(CodeGenerationUtils.packageToPath(config.getServicePackage())).toFile();
        File serviceImplPackage = astForest.getAstForestRoot()
                .resolve(CodeGenerationUtils.packageToPath(config.getServiceImplPackage())).toFile();
        List<CompilationUnit> serviceOrImplCu = Lists.newArrayList();
        FileUtils.iterateFiles(servicePackage, BaseConstant.JAVA_EXTENSIONS, true)
                .forEachRemaining(java -> serviceOrImplCu.add(CompilationUnitUtils.parseJava(java)));
        FileUtils.iterateFiles(serviceImplPackage, BaseConstant.JAVA_EXTENSIONS, true)
                .forEachRemaining(java -> serviceOrImplCu.add(CompilationUnitUtils.parseJava(java)));
        return serviceOrImplCu;
    }

}