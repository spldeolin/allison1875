package com.spldeolin.allison1875.docanalyzer.javabean;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
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
public class MvcControllerDto {

    String cat;

    ClassOrInterfaceDeclaration coid;

    Class<?> reflection;

}