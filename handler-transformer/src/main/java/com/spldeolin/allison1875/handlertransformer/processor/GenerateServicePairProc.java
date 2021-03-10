package com.spldeolin.allison1875.handlertransformer.processor;

import java.nio.file.Path;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.constant.AnnotationConstant;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.base.util.ast.Javadocs;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.handle.CreateServiceMethodHandle;
import com.spldeolin.allison1875.handlertransformer.handle.javabean.CreateServiceMethodHandleResult;
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

    @Inject
    private CreateServiceMethodHandle createServiceMethodHandle;

    @Inject
    private EnsureNoRepeatProc ensureNoRepeatProc;

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
                service.setPublic(true).setStatic(false).setInterface(true)
                        .setName(standardizeServiceName(serviceName));
                serviceCu.setType(0, service);
                Path storage = Locations.getStorage(param.getCu()).getPath();
                storage = storage.resolve(CodeGenerationUtils.packageToPath(conf.getServicePackage()));
                storage = storage.resolve(service.getName() + ".java");
                serviceCu.setStorage(storage);
                Saves.add(serviceCu);
                log.info("generate Service [{}]", service.getName());

                CompilationUnit serviceImplCu = new CompilationUnit();
                serviceImplCu.setPackageDeclaration(conf.getServiceImplPackage());
                serviceImplCu.setImports(param.getCu().getImports());
                serviceImplCu.addImport(AnnotationConstant.SLF4J_QUALIFIER);
                ClassOrInterfaceDeclaration serviceImpl = new ClassOrInterfaceDeclaration();
                serviceImpl.setJavadocComment(Javadocs.createJavadoc("", conf.getAuthor()));
                serviceImpl.addAnnotation(AnnotationConstant.OVERRIDE);
                serviceImpl.addAnnotation(AnnotationConstant.SLF4J);
                serviceImpl.setPublic(true).setStatic(false).setInterface(false).setName(service.getName() + "Impl");
                serviceImplCu.setType(0, serviceImpl);
                storage = Locations.getStorage(param.getCu()).getPath();
                storage = storage.resolve(CodeGenerationUtils.packageToPath(conf.getServiceImplPackage()));
                storage = storage.resolve(serviceImpl.getName() + ".java");
                serviceImplCu.setStorage(storage);
                Saves.add(serviceCu);
                log.info("generate ServiceImpl [{}]", serviceImpl.getName());
                pair = new ServicePairDto().setService(service).setServiceImpls(Lists.newArrayList(serviceImpl));
            }

        } else {
            // 创建Single Method Service

            pair = new ServicePairDto();
        }

        // TODO 调用handle创建Service Method，添加到ServicePair中
        CreateServiceMethodHandleResult methodGeneration = createServiceMethodHandle
                .createMethodImpl(firstLineDto, param.getReqDtoRespDtoInfo().getParamType(),
                        param.getReqDtoRespDtoInfo().getResultType());
        // 方法名去重
        String noRepeat = ensureNoRepeatProc
                .inService(pair.getService(), methodGeneration.getServiceMethod().getNameAsString());
        methodGeneration.getServiceMethod().setName(noRepeat);

        MethodDeclaration serviceMethodImpl = methodGeneration.getServiceMethod();
        MethodDeclaration method = new MethodDeclaration().setType(serviceMethodImpl.getType())
                .setName(serviceMethodImpl.getName()).setParameters(serviceMethodImpl.getParameters());
        method.setBody(null);
        pair.getService().addMember(method);
        log.info("Method [{}] append to  Service [{}]", method.getName(), pair.getService().getName());
        pair.getServiceImpls().forEach(serviceImpl -> serviceImpl.addMember(serviceMethodImpl));
        log.info("MethodImpl [{}] append to  ServiceImpl [{}]", method.getName(),
                pair.getServiceImpls().stream().map(ClassOrInterfaceDeclaration::getNameAsString)
                        .collect(Collectors.joining(", ")));
        for (String appendImport : methodGeneration.getAppendImports()) {
            Imports.ensureImported(pair.getService(), appendImport);
            pair.getServiceImpls().forEach(serviceImpl -> Imports.ensureImported(serviceImpl, appendImport));
        }

        ServiceGeneration result = new ServiceGeneration();
        return result;
    }

    public ServiceImplGeneration generateServiceImpl(GenerateServiceImplParam param) {

        ServiceImplGeneration result = new ServiceImplGeneration();
        return result;
    }

    private String standardizeServiceName(String serviceName) {
        String result = MoreStringUtils.upperFirstLetter(serviceName);
        if (!result.endsWith("Service")) {
            result += "Service";
        }
        // report standardize
        if (!result.equals(serviceName)) {
            log.info("Service name standardize from [{}] to [{}]", serviceName, result);
        }
        return result;
    }

    private String calcServiceName(FirstLineDto firstLineDto) {
        String serviceName;
        if (StringUtils.isNotBlank(firstLineDto.getServiceName())) {
            serviceName = MoreStringUtils.upperFirstLetter(firstLineDto.getServiceName());
            if (!serviceName.endsWith("Service")) {
                serviceName += "Service";
            }
            // report standardize
            if (!serviceName.equals(firstLineDto.getServiceName())) {
                log.info("Service name standardize from [{}] to [{}]", firstLineDto.getServiceName(), serviceName);
            }
        } else {
            serviceName = MoreStringUtils.upperFirstLetter(firstLineDto.getHandlerName()) + "Service";
        }
        return serviceName;
    }

}