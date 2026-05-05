package com.spldeolin.allison1875.handlertransformer.service.impl;

import java.nio.file.Path;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.config.DomainContext;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.handlertransformer.dto.AddMethodToServiceArgs;
import com.spldeolin.allison1875.handlertransformer.dto.BuildServiceImplMethodBodyRetval;
import com.spldeolin.allison1875.handlertransformer.dto.GenerateServiceAndImplArgs;
import com.spldeolin.allison1875.handlertransformer.dto.GenerateServiceAndImplRetval;
import com.spldeolin.allison1875.handlertransformer.dto.GenerateServiceMethodRetval;
import com.spldeolin.allison1875.handlertransformer.dto.InitDecAnalysisDTO;
import com.spldeolin.allison1875.handlertransformer.service.ServiceLayerExpansionService;
import com.spldeolin.allison1875.handlertransformer.service.ServiceLayerService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-01-10
 */
@Singleton
@Slf4j
public class ServiceLayerServiceImpl implements ServiceLayerService {

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private Config config;

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Inject
    private ImportExprService importExprService;

    @Inject
    private ServiceLayerExpansionService serviceLayerExpansionService;

    @Override
    public GenerateServiceMethodRetval generateServiceMethod(InitDecAnalysisDTO initDecAnalysisDTO,
            String reqBodyDTOType, List<VariableDeclarator> reqParams, String respBodyDTOType) {
        MethodDeclaration method = new MethodDeclaration();
        serviceLayerExpansionService.buildAnnotationsFormServiceImplMethod(initDecAnalysisDTO)
                .forEach(method::addAnnotation);
        method.addAnnotation(annotationExprService.javaOverride());
        method.setPublic(true);
        if (respBodyDTOType != null) {
            method.setType(respBodyDTOType);
        } else {
            method.setType(new VoidType());
        }
        method.setName(initDecAnalysisDTO.getMvcHandlerMethodName());
        if (reqBodyDTOType != null) {
            method.addParameter(reqBodyDTOType, "req");
        }
        for (VariableDeclarator vd : reqParams) {
            method.addParameter(new Parameter(vd.getType(), vd.getName()));
        }

        BuildServiceImplMethodBodyRetval buildServiceImplMethodBodyRetval =
                serviceLayerExpansionService.buildServiceImplMethodBody(
                initDecAnalysisDTO, reqBodyDTOType, reqParams, respBodyDTOType);
        method.setBody(buildServiceImplMethodBodyRetval.getBody());

        return new GenerateServiceMethodRetval().setMethod(method)
                .setNeededImportsInImpl(buildServiceImplMethodBodyRetval.getNeededImports());
    }

    @Override
    public String addMethodToService(AddMethodToServiceArgs args) {
        GenerateServiceAndImplRetval serviceRetval = args.getGenerateServiceAndImplRetval();

        // 方法名去重
        String serviceMethodName = antiDuplicationService.getNewMethodNameIfExist(
                args.getServiceMethod().getNameAsString(), serviceRetval.getService());
        args.getServiceMethod().setName(serviceMethodName);

        // 将方法 添加到 Service
        MethodDeclaration serviceMethodImpl = args.getServiceMethod();
        MethodDeclaration serviceMethod = new MethodDeclaration().setType(serviceMethodImpl.getType())
                .setName(serviceMethodImpl.getName()).setParameters(serviceMethodImpl.getParameters());
        serviceMethod.setBody(null);
        ClassOrInterfaceDeclaration service = serviceRetval.getService();
        service.addMember(serviceMethod);
        log.info("Method [{}] append to Service [{}]", serviceMethod.getName(), service.getName());

        // 将方法 添加到 每个ServiceImpl
        ClassOrInterfaceDeclaration serviceImpl = serviceRetval.getServiceImpl();
        serviceImpl.addMember(serviceMethodImpl.clone());
        log.info("Method [{}] append to Service Impl [{}]", serviceMethodImpl.getName(), serviceImpl.getName());

        importExprService.extractQualifiedTypeToImport(serviceRetval.getServiceCu());
        importExprService.extractQualifiedTypeToImport(serviceRetval.getServiceImplCu());

        CompilationUnitUtils.writeJava(serviceRetval.getServiceCu());
        CompilationUnitUtils.writeJava(serviceRetval.getServiceImplCu());

        return serviceMethod.getNameAsString();
    }

    @Override
    public GenerateServiceAndImplRetval generateServiceAndImpl(GenerateServiceAndImplArgs args) {
        String serviceName;
        if (config.getEnableOneService()) {
            serviceName = args.getControllerCu().getPrimaryTypeName()
                    .orElseThrow(() -> new Allison1875Exception("cannot find primary type name"))
                    .replace("Controller", "Service");
        } else {
            serviceName =
                    MoreStringUtils.toUpperCamel(args.getInitDecAnalysisDTO().getMvcHandlerMethodName()) + "Service";
        }
        Path serviceSourceRoot = DomainContext.get().getServiceSourceRoot();
        Path absolutePath = CodeGenerationUtils.fileInPackageAbsolutePath(serviceSourceRoot,
                DomainContext.get().getServicePackage(),
                serviceName + ".java");

        // 查找或生成service和cu
        CompilationUnit serviceCu;
        ClassOrInterfaceDeclaration service;
        if (absolutePath.toFile().exists()) {
            // 文件存在
            if (config.getEnableOneService()) {
                // 单service模式下直接查找
                serviceCu = CompilationUnitUtils.tryFindCu(serviceSourceRoot,
                                DomainContext.get().getServicePackage() + "." + serviceName)
                        .orElseThrow(() -> new Allison1875Exception("fail to parse cu"));
                service = serviceCu.getPrimaryType().filter(TypeDeclaration::isClassOrInterfaceDeclaration)
                        .orElseThrow(() -> new Allison1875Exception("")).asClassOrInterfaceDeclaration();
            } else {
                // 非单service模式下直接生成empty service
                CuAndCoid result = this.generateEmptyService(args, absolutePath);
                serviceCu = result.cu;
                service = result.coid;
            }
        } else {
            // 文件不存在，是否单service模式都需要直接生成empty service
            CuAndCoid temp = this.generateEmptyService(args, absolutePath);
            serviceCu = temp.cu;
            service = temp.coid;
        }

        String serviceImplName = serviceName + "Impl";
        Path serviceImplSourceRoot = DomainContext.get().getServiceImplSourceRoot();
        absolutePath = CodeGenerationUtils.fileInPackageAbsolutePath(serviceImplSourceRoot,
                DomainContext.get().getServiceImplPackage(), serviceImplName + ".java");

        // 查找或生成serviceImpl和cu
        CompilationUnit serviceImplCu;
        ClassOrInterfaceDeclaration serviceImpl;
        if (absolutePath.toFile().exists()) {
            // 文件存在
            if (config.getEnableOneService()) {
                // 单service模式下直接查找
                serviceImplCu = CompilationUnitUtils.tryFindCu(serviceImplSourceRoot,
                                DomainContext.get().getServiceImplPackage() + "." + serviceImplName)
                        .orElseThrow(() -> new Allison1875Exception("fail to parse cu"));
                serviceImpl = serviceImplCu.getPrimaryType().filter(TypeDeclaration::isClassOrInterfaceDeclaration)
                        .orElseThrow(() -> new Allison1875Exception("")).asClassOrInterfaceDeclaration();
            } else {
                // 非单service模式下直接生成empty serviceImpl
                CuAndCoid result = generateEmptyServiceImpl(args, absolutePath, service);
                serviceImplCu = result.cu;
                serviceImpl = result.coid;
            }
        } else {
            // 文件不存在，是否单service模式都需要直接生成empty service
            CuAndCoid temp = generateEmptyServiceImpl(args, absolutePath, service);
            serviceImplCu = temp.cu;
            serviceImpl = temp.coid;
        }

        // 为serviceImpl构建Field
        serviceLayerExpansionService.buildFieldsForServiceImpl(serviceImpl, args.getInitDecAnalysisDTO())
                .forEach(serviceImpl::addMember);

        GenerateServiceAndImplRetval retval = new GenerateServiceAndImplRetval();
        retval.setService(service);
        retval.setServiceCu(serviceCu);
        retval.setServiceImpl(serviceImpl);
        retval.setServiceImplCu(serviceImplCu);
        retval.setServiceVarName(MoreStringUtils.toLowerCamel(service.getNameAsString()));
        retval.setServiceQualifier(DomainContext.get().getServicePackage() + "." + serviceName);
        return retval;
    }

    private CuAndCoid generateEmptyServiceImpl(GenerateServiceAndImplArgs args, Path absolutePath,
            ClassOrInterfaceDeclaration service) {
        // anti-duplication
        absolutePath = antiDuplicationService.getNewPathIfExist(absolutePath);
        String serviceImplName = FilenameUtils.getBaseName(absolutePath.toString());

        CompilationUnit serviceImplCu = new CompilationUnit();
        serviceImplCu.setPackageDeclaration(DomainContext.get().getServiceImplPackage());
        importExprService.copyImports(args.getControllerCu(), serviceImplCu);
        ClassOrInterfaceDeclaration serviceImpl = new ClassOrInterfaceDeclaration();
        JavadocUtils.setJavadoc(serviceImpl, concatServiceDescription(args.getInitDecAnalysisDTO()),
                config.getAuthor());
        serviceImpl.addAnnotation(annotationExprService.lombokSlf4J());
        serviceImpl.addAnnotation(annotationExprService.springService());
        serviceImpl.setPublic(true).setStatic(false).setInterface(false).setName(serviceImplName).addImplementedType(
                service.getFullyQualifiedName().orElseThrow(
                        () -> new Allison1875Exception("Node '" + service.getName() + "' has no Qualifier")));
        serviceImplCu.setTypes(new NodeList<>(serviceImpl));
        serviceImplCu.setStorage(absolutePath);
        log.info("generate empty ServiceImpl [{}]", serviceImpl.getName());
        return new CuAndCoid(serviceImplCu, serviceImpl);
    }

    private CuAndCoid generateEmptyService(GenerateServiceAndImplArgs args, Path absolutePath) {
        // anti-duplication
        absolutePath = antiDuplicationService.getNewPathIfExist(absolutePath);
        String serviceName = FilenameUtils.getBaseName(absolutePath.toString());

        CompilationUnit serviceCu = new CompilationUnit();
        serviceCu.setPackageDeclaration(DomainContext.get().getServicePackage());
        importExprService.copyImports(args.getControllerCu(), serviceCu);
        ClassOrInterfaceDeclaration service = new ClassOrInterfaceDeclaration();
        String comment = concatServiceDescription(args.getInitDecAnalysisDTO());
        JavadocUtils.setJavadoc(service, comment, config.getAuthor());
        service.setPublic(true).setStatic(false).setInterface(true).setName(serviceName);
        serviceCu.setTypes(new NodeList<>(service));
        serviceCu.setStorage(absolutePath);
        log.info("generate empty Service [{}]", service.getName());
        return new CuAndCoid(serviceCu, service);
    }

    private static class CuAndCoid {

        private final CompilationUnit cu;

        private final ClassOrInterfaceDeclaration coid;

        private CuAndCoid(CompilationUnit serviceCu, ClassOrInterfaceDeclaration service) {
            this.cu = serviceCu;
            this.coid = service;
        }

    }

    private String concatServiceDescription(InitDecAnalysisDTO initDecAnalysis) {
        return "";
    }

}