package com.spldeolin.allison1875.handlertransformer.processor;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.builder.FieldDeclarationBuilder;
import com.spldeolin.allison1875.base.builder.SingleMethodServiceCuBuilder;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.handlertransformer.handle.CreateHandlerHandle;
import com.spldeolin.allison1875.handlertransformer.handle.javabean.CreateHandlerHandleResult;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.ReqDtoRespDtoInfo;
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
                if (resolve.hasAnnotation(QualifierConstants.CONTROLLER) || QualifierConstants.CONTROLLER
                        .equals(resolve.getQualifiedName())) {
                    return true;
                }
            } catch (Exception e) {
                log.error("annotation [{}] of class [{}] cannot resolve", annotation.getNameAsString(),
                        coid.getNameAsString(), e);
            }
        }
        return false;
    }

    public void createHandlerToController(FirstLineDto firstLineDto, ClassOrInterfaceDeclaration controller,
            SingleMethodServiceCuBuilder serviceBuilder, ReqDtoRespDtoInfo reqDtoRespDtoInfo) {

        // 确保controller有autowired 新生成的service
        if (!controller.getFieldByName(serviceBuilder.getServiceVarName()).isPresent()) {
            FieldDeclarationBuilder serviceField = new FieldDeclarationBuilder();
            serviceField.annotationExpr("@Autowired");
            serviceField.type(serviceBuilder.getService().getNameAsString());
            serviceField.fieldName(serviceBuilder.getServiceVarName());
            controller.addMember(serviceField.build());
        }
        log.info("append @Autowired Field [{}] into Controller [{}]", serviceBuilder.getServiceVarName(),
                controller.getNameAsString());

        // 使用handle创建Handler方法，并追加到controller中
        CreateHandlerHandleResult handlerCreation = createHandlerHandle
                .createHandler(firstLineDto, reqDtoRespDtoInfo.getParamType(), reqDtoRespDtoInfo.getResultType(),
                        serviceBuilder);
        controller.addMember(handlerCreation.getHandler());
        log.info("append Handler [{}] into Controller [{}]", handlerCreation.getHandler().getNameAsString(),
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
        Imports.ensureImported(controller, serviceBuilder.getJavabeanQualifier());
    }

}