package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.List;
import com.github.javaparser.ast.type.Type;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2021-06-01
 */
@Data
@Accessors(chain = true)
public class ResultTransformationDto {

    private List<String> imports;

    private Type resultType;

    private Boolean isSpecifiedEntity;

}