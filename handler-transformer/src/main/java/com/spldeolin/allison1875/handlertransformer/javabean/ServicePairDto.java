package com.spldeolin.allison1875.handlertransformer.javabean;

import java.util.Collection;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-03-05
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServicePairDto {

     ClassOrInterfaceDeclaration service;

     Collection<ClassOrInterfaceDeclaration> serviceImpls = Lists.newArrayList();

}