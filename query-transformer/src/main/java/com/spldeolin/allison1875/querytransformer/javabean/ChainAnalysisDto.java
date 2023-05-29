package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.LotNo;
import com.spldeolin.allison1875.querytransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnClassifyEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-12-09
 */
@Data
@Accessors(chain = true)
public class ChainAnalysisDto {

    private String methodName;

    private Boolean noSpecifiedMethodName;

    private ChainMethodEnum chainMethod;

    private ReturnClassifyEnum returnClassify;

    private Set<PhraseDto> queryPhrases = Sets.newLinkedHashSet();

    private Set<PhraseDto> byPhrases = Sets.newLinkedHashSet();

    private Set<PhraseDto> orderPhrases = Sets.newLinkedHashSet();

    private Set<PhraseDto> updatePhrases = Sets.newLinkedHashSet();

    @JsonIgnore
    private MethodCallExpr chain;

    private BlockStmt directBlock;

    private String indent;

    private Boolean isAssigned;

    private Boolean isAssignedToType;

    private Boolean isByForced;

    private LotNo lotNo;

}