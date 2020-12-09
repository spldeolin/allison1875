package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.Collection;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-12-09
 */
@Data
@Accessors(chain = true)
public class AnalyzeCriterionResultDto {

    private Collection<CriterionDto> criterions;

    private String queryMethodName;

}