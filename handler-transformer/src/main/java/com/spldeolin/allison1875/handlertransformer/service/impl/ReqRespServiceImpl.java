package com.spldeolin.allison1875.handlertransformer.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.constant.AnnotationConstant;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.exception.ParentAbsentException;
import com.spldeolin.allison1875.common.javabean.JavabeanArg;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.service.JavabeanGeneratorService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.enums.JavabeanTypeEnum;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.ReqDtoRespDtoInfo;
import com.spldeolin.allison1875.handlertransformer.service.FieldService;
import com.spldeolin.allison1875.handlertransformer.service.ReqRespService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-01-12
 */
@Singleton
@Log4j2
public class ReqRespServiceImpl implements ReqRespService {

    @Inject
    private HandlerTransformerConfig config;

    @Inject
    private FieldService fieldService;

    @Inject
    private JavabeanGeneratorService javabeanGeneratorService;

    @Override
    public void checkInitBody(BlockStmt initBody, FirstLineDto firstLineDto) {
        if (initBody.findAll(LocalClassDeclarationStmt.class).size() > 2) {
            throw new IllegalArgumentException(
                    "构造代码块下最多只能有2个类声明，分别用于代表ReqDto和RespDto。[" + firstLineDto + "] 当前："
                            + initBody.findAll(LocalClassDeclarationStmt.class).stream()
                            .map(one -> one.getClassDeclaration().getNameAsString()).collect(Collectors.joining("、")));
        }
        if (initBody.findAll(LocalClassDeclarationStmt.class).size() > 0) {
            for (LocalClassDeclarationStmt lcds : initBody.findAll(LocalClassDeclarationStmt.class)) {
                if (!StringUtils.equalsAnyIgnoreCase(lcds.getClassDeclaration().getNameAsString(), "Req", "Resp")) {
                    throw new IllegalArgumentException(
                            "构造代码块下类的命名只能是「Req」或者「Resp」。[" + firstLineDto + "] 当前：" + initBody.findAll(
                                            LocalClassDeclarationStmt.class).stream()
                                    .map(one -> one.getClassDeclaration().getNameAsString())
                                    .collect(Collectors.joining("、")));
                }
            }
        }
        if (initBody.findAll(ClassOrInterfaceDeclaration.class, coid -> coid.getNameAsString().equals("Req")).size()
                > 1) {
            throw new IllegalArgumentException("构造代码块下不能重复声明Req类。[" + firstLineDto + "]");
        }
        if (initBody.findAll(ClassOrInterfaceDeclaration.class, coid -> coid.getNameAsString().equals("Resp")).size()
                > 1) {
            throw new IllegalArgumentException("构造代码块下不能重复声明Resp类。[" + firstLineDto + "]");
        }
    }

    @Override
    public ReqDtoRespDtoInfo createJavabeans(AstForest astForest, FirstLineDto firstLineDto,
            List<ClassOrInterfaceDeclaration> dtos) {
        ReqDtoRespDtoInfo result = new ReqDtoRespDtoInfo();

        // 生成ReqDto、RespDto、NestDto
        for (ClassOrInterfaceDeclaration dto : dtos) {
            JavabeanTypeEnum javabeanType = estimateJavabeanType(dto);
            String packageName = estimatePackageName(javabeanType);
            String javabeanName = standardizeJavabeanName(firstLineDto, dto, javabeanType);

            JavabeanArg arg = new JavabeanArg();
            arg.setAstForest(astForest);
            arg.setPackageName(packageName);
            arg.setClassName(javabeanName);
            arg.setDescription(concatDtoDescription(firstLineDto));
            arg.setAuthorName(config.getAuthor());
            arg.setMore4Javabean((cu1, javabean) -> {
                for (FieldDeclaration field : dto.getFields()) {
                    for (ImportDeclaration anImport : fieldService.resolveLongType(field, javabeanType)) {
                        cu1.addImport(anImport);
                    }
                    for (ImportDeclaration anImport : fieldService.resolveTimeType(field, javabeanType)) {
                        cu1.addImport(anImport);
                    }
                }
                javabean.setMembers(dto.getMembers());
            });
            arg.setJavabeanExistenceResolution(FileExistenceResolutionEnum.RENAME);
            JavabeanGeneration javabeanGeneration = javabeanGeneratorService.generate(arg);
            result.getJavabeanCus().add(javabeanGeneration.getCu());

            dto.setName(javabeanGeneration.getJavabeanName());

            String javabeanQualifier = javabeanGeneration.getJavabeanQualifier();
            if (javabeanType == JavabeanTypeEnum.REQ_DTO) {
                result.setParamType(calcType(dto));
                result.setReqDtoQualifier(javabeanQualifier);
            }
            if (javabeanType == JavabeanTypeEnum.RESP_DTO) {
                result.setResultType(calcType(dto));
                result.setRespDtoQualifier(javabeanQualifier);
            }
            result.getJavabeanQualifiers().add(javabeanQualifier);

            // 遍历到NestDto时，将父节点中的自身替换为Field
            if (Lists.newArrayList(JavabeanTypeEnum.NEST_DTO_IN_REQ, JavabeanTypeEnum.NEST_DTO_IN_RESP)
                    .contains(javabeanType)) {
                ClassOrInterfaceDeclaration parentCoid = (ClassOrInterfaceDeclaration) dto.getParentNode()
                        .orElseThrow(ParentAbsentException::new);
                FieldDeclaration field = new FieldDeclaration();
                if (javabeanType == JavabeanTypeEnum.NEST_DTO_IN_REQ) {
                    field.addAnnotation(AnnotationConstant.VALID);
                }
                this.moveAnnotationsFromDtoToField(dto, field);
                field.addVariable(new VariableDeclarator(StaticJavaParser.parseType(calcType(dto)),
                        standardizeNestDtoFieldName(dto)));
                parentCoid.replace(dto, field);
            }
        }

        for (CompilationUnit javabeanCu : result.getJavabeanCus()) {
            for (String javabeanQualifier : result.getJavabeanQualifiers()) {
                javabeanCu.addImport(javabeanQualifier);
            }
            javabeanCu.addImport(config.getPageTypeQualifier());
        }

        return result;
    }

    private String estimatePackageName(JavabeanTypeEnum javabeanType) {
        String packageName;
        if (javabeanType == JavabeanTypeEnum.REQ_DTO) {
            packageName = config.getReqDtoPackage();
        } else if (javabeanType == JavabeanTypeEnum.RESP_DTO) {
            packageName = config.getRespDtoPackage();
        } else if (javabeanType == JavabeanTypeEnum.NEST_DTO_IN_REQ) {
            packageName = config.getReqNestDtoPackage();
            if (packageName == null) {
                packageName = config.getReqDtoPackage() + ".dto";
            }
        } else {
            packageName = config.getRespNestDtoPackage();
            if (packageName == null) {
                packageName = config.getRespDtoPackage() + ".dto";
            }
        }
        return packageName;
    }

    private String standardizeNestDtoFieldName(ClassOrInterfaceDeclaration dto) {
        boolean isCollectionOrPage =
                dto.getAnnotationByName("L").isPresent() || dto.getAnnotationByName("P").isPresent();
        String typeName = dto.getNameAsString();
        String fieldName = MoreStringUtils.lowerFirstLetter(typeName);
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
            throw new RuntimeException("impossible unless bug.");
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

    private String standardizeJavabeanName(FirstLineDto firstLineDto, ClassOrInterfaceDeclaration dto,
            JavabeanTypeEnum javabeanType) {
        String javaBeanName;
        if (javabeanType == JavabeanTypeEnum.REQ_DTO) {
            javaBeanName = MoreStringUtils.upperFirstLetter(firstLineDto.getHandlerName()) + "ReqDto";
        } else if (javabeanType == JavabeanTypeEnum.RESP_DTO) {
            javaBeanName = MoreStringUtils.upperFirstLetter(firstLineDto.getHandlerName()) + "RespDto";
        } else {
            String originName = dto.getNameAsString();
            if (!MoreStringUtils.endsWithIgnoreCase(originName, "dto")) {
                javaBeanName = MoreStringUtils.upperFirstLetter(originName) + "Dto";
            } else {
                javaBeanName = originName;
            }
        }
        return javaBeanName;
    }


    private String calcType(ClassOrInterfaceDeclaration dto) {
        if (dto.getAnnotationByName("L").isPresent()) {
            return "List<" + dto.getNameAsString() + ">";
        }
        if (dto.getAnnotationByName("P").isPresent()) {
            String[] split = config.getPageTypeQualifier().split("\\.");
            return split[split.length - 1] + "<" + dto.getNameAsString() + ">";
        }
        return dto.getNameAsString();
    }

    private String concatDtoDescription(FirstLineDto firstLine) {
        String result = "";
        if (config.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + firstLine.getLotNo();
        }
        return result;
    }

}