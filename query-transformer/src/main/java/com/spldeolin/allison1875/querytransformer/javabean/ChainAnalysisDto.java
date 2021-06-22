package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.Set;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-12-09
 */
@Data
@Accessors(chain = true)
public class ChainAnalysisDto {

    private String methodName;

    private boolean queryOrUpdate;

    private boolean returnManyOrOne;

    private Set<PhraseDto> queryPhrases = Sets.newHashSet();

    private Set<PhraseDto> byPhrases = Sets.newHashSet();

    private Set<PhraseDto> orderPhrases = Sets.newHashSet();

    private Set<PhraseDto> updatePhrases = Sets.newHashSet();

    private MethodCallExpr chain;

    private String indent;

}