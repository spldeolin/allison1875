package com.spldeolin.allison1875.handlertransformer.processor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.exception.HandlerNameConflictException;
import com.spldeolin.allison1875.handlertransformer.javabean.MetaInfo;

/**
 * @author Deolin 2020-08-28
 */
public class GenerateHandlerProc {

    private final HandlerTransformerConfig config;

    public GenerateHandlerProc(HandlerTransformerConfig config) {
        this.config = config;
    }

    public CompilationUnit process(MetaInfo metaInfo, String serviceQualifier) throws HandlerNameConflictException {
        ClassOrInterfaceDeclaration controller = metaInfo.getController();
        if (!metaInfo.isReqAbsent()) {
            Imports.ensureImported(controller, metaInfo.getReqBody().getTypeQualifier());
        }
        if (!metaInfo.isRespAbsent()) {
            Imports.ensureImported(controller, metaInfo.getRespBody().getTypeQualifier());
        }
        Imports.ensureImported(controller, serviceQualifier);
        for (String controllerImport : config.getControllerImports()) {
            Imports.ensureImported(controller, controllerImport);
        }
        if (controller.getMethodsByName(metaInfo.getHandlerName()).size() > 0) {
            throw new HandlerNameConflictException();
        }

        FieldDeclaration field = controller
                .addField(StringUtils.upperFirstLetter(metaInfo.getHandlerName()) + "Service",
                        metaInfo.getHandlerName() + "Service");
        field.addAnnotation(StaticJavaParser.parseAnnotation("@Autowired"));
        field.setPrivate(true);
        MethodDeclaration handler = new MethodDeclaration();
        Javadoc javadoc = new JavadocComment(metaInfo.getHandlerDescription()).parse();
        handler.setJavadocComment(javadoc);
        String handlerName = metaInfo.getHandlerName();
        handler.addAnnotation(StaticJavaParser.parseAnnotation("@PostMapping(\"/" + handlerName + "\")"));
        for (String handlerAnnotation : config.getHandlerAnnotations()) {
            handler.addAnnotation(StaticJavaParser.parseAnnotation(handlerAnnotation));
        }
        handler.setPublic(true);
        if (metaInfo.isRespAbsent()) {
            handler.setType(config.getResultVoid());
        } else {
            handler.setType(String.format(config.getResult(), metaInfo.getRespBody().getTypeName()));
        }
        handler.setName(handlerName);
        if (!metaInfo.isReqAbsent()) {
            Parameter requestBody = StaticJavaParser.parseParameter(metaInfo.getReqBody().getTypeName() + " req");
            requestBody.addAnnotation(StaticJavaParser.parseAnnotation("@RequestBody"));
            requestBody.addAnnotation(StaticJavaParser.parseAnnotation("@Valid"));
            handler.addParameter(requestBody);
        }

        String handlerPattern;
        if (metaInfo.isRespAbsent()) {
            handlerPattern = config.getHandlerBodyPatternInNoResponseBodySituation();
        } else {
            handlerPattern = config.getHandlerBodyPattern();
        }
        String serviceCallExpr = metaInfo.getHandlerName() + "Service." + metaInfo.getHandlerName();
        if (metaInfo.isReqAbsent()) {
            serviceCallExpr += "()";
        } else {
            serviceCallExpr += "(req)";
        }
        handler.setBody(StaticJavaParser.parseBlock("{" + String.format(handlerPattern, serviceCallExpr) + "}"));
        controller.addMember(handler);

        return controller.findCompilationUnit().orElseThrow(CuAbsentException::new);
    }

}