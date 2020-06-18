package com.spldeolin.allison1875.transformer.copy.javadoc.to.getter.and.setter;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.ast.Saves;

/**
 * 将field的javadoc复制到它的getter和setter上
 *
 * @author Deolin 2020-05-25
 */
public class CopyJavadocToGetterAndSetter {

    public static void main(String[] args) {
        for (CompilationUnit cu : AstForest.getInstance()) {
            boolean update = false;
            for (ClassOrInterfaceDeclaration coid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
//                if (!Authors.getAuthor(cu).startsWith("Deolin")) {
//                    continue;
//                }
                if (!isJavabean(coid)) {
                    continue;
                }

                for (FieldDeclaration field : coid.getFields()) {
                    Optional<Javadoc> fieldJavadoc = field.getJavadoc();
                    if (!fieldJavadoc.isPresent()) {
                        continue;
                    }
                    for (VariableDeclarator var : field.getVariables()) {
                        Class<?> placeholder = Object.class;
                        if (var.getType().isPrimitiveType() && var.getTypeAsString().equals("boolean")) {
                            placeholder = boolean.class;
                        }
                        String getterName = CodeGenerationUtils.getterName(placeholder, var.getNameAsString());
                        String setterName = CodeGenerationUtils.setterName(var.getNameAsString());

                        List<MethodDeclaration> getters = coid.getMethodsBySignature(getterName);
                        List<MethodDeclaration> setters = coid.getMethodsBySignature(setterName, var.getTypeAsString());

                        for (MethodDeclaration getter : getters) {
                            if (!getter.getJavadoc().isPresent()) {
                                getter.setJavadocComment(fieldJavadoc.get());
                                update = true;
                            }
                        }
                        for (MethodDeclaration setter : setters) {
                            if (!setter.getJavadoc().isPresent()) {
                                setter.setJavadocComment(fieldJavadoc.get());
                                update = true;
                            }
                        }
                    }
                }

            }
            if (update) {
                Saves.prettySave(cu);
            }
        }
    }

    private static boolean isJavabean(ClassOrInterfaceDeclaration coid) {
        if (coid.isInterface()) {
            return false;
        }
        return StringUtils
                .containsAny(coid.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new), ".entity.",
                        ".dto.", ".bo.", "req", "res");
    }

}
