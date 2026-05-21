package com.spldeolin.allison1875.common.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseAnnotation;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;

import java.lang.annotation.Annotation;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.dto.GenerateMvcHandlerArgs;
import com.spldeolin.allison1875.common.dto.GenerateMvcHandlerRetval;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.service.MvcHandlerGeneratorService;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-02-17
 */
@Singleton
@Slf4j
public class MvcHandlerGeneratorServiceImpl implements MvcHandlerGeneratorService {

    @Inject
    private Config config;

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

        mvcHandler.addAnnotation(parseAnnotation(String.format(requestMapping, args.getMvcHandlerUrl())));

        mvcHandler.setPublic(true);

        String serviceResultType = args.getRespBodyDTOType();
        // 判断是否配置了统一返回类型模板
        if (StringUtils.isNotEmpty(config.getCodeSnippet().getRequestResultTypeDeclaration())) {
            // 全限定的统一返回类型
            String requestResultTypeDeclaration =
                    MoreStringUtils.splitAndRemoveLastPart(config.getCodeSnippet().getRequestResultQualifier(), ".")
                            + "." + config.getCodeSnippet().getRequestResultTypeDeclaration();
            // 配置了统一返回类型：无论是否有业务数据，返回类型均使用统一返回类型声明
            String returnTypeDecl;
            if (serviceResultType != null) {
                // 有业务数据类型，将 ${dataType} 替换为实际业务数据类型
                returnTypeDecl = requestResultTypeDeclaration.replace("${dataType}", serviceResultType);
            } else {
                // 无业务数据类型，将 ${dataType} 替换为Void
                returnTypeDecl = requestResultTypeDeclaration.replace("${dataType}", "Void");
            }
            mvcHandler.setType(returnTypeDecl);
        } else {
            // 未配置统一返回类型：沿用原有逻辑
            if (serviceResultType != null) {
                mvcHandler.setType(serviceResultType);
            } else {
                mvcHandler.setType(new VoidType());
            }
        }

        String methodName = MoreStringUtils.toLowerCamel(args.getMvcHandlerUrl());
        if (args.getMvcController() != null) {
            methodName = antiDuplicationService.getNewMethodNameIfExist(methodName, args.getMvcController());
        }
        mvcHandler.setName(methodName);

        String serviceParamType = args.getReqBodyDTOType();
        if (serviceParamType != null) {
            Parameter param = new Parameter();
            param.addAnnotation(annotationExprService.springRequestBody());
            param.addAnnotation(annotationExprService.javaxValid());
            param.setType(serviceParamType);
            param.setName("req");
            mvcHandler.addParameter(param);
        }
        if (!args.getReqParams().isEmpty()) {
            Javadoc javadoc = mvcHandler.getJavadoc().orElse(new JavadocComment().parse());
            for (VariableDeclarator vd : args.getReqParams()) {
                Optional<FieldDeclaration> fdOpt = vd.getParentNode().filter(p -> p instanceof FieldDeclaration)
                        .map(p -> (FieldDeclaration) p);
                Parameter param = new Parameter(vd.getType(), vd.getName());

                // @RequestParam
                NormalAnnotationExpr anno = annotationExprService.springRequestParamWithProperty();
                // @RequestParam.defaultValue
                vd.getInitializer().filter(Expression::isLiteralExpr).map(Expression::asLiteralExpr).ifPresent(init -> {
                    if (init.isLiteralStringValueExpr()) {
                        anno.addPair("defaultValue", new StringLiteralExpr(init.asLiteralStringValueExpr().getValue()));
                    }
                    if (init.isBooleanLiteralExpr()) {
                        anno.addPair("defaultValue",
                                new StringLiteralExpr(String.valueOf(init.asBooleanLiteralExpr().getValue())));
                    }
                });
                // @RequestParam.required
                fdOpt.ifPresent(fd -> {
                    if (isNoneAnnotated(fd, NotNull.class, NotEmpty.class,
                            org.hibernate.validator.constraints.NotEmpty.class, NotBlank.class,
                            org.hibernate.validator.constraints.NotBlank.class)) {
                        anno.addPair("required", new BooleanLiteralExpr(false));
                    }
                });
                // using @RequestParam instead of @RequestParam() if nessary
                param.addAnnotation(
                        anno.getPairs().isEmpty() ? annotationExprService.springRequestParamWithoutProperty() : anno);

                // @DateTimeFormat
                if (Lists.newArrayList("Date", "LocalDate", "LocalTime", "LocalDateTime")
                        .contains(vd.getTypeAsString())) {
                    param.addAnnotation(annotationExprService.springDateTimeFormat());
                }

                mvcHandler.addParameter(param);

                // Javadoc @param标签
                fdOpt.ifPresent(fd -> fd.getJavadoc().ifPresent(
                        jd -> javadoc.addBlockTag(Type.PARAM.name().toLowerCase(),
                                vd.getNameAsString() + " " + JavadocUtils.getDescription(fd))));
            }
            mvcHandler.setJavadocComment(javadoc);
        }

        BlockStmt body = new BlockStmt();
        String serviceVarName = args.getInjectedServiceVarName();
        String serviceMethodName = args.getServiceMethodName();
        StringBuilder argNames = new StringBuilder(serviceParamType != null ? "req" : "");
        for (VariableDeclarator requestParam : args.getReqParams()) {
            if (argNames.length() > 0) {
                argNames.append(",");
            }
            argNames.append(requestParam.getName());
        }

        if (StringUtils.isNotEmpty(config.getCodeSnippet().getRequestResultTypeDeclaration())) {
            // 使用统一返回类型包装
            String serviceCall = String.format("%s.%s(%s)", serviceVarName, serviceMethodName, argNames);
            if (serviceResultType != null) {
                // 有业务数据：先调用 service 获取数据，再用统一返回成功（有数据）模板包装
                // 例如：return RequestResult.success(serviceVar.methodName(args));
                String successWithData = config.getCodeSnippet().getRequestResultSuccessWithData()
                        .replace("${data}", serviceCall);
                body.addStatement(parseStatement("return %s;", successWithData));
            } else {
                // 无业务数据：先执行 service 调用，再 return 统一返回成功（无数据）模板
                // 例如：serviceVar.methodName(args); return RequestResult.success();
                body.addStatement(parseStatement("%s;", serviceCall));
                body.addStatement(
                        parseStatement("return %s;", config.getCodeSnippet().getRequestResultSuccessNoData()));
            }
        } else {
            // 未配置统一返回类型：沿用原有逻辑
            String returnOrNot = serviceResultType != null ? "return" : "";
            body.addStatement(
                    parseStatement("%s %s.%s(%s);", returnOrNot, serviceVarName, serviceMethodName, argNames));
        }

        mvcHandler.setBody(body);
        return new GenerateMvcHandlerRetval().setMvcHandler(mvcHandler);
    }

    private boolean isNoneAnnotated(NodeWithAnnotations<?> node, Class<? extends Annotation>... classes) {
        for (Class<? extends Annotation> clazz : classes) {
            if (node.getAnnotationByName(clazz.getSimpleName()).isPresent()) {
                return false;
            }
        }
        return true;
    }

}