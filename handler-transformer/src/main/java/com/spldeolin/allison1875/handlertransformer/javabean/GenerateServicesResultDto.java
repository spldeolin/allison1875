package com.spldeolin.allison1875.handlertransformer.javabean;

import com.github.javaparser.ast.CompilationUnit;
import lombok.Data;

/**
 * @author Deolin 2020-12-07
 */
@Data
public class GenerateServicesResultDto {

    private CompilationUnit serviceCu;

    private CompilationUnit serviceImplCu;

    private String serviceQualifier;

}