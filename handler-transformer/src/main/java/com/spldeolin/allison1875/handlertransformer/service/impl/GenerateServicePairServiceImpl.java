package com.spldeolin.allison1875.handlertransformer.service.impl;

import java.nio.file.Path;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.constant.AnnotationConstant;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.constant.ImportConstant;
import com.spldeolin.allison1875.common.exception.CuAbsentException;
import com.spldeolin.allison1875.common.exception.QualifierAbsentException;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.javabean.CreateServiceMethodHandleResult;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateServiceParam;
import com.spldeolin.allison1875.handlertransformer.javabean.ServiceGeneration;
import com.spldeolin.allison1875.handlertransformer.javabean.ServicePairDto;
import com.spldeolin.allison1875.handlertransformer.service.CreateServiceMethodService;
import com.spldeolin.allison1875.handlertransformer.service.FindServiceService;
import com.spldeolin.allison1875.handlertransformer.service.GenerateServicePairService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-03-05
 */
@Slf4j
@Singleton
public class GenerateServicePairServiceImpl implements GenerateServicePairService {

    @Inject
    private FindServiceService findServiceProc;

    @Inject
    private HandlerTransformerConfig config;

    @Inject
    private CreateServiceMethodService createServiceMethodService;

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Override
    public ServiceGeneration generateService(GenerateServiceParam param) {
        FirstLineDto firstLineDto = param.getFirstLineDto();
        String presentServiceQualifier = firstLineDto.getPresentServiceQualifier();
        String serviceName = firstLineDto.getServiceName();

        ServicePairDto pair;

        if (StringUtils.isNotBlank(presentServiceQualifier)) {
            // 指定了Service全限定名
            pair = findServiceProc.findPresent(param.getAstForest(), presentServiceQualifier,
                    param.getQualifier2Pair());
            if (pair.getService() == null) {
                log.warn("cannot find Service [{}]", presentServiceQualifier);
                return null;
            }

        } else if (StringUtils.isNotBlank(serviceName)) {
            String standardizedServiceName = standardizeServiceName(serviceName);
            // 指定了Service的类名
            pair = findServiceProc.findGenerated(standardizedServiceName, param.getName2Pair(), param.getAstForest());
            if (pair.getService() == null) {
                // 生成全新的 Service 与 ServiceImpl （往往时第一次获取到ServiceName时）
                pair = generateServicePair(param, standardizedServiceName, param.getName2Pair());
            }

        } else {
            // 生成全新的 Service 与 ServiceImpl
            serviceName = MoreStringUtils.upperFirstLetter(param.getFirstLineDto().getHandlerName()) + "Service";
            pair = generateServicePair(param, serviceName, param.getName2Pair());
        }

        // 调用handle创建Service Method，添加到ServicePair中
        CreateServiceMethodHandleResult methodGeneration = createServiceMethodService.createMethodImpl(firstLineDto,
                param.getReqDtoRespDtoInfo().getParamType(), param.getReqDtoRespDtoInfo().getResultType());
        // 方法名去重
        String serviceMethodName = antiDuplicationService.getNewMethodNameIfExist(
                methodGeneration.getServiceMethod().getNameAsString(), pair.getService());
        methodGeneration.getServiceMethod().setName(serviceMethodName);

        // 将方法以及Req、Resp的全名 添加到 Service
        MethodDeclaration serviceMethodImpl = methodGeneration.getServiceMethod();
        MethodDeclaration serviceMethod = new MethodDeclaration().setType(serviceMethodImpl.getType())
                .setName(serviceMethodImpl.getName()).setParameters(serviceMethodImpl.getParameters());
        serviceMethod.setBody(null);
        ClassOrInterfaceDeclaration service = pair.getService();
        service.addMember(serviceMethod);
        CompilationUnit serverCu = service.findCompilationUnit().orElseThrow(() -> new CuAbsentException(service));
        serverCu.addImport(param.getReqDtoRespDtoInfo().getReqDtoQualifier());
        serverCu.addImport(param.getReqDtoRespDtoInfo().getRespDtoQualifier());
        serverCu.addImport(ImportConstant.JAVA_UTIL);
        serverCu.addImport(config.getPageTypeQualifier());
        log.info("Method [{}] append to Service [{}]", serviceMethod.getName(), service.getName());

        // 将方法以及Req、Resp的全名 均添加到 每个ServiceImpl
        for (ClassOrInterfaceDeclaration serviceImpl : pair.getServiceImpls()) {
            serviceImpl.addMember(serviceMethodImpl);
            CompilationUnit serverImplCu = serviceImpl.findCompilationUnit()
                    .orElseThrow(() -> new CuAbsentException(serviceImpl));
            serverImplCu.addImport(param.getReqDtoRespDtoInfo().getReqDtoQualifier());
            serverImplCu.addImport(param.getReqDtoRespDtoInfo().getRespDtoQualifier());
            serverImplCu.addImport(ImportConstant.JAVA_UTIL);
            serverImplCu.addImport(config.getPageTypeQualifier());
            log.info("Method [{}] append to Service Impl [{}]", serviceMethodImpl.getName(), serviceImpl.getName());
        }

        // 将生成的方法所需的import 均添加到 Service 和 每个 ServiceImpl
        for (String appendImport : methodGeneration.getAppendImports()) {
            CompilationUnit serviceCu = service.findCompilationUnit().orElseThrow(() -> new CuAbsentException(service));
            serviceCu.addImport(appendImport);
            serviceCu.addImport(param.getReqDtoRespDtoInfo().getReqDtoQualifier());
            pair.getServiceImpls().forEach(serviceImpl -> serviceImpl.findCompilationUnit()
                    .orElseThrow(() -> new CuAbsentException(serviceImpl)).addImport(appendImport));
        }

        ServiceGeneration result = new ServiceGeneration();
        result.setServiceVarName(MoreStringUtils.lowerFirstLetter(service.getNameAsString()));
        result.setService(service);
        result.getServiceImpls().addAll(pair.getServiceImpls());
        result.setServiceQualifier(
                service.getFullyQualifiedName().orElseThrow(() -> new QualifierAbsentException(service)));
        result.setMethodName(serviceMethod.getNameAsString());
        return result;
    }

    private ServicePairDto generateServicePair(GenerateServiceParam param, String serviceName,
            Map<String, ServicePairDto> name2Pair) {
        Path sourceRoot = param.getAstForest().getAstForestRoot();
        ServicePairDto pair;
        CompilationUnit serviceCu = new CompilationUnit();
        serviceCu.setPackageDeclaration(config.getServicePackage());
        serviceCu.setImports(param.getCu().getImports());
        ClassOrInterfaceDeclaration service = new ClassOrInterfaceDeclaration();
        String comment = concatServiceDescription(param.getFirstLineDto());
        JavadocUtils.setJavadoc(service, comment, config.getAuthor());
        service.setPublic(true).setStatic(false).setInterface(true).setName(serviceName);
        Path absolutePath = CodeGenerationUtils.fileInPackageAbsolutePath(sourceRoot, config.getServicePackage(),
                service.getName() + ".java");

        // anti-duplication
        absolutePath = antiDuplicationService.getNewPathIfExist(absolutePath);
        service.setName(FilenameUtils.getBaseName(absolutePath.toString()));

        serviceCu.setTypes(new NodeList<>(service));
        serviceCu.setStorage(absolutePath);
        log.info("generate Service [{}]", service.getName());

        CompilationUnit serviceImplCu = new CompilationUnit();
        serviceImplCu.setPackageDeclaration(config.getServiceImplPackage());
        serviceImplCu.setImports(param.getCu().getImports());
        serviceImplCu.addImport(
                service.getFullyQualifiedName().orElseThrow(() -> new QualifierAbsentException(service)));
        serviceImplCu.addImport(ImportConstant.LOMBOK_SLF4J);
        serviceImplCu.addImport(ImportConstant.SPRING_SERVICE);
        ClassOrInterfaceDeclaration serviceImpl = new ClassOrInterfaceDeclaration();
        JavadocUtils.setJavadoc(serviceImpl, comment, config.getAuthor());
        serviceImpl.addAnnotation(AnnotationConstant.SLF4J);
        serviceImpl.addAnnotation(AnnotationConstant.SERVICE);
        String serviceImplName = service.getNameAsString() + "Impl";
        serviceImpl.setPublic(true).setStatic(false).setInterface(false).setName(serviceImplName)
                .addImplementedType(service.getNameAsString());
        serviceImplCu.setTypes(new NodeList<>(serviceImpl));
        absolutePath = CodeGenerationUtils.fileInPackageAbsolutePath(sourceRoot, config.getServiceImplPackage(),
                serviceImpl.getName() + ".java");

        // anti-duplication
        absolutePath = antiDuplicationService.getNewPathIfExist(absolutePath);
        serviceImpl.setName(FilenameUtils.getBaseName(absolutePath.toString()));

        serviceImplCu.setStorage(absolutePath);
        log.info("generate ServiceImpl [{}]", serviceImpl.getName());
        pair = new ServicePairDto().setService(service).setServiceImpls(Lists.newArrayList(serviceImpl));
        name2Pair.put(serviceName, pair);
        return pair;
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

    private String concatServiceDescription(FirstLineDto firstLine) {
        String result = "";
        if (config.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + firstLine.getLotNo();
        }
        return result;
    }

}