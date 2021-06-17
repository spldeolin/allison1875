package com.spldeolin.allison1875.base.factory;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeSet;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.spldeolin.allison1875.base.constant.AnnotationConstant;
import com.spldeolin.allison1875.base.factory.javabean.FieldArg;
import com.spldeolin.allison1875.base.factory.javabean.JavabeanArg;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-05-26
 */
@Log4j2
public class JavabeanFactory {

    public static CompilationUnit buildCu(JavabeanArg javabeanArg) {
        CompilationUnit cu = new CompilationUnit();
        Path absulutePath = CodeGenerationUtils
                .fileInPackageAbsolutePath(javabeanArg.getAstForest().getPrimaryJavaRoot(),
                        javabeanArg.getPackageName(), javabeanArg.getClassName() + ".java");

        // 收集既存Javabean中的成员变量
        TreeSet<String> originalProperties = Sets.newTreeSet();
        // 收集既存javabean的作者信息
        List<JavadocBlockTag> authorTags = Lists.newArrayList();
        if (absulutePath.toFile().exists()) {
            try {
                CompilationUnit existCu = StaticJavaParser.parse(absulutePath);
                for (FieldDeclaration field : existCu.findAll(FieldDeclaration.class)) {
                    for (VariableDeclarator variable : field.getVariables()) {
                        originalProperties.add(variable.getNameAsString());
                    }
                }
                collect(authorTags, existCu);
            } catch (Exception e) {
                log.warn("StaticJavaParser.parse failed absulutePath={}", absulutePath, e);
            }
            log.info("Javabean file is exist, override it. [{}]", absulutePath);
        } else {
            authorTags.add(new JavadocBlockTag(Type.AUTHOR, javabeanArg.getAuthorName() + " " + LocalDate.now()));
        }

        // 报告成员变量的变动点
        reportDiff(originalProperties, javabeanArg);

        cu.setStorage(absulutePath);
        cu.setPackageDeclaration(javabeanArg.getPackageName());
        cu.addImport(AnnotationConstant.DATA_QUALIFIER);
        cu.addImport(AnnotationConstant.ACCESSORS_QUALIFIER);

        ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
        coid.addAnnotation(AnnotationConstant.DATA);
        coid.addAnnotation(AnnotationConstant.ACCESSORS);
        coid.setPublic(true).setInterface(false).setName(javabeanArg.getClassName());
        String description = MoreObjects.firstNonNull(javabeanArg.getDescription(), "");
        Javadoc javadoc = new JavadocComment(description).parse();
        javadoc.getBlockTags().addAll(authorTags);
        coid.setJavadocComment(javadoc);
        cu.addType(coid);

        for (FieldArg fieldArg : javabeanArg.getFieldArgs()) {
            if (fieldArg.getTypeQualifier() != null) {
                cu.addImport(fieldArg.getTypeQualifier());
            }
            FieldDeclaration field = coid.addField(fieldArg.getTypeName(), fieldArg.getFieldName(), Keyword.PRIVATE);
            if (fieldArg.getDescription() != null) {
                field.setJavadocComment(fieldArg.getDescription());
            }
            // more
            if (fieldArg.getMore4Field() != null) {
                fieldArg.getMore4Field().accept(coid, field);
            }
        }

        // more
        if (javabeanArg.getMore4Javabean() != null) {
            javabeanArg.getMore4Javabean().accept(cu, coid);
        }

        return cu;
    }

    private static void collect(List<JavadocBlockTag> authorTags, CompilationUnit cu) {
        cu.getPrimaryType().flatMap(NodeWithJavadoc::getJavadoc)
                .ifPresent(javadoc -> javadoc.getBlockTags().forEach(javadocTag -> {
                    if (javadocTag.getType() == Type.AUTHOR) {
                        authorTags.add(javadocTag);
                    }
                }));
    }

    private static void reportDiff(TreeSet<String> originalVariables, JavabeanArg javabeanArg) {
        TreeSet<String> destinedVariables = Sets.newTreeSet();
        for (FieldArg fieldArg : javabeanArg.getFieldArgs()) {
            destinedVariables.add(fieldArg.getFieldName());
        }

        if (!originalVariables.isEmpty()) {
            SetView<String> delete = Sets.difference(originalVariables, destinedVariables);
            SetView<String> add = Sets.difference(destinedVariables, originalVariables);
            if (add.size() > 0) {
                log.info("{} will add property {}", javabeanArg.getClassName(), Joiner.on(", ").join(add));
            }
            if (delete.size() > 0) {
                log.info("{} will delete property {}", javabeanArg.getClassName(), Joiner.on(", ").join(delete));
            }
            if (add.size() == 0 && delete.size() == 0) {
                log.info("{} will not add or delete properties any more.", javabeanArg.getClassName());
            }
        }
    }

}
