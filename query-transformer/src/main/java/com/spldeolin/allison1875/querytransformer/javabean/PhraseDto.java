package com.spldeolin.allison1875.querytransformer.javabean;

import com.github.javaparser.ast.expr.Expression;
import com.spldeolin.allison1875.querytransformer.enums.VerbEnum;
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

    private VerbEnum verb;

    private Expression objectExpr;

    public String toString() {
        if (verb == null && objectExpr == null) {
            return subjectPropertyName;
        }
        if (objectExpr == null) {
            return subjectPropertyName + " " + verb.getValue();
        }
        if (verb == null) {
            return subjectPropertyName + " . " + objectExpr;
        }
        return subjectPropertyName + " " + verb.getValue() + " " + objectExpr;

    }

}