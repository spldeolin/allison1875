package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.Set;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.querytransformer.enums.ChainMethodEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-12-09
 */
@Data
@Accessors(chain = true)
public class ChainAnalysisDto {

    private String methodName;

    private ChainMethodEnum chainMethod;

    private boolean returnManyOrOne;

    private Set<PhraseDto> queryPhrases = Sets.newLinkedHashSet();

    private Set<PhraseDto> byPhrases = Sets.newLinkedHashSet();

    private Set<PhraseDto> orderPhrases = Sets.newLinkedHashSet();

    private Set<PhraseDto> updatePhrases = Sets.newLinkedHashSet();

    private MethodCallExpr chain;

    private String indent;

}