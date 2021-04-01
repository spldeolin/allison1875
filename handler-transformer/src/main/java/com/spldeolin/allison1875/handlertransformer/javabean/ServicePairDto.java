package com.spldeolin.allison1875.handlertransformer.javabean;

import java.util.Collection;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2021-03-05
 */
@Data
@Accessors(chain = true)
public class ServicePairDto {

    private ClassOrInterfaceDeclaration service;

    private Collection<ClassOrInterfaceDeclaration> serviceImpls = Lists.newArrayList();

}