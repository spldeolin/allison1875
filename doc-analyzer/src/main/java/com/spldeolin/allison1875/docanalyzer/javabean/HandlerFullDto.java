package com.spldeolin.allison1875.docanalyzer.javabean;

import java.lang.reflect.Method;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-12-04
 */
@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HandlerFullDto {

    String cat;

    MethodDeclaration md;

    Method reflection;

    ControllerFullDto controller;

}