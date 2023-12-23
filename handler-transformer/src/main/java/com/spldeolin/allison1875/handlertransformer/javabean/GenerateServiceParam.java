package com.spldeolin.allison1875.handlertransformer.javabean;

import java.util.Map;
import com.github.javaparser.ast.CompilationUnit;
import com.spldeolin.allison1875.base.ast.AstForest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-03-05
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenerateServiceParam {

     CompilationUnit cu;

     FirstLineDto firstLineDto;

     ReqDtoRespDtoInfo reqDtoRespDtoInfo;

     AstForest astForest;

     Map<String, ServicePairDto> qualifier2Pair;

     Map<String, ServicePairDto> name2Pair;

}