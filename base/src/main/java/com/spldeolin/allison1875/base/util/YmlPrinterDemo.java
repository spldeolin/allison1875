package com.spldeolin.allison1875.base.util;

import java.io.File;
import java.io.FileNotFoundException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.YamlPrinter;

/**
 * @author Deolin 2020-02-14
 */
public class YmlPrinterDemo {

    public static void main(String[] args) throws FileNotFoundException {

        File demoFile = new File(
                "/Users/deolin/Documents/project-repo/motherbuy/motherbuy/topaiebiz-member/src/main/java/com"
                        + "/topaiebiz/member/identity/service/impl/MemberIdentityServiceImpl.java");

        CompilationUnit cu = StaticJavaParser.parse(demoFile);

        YamlPrinter.print(cu);
    }

}
