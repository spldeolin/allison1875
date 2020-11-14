package com.spldeolin.allison1875.persistencegenerator.strategy;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;

/**
 * @author Deolin 2020-11-14
 */
public interface GenerateFieldCallbackStrategy {

    void handle(PropertyDto propertyDto, FieldDeclaration field);

}
