package com.spldeolin.allison1875.handlertransformer.service.impl;

import java.util.List;
import java.util.stream.IntStream;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateMvcHandlerArgs;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateMvcHandlerRetval;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateServiceAndImplRetval;
import com.spldeolin.allison1875.handlertransformer.javabean.InitDecAnalysisDto;
import com.spldeolin.allison1875.handlertransformer.service.MvcControllerService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-01-12
 */
@Singleton
@Slf4j
public class MvcControllerServiceImpl implements MvcControllerService {

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private HandlerTransformerConfig config;

    @Override
    public List<ClassOrInterfaceDeclaration> detectMvcControllers(CompilationUnit cu) {
        return cu.findAll(ClassOrInterfaceDeclaration.class, this::isController);
    }

    private boolean isController(ClassOrInterfaceDeclaration coid) {
        for (AnnotationExpr annotation : coid.getAnnotations()) {
            try {
                ResolvedAnnotationDeclaration resolve = annotation.resolve();
                if (resolve.hasAnnotation(annotationExprService.springController().getNameAsString())
                        || annotationExprService.springController().getNameAsString()
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

    @Override
    public void replaceMvcHandlerToInitDec(InitDecAnalysisDto initDecAnalysisDto,
            GenerateServiceAndImplRetval generateServiceAndImplRetval,
            GenerateMvcHandlerRetval generateMvcHandlerRetval) {
        ClassOrInterfaceDeclaration mvcController = initDecAnalysisDto.getMvcController();

        NodeList<BodyDeclaration<?>> members = mvcController.getMembers();

        // 确保controller有autowired 新生成的service
        if (!mvcController.getFieldByName(generateServiceAndImplRetval.getServiceVarName()).isPresent()) {
            FieldDeclaration field = new FieldDeclaration();
            field.addAnnotation(annotationExprService.springAutowired());
            field.setPrivate(true).addVariable(new VariableDeclarator(
                    StaticJavaParser.parseType(generateServiceAndImplRetval.getServiceQualifier()),
                    generateServiceAndImplRetval.getServiceVarName()));

            int lastIndexOfFieldDeclaration = IntStream.range(0, members.size())
                    .filter(i -> members.get(i) instanceof FieldDeclaration).reduce((first, second) -> second)
                    .orElse(-1);
            members.add(lastIndexOfFieldDeclaration + 1, field);
        }
        log.info("append @Autowired Field [{}] into Controller [{}].", generateServiceAndImplRetval.getServiceVarName(),
                mvcController.getNameAsString());

        // 使用handle创建Handler方法，并追加到controller中
        members.replace(initDecAnalysisDto.getInitDec(), generateMvcHandlerRetval.getMvcHandler());

        for (AnnotationExpr annotationExpr : generateMvcHandlerRetval.getAppendAnnotations4Controller()) {
            if (!mvcController.getAnnotations().contains(annotationExpr)) {
                mvcController.addAnnotation(annotationExpr);
            }
        }
    }

    @Override
    public GenerateMvcHandlerRetval generateMvcHandler(GenerateMvcHandlerArgs args) {
        InitDecAnalysisDto initDecAnalysis = args.getInitDecAnalysis();
        String serviceParamType = args.getServiceParamType();
        String serviceResultType = args.getServiceResultType();

        MethodDeclaration handler = new MethodDeclaration();
        handler.setJavadocComment(concatHandlerDescription(initDecAnalysis));
        handler.addAnnotation(StaticJavaParser.parseAnnotation(
                String.format("@org.springframework.web.bind.annotation.PostMapping(\"%s\")",
                        initDecAnalysis.getMvcHandlerUrl())));
        handler.setPublic(true);
        if (serviceResultType != null) {
            handler.setType(serviceResultType);
        } else {
            handler.setType(new VoidType());
        }
        handler.setName(initDecAnalysis.getMvcHandlerMethodName());
        if (serviceParamType != null) {
            Parameter parameter = new Parameter();
            parameter.addAnnotation(annotationExprService.springRequestbody());
            parameter.addAnnotation(annotationExprService.javaxValid());
            parameter.setType(serviceParamType);
            parameter.setName("req");
            handler.addParameter(parameter);
        }

        BlockStmt body = new BlockStmt();
        if (serviceResultType != null) {
            if (serviceParamType != null) {
                body.addStatement(StaticJavaParser.parseStatement(
                        String.format("return %s.%s(req);", args.getServiceVarName(), args.getServiceMethodName())));
            } else {
                body.addStatement(StaticJavaParser.parseStatement(
                        String.format("return %s.%s();", args.getServiceVarName(), args.getServiceMethodName())));
            }
        } else {
            if (serviceParamType != null) {
                body.addStatement(StaticJavaParser.parseStatement(
                        String.format("%s.%s(req);", args.getServiceVarName(), args.getServiceMethodName())));
            } else {
                body.addStatement(StaticJavaParser.parseStatement(
                        String.format("%s.%s();", args.getServiceVarName(), args.getServiceMethodName())));
            }
        }
        handler.setBody(body);

        return new GenerateMvcHandlerRetval().setMvcHandler(handler);
    }

    private String concatHandlerDescription(InitDecAnalysisDto initDecAnalysis) {
        String result = initDecAnalysis.getMvcHandlerDescription();
        if (config.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION
                    + initDecAnalysis.getLotNo();
        }
        return result;
    }

}