package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.LotNo;
import com.spldeolin.allison1875.querytransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnClassifyEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-12-09
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChainAnalysisDto {

    String methodName;

    Boolean noSpecifiedMethodName;

    ChainMethodEnum chainMethod;

    ReturnClassifyEnum returnClassify;

    Set<PhraseDto> queryPhrases = Sets.newLinkedHashSet();

    Set<PhraseDto> byPhrases = Sets.newLinkedHashSet();

    Set<PhraseDto> orderPhrases = Sets.newLinkedHashSet();

    Set<PhraseDto> updatePhrases = Sets.newLinkedHashSet();

    @JsonIgnore
    MethodCallExpr chain;

    BlockStmt directBlock;

    String indent;

    Boolean isAssigned;

    Boolean isAssignedToType;

    Boolean isByForced;

    LotNo lotNo;

}