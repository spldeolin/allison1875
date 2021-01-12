package com.spldeolin.allison1875.htex.processor;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.builder.FieldDeclarationBuilder;
import com.spldeolin.allison1875.base.builder.JavabeanCuBuilder;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.htex.HandlerTransformerConfig;
import com.spldeolin.allison1875.htex.javabean.FirstLineDto;
import com.spldeolin.allison1875.htex.javabean.ReqDtoRespDtoInfo;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-01-12
 */
@Singleton
@Log4j2
public class ReqRespProc {

    @Inject
    private HandlerTransformerConfig handlerTransformerConfig;

    public void checkInitBody(BlockStmt initBody, FirstLineDto firstLineDto) {
        if (initBody.findAll(LocalClassDeclarationStmt.class).size() > 2) {
            throw new IllegalArgumentException(
                    "构造代码块下最多只能有2个类声明，分别用于代表Req和Resp。[" + firstLineDto.getHandlerUrl() + "] 当前：" + initBody
                            .findAll(LocalClassDeclarationStmt.class).stream()
                            .map(one -> one.getClassDeclaration().getNameAsString()).collect(Collectors.joining("、")));
        }
        if (initBody.findAll(LocalClassDeclarationStmt.class).size() > 0) {
            for (LocalClassDeclarationStmt lcds : initBody.findAll(LocalClassDeclarationStmt.class)) {
                if (!StringUtils.equalsAnyIgnoreCase(lcds.getClassDeclaration().getNameAsString(), "Req", "Resp")) {
                    throw new IllegalArgumentException(
                            "构造代码块下类的命名只能是Req或者Resp。[" + firstLineDto.getHandlerUrl() + "] 当前：" + initBody
                                    .findAll(LocalClassDeclarationStmt.class).stream()
                                    .map(one -> one.getClassDeclaration().getNameAsString())
                                    .collect(Collectors.joining("、")));
                }
            }
        }
        if (initBody.findAll(ClassOrInterfaceDeclaration.class, coid -> coid.getNameAsString().equals("Req")).size()
                > 1) {
            throw new IllegalArgumentException("构造代码块下不能重复声明Req类。[" + firstLineDto.getHandlerUrl() + "]");
        }
        if (initBody.findAll(ClassOrInterfaceDeclaration.class, coid -> coid.getNameAsString().equals("Resp")).size()
                > 1) {
            throw new IllegalArgumentException("构造代码块下不能重复声明Resp类。[" + firstLineDto.getHandlerUrl() + "]");
        }
    }

    public ReqDtoRespDtoInfo generateDtos(Set<CompilationUnit> toCreate, CompilationUnit cu, FirstLineDto firstLineDto,
            List<ClassOrInterfaceDeclaration> dtos) {
        ReqDtoRespDtoInfo result = new ReqDtoRespDtoInfo();
        Collection<JavabeanCuBuilder> builders = Lists.newArrayList();
        Collection<String> dtoQualifiers = Lists.newArrayList();
        // 生成ReqDto、RespDto、NestDto
        for (ClassOrInterfaceDeclaration dto : dtos) {
            JavabeanCuBuilder builder = new JavabeanCuBuilder();
            builder.sourceRoot(Locations.getStorage(cu).getSourceRoot());
            boolean isReq = dto.getNameAsString().equals("Req");
            boolean isResp = dto.getNameAsString().equals("Resp");
            boolean isInReq = dto.findAncestor(ClassOrInterfaceDeclaration.class,
                    ancestor -> ancestor.getNameAsString().equals("Req")).isPresent();
            boolean isInResp = dto.findAncestor(ClassOrInterfaceDeclaration.class,
                    ancestor -> ancestor.getNameAsString().equals("Resp")).isPresent();

            if (isReq || isResp) {
                dto.setName(MoreStringUtils.upperFirstLetter(firstLineDto.getHandlerName()) + dto.getNameAsString());
            }

            // 计算每一个dto的package
            String pkg;
            if (isReq) {
                pkg = handlerTransformerConfig.getReqDtoPackage();
            } else if (isResp) {
                pkg = handlerTransformerConfig.getRespDtoPackage();
            } else if (isInReq) {
                pkg = handlerTransformerConfig.getReqDtoPackage() + ".dto";
            } else if (isInResp) {
                pkg = handlerTransformerConfig.getRespDtoPackage() + ".dto";
            } else {
                throw new RuntimeException("impossible unless bug.");
            }
            builder.packageDeclaration(pkg);
            builder.importDeclarations(cu.getImports());
            builder.importDeclarationsString(Lists.newArrayList("javax.validation.Valid", "java.util.Collection",
                    handlerTransformerConfig.getPageTypeQualifier()));
            ClassOrInterfaceDeclaration clone = dto.clone();
            clone.setPublic(true).getFields().forEach(field -> field.setPrivate(true));
            clone.getAnnotations().removeIf(
                    annotationExpr -> StringUtils.equalsAnyIgnoreCase(annotationExpr.getNameAsString(), "l", "p"));
            builder.coid(clone.setPublic(true));
            if (isReq) {
                result.setParamType(calcType(dto));
                result.setReqDtoQualifier(pkg + "." + clone.getNameAsString());
            }
            if (isResp) {
                result.setResultType(calcType(dto));
                result.setRespDtoQualifier(pkg + "." + clone.getNameAsString());
            }
            builders.add(builder);
            dtoQualifiers.add(pkg + "." + clone.getNameAsString());

            // 遍历到NestDto时，将父节点中的自身替换为Field
            if (dto.getParentNode().filter(parent -> parent instanceof ClassOrInterfaceDeclaration).isPresent()) {
                ClassOrInterfaceDeclaration parentCoid = (ClassOrInterfaceDeclaration) dto.getParentNode().get();
                FieldDeclarationBuilder fieldBuilder = new FieldDeclarationBuilder();
                dto.getJavadoc().ifPresent(fieldBuilder::javadoc);
                fieldBuilder.annotationExpr("@Valid");
                fieldBuilder.type(calcType(dto));
                fieldBuilder.fieldName(MoreStringUtils.upperCamelToLowerCamel(dto.getNameAsString()));
                parentCoid.replace(dto, fieldBuilder.build());
            }
        }
        for (JavabeanCuBuilder builder : builders) {
            builder.importDeclarationsString(dtoQualifiers);
            toCreate.add(builder.build());
        }
        return result;
    }


    private String calcType(ClassOrInterfaceDeclaration dto) {
        if (dto.getAnnotationByName("L").isPresent()) {
            return "Collection<" + dto.getNameAsString() + ">";
        }
        if (dto.getAnnotationByName("P").isPresent()) {
            String[] split = handlerTransformerConfig.getPageTypeQualifier().split("\\.");
            return split[split.length - 1] + "<" + dto.getNameAsString() + ">";
        }
        return dto.getNameAsString();
    }

}