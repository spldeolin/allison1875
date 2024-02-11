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
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.exception.QualifierAbsentException;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.service.ImportExprService;
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

    @Inject
    private ImportExprService importExprService;

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public ServiceGeneration generateService(GenerateServiceParam param) {
        FirstLineDto firstLineDto = param.getFirstLineDto();

        // 生成全新的 Service 与 ServiceImpl
        String serviceName = MoreStringUtils.upperFirstLetter(param.getFirstLineDto().getHandlerName()) + "Service";
        ServicePairDto pair = generateServicePair(param, serviceName);

        // 调用handle创建Service Method
        CreateServiceMethodHandleResult methodGeneration = createServiceMethodService.createMethodImpl(firstLineDto,
                param.getReqDtoRespDtoInfo().getParamType(), param.getReqDtoRespDtoInfo().getResultType());
        // 方法名去重
        String serviceMethodName = antiDuplicationService.getNewMethodNameIfExist(
                methodGeneration.getServiceMethod().getNameAsString(), pair.getService());
        methodGeneration.getServiceMethod().setName(serviceMethodName);

        // 将方法 添加到 Service
        MethodDeclaration serviceMethodImpl = methodGeneration.getServiceMethod();
        MethodDeclaration serviceMethod = new MethodDeclaration().setType(serviceMethodImpl.getType())
                .setName(serviceMethodImpl.getName()).setParameters(serviceMethodImpl.getParameters());
        serviceMethod.setBody(null);
        ClassOrInterfaceDeclaration service = pair.getService();
        service.addMember(serviceMethod);
        log.info("Method [{}] append to Service [{}]", serviceMethod.getName(), service.getName());

        // 将方法 添加到 每个ServiceImpl
        ClassOrInterfaceDeclaration serviceImpl = pair.getServiceImpl();
        serviceImpl.addMember(serviceMethodImpl);
        log.info("Method [{}] append to Service Impl [{}]", serviceMethodImpl.getName(), serviceImpl.getName());

        importExprService.extractQualifiedTypeToImport(pair.getServiceCu());
        importExprService.extractQualifiedTypeToImport(pair.getServiceImplCu());

        ServiceGeneration result = new ServiceGeneration();
        result.setServiceVarName(MoreStringUtils.lowerFirstLetter(service.getNameAsString()));
        result.setServiceQualifier(
                service.getFullyQualifiedName().orElseThrow(() -> new QualifierAbsentException(service)));
        result.setMethodName(serviceMethod.getNameAsString());
        result.getFlushes().add(FileFlush.build(pair.getServiceCu()));
        result.getFlushes().add(FileFlush.build(pair.getServiceImplCu()));
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
        ClassOrInterfaceDeclaration serviceImpl = new ClassOrInterfaceDeclaration();
        JavadocUtils.setJavadoc(serviceImpl, comment, config.getAuthor());
        serviceImpl.addAnnotation(annotationExprService.lombokSlf4J());
        serviceImpl.addAnnotation(annotationExprService.springService());
        String serviceImplName = service.getNameAsString() + "Impl";
        serviceImpl.setPublic(true).setStatic(false).setInterface(false).setName(serviceImplName).addImplementedType(
                service.getFullyQualifiedName().orElseThrow(() -> new QualifierAbsentException(service)));
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