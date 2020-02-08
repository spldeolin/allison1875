package com.spldeolin.allison1875.da;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-08
 */
@Log4j2
public class Boot {

    public static void main(String[] args) {
        CompilationUnit parse = StaticJavaParser
                .parse("public class C {static void m(int i) {i = -11; System.out.println(i);}}");

        log.info(parse.getType(0).getMember(0));
    }

}
