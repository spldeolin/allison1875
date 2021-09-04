package com.spldeolin.allison1875.persistencegenerator.javabean;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.spldeolin.allison1875.base.factory.javabean.JavabeanArg;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-12-08
 */
@Data
@Accessors(chain = true)
public class EntityGeneration {

    private boolean sameNameAndLotNoPresent = false;

    private String entityName;

    private String entityQualifier;

    private JavabeanArg javabeanArg;

    private ClassOrInterfaceDeclaration entity;

}