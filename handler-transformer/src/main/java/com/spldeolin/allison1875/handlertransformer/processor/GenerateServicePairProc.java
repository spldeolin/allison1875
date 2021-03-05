package com.spldeolin.allison1875.handlertransformer.processor;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.ast.Javadocs;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateServiceImplParam;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateServiceParam;
import com.spldeolin.allison1875.handlertransformer.javabean.ServiceGeneration;
import com.spldeolin.allison1875.handlertransformer.javabean.ServiceImplGeneration;
import com.spldeolin.allison1875.handlertransformer.javabean.ServicePairDto;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-03-05
 */
@Slf4j
@Singleton
public class GenerateServicePairProc {

    @Inject
    private FindServiceProc findServiceProc;

    @Inject
    private HandlerTransformerConfig conf;

    public ServiceGeneration generateService(GenerateServiceParam param) {
        FirstLineDto firstLineDto = param.getFirstLineDto();
        String presentServiceQualifier = firstLineDto.getPresentServiceQualifier();
        String serviceName = firstLineDto.getServiceName();

        ServicePairDto pair;

        if (StringUtils.isNotBlank(presentServiceQualifier)) {
            // 使用既存的Service
            pair = findServiceProc
                    .findPresent(param.getAstForest(), presentServiceQualifier, param.getQualifier2Pair());
            if (pair.getService() == null) {
                log.warn("cannot find Service [{}]", presentServiceQualifier);
                return null;
            }

        } else if (StringUtils.isNotBlank(serviceName)) {
            // 使用指定命名的Service
            pair = findServiceProc.findGenerated(serviceName);
            if (pair.getService() == null) {
                CompilationUnit serviceCu = new CompilationUnit();
                serviceCu.setPackageDeclaration(conf.getServicePackage());
                serviceCu.setImports(param.getCu().getImports());
                ClassOrInterfaceDeclaration service = new ClassOrInterfaceDeclaration();
                service.setJavadocComment(Javadocs.createJavadoc("", conf.getAuthor()));
                service.setPublic(true).setStatic(false).setInterface(true).setName(serviceName);


                // TODO 创建Service Pair
            }

        } else {
            // 创建Single Method Service
            // TODO 创建Service Pair
        }

        // TODO 调用handle创建Service Method，添加到ServicePair中

        ServiceGeneration result = new ServiceGeneration();
        return result;
    }

    public ServiceImplGeneration generateServiceImpl(GenerateServiceImplParam param) {

        ServiceImplGeneration result = new ServiceImplGeneration();
        return result;
    }

}