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

    /**
     * TODO JavabeanFactory#buildCu 如果absulutePath对应的文件已存在，需要在新的primaryType使用原有的Javadoc Author
     * TODO JavabeanFactory#buildCu 如果absulutePath对应的文件已存在，需要打印field的变动点
     */
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
        coid.setPublic(true).setInterface(false).setName(javabeanArg.getClassName());
        if (StringUtils.isNotEmpty(javabeanArg.getAuthorName())) {
            Authors.ensureAuthorExist(coid, javabeanArg.getAuthorName());
        }
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

}
