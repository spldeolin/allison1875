package com.spldeolin.allison1875.common.test.javadoc;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.MavenProjectBuiltAstForest;
import com.spldeolin.allison1875.common.test.AstForestTest;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.JavadocUtils;

/**
 * @author Deolin 2024-01-16
 */
public class JavadocTest {

    public static void main(String[] args) throws IOException {
        AstForest astForest = new MavenProjectBuiltAstForest(AstForestTest.class.getClassLoader(), new File("common/"));
        Optional<File> resolve = astForest.resolve(
                "src/test/java/com/spldeolin/allison1875/common/test/javadoc/TestSubject.java", false);
        CompilationUnit cu = CompilationUnitUtils.parseJava(resolve.get());
//        LexicalPreservingPrinter.setup(cu);
//        System.out.println(LexicalPreservingPrinter.print(cu));

        TypeDeclaration<?> primaryType = cu.getPrimaryType().get();
        Javadoc javadoc = primaryType.getJavadoc().get();

        JavadocUtils.getComment(primaryType);
        JavadocUtils.getComment(primaryType.getMethodsByName("m1").get(0));
        JavadocUtils.getComment(primaryType.getMethodsByName("m2").get(0));
        JavadocUtils.getAuthor(cu.findAll(FieldDeclaration.class,
                fieldDeclaration -> fieldDeclaration.toString().equals("private String f1;")).get(0));

    }

}