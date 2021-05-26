package com.spldeolin.allison1875.base.factory;

import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.spldeolin.allison1875.base.constant.AnnotationConstant;
import com.spldeolin.allison1875.base.factory.javabean.FieldArg;
import com.spldeolin.allison1875.base.factory.javabean.JavabeanArg;
import com.spldeolin.allison1875.base.util.ast.Authors;

/**
 * @author Deolin 2021-05-26
 */
public class JavabeanFactory {

    public static CompilationUnit buildCu(JavabeanArg javabeanArg) {
        CompilationUnit cu = new CompilationUnit();
        Path absulutePath = CodeGenerationUtils
                .fileInPackageAbsolutePath(javabeanArg.getAstForest().getPrimaryJavaRoot(),
                        javabeanArg.getPackageName(), javabeanArg.getClassName() + ".java");
        cu.setStorage(absulutePath);
        cu.setPackageDeclaration(javabeanArg.getPackageName());
        cu.addImport(AnnotationConstant.DATA_QUALIFIER);
        cu.addImport(AnnotationConstant.ACCESSORS_QUALIFIER);

        ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
        coid.addAnnotation(AnnotationConstant.DATA);
        coid.addAnnotation(AnnotationConstant.ACCESSORS);
        coid.setName(javabeanArg.getClassName());
        if (StringUtils.isNotEmpty(javabeanArg.getAuthorName())) {
            Authors.ensureAuthorExist(coid, javabeanArg.getAuthorName());
        }
        for (FieldArg fieldArg : javabeanArg.getFieldArgs()) {
            if (fieldArg.getTypeQualifier() != null) {
                cu.addImport(fieldArg.getTypeQualifier());
            }
            FieldDeclaration field = coid.addField(fieldArg.getTypeName(), fieldArg.getFieldName(), Keyword.PRIVATE);
            if (fieldArg.getDescription() != null) {
                field.setJavadocComment(fieldArg.getDescription());
            }
        }
        cu.addType(coid);

        return cu;
    }

}
