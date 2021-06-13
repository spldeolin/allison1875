package com.spldeolin.allison1875.querytransformer.javabean;

import com.github.javaparser.ast.expr.Expression;
import com.spldeolin.allison1875.querytransformer.enums.PredicateEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2021-05-30
 */
@Data
@Accessors(chain = true)
public class PhraseDto {

    private String subjectPropertyName;

    private String varName;

    private PredicateEnum predicate;

    private Expression objectExpr;

    public String toString() {
        if (predicate == null && objectExpr == null) {
            return subjectPropertyName;
        }
        if (objectExpr == null) {
            return subjectPropertyName + " " + predicate.getValue();
        }
        if (predicate == null) {
            return subjectPropertyName + " . " + objectExpr;
        }
        return subjectPropertyName + " " + predicate.getValue() + " " + objectExpr;

    }

}