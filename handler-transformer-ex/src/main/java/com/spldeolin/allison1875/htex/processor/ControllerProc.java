package com.spldeolin.allison1875.htex.processor;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.builder.FieldDeclarationBuilder;
import com.spldeolin.allison1875.base.builder.SingleMethodServiceCuBuilder;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.util.ast.Imports;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-01-12
 */
@Singleton
@Log4j2
public class ControllerProc {

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

    public void addHandlerToController(ClassOrInterfaceDeclaration controller,
            ClassOrInterfaceDeclaration controllerClone, String reqDtoQualifier, String respDtoQualifier,
            SingleMethodServiceCuBuilder serviceBuilder, MethodDeclaration handler) {
        if (!controller.getFieldByName(serviceBuilder.getServiceVarName()).isPresent()) {
            FieldDeclarationBuilder serviceField = new FieldDeclarationBuilder();
            serviceField.annotationExpr("@Autowired");
            serviceField.type(serviceBuilder.getService().getNameAsString());
            serviceField.fieldName(serviceBuilder.getServiceVarName());
            controllerClone.addMember(serviceField.build());
        }
        controllerClone.addMember(handler);
        if (reqDtoQualifier != null) {
            Imports.ensureImported(controller, reqDtoQualifier);
        }
        if (respDtoQualifier != null) {
            Imports.ensureImported(controller, respDtoQualifier);
        }
        Imports.ensureImported(controller, serviceBuilder.getJavabeanQualifier());
    }

}