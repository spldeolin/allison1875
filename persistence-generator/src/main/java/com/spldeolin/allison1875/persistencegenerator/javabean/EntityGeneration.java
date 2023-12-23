package com.spldeolin.allison1875.persistencegenerator.javabean;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.spldeolin.allison1875.base.factory.javabean.JavabeanArg;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-12-08
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EntityGeneration {

     boolean sameNameAndLotNoPresent = false;

     String entityName;

     String entityQualifier;

     JavabeanArg javabeanArg;

     ClassOrInterfaceDeclaration entity;

     CompilationUnit entityCu;

}