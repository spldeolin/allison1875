package com.spldeolin.allison1875.base.generator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import org.apache.commons.io.FilenameUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.base.MoreObjects;
import com.spldeolin.allison1875.base.ast.FileFlush;
import com.spldeolin.allison1875.base.constant.AnnotationConstant;
import com.spldeolin.allison1875.base.constant.ImportConstant;
import com.spldeolin.allison1875.base.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.base.generator.javabean.FieldArg;
import com.spldeolin.allison1875.base.generator.javabean.JavabeanArg;
import com.spldeolin.allison1875.base.generator.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.base.util.ValidateUtils;
import com.spldeolin.allison1875.base.util.ast.Javadocs;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-05-26
 */
@Log4j2
public class JavabeanGenerator {

    public static JavabeanGeneration generate(JavabeanArg arg) {
        ValidateUtils.ensureValid(arg);

        String className = arg.getClassName();
        Path absulutePath = CodeGenerationUtils.fileInPackageAbsolutePath(arg.getAstForest().getPrimaryJavaRoot(),
                arg.getPackageName(), className + ".java");

        if (absulutePath.toFile().exists()) {
            if (arg.getEntityExistenceResolution() == FileExistenceResolutionEnum.OVERWRITE) {
                log.info("Entity [{}] is exist, use [overwrite] resolution", className);
            } else if (arg.getEntityExistenceResolution() == FileExistenceResolutionEnum.RENAME) {
                String oldClassName = className;
                absulutePath = rename(absulutePath);
                className = FilenameUtils.getBaseName(absulutePath.toString());
                log.info("Entity [{}] is exist, use [rename] resolution, newClassName={}", oldClassName, className);
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
        return result;
    }

    private static Path rename(Path path) {
        String extension = FilenameUtils.getExtension(path.toString());
        Path newPath = Paths.get(FilenameUtils.removeExtension(path.toString()) + "Ex" + "." + extension);
        if (newPath.toFile().exists()) {
            return rename(newPath);
        } else {
            return newPath;
        }
    }

}
