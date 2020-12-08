package com.spldeolin.allison1875.persistencegenerator.javabean;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-12-08
 */
@Data
@Accessors(chain = true)
public class FindOrCreateMapperResultDto {

    private ClassOrInterfaceDeclaration mapper;

    private CompilationUnit cu;

}