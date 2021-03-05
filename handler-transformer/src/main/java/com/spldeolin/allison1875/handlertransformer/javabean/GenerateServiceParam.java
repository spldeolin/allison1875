package com.spldeolin.allison1875.handlertransformer.javabean;

import java.util.Map;
import com.github.javaparser.ast.CompilationUnit;
import com.spldeolin.allison1875.base.ast.AstForest;
import lombok.Data;

/**
 * @author Deolin 2021-03-05
 */
@Data
public class GenerateServiceParam {

    private CompilationUnit cu;

    private FirstLineDto firstLineDto;

    private ReqDtoRespDtoInfo reqDtoRespDtoInfo;

    private AstForest astForest;

    private Map<String, ServicePairDto> qualifier2Pair;

}