package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.List;
import com.github.javaparser.ast.body.Parameter;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2021-06-01
 */
@Data
@Accessors(chain = true)
public class ParameterTransformationDto {

    private List<String> imports;

    private List<Parameter> parameters;

    private Boolean isJavabean;

}