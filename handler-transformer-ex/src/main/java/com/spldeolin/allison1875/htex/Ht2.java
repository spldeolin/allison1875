package com.spldeolin.allison1875.htex;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node.TreeTraversal;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.builder.FieldDeclarationBuilder;
import com.spldeolin.allison1875.base.builder.JavabeanCuBuilder;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.util.CollectionUtils;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.htex.javabean.FirstLineDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-12-22
 */
@Log4j2
public class Ht2 {

    public static void main(String[] args) throws Exception {
        CompilationUnit cu = StaticJavaParser.parse(new File(
                "/Users/deolin/Documents/project-repo/allison1875/base/src/main/java/com/spldeolin/allison1875"
                        + "/handlertransformer2/Sample.java"));

        Collection<CompilationUnit> toCreate = Lists.newArrayList();
        for (ClassOrInterfaceDeclaration controller : cu
                .findAll(ClassOrInterfaceDeclaration.class/*, Ht2::isController*/)) {
            for (BodyDeclaration<?> member : controller.getMembers()) {
                if (member.isInitializerDeclaration()) {
                    InitializerDeclaration init = member.asInitializerDeclaration();
                    if (!isFirstLineCommentPresent(init)) {
                        continue;
                    }
                    String firstLine = tryGetFirstLine(init);
                    FirstLineDto firstLineDto = parseFirstLine(firstLine);
                    if (firstLineDto == null) {
                        continue;
                    }
                    log.info(firstLineDto);

                    // 广度优先遍历收集 + 反转
                    List<ClassOrInterfaceDeclaration> dtos = Lists.newArrayList();
                    init.walk(TreeTraversal.BREADTHFIRST, node -> {
                        if (node instanceof ClassOrInterfaceDeclaration) {
                            ClassOrInterfaceDeclaration coid = (ClassOrInterfaceDeclaration) node;
                            if (!coid.isInterface()) {
                                dtos.add(coid);
                            }
                        }
                    });
                    Collections.reverse(dtos);

                    String reqDtoQualifier = null;
                    String respDtoQualifier = null;
                    // 生成ReqDto、RespDto、NestDto
                    for (ClassOrInterfaceDeclaration dto : dtos) {
                        JavabeanCuBuilder builder = new JavabeanCuBuilder();
                        builder.sourceRoot(Locations.getStorage(cu).getSourceRoot());
                        builder.packageDeclaration("com.aaa.aa"); // TODO 使用Config
                        builder.importDeclarations(cu.getImports());
                        builder.importDeclaration("javax.validation.Valid");
                        ClassOrInterfaceDeclaration clone = dto.clone();
                        clone.setPublic(true).getFields().forEach(field -> field.setPrivate(true));
                        builder.coid(clone.setPublic(true));
                        toCreate.add(builder.build());

                        // 遍历到NestDto时，将父节点中的自身替换为Field
                        if (dto.getParentNode().filter(parent -> parent instanceof ClassOrInterfaceDeclaration)
                                .isPresent()) {
                            ClassOrInterfaceDeclaration parentCoid = (ClassOrInterfaceDeclaration) dto.getParentNode()
                                    .get();
                            FieldDeclarationBuilder fieldBuilder = new FieldDeclarationBuilder();
                            dto.getJavadoc().ifPresent(fieldBuilder::javadoc);
                            fieldBuilder.annotationExpr("@Valid");
                            fieldBuilder.type(dto.getNameAsString());
                            fieldBuilder.fieldName(MoreStringUtils.upperCamelToLowerCamel(dto.getNameAsString()));
                            parentCoid.replace(dto, fieldBuilder.build());
                        }

                        // 遍历到ReqDto或RespDto时，保存全限定名
                        if (dto.getNameAsString().equals("Req")) {
                            reqDtoQualifier = builder.getJavabeanQualifier();
                        }
                        if (dto.getNameAsString().equals("Resp")) {
                            respDtoQualifier = builder.getJavabeanQualifier();
                        }
                    }

                    // TODO 生成Service

                    // TODO Controller中的InitBlock转化为handler方法

                }
            }

        }

        toCreate.forEach(Saves::save);


        Optional<TypeDeclaration<?>> primaryType = cu.getPrimaryType();
        if (!primaryType.isPresent()) {
            // 没有处理的必要
            return;
        }

        TypeDeclaration<?> pt = primaryType.get();

    }


    private static boolean isFirstLineCommentPresent(InitializerDeclaration init) {
        NodeList<Statement> statements = init.getBody().getStatements();
        if (CollectionUtils.isEmpty(statements)) {
            return false;
        }
        return statements.get(0).getComment().filter(Comment::isLineComment).isPresent();
    }

    private static String tryGetFirstLine(InitializerDeclaration init) {
        NodeList<Statement> statements = init.getBody().getStatements();
        if (CollectionUtils.isEmpty(statements)) {
            return null;
        }
        Statement first = statements.get(0);
        if (!first.getComment().filter(Comment::isLineComment).isPresent()) {
            return null;
        }
        String firstLineContent = first.getComment().get().asLineComment().getContent();
        return firstLineContent;
    }

    private static FirstLineDto parseFirstLine(String firstLineContent) {
        if (StringUtils.isBlank(firstLineContent)) {
            return null;
        }
        firstLineContent = firstLineContent.trim();
        // extract to processor
        String[] parts = firstLineContent.split(" ");
        if (parts.length != 2) {
            return null;
        }

        FirstLineDto firstLineDto = new FirstLineDto();
        firstLineDto.setHandlerUrl(parts[0]);
        firstLineDto.setHandlerName(MoreStringUtils.slashToLowerCamel(parts[0]));
        firstLineDto.setHandlerDescription(parts[1]);
        firstLineDto.setMore(null); // privode handle
        return firstLineDto;
    }

    private static boolean isController(ClassOrInterfaceDeclaration coid) {
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

    private static boolean isClass(ClassOrInterfaceDeclaration coid) {
        return !coid.isInterface();
    }

}