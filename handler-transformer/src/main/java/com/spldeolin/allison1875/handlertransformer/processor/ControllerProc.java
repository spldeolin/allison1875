package com.spldeolin.allison1875.handlertransformer.processor;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.builder.FieldDeclarationBuilder;
import com.spldeolin.allison1875.base.constant.AnnotationConstant;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.handlertransformer.handle.CreateHandlerHandle;
import com.spldeolin.allison1875.handlertransformer.handle.javabean.HandlerCreation;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.ReqDtoRespDtoInfo;
import com.spldeolin.allison1875.handlertransformer.javabean.ServiceGeneration;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-01-12
 */
@Singleton
@Log4j2
public class ControllerProc {

    @Inject
    private CreateHandlerHandle createHandlerHandle;

    public Collection<ClassOrInterfaceDeclaration> collect(CompilationUnit cu) {
        return cu.findAll(ClassOrInterfaceDeclaration.class, this::isController);
    }

    private boolean isController(ClassOrInterfaceDeclaration coid) {
        for (AnnotationExpr annotation : coid.getAnnotations()) {
            try {
                ResolvedAnnotationDeclaration resolve = annotation.resolve();
                if (resolve.hasAnnotation(AnnotationConstant.CONTROLLER_QUALIFIER)
                        || AnnotationConstant.CONTROLLER_QUALIFIER.equals(resolve.getQualifiedName())) {
                    return true;
                }
            } catch (Exception e) {
                log.error("annotation [{}] of class [{}] cannot resolve", annotation.getNameAsString(),
                        coid.getNameAsString(), e);
            }
        }
        return false;
    }

    public HandlerCreation createHandlerToController(FirstLineDto firstLineDto, ClassOrInterfaceDeclaration controller,
            ServiceGeneration serviceGeneration, ReqDtoRespDtoInfo reqDtoRespDtoInfo) {

        // 确保controller有autowired 新生成的service
        if (!controller.getFieldByName(serviceGeneration.getServiceVarName()).isPresent()) {
            FieldDeclarationBuilder serviceField = new FieldDeclarationBuilder();
            serviceField.annotationExpr("@Autowired");
            serviceField.type(serviceGeneration.getService().getNameAsString());
            serviceField.fieldName(serviceGeneration.getServiceVarName());
            controller.addMember(serviceField.build());
        }
        log.info("append @Autowired Field [{}] into Controller [{}].", serviceGeneration.getServiceVarName(),
                controller.getNameAsString());

        // 使用handle创建Handler方法，并追加到controller中
        HandlerCreation handlerCreation = createHandlerHandle
                .createHandler(firstLineDto, reqDtoRespDtoInfo.getParamType(), reqDtoRespDtoInfo.getResultType(),
                        serviceGeneration);
        controller.addMember(handlerCreation.getHandler());
        log.info("append Handler [{}] into Controller [{}].", handlerCreation.getHandler().getNameAsString(),
                controller.getNameAsString());

        for (String appendImport : handlerCreation.getAppendImports()) {
            Imports.ensureImported(controller, appendImport);
        }

        if (reqDtoRespDtoInfo.getReqDtoQualifier() != null) {
            Imports.ensureImported(controller, reqDtoRespDtoInfo.getReqDtoQualifier());
        }
        if (reqDtoRespDtoInfo.getRespDtoQualifier() != null) {
            Imports.ensureImported(controller, reqDtoRespDtoInfo.getRespDtoQualifier());
        }
        Imports.ensureImported(controller, serviceGeneration.getServiceQualifier());
        return handlerCreation;
    }

}