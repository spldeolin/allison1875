package com.spldeolin.allison1875.common.service.impl;

import java.nio.file.Path;
import java.time.LocalDate;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.javabean.FieldArg;
import com.spldeolin.allison1875.common.javabean.JavabeanArg;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.service.JavabeanGeneratorService;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-05-26
 */
@Singleton
@Slf4j
public class JavabeanGeneratorServiceImpl implements JavabeanGeneratorService {

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Inject
    private ImportExprService importExprService;

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public JavabeanGeneration generate(JavabeanArg arg) {
        String className = arg.getClassName();
        Path absulutePath = CodeGenerationUtils.fileInPackageAbsolutePath(arg.getAstForest().getSourceRoot(),
                arg.getPackageName(), className + ".java");

        if (absulutePath.toFile().exists()) {
            if (arg.getJavabeanExistenceResolution() == FileExistenceResolutionEnum.OVERWRITE) {
                log.info("Entity [{}] is exist, use [overwrite] resolution", className);
            } else if (arg.getJavabeanExistenceResolution() == FileExistenceResolutionEnum.RENAME) {
                String oldClassName = className;
                log.info("Entity [{}] is exist, use [rename] resolution", oldClassName);
                absulutePath = antiDuplicationService.getNewPathIfExist(absulutePath);
                className = FilenameUtils.getBaseName(absulutePath.toString());
            } else {
                throw new Allison1875Exception(
                        "unknown FileExistenceResolutionEnum [" + arg.getJavabeanExistenceResolution() + "]");
            }
        }

        CompilationUnit cu = new CompilationUnit();
        cu.setStorage(absulutePath);
        cu.setPackageDeclaration(arg.getPackageName());

        ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
        coid.addAnnotation(annotationExprService.lombokData());
        coid.addAnnotation(annotationExprService.lombokAccessors());
        coid.addAnnotation(annotationExprService.lomokFieldDefaultsPrivate());
        coid.setPublic(true).setInterface(false).setName(className);
        if (arg.getIsJavabeanSerializable()) {
            coid.addImplementedType("java.io.Serializable");
        }
        if (arg.getIsJavabeanCloneable()) {
            coid.addImplementedType("Cloneable");
        }
        String comment = MoreObjects.firstNonNull(arg.getDescription(), "");
        JavadocUtils.setJavadoc(coid, comment, arg.getAuthor() + " " + LocalDate.now());
        cu.addType(coid);

        for (FieldArg fieldArg : arg.getFieldArgs()) {
            FieldDeclaration field = coid.addField(fieldArg.getTypeQualifier(), fieldArg.getFieldName());
            if (fieldArg.getDescription() != null) {
                field.setJavadocComment(fieldArg.getDescription());
            }
            // more for field
            if (fieldArg.getMore4Field() != null) {
                fieldArg.getMore4Field().accept(coid, field);
            }
        }

        // more for javabean
        if (arg.getMore4Javabean() != null) {
            arg.getMore4Javabean().accept(cu, coid);
        }

        if (arg.getIsJavabeanSerializable()) {
            coid.getMembers().addFirst(StaticJavaParser.parseBodyDeclaration(
                    "private static final long serialVersionUID = " + RandomUtils.nextLong() + "L;"));
        }
        if (arg.getIsJavabeanCloneable()) {
            coid.getMembers().addLast(StaticJavaParser.parseBodyDeclaration(
                    "@Override public Object clone() throws CloneNotSupportedException { return super.clone(); }"));
        }

        importExprService.extractQualifiedTypeToImport(cu);

        JavabeanGeneration result = new JavabeanGeneration();
        result.setCu(cu);
        result.setFileFlush(FileFlush.build(cu));
        result.setJavabeanName(className);
        result.setJavabeanQualifier(arg.getPackageName() + "." + className);
        result.setCoid(coid);
        result.setPath(absulutePath);
        return result;
    }

}
