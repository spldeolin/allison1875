package com.spldeolin.allison1875.docanalyzer.javabean;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Deolin 2020-12-04
 */
@Data
@AllArgsConstructor
public class ControllerFullDto {

    private String cat;

    private ClassOrInterfaceDeclaration coid;

    private Class<?> reflection;

}