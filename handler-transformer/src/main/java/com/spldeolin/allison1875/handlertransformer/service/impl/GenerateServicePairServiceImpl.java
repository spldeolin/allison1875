package com.spldeolin.allison1875.handlertransformer.service.impl;

import java.nio.file.Path;
import org.apache.commons.io.FilenameUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.constant.AnnotationConstant;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.constant.ImportConstant;
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
import com.spldeolin.allison1875.handlertransformer.service.GenerateServicePairService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-03-05
 */
@Slf4j
@Singleton
public class GenerateServicePairServiceImpl implements GenerateServicePairService {

    @Inject
    private HandlerTransformerConfig config;

    @Inject
    private CreateServiceMethodService createServiceMethodService;

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Override
    public ServiceGeneration generateService(GenerateServiceParam param) {
        FirstLineDto firstLineDto = param.getFirstLineDto();

        // 生成全新的 Service 与 ServiceImpl
        String serviceName = MoreStringUtils.upperFirstLetter(param.getFirstLineDto().getHandlerName()) + "Service";
        ServicePairDto pair = generateServicePair(param, serviceName);

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
        pair.getServiceCu().addImport(param.getReqDtoRespDtoInfo().getReqDtoQualifier());
        pair.getServiceCu().addImport(param.getReqDtoRespDtoInfo().getRespDtoQualifier());
        pair.getServiceCu().addImport(ImportConstant.JAVA_UTIL);
        pair.getServiceCu().addImport(config.getPageTypeQualifier());
        log.info("Method [{}] append to Service [{}]", serviceMethod.getName(), service.getName());

        // 将方法以及Req、Resp的全名 均添加到 每个ServiceImpl

        ClassOrInterfaceDeclaration serviceImpl = pair.getServiceImpl();
        serviceImpl.addMember(serviceMethodImpl);
        pair.getServiceImplCu().addImport(param.getReqDtoRespDtoInfo().getReqDtoQualifier());
        pair.getServiceImplCu().addImport(param.getReqDtoRespDtoInfo().getRespDtoQualifier());
        pair.getServiceImplCu().addImport(ImportConstant.JAVA_UTIL);
        pair.getServiceImplCu().addImport(config.getPageTypeQualifier());
        log.info("Method [{}] append to Service Impl [{}]", serviceMethodImpl.getName(), serviceImpl.getName());

        // 将生成的方法所需的import 均添加到 Service 和 每个 ServiceImpl
        for (String appendImport : methodGeneration.getAppendImports()) {
            pair.getServiceCu().addImport(appendImport);
            pair.getServiceCu().addImport(param.getReqDtoRespDtoInfo().getReqDtoQualifier());
            pair.getServiceImplCu().addImport(appendImport);
        }

        ServiceGeneration result = new ServiceGeneration();
        result.setServiceVarName(MoreStringUtils.lowerFirstLetter(service.getNameAsString()));
        result.setService(service);
        result.setServiceCu(pair.getServiceCu());
        result.setServiceImpl(serviceImpl);
        result.setServiceImplCu(pair.getServiceImplCu());
        result.setServiceQualifier(
                service.getFullyQualifiedName().orElseThrow(() -> new QualifierAbsentException(service)));
        result.setMethodName(serviceMethod.getNameAsString());
        return result;
    }

    private ServicePairDto generateServicePair(GenerateServiceParam param, String serviceName) {
        Path sourceRoot = param.getAstForest().getAstForestRoot();
        CompilationUnit serviceCu = new CompilationUnit();
        serviceCu.setPackageDeclaration(config.getServicePackage());
        serviceCu.setImports(param.getControllerCu().getImports());
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
        serviceImplCu.setImports(param.getControllerCu().getImports());
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
        ServicePairDto pair = new ServicePairDto();
        pair.setService(service);
        pair.setServiceCu(serviceCu);
        pair.setServiceImpl(serviceImpl);
        pair.setServiceImplCu(serviceImplCu);
        return pair;
    }

    private String concatServiceDescription(FirstLineDto firstLine) {
        String result = "";
        if (config.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + firstLine.getLotNo();
        }
        return result;
    }

}