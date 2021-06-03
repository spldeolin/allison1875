package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.Collection;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-12-09
 */
@Data
@Accessors(chain = true)
public class ChainAnalysisDto {

    private Collection<CriterionDto> criterions;

    private String methodName;

    private boolean queryOrUpdate;

    private boolean returnManyOrOne;

    private Set<PhraseDto> queryPhrases;

    private Set<PhraseDto> byPhrases;

    private Set<PhraseDto> orderPhrases;

    private Set<PhraseDto> updatePhrases;

}