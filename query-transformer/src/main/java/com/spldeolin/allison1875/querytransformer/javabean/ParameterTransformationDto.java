package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.List;
import com.github.javaparser.ast.body.Parameter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-06-01
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParameterTransformationDto {

     List<String> imports;

     List<Parameter> parameters;

     Boolean isJavabean;

}