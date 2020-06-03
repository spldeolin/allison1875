package com.spldeolin.allison1875.da;

import java.util.Collection;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.da.dto.PropertyValidatorDto;
import com.spldeolin.allison1875.da.enums.ValidatorTypeEnum;
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
                    result.add(new PropertyValidatorDto().setValidatorType(ValidatorTypeEnum.notEmpty.getValue()));
                    break;
                case "NotBlank":
                    result.add(new PropertyValidatorDto().setValidatorType(ValidatorTypeEnum.notBlank.getValue()));
                    break;
                case "Size":
                case "Length":
                    annotation.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                        PropertyValidatorDto validator = new PropertyValidatorDto().setNote(pair.getValue().toString());
                        if (nameOf(pair, "min")) {
                            result.add(validator.setValidatorType(ValidatorTypeEnum.minSize.getValue()));
                        }
                        if (nameOf(pair, "max")) {
                            result.add(validator.setValidatorType(ValidatorTypeEnum.maxSize.getValue()));
                        }
                    });
                    break;
                case "Max":
                    annotation.ifSingleMemberAnnotationExpr(singleAnno -> result
                            .add(new PropertyValidatorDto().setValidatorType(ValidatorTypeEnum.maxInteger.getValue())
                                    .setNote(singleAnno.getMemberValue().toString())));
                    annotation.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> result.add(new PropertyValidatorDto()
                                            .setValidatorType(ValidatorTypeEnum.maxInteger.getValue())
                                            .setNote(pair.getValue().toString()))));
                    break;
                case "Min":
                    annotation.ifSingleMemberAnnotationExpr(singleAnno -> result
                            .add(new PropertyValidatorDto().setValidatorType(ValidatorTypeEnum.minInteger.getValue())
                                    .setNote(singleAnno.getMemberValue().toString())));
                    annotation.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> result.add(new PropertyValidatorDto()
                                            .setValidatorType(ValidatorTypeEnum.minInteger.getValue())
                                            .setNote(pair.getValue().toString()))));
                    break;
                case "DecimalMax":
                    annotation.ifSingleMemberAnnotationExpr(singleAnno -> result
                            .add(new PropertyValidatorDto().setValidatorType(ValidatorTypeEnum.maxFloat.getValue())
                                    .setNote(singleAnno.getMemberValue().toString())));
                    annotation.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> result.add(new PropertyValidatorDto()
                                            .setValidatorType(ValidatorTypeEnum.maxFloat.getValue())
                                            .setNote(pair.getValue().toString()))));
                    break;
                case "DecimalMin":
                    annotation.ifSingleMemberAnnotationExpr(singleAnno -> result
                            .add(new PropertyValidatorDto().setValidatorType(ValidatorTypeEnum.minFloat.getValue())
                                    .setNote(singleAnno.getMemberValue().toString())));
                    annotation.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> result.add(new PropertyValidatorDto()
                                            .setValidatorType(ValidatorTypeEnum.minFloat.getValue())
                                            .setNote(pair.getValue().toString()))));
                    break;
                case "Future":
                    result.add(new PropertyValidatorDto().setValidatorType(ValidatorTypeEnum.future.getValue()));
                    break;
                case "Past":
                    result.add(new PropertyValidatorDto().setValidatorType(ValidatorTypeEnum.past.getValue()));
                    break;
                case "Digits":
                    annotation.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                        PropertyValidatorDto validator = new PropertyValidatorDto().setNote(pair.getValue().toString());
                        if (nameOf(pair, "integer")) {
                            result.add(validator.setValidatorType(ValidatorTypeEnum.maxIntegralDigits.getValue()));
                        }
                        if (nameOf(pair, "fraction")) {
                            result.add(validator.setValidatorType(ValidatorTypeEnum.maxFractionalDigits.getValue()));
                        }
                    });
                    break;
                case "Positive":
                    result.add(new PropertyValidatorDto().setValidatorType(ValidatorTypeEnum.positive.getValue()));
                    break;
                case "Negative":
                    result.add(new PropertyValidatorDto().setValidatorType(ValidatorTypeEnum.negative.getValue()));
                    break;
                case "Pattern":
                    annotation.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                        if (nameOf(pair, "regexp")) {
                            result.add(new PropertyValidatorDto().setValidatorType(ValidatorTypeEnum.regex.getValue())
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
