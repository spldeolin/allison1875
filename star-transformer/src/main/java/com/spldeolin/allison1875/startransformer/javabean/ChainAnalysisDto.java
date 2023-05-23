package com.spldeolin.allison1875.startransformer.javabean;

import java.util.List;
import com.github.javaparser.ast.expr.Expression;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2023-05-02
 */
@Data
@Accessors(chain = true)
public class ChainAnalysisDto {

    private String cftEntityQualifier;

    private String cftEntityName;

    private String cftDesignName;

    private String cftDesignQualifier;

    private Expression cftSecondArgument;

    private List<PhraseDto> phrases;

    private String wholeDtoName;

}