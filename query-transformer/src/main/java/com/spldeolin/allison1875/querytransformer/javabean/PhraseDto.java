package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    private Expression objectExpr;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PhraseDto phraseDto = (PhraseDto) o;
        return Objects.equals(subjectPropertyName, phraseDto.subjectPropertyName) && predicate == phraseDto.predicate
                && Objects.equals(objectExpr, phraseDto.objectExpr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subjectPropertyName, predicate, objectExpr);
    }

}