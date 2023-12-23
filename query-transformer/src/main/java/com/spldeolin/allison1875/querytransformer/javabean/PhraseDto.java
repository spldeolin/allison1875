package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.javaparser.ast.expr.Expression;
import com.spldeolin.allison1875.querytransformer.enums.PredicateEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-05-30
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhraseDto {

    String subjectPropertyName;

    String varName;

    PredicateEnum predicate;

    @JsonIgnore
    Expression objectExpr;

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