package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.List;
import javax.annotation.Nullable;
import com.github.javaparser.ast.type.Type;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2021-06-01
 */
@Data
@Accessors(chain = true)
public class ResultTransformationDto {

    private List<String> imports = Lists.newArrayList();

    private Type resultType;

    private Boolean isAssigned;

    @Nullable
    private String javabeanQualifier;

}