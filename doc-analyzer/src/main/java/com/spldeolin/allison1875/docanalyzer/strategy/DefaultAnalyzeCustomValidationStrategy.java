package com.spldeolin.allison1875.docanalyzer.strategy;

import java.util.Collection;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.docanalyzer.dto.ValidatorDto;

/**
 * @author Deolin 2020-06-18
 */
public class DefaultAnalyzeCustomValidationStrategy implements AnalyzeCustomValidationStrategy {

    @Override
    public Collection<ValidatorDto> analyzeCustomValidation(String qualifier, AnnotationExpr annotation) {
        return Lists.newArrayList();
    }

}
