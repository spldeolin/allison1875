package com.spldeolin.allison1875.querytransformer.javabean;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2021-06-05
 */
@Data
@Accessors(chain = true)
public class EntityAndSuperEntityDto {

    private ClassOrInterfaceDeclaration entity;

    private ClassOrInterfaceDeclaration superEntity;

}