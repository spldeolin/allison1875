package com.spldeolin.allison1875.handlertransformer.javabean;

import com.github.javaparser.ast.CompilationUnit;
import lombok.Data;

/**
 * @author Deolin 2021-03-05
 */
@Data
public class GenerateServiceParam {

    private CompilationUnit cu;

    private FirstLineDto firstLineDto;

    private ReqDtoRespDtoInfo reqDtoRespDtoInfo;

}