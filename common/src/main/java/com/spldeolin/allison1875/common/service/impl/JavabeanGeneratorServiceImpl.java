package com.spldeolin.allison1875.common.service.impl;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.constant.AnnotationConstant;
import com.spldeolin.allison1875.common.constant.ImportConstant;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.service.JavabeanGeneratorService;
import com.spldeolin.allison1875.common.service.javabean.FieldArg;
import com.spldeolin.allison1875.common.service.javabean.JavabeanArg;
import com.spldeolin.allison1875.common.service.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.util.ValidUtils;
import com.spldeolin.allison1875.common.util.ast.Javadocs;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-05-26
 */
@Singleton
@Log4j2
public class JavabeanGeneratorServiceImpl implements JavabeanGeneratorService {

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Override
    public JavabeanGeneration generate(JavabeanArg arg) {
        List<InvalidDto> valid = ValidUtils.valid(arg);
        if (!valid.isEmpty()) {
            throw new Allison1875Exception("JavabeanArg illegal, " + arg);
        }

        String className = arg.getClassName();
        Path absulutePath = CodeGenerationUtils.fileInPackageAbsolutePath(arg.getAstForest().getPrimaryJavaRoot(),
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
                throw new RuntimeException("impossible unless bug");
            }
        }

        CompilationUnit cu = new CompilationUnit();
        cu.setStorage(absulutePath);
        cu.setPackageDeclaration(arg.getPackageName());
        cu.addImport(ImportConstant.LOMBOK);
        cu.addImport(ImportConstant.LOMBOK_EXPERIMENTAL);
        cu.addImport(ImportConstant.JAVAX_VALID);
        cu.addImport(ImportConstant.GOOGLE_COMMON_COLLECTION);
        cu.addImport(ImportConstant.JAVA_UTIL);
        cu.addImport(ImportConstant.JAVA_TIME);

        ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
        coid.addAnnotation(AnnotationConstant.DATA);
        coid.addAnnotation(AnnotationConstant.ACCESSORS);
        coid.addAnnotation(AnnotationConstant.FIELD_DEFAULTS_PRIVATE);
        coid.setPublic(true).setInterface(false).setName(className);
        String description = MoreObjects.firstNonNull(arg.getDescription(), "");
        coid.setJavadocComment(Javadocs.createJavadoc(description, arg.getAuthorName() + " " + LocalDate.now()));
        cu.addType(coid);

        for (FieldArg fieldArg : arg.getFieldArgs()) {
            if (fieldArg.getTypeQualifier() != null) {
                cu.addImport(fieldArg.getTypeQualifier());
            }
            FieldDeclaration field = coid.addField(fieldArg.getTypeName(), fieldArg.getFieldName());
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
