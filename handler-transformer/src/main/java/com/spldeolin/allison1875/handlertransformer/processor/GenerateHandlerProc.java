package com.spldeolin.allison1875.handlertransformer.processor;

import org.apache.commons.collections4.CollectionUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.javadoc.Javadoc;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.exception.HandlerNameConflictException;
import com.spldeolin.allison1875.handlertransformer.javabean.MetaInfo;
import lombok.Getter;

/**
 * @author Deolin 2020-08-28
 */
class GenerateHandlerProc {

    private final MetaInfo metaInfo;

    private final String serviceQualifier;

    @Getter
    private CompilationUnit controllerCu;

    GenerateHandlerProc(MetaInfo metaInfo, String serviceQualifier) {
        this.metaInfo = metaInfo;
        this.serviceQualifier = serviceQualifier;
    }

    GenerateHandlerProc process() throws HandlerNameConflictException {
        ClassOrInterfaceDeclaration controller = metaInfo.getController();
        Imports.ensureImported(controller, metaInfo.getReqBody().getTypeQualifier());
        Imports.ensureImported(controller, metaInfo.getRespBody().getTypeQualifier());
        Imports.ensureImported(controller, serviceQualifier);
        for (String controllerImport : HandlerTransformerConfig.getInstance().getControllerImports()) {
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
        for (String handlerAnnotation : HandlerTransformerConfig.getInstance().getHandlerAnnotations()) {
            handler.addAnnotation(StaticJavaParser.parseAnnotation(handlerAnnotation));
        }
        handler.setPublic(true);
        if (CollectionUtils.isEmpty(metaInfo.getRespBody().getVariableDeclarators())) {
            handler.setType(HandlerTransformerConfig.getInstance().getResultVoid());
        } else {
            handler.setType(String.format(HandlerTransformerConfig.getInstance().getResult(),
                    metaInfo.getRespBody().getTypeName()));
        }
        handler.setName(handlerName);
        if (CollectionUtils.isNotEmpty(metaInfo.getReqBody().getVariableDeclarators())) {
            Parameter requestBody = StaticJavaParser.parseParameter(metaInfo.getReqBody().getTypeName() + " req");
            requestBody.addAnnotation(StaticJavaParser.parseAnnotation("@RequestBody"));
            requestBody.addAnnotation(StaticJavaParser.parseAnnotation("@Valid"));
            handler.addParameter(requestBody);
        }
        BlockStmt body = new BlockStmt();
        String serviceCallExpr = metaInfo.getHandlerName() + "Service." + metaInfo.getHandlerName() + "(req)";
        String handlerPattern;
        if (CollectionUtils.isEmpty(metaInfo.getRespBody().getVariableDeclarators())) {
            handlerPattern = HandlerTransformerConfig.getInstance().getHandlerBodyPatternInNoResponseBodySituation();
        } else {
            handlerPattern = HandlerTransformerConfig.getInstance().getHandlerBodyPattern();
        }
        body.addStatement(StaticJavaParser.parseStatement(String.format(handlerPattern, serviceCallExpr)));
        handler.setBody(body);
        controller.addMember(handler);

        controllerCu = controller.findCompilationUnit().orElseThrow(CuAbsentException::new);

        return this;
    }

}