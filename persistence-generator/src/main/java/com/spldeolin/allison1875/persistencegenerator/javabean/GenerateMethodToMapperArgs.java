package com.spldeolin.allison1875.persistencegenerator.javabean;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-02-13
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenerateMethodToMapperArgs {

    TableStructureAnalysisDto tableStructureAnalysisDto;

    JavabeanGeneration entityGeneration;

    PropertyDto key;

    ClassOrInterfaceDeclaration mapper;

}
