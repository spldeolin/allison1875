package com.spldeolin.allison1875.da.approved;

import static com.spldeolin.allison1875.da.approved.enums.ValidatorTypeEnum.future;
import static com.spldeolin.allison1875.da.approved.enums.ValidatorTypeEnum.maxFloat;
import static com.spldeolin.allison1875.da.approved.enums.ValidatorTypeEnum.maxFractionalDigits;
import static com.spldeolin.allison1875.da.approved.enums.ValidatorTypeEnum.maxInteger;
import static com.spldeolin.allison1875.da.approved.enums.ValidatorTypeEnum.maxIntegralDigits;
import static com.spldeolin.allison1875.da.approved.enums.ValidatorTypeEnum.maxSize;
import static com.spldeolin.allison1875.da.approved.enums.ValidatorTypeEnum.minFloat;
import static com.spldeolin.allison1875.da.approved.enums.ValidatorTypeEnum.minInteger;
import static com.spldeolin.allison1875.da.approved.enums.ValidatorTypeEnum.minSize;
import static com.spldeolin.allison1875.da.approved.enums.ValidatorTypeEnum.notBlank;
import static com.spldeolin.allison1875.da.approved.enums.ValidatorTypeEnum.notEmpty;
import static com.spldeolin.allison1875.da.approved.enums.ValidatorTypeEnum.past;
import static com.spldeolin.allison1875.da.approved.enums.ValidatorTypeEnum.positive;
import static com.spldeolin.allison1875.da.approved.enums.ValidatorTypeEnum.regex;

import java.util.Collection;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.da.approved.dto.PropertyValidatorDto;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * 校验项
 *
 * @author Deolin 2019-12-09
 */
@Log4j2
@Accessors(fluent = true)
public class ValidatorProcessor {

    public Collection<PropertyValidatorDto> process(NodeWithAnnotations<?> node) {
        Collection<PropertyValidatorDto> result = Lists.newLinkedList();
        for (AnnotationExpr annotation : node.getAnnotations()) {
            switch (annotation.getNameAsString()) {
                case "NotEmpty":
                    result.add(new PropertyValidatorDto().setValidatorType(notEmpty));
                    break;
                case "NotBlank":
                    result.add(new PropertyValidatorDto().setValidatorType(notBlank));
                    break;
                case "Size":
                case "Length":
                    annotation.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                        PropertyValidatorDto validator = new PropertyValidatorDto().setNote(pair.getValue().toString());
                        if (nameOf(pair, "min")) {
                            result.add(validator.setValidatorType(minSize));
                        }
                        if (nameOf(pair, "max")) {
                            result.add(validator.setValidatorType(maxSize));
                        }
                    });
                    break;
                case "Max":
                    annotation.ifSingleMemberAnnotationExpr(singleAnno -> result
                            .add(new PropertyValidatorDto().setValidatorType(maxInteger)
                                    .setNote(singleAnno.getMemberValue().toString())));
                    annotation.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> result.add(new PropertyValidatorDto().setValidatorType(maxInteger)
                                            .setNote(pair.getValue().toString()))));
                    break;
                case "Min":
                    annotation.ifSingleMemberAnnotationExpr(singleAnno -> result
                            .add(new PropertyValidatorDto().setValidatorType(minInteger)
                                    .setNote(singleAnno.getMemberValue().toString())));
                    annotation.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> result.add(new PropertyValidatorDto().setValidatorType(minInteger)
                                            .setNote(pair.getValue().toString()))));
                    break;
                case "DecimalMax":
                    annotation.ifSingleMemberAnnotationExpr(singleAnno -> result
                            .add(new PropertyValidatorDto().setValidatorType(maxFloat)
                                    .setNote(singleAnno.getMemberValue().toString())));
                    annotation.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> result.add(new PropertyValidatorDto().setValidatorType(maxFloat)
                                            .setNote(pair.getValue().toString()))));
                    break;
                case "DecimalMin":
                    annotation.ifSingleMemberAnnotationExpr(singleAnno -> result
                            .add(new PropertyValidatorDto().setValidatorType(minFloat)
                                    .setNote(singleAnno.getMemberValue().toString())));
                    annotation.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> result.add(new PropertyValidatorDto().setValidatorType(minFloat)
                                            .setNote(pair.getValue().toString()))));
                    break;
                case "Future":
                    result.add(new PropertyValidatorDto().setValidatorType(future));
                    break;
                case "Past":
                    result.add(new PropertyValidatorDto().setValidatorType(past));
                    break;
                case "Digits":
                    annotation.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                        PropertyValidatorDto validator = new PropertyValidatorDto().setNote(pair.getValue().toString());
                        if (nameOf(pair, "integer")) {
                            result.add(validator.setValidatorType(maxIntegralDigits));
                        }
                        if (nameOf(pair, "fraction")) {
                            result.add(validator.setValidatorType(maxFractionalDigits));
                        }
                    });
                    break;
                case "Positive":
                    result.add(new PropertyValidatorDto().setValidatorType(positive));
                    break;
                case "Pattern":
                    annotation.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                        if (nameOf(pair, "regexp")) {
                            result.add(new PropertyValidatorDto().setValidatorType(regex)
                                    .setNote(pair.getValue().asStringLiteralExpr().asString()));
                        }
                    });
                    break;
            }
        }
        return result;
    }

    private boolean nameIsValue(MemberValuePair pair) {
        return nameOf(pair, "value");
    }

    private boolean nameOf(MemberValuePair pair, String value) {
        return pair.getNameAsString().equals(value);
    }

}
