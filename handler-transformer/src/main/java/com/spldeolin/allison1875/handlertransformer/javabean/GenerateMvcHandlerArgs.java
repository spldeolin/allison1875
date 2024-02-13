package com.spldeolin.allison1875.handlertransformer.javabean;

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
public class GenerateMvcHandlerArgs {

    InitDecAnalysisDto initDecAnalysis;

    String serviceParamType;

    String serviceResultType;

    String serviceVarName;

    String serviceMethodName;

}