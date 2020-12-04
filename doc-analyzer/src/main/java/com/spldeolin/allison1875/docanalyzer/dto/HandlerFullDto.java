package com.spldeolin.allison1875.docanalyzer.dto;

import java.lang.reflect.Method;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Deolin 2020-12-04
 */
@Data
@AllArgsConstructor
public class HandlerFullDto {

    private String cat;

    private MethodDeclaration md;

    private Method reflection;

    private ControllerFullDto controller;

}