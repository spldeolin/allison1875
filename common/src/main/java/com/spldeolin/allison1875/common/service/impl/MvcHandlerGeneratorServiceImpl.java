package com.spldeolin.allison1875.common.service.impl;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.VoidType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.javabean.GenerateMvcHandlerArgs;
import com.spldeolin.allison1875.common.javabean.GenerateMvcHandlerRetval;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.service.MvcHandlerGeneratorService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-02-17
 */
@Singleton
@Slf4j
public class MvcHandlerGeneratorServiceImpl implements MvcHandlerGeneratorService {

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public GenerateMvcHandlerRetval generateMvcHandler(GenerateMvcHandlerArgs args) {
        MethodDeclaration mvcHandler = new MethodDeclaration();

        if (StringUtils.isNotEmpty(args.getDescription())) {
            mvcHandler.setJavadocComment(args.getDescription());
        }

        String requestMapping;
        if (args.getIsHttpGet()) {
            requestMapping = "@org.springframework.web.bind.annotation.GetMapping(\"%s\")";
        } else {
            requestMapping = "@org.springframework.web.bind.annotation.PostMapping(\"%s\")";
        }

        mvcHandler.addAnnotation(
                StaticJavaParser.parseAnnotation(String.format(requestMapping, args.getMvcHandlerUrl())));

        mvcHandler.setPublic(true);

        String serviceResultType = args.getRespBodyDtoType();
        if (serviceResultType != null) {
            mvcHandler.setType(serviceResultType);
        } else {
            mvcHandler.setType(new VoidType());
        }

        String methodName = MoreStringUtils.toLowerCamel(args.getMvcHandlerUrl());
        if (args.getMvcController() != null) {
            methodName = antiDuplicationService.getNewMethodNameIfExist(methodName, args.getMvcController());
        }
        mvcHandler.setName(methodName);

        String serviceParamType = args.getReqBodyDtoType();
        if (serviceParamType != null) {
            Parameter param = new Parameter();
            param.addAnnotation(annotationExprService.springRequestBody());
            param.addAnnotation(annotationExprService.javaxValid());
            param.setType(serviceParamType);
            param.setName("req");
            mvcHandler.addParameter(param);
        }
        for (VariableDeclarator vd : args.getReqParams()) {
            Parameter param = new Parameter(vd.getType(), vd.getName());
            NormalAnnotationExpr anno = annotationExprService.springRequestParam();
            vd.getInitializer().filter(Expression::isLiteralExpr).map(Expression::asLiteralExpr).ifPresent(init -> {
                if (init.isLiteralStringValueExpr()) {
                    anno.addPair("defaultValue", new StringLiteralExpr(init.asLiteralStringValueExpr().getValue()));
                }
                if (init.isBooleanLiteralExpr()) {
                    anno.addPair("defaultValue",
                            new StringLiteralExpr(String.valueOf(init.asBooleanLiteralExpr().getValue())));
                }
            });
            param.addAnnotation(anno);
            mvcHandler.addParameter(param);
        }

        BlockStmt body = new BlockStmt();
        String returnOrNot = serviceResultType != null ? "return" : "";
        String serviceVarName = args.getInjectedServiceVarName();
        String serviceMethodName = args.getServiceMethodName();
        StringBuilder argNames = new StringBuilder(serviceParamType != null ? "req" : "");
        for (VariableDeclarator requestParam : args.getReqParams()) {
            if (argNames.length() > 0) {
                argNames.append(",");
            }
            argNames.append(requestParam.getName());
        }

        String statement = String.format("%s %s.%s(%s);", returnOrNot, serviceVarName, serviceMethodName, argNames);
        body.addStatement(StaticJavaParser.parseStatement(statement));

//        if (serviceResultType != null) {
//            if (serviceParamType != null) {
//                body.addStatement(StaticJavaParser.parseStatement(
//                        String.format("return %s.%s(req);", serviceVarName, serviceMethodName)));
//            } else {
//                body.addStatement(StaticJavaParser.parseStatement(
//                        String.format("return %s.%s();", serviceVarName, serviceMethodName)));
//            }
//        } else {
//            if (serviceParamType != null) {
//                body.addStatement(StaticJavaParser.parseStatement(
//                        String.format("%s.%s(req);", serviceVarName, serviceMethodName)));
//            } else {
//                body.addStatement(
//                        StaticJavaParser.parseStatement(String.format("%s.%s();", serviceVarName,
//                        serviceMethodName)));
//            }
//        }
        mvcHandler.setBody(body);

        return new GenerateMvcHandlerRetval().setMvcHandler(mvcHandler);
    }

}