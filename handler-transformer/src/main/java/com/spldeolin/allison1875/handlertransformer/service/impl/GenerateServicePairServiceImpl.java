package com.spldeolin.allison1875.handlertransformer.service.impl;

import java.nio.file.Path;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.constant.AnnotationConstant;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.constant.ImportConstant;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Javadocs;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.javabean.CreateServiceMethodHandleResult;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateServiceParam;
import com.spldeolin.allison1875.handlertransformer.javabean.ServiceGeneration;
import com.spldeolin.allison1875.handlertransformer.javabean.ServicePairDto;
import com.spldeolin.allison1875.handlertransformer.service.CreateServiceMethodService;
import com.spldeolin.allison1875.handlertransformer.service.EnsureNoRepeatService;
import com.spldeolin.allison1875.handlertransformer.service.FindServiceService;
import com.spldeolin.allison1875.handlertransformer.service.GenerateServicePairService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-03-05
 */
@Log4j2
@Singleton
public class GenerateServicePairServiceImpl implements GenerateServicePairService {

    @Inject
    private FindServiceService findServiceProc;

    @Inject
    private HandlerTransformerConfig handlerTransformerConfig;

    @Inject
    private CreateServiceMethodService createServiceMethodService;

    @Inject
    private EnsureNoRepeatService ensureNoRepeatService;

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
            pair = findServiceProc.findGenerated(param.getCu(), standardizedServiceName, param.getName2Pair());
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
        String noRepeat = ensureNoRepeatService.inService(pair.getService(),
                methodGeneration.getServiceMethod().getNameAsString());
        methodGeneration.getServiceMethod().setName(noRepeat);

        // 将方法以及Req、Resp的全名 添加到 Service
        MethodDeclaration serviceMethodImpl = methodGeneration.getServiceMethod();
        MethodDeclaration serviceMethod = new MethodDeclaration().setType(serviceMethodImpl.getType())
                .setName(serviceMethodImpl.getName()).setParameters(serviceMethodImpl.getParameters());
        serviceMethod.setBody(null);
        pair.getService().addMember(serviceMethod);
        CompilationUnit serverCu = pair.getService().findCompilationUnit().orElseThrow(CuAbsentException::new);
        serverCu.addImport(param.getReqDtoRespDtoInfo().getReqDtoQualifier());
        serverCu.addImport(param.getReqDtoRespDtoInfo().getRespDtoQualifier());
        serverCu.addImport(ImportConstant.JAVA_UTIL);
        serverCu.addImport(handlerTransformerConfig.getPageTypeQualifier());
        log.info("Method [{}] append to Service [{}]", serviceMethod.getName(), pair.getService().getName());

        // 将方法以及Req、Resp的全名 均添加到 每个ServiceImpl
        for (ClassOrInterfaceDeclaration serviceImpl : pair.getServiceImpls()) {
            serviceImpl.addMember(serviceMethodImpl);
            CompilationUnit serverImplCu = serviceImpl.findCompilationUnit().orElseThrow(CuAbsentException::new);
            serverImplCu.addImport(param.getReqDtoRespDtoInfo().getReqDtoQualifier());
            serverImplCu.addImport(param.getReqDtoRespDtoInfo().getRespDtoQualifier());
            serverImplCu.addImport(ImportConstant.JAVA_UTIL);
            serverImplCu.addImport(handlerTransformerConfig.getPageTypeQualifier());
            log.info("Method [{}] append to Service Impl [{}]", serviceMethodImpl.getName(), serviceImpl.getName());
        }

        // 将生成的方法所需的import 均添加到 Service 和 每个 ServiceImpl
        for (String appendImport : methodGeneration.getAppendImports()) {
            CompilationUnit serviceCu = pair.getService().findCompilationUnit().orElseThrow(CuAbsentException::new);
            serviceCu.addImport(appendImport);
            serviceCu.addImport(param.getReqDtoRespDtoInfo().getReqDtoQualifier());
            pair.getServiceImpls().forEach(
                    serviceImpl -> serviceImpl.findCompilationUnit().orElseThrow(CuAbsentException::new)
                            .addImport(appendImport));
        }

        ServiceGeneration result = new ServiceGeneration();
        result.setServiceVarName(MoreStringUtils.lowerFirstLetter(pair.getService().getNameAsString()));
        result.setService(pair.getService());
        result.getServiceImpls().addAll(pair.getServiceImpls());
        result.setServiceQualifier(
                pair.getService().getFullyQualifiedName().orElseThrow(QualifierAbsentException::new));
        result.setMethodName(serviceMethod.getNameAsString());
        return result;
    }

    private ServicePairDto generateServicePair(GenerateServiceParam param, String serviceName,
            Map<String, ServicePairDto> name2Pair) {
        Path sourceRoot = Locations.getStorage(param.getCu()).getSourceRoot();
        ServicePairDto pair;
        CompilationUnit serviceCu = new CompilationUnit();
        serviceCu.setPackageDeclaration(handlerTransformerConfig.getServicePackage());
        serviceCu.setImports(param.getCu().getImports());
        ClassOrInterfaceDeclaration service = new ClassOrInterfaceDeclaration();
        service.setJavadocComment(Javadocs.createJavadoc(concatServiceDescription(param.getFirstLineDto()),
                handlerTransformerConfig.getAuthor()));
        service.setPublic(true).setStatic(false).setInterface(true)
                .setName(ensureNoRepeatService.inAstForest(param.getAstForest(), serviceName));
        serviceCu.setTypes(new NodeList<>(service));
        Path storage = CodeGenerationUtils.fileInPackageAbsolutePath(sourceRoot,
                handlerTransformerConfig.getServicePackage(), service.getName() + ".java");
        serviceCu.setStorage(storage);
        log.info("generate Service [{}]", service.getName());

        CompilationUnit serviceImplCu = new CompilationUnit();
        serviceImplCu.setPackageDeclaration(handlerTransformerConfig.getServiceImplPackage());
        serviceImplCu.setImports(param.getCu().getImports());
        serviceImplCu.addImport(service.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new));
        serviceImplCu.addImport(ImportConstant.LOMBOK_SLF4J);
        serviceImplCu.addImport(ImportConstant.SPRING_SERVICE);
        ClassOrInterfaceDeclaration serviceImpl = new ClassOrInterfaceDeclaration();
        serviceImpl.setJavadocComment(Javadocs.createJavadoc(concatServiceDescription(param.getFirstLineDto()),
                handlerTransformerConfig.getAuthor()));
        serviceImpl.addAnnotation(AnnotationConstant.SLF4J);
        serviceImpl.addAnnotation(AnnotationConstant.SERVICE);
        String serviceImplName = ensureNoRepeatService.inAstForest(param.getAstForest(),
                service.getNameAsString() + "Impl");
        serviceImpl.setPublic(true).setStatic(false).setInterface(false).setName(serviceImplName)
                .addImplementedType(service.getNameAsString());
        serviceImplCu.setTypes(new NodeList<>(serviceImpl));
        storage = CodeGenerationUtils.fileInPackageAbsolutePath(sourceRoot,
                handlerTransformerConfig.getServiceImplPackage(), serviceImpl.getName() + ".java");
        serviceImplCu.setStorage(storage);
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
        if (handlerTransformerConfig.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + firstLine.getLotNo();
        }
        return result;
    }

}