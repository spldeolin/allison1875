package com.spldeolin.allison1875.common.test.javadoc;

import java.io.IOException;
import java.nio.file.Path;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.spldeolin.allison1875.common.ast.MavenPathResolver;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.JavadocUtils;

/**
 * @author Deolin 2024-01-16
 */
public class JavadocTest {

    public static void main(String[] args) throws IOException {
        Path mavenModule = MavenPathResolver.findMavenModule(JavadocTest.class);
        Path path = CodeGenerationUtils.fileInPackageAbsolutePath(mavenModule + "/src/test/java",
                "com.spldeolin.allison1875.common.test.javadoc", "TestSubject.java");
        CompilationUnit cu = CompilationUnitUtils.parseCu(path.toFile());
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