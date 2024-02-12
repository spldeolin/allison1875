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
public class MvcHandlerDto {

    String cat;

    MethodDeclaration methodDec;

    Method reflection;

    MvcControllerDto mvcController;

}