package com.spldeolin.allison1875.handlertransformer.processor;

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
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.base.util.ast.Javadocs;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.handle.CreateServiceMethodHandle;
import com.spldeolin.allison1875.handlertransformer.handle.javabean.CreateServiceMethodHandleResult;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateServiceParam;
import com.spldeolin.allison1875.handlertransformer.javabean.ServiceGeneration;
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
            // 指定了Service全限定名
            pair = findServiceProc
                    .findPresent(param.getAstForest(), presentServiceQualifier, param.getQualifier2Pair());
            if (pair.getService() == null) {
                log.warn("cannot find Service [{}]", presentServiceQualifier);
                return null;
            }

        } else if (StringUtils.isNotBlank(serviceName)) {
            String standardizedServiceName = standardizeServiceName(serviceName);
            // 指定了Service的类名
            pair = findServiceProc.findGenerated(param.getAstForest(), standardizedServiceName, param.getName2Pair());
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
        CreateServiceMethodHandleResult methodGeneration = createServiceMethodHandle
                .createMethodImpl(firstLineDto, param.getReqDtoRespDtoInfo().getParamType(),
                        param.getReqDtoRespDtoInfo().getResultType());
        // 方法名去重
        String noRepeat = ensureNoRepeatProc
                .inService(pair.getService(), methodGeneration.getServiceMethod().getNameAsString());
        methodGeneration.getServiceMethod().setName(noRepeat);

        // 将方法以及Req、Resp的全名 添加到 Service
        MethodDeclaration serviceMethodImpl = methodGeneration.getServiceMethod();
        MethodDeclaration serviceMethod = new MethodDeclaration().setType(serviceMethodImpl.getType())
                .setName(serviceMethodImpl.getName()).setParameters(serviceMethodImpl.getParameters());
        serviceMethod.setBody(null);
        pair.getService().addMember(serviceMethod);
        Imports.ensureImported(pair.getService(), param.getReqDtoRespDtoInfo().getReqDtoQualifier());
        Imports.ensureImported(pair.getService(), param.getReqDtoRespDtoInfo().getRespDtoQualifier());
        log.info("Method [{}] append to Service [{}]", serviceMethod.getName(), pair.getService().getName());
        Saves.add(pair.getService().findCompilationUnit().orElseThrow(CuAbsentException::new));

        // 将方法以及Req、Resp的全名 均添加到 每个ServiceImpl
        for (ClassOrInterfaceDeclaration serviceImpl : pair.getServiceImpls()) {
            serviceImpl.addMember(serviceMethodImpl);
            Imports.ensureImported(serviceImpl, param.getReqDtoRespDtoInfo().getReqDtoQualifier());
            Imports.ensureImported(serviceImpl, param.getReqDtoRespDtoInfo().getRespDtoQualifier());
            log.info("Method [{}] append to Service Impl [{}]", serviceMethodImpl.getName(), serviceImpl.getName());
            Saves.add(serviceImpl.findCompilationUnit().orElseThrow(CuAbsentException::new));
        }

        // 将生成的方法所需的import 均添加到 Service 和 每个 ServiceImpl
        for (String appendImport : methodGeneration.getAppendImports()) {
            Imports.ensureImported(pair.getService(), appendImport);
            Imports.ensureImported(pair.getService(), param.getReqDtoRespDtoInfo().getReqDtoQualifier());
            pair.getServiceImpls().forEach(serviceImpl -> Imports.ensureImported(serviceImpl, appendImport));
        }

        ServiceGeneration result = new ServiceGeneration();
        result.setServiceVarName(MoreStringUtils.lowerFirstLetter(pair.getService().getNameAsString()));
        result.setService(pair.getService());
        result.setServiceQualifier(
                pair.getService().getFullyQualifiedName().orElseThrow(QualifierAbsentException::new));
        result.setMethodName(serviceMethod.getNameAsString());
        return result;
    }

    private ServicePairDto generateServicePair(GenerateServiceParam param, String serviceName,
            Map<String, ServicePairDto> name2Pair) {
        ServicePairDto pair;
        CompilationUnit serviceCu = new CompilationUnit();
        serviceCu.setPackageDeclaration(conf.getServicePackage());
        serviceCu.setImports(param.getCu().getImports());
        ClassOrInterfaceDeclaration service = new ClassOrInterfaceDeclaration();
        service.setJavadocComment(Javadocs.createJavadoc("", conf.getAuthor()));
        service.setPublic(true).setStatic(false).setInterface(true).setName(serviceName);
        serviceCu.setTypes(new NodeList<>(service));
        Path storage = Locations.getStorage(param.getCu()).getSourceRoot();
        storage = storage.resolve(CodeGenerationUtils.packageToPath(conf.getServicePackage()));
        storage = storage.resolve(service.getName() + ".java");
        serviceCu.setStorage(storage);
        Saves.add(serviceCu);
        log.info("generate Service [{}]", service.getName());

        CompilationUnit serviceImplCu = new CompilationUnit();
        serviceImplCu.setPackageDeclaration(conf.getServiceImplPackage());
        serviceImplCu.setImports(param.getCu().getImports());
        serviceImplCu.addImport(service.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new));
        serviceImplCu.addImport(AnnotationConstant.SLF4J_QUALIFIER);
        ClassOrInterfaceDeclaration serviceImpl = new ClassOrInterfaceDeclaration();
        serviceImpl.setJavadocComment(Javadocs.createJavadoc("", conf.getAuthor()));
        serviceImpl.addAnnotation(AnnotationConstant.SLF4J);
        serviceImpl.setPublic(true).setStatic(false).setInterface(false).setName(service.getName() + "Impl")
                .addImplementedType(service.getNameAsString());
        serviceImplCu.setTypes(new NodeList<>(serviceImpl));
        storage = Locations.getStorage(param.getCu()).getSourceRoot();
        storage = storage.resolve(CodeGenerationUtils.packageToPath(conf.getServiceImplPackage()));
        storage = storage.resolve(serviceImpl.getName() + ".java");
        serviceImplCu.setStorage(storage);
        Saves.add(serviceImplCu);
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

}