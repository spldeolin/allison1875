package com.spldeolin.allison1875.handlertransformer.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.exception.ParentAbsentException;
import com.spldeolin.allison1875.common.javabean.JavabeanArg;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.service.JavabeanGeneratorService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.enums.JavabeanTypeEnum;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateDtoJavabeansRetval;
import com.spldeolin.allison1875.handlertransformer.javabean.InitDecAnalysisDto;
import com.spldeolin.allison1875.handlertransformer.service.FieldService;
import com.spldeolin.allison1875.handlertransformer.service.ReqRespService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-01-12
 */
@Singleton
@Slf4j
public class ReqRespServiceImpl implements ReqRespService {

    @Inject
    private HandlerTransformerConfig config;

    @Inject
    private FieldService fieldService;

    @Inject
    private JavabeanGeneratorService javabeanGeneratorService;

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private ImportExprService importExprService;

    @Override
    public void validInitBody(BlockStmt initBody, InitDecAnalysisDto initDecAnalysis) {
        if (initBody.findAll(LocalClassDeclarationStmt.class).size() > 2) {
            throw new IllegalArgumentException(
                    "构造代码块下最多只能有2个类声明，分别用于代表ReqDto和RespDto。[" + initDecAnalysis + "] 当前："
                            + initBody.findAll(LocalClassDeclarationStmt.class).stream()
                            .map(one -> one.getClassDeclaration().getNameAsString()).collect(Collectors.joining("、")));
        }
        if (CollectionUtils.isNotEmpty(initBody.findAll(LocalClassDeclarationStmt.class))) {
            for (LocalClassDeclarationStmt lcds : initBody.findAll(LocalClassDeclarationStmt.class)) {
                if (!StringUtils.equalsAnyIgnoreCase(lcds.getClassDeclaration().getNameAsString(), "Req", "Resp")) {
                    throw new IllegalArgumentException(
                            "构造代码块下类的命名只能是「Req」或者「Resp」。[" + initDecAnalysis + "] 当前："
                                    + initBody.findAll(
                                            LocalClassDeclarationStmt.class).stream()
                                    .map(one -> one.getClassDeclaration().getNameAsString())
                                    .collect(Collectors.joining("、")));
                }
            }
        }
        if (initBody.findAll(ClassOrInterfaceDeclaration.class, coid -> coid.getNameAsString().equals("Req")).size()
                > 1) {
            throw new IllegalArgumentException("构造代码块下不能重复声明Req类。[" + initDecAnalysis + "]");
        }
        if (initBody.findAll(ClassOrInterfaceDeclaration.class, coid -> coid.getNameAsString().equals("Resp")).size()
                > 1) {
            throw new IllegalArgumentException("构造代码块下不能重复声明Resp类。[" + initDecAnalysis + "]");
        }
    }

    @Override
    public GenerateDtoJavabeansRetval generateDtoJavabeans(AstForest astForest, InitDecAnalysisDto initDecAnalysis,
            List<ClassOrInterfaceDeclaration> dtos) {
        GenerateDtoJavabeansRetval result = new GenerateDtoJavabeansRetval();

        // 生成ReqDto、RespDto、NestDto
        for (ClassOrInterfaceDeclaration dto : dtos) {
            JavabeanTypeEnum javabeanType = estimateJavabeanType(dto);
            String packageName = estimatePackageName(javabeanType);
            String javabeanName = standardizeJavabeanName(initDecAnalysis, dto, javabeanType);

            JavabeanArg arg = new JavabeanArg();
            arg.setAstForest(astForest);
            arg.setPackageName(packageName);
            arg.setClassName(javabeanName);
            arg.setDescription(concatDtoDescription(initDecAnalysis));
            arg.setAuthorName(config.getCommonConfig().getAuthor());
            arg.setMore4Javabean((tempCu, javabean) -> {
                for (FieldDeclaration field : dto.getFields()) {
                    fieldService.more4SpecialTypeField(field, javabeanType);
                }
                importExprService.copyImports(initDecAnalysis.getMvcControllerCu(), tempCu);
                javabean.setMembers(dto.getMembers());
            });
            arg.setJavabeanExistenceResolution(FileExistenceResolutionEnum.RENAME);
            JavabeanGeneration javabeanGeneration = javabeanGeneratorService.generate(arg);
            result.getFlushes().add(FileFlush.build(javabeanGeneration.getCu()));

            dto.setName(javabeanGeneration.getJavabeanName());

            String javabeanQualifier = javabeanGeneration.getJavabeanQualifier();
            if (javabeanType == JavabeanTypeEnum.REQ_DTO) {
                result.setParamType(calcType(dto, javabeanQualifier));
            }
            if (javabeanType == JavabeanTypeEnum.RESP_DTO) {
                result.setResultType(calcType(dto, javabeanQualifier));
            }

            // 遍历到NestDto时，将父节点中的自身替换为Field
            if (Lists.newArrayList(JavabeanTypeEnum.NEST_DTO_IN_REQ, JavabeanTypeEnum.NEST_DTO_IN_RESP)
                    .contains(javabeanType)) {
                ClassOrInterfaceDeclaration parentCoid = (ClassOrInterfaceDeclaration) dto.getParentNode()
                        .orElseThrow(() -> new ParentAbsentException(dto));
                FieldDeclaration field = new FieldDeclaration();
                if (javabeanType == JavabeanTypeEnum.NEST_DTO_IN_REQ) {
                    field.addAnnotation(annotationExprService.javaxValid());
                }
                this.moveAnnotationsFromDtoToField(dto, field);
                field.addVariable(new VariableDeclarator(StaticJavaParser.parseType(calcType(dto, javabeanQualifier)),
                        standardizeNestDtoFieldName(dto)));
                parentCoid.replace(dto, field);
            }
        }

        return result;
    }

    private String estimatePackageName(JavabeanTypeEnum javabeanType) {
        String packageName;
        if (javabeanType == JavabeanTypeEnum.REQ_DTO) {
            packageName = config.getCommonConfig().getReqDtoPackage();
        } else if (javabeanType == JavabeanTypeEnum.RESP_DTO) {
            packageName = config.getCommonConfig().getRespDtoPackage();
        } else if (javabeanType == JavabeanTypeEnum.NEST_DTO_IN_REQ) {
            packageName = config.getCommonConfig().getReqDtoPackage();
        } else {
            packageName = config.getCommonConfig().getRespDtoPackage();
        }
        return packageName;
    }

    private String standardizeNestDtoFieldName(ClassOrInterfaceDeclaration dto) {
        boolean isCollectionOrPage =
                dto.getAnnotationByName("L").isPresent() || dto.getAnnotationByName("P").isPresent();
        String typeName = dto.getNameAsString();
        String fieldName = MoreStringUtils.toLowerCamel(typeName);
        fieldName = StringUtils.removeEndIgnoreCase(fieldName, "dto");
        if (isCollectionOrPage) {
            fieldName = English.plural(fieldName);
        }
        return fieldName;
    }

    private JavabeanTypeEnum estimateJavabeanType(ClassOrInterfaceDeclaration dto) {
        JavabeanTypeEnum javabeanType;
        if (dto.getNameAsString().equalsIgnoreCase("Req")) {
            javabeanType = JavabeanTypeEnum.REQ_DTO;
        } else if (dto.getNameAsString().equalsIgnoreCase("Resp")) {
            javabeanType = JavabeanTypeEnum.RESP_DTO;
        } else if (dto.findAncestor(ClassOrInterfaceDeclaration.class,
                ancestor -> ancestor.getNameAsString().equalsIgnoreCase("Req")).isPresent()) {
            javabeanType = JavabeanTypeEnum.NEST_DTO_IN_REQ;
        } else if (dto.findAncestor(ClassOrInterfaceDeclaration.class,
                ancestor -> ancestor.getNameAsString().equalsIgnoreCase("Resp")).isPresent()) {
            javabeanType = JavabeanTypeEnum.NEST_DTO_IN_RESP;
        } else {
            throw new Allison1875Exception("unknown Name [" + dto.getNameAsString() + "]");
        }
        return javabeanType;
    }

    private void moveAnnotationsFromDtoToField(ClassOrInterfaceDeclaration dto, FieldDeclaration field) {
        for (AnnotationExpr annotation : dto.getAnnotations()) {
            if (!StringUtils.equalsAny(annotation.getNameAsString(), "L", "P")) {
                field.addAnnotation(annotation);
            }
        }
    }

    private String standardizeJavabeanName(InitDecAnalysisDto initDecAnalysis, ClassOrInterfaceDeclaration dto,
            JavabeanTypeEnum javabeanType) {
        String javaBeanName;
        if (javabeanType == JavabeanTypeEnum.REQ_DTO) {
            javaBeanName = MoreStringUtils.toUpperCamel(initDecAnalysis.getMvcHandlerMethodName()) + "ReqDto";
        } else if (javabeanType == JavabeanTypeEnum.RESP_DTO) {
            javaBeanName = MoreStringUtils.toUpperCamel(initDecAnalysis.getMvcHandlerMethodName()) + "RespDto";
        } else {
            String originName = dto.getNameAsString();
            if (!StringUtils.endsWithIgnoreCase(originName, "dto")) {
                javaBeanName = MoreStringUtils.toUpperCamel(originName) + "Dto";
            } else {
                javaBeanName = originName;
            }
        }
        return javaBeanName;
    }


    private String calcType(ClassOrInterfaceDeclaration dto, String javabeanQualifier) {
        if (dto.getAnnotationByName("L").isPresent()) {
            return "java.util.List<" + javabeanQualifier + ">";
        }
        if (dto.getAnnotationByName("P").isPresent()) {
            return String.format("%s<%s>", MoreStringUtils.splitAndGetLastPart(config.getPageTypeQualifier(), "."),
                    javabeanQualifier);
        }
        return javabeanQualifier;
    }

    private String concatDtoDescription(InitDecAnalysisDto initDecAnalysis) {
        String result = "";
        if (config.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION
                    + initDecAnalysis.getLotNo();
        }
        return result;
    }

}