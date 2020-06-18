package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.docanalyzer.dto.ValidatorDto;
import com.spldeolin.allison1875.docanalyzer.enums.ValidatorTypeEnum;
import com.spldeolin.allison1875.docanalyzer.strategy.AnalyzeCustomValidationStrategy;
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

    private final AnalyzeCustomValidationStrategy analyzeCustomValidationStrategy;

    public ValidatorProcessor(AnalyzeCustomValidationStrategy analyzeCustomValidationStrategy) {
        this.analyzeCustomValidationStrategy = analyzeCustomValidationStrategy;
    }

    public Collection<ValidatorDto> process(NodeWithAnnotations<?> node) {
        Collection<ValidatorDto> result = Lists.newLinkedList();
        for (AnnotationExpr annotation : node.getAnnotations()) {
            ResolvedAnnotationDeclaration resolve = annotation.resolve();
            String qualifier = resolve.getQualifiedName();

            result.addAll(analyzeCustomValidationStrategy.analyzeCustomValidation(qualifier, annotation));

            if (StringUtils.equalsAny(qualifier, "javax.validation.constraints.NotEmpty",
                    "org.hibernate.validator.constraints.NotEmpty")) {
                result.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.NOT_EMPTY.getValue()));
            }

            if (StringUtils.equalsAny(qualifier, "javax.validation.constraints.NotBlank",
                    "org.hibernate.validator.constraints.NotBlank")) {
                result.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.NOT_BLANK.getValue()));
            }

            if (StringUtils.equalsAny(qualifier, "javax.validation.constraints.Size",
                    "org.hibernate.validator.constraints.Length")) {
                annotation.ifNormalAnnotationExpr(nae -> nae.getPairs().forEach(pair -> {
                    ValidatorDto validator = new ValidatorDto().setNote(pair.getValue().toString());
                    if (nameOf(pair, "min")) {
                        result.add(validator.setValidatorType(ValidatorTypeEnum.MIN_SIZE.getValue()));
                    }
                    if (nameOf(pair, "max")) {
                        result.add(validator.setValidatorType(ValidatorTypeEnum.MAX_SIZE.getValue()));
                    }
                }));
            }

            if (StringUtils.equalsAny(qualifier, "javax.validation.constraints.Max")) {
                annotation.ifSingleMemberAnnotationExpr(singleAnno -> result
                        .add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.MAX_NUMBER.getValue())
                                .setNote(singleAnno.getMemberValue().toString())));
                annotation.ifNormalAnnotationExpr(
                        normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny()
                                .ifPresent(pair -> result.add(new ValidatorDto()
                                        .setValidatorType(ValidatorTypeEnum.MAX_NUMBER.getValue())
                                        .setNote(pair.getValue().toString()))));
            }

            if (StringUtils.equalsAny(qualifier, "javax.validation.constraints.Min")) {
                annotation.ifSingleMemberAnnotationExpr(singleAnno -> result
                        .add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.MIN_NUMBER.getValue())
                                .setNote(singleAnno.getMemberValue().toString())));
                annotation.ifNormalAnnotationExpr(
                        normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny()
                                .ifPresent(pair -> result.add(new ValidatorDto()
                                        .setValidatorType(ValidatorTypeEnum.MIN_NUMBER.getValue())
                                        .setNote(pair.getValue().toString()))));
            }

            if (StringUtils.equalsAny(qualifier, "javax.validation.constraints.DecimalMax")) {
                annotation.ifSingleMemberAnnotationExpr(singleAnno -> result
                        .add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.MAX_NUMBER.getValue())
                                .setNote(singleAnno.getMemberValue().toString())));
                annotation.ifNormalAnnotationExpr(
                        normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny()
                                .ifPresent(pair -> result.add(new ValidatorDto()
                                        .setValidatorType(ValidatorTypeEnum.MAX_NUMBER.getValue())
                                        .setNote(pair.getValue().toString()))));
            }

            if (StringUtils.equalsAny(qualifier, "javax.validation.constraints.DecimalMin")) {
                annotation.ifSingleMemberAnnotationExpr(singleAnno -> result
                        .add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.MIN_NUMBER.getValue())
                                .setNote(singleAnno.getMemberValue().toString())));
                annotation.ifNormalAnnotationExpr(
                        normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny()
                                .ifPresent(pair -> result.add(new ValidatorDto()
                                        .setValidatorType(ValidatorTypeEnum.MIN_NUMBER.getValue())
                                        .setNote(pair.getValue().toString()))));
            }

            if (StringUtils.equalsAny(qualifier, "javax.validation.constraints.Future")) {
                result.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.VALIDATOR_TYPE_ENUM.getValue()));
            }

            if (StringUtils.equalsAny(qualifier, "javax.validation.constraints.Past")) {
                result.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.PAST.getValue()));
            }

            if (StringUtils.equalsAny(qualifier, "javax.validation.constraints.Digits")) {
                annotation.ifNormalAnnotationExpr(nae -> nae.getPairs().forEach(pair -> {
                    ValidatorDto validator = new ValidatorDto().setNote(pair.getValue().toString());
                    if (nameOf(pair, "integer")) {
                        result.add(validator.setValidatorType(ValidatorTypeEnum.MAX_INTEGRAL_DIGITS.getValue()));
                    }
                    if (nameOf(pair, "fraction")) {
                        result.add(validator.setValidatorType(ValidatorTypeEnum.MAX_FRACTIONAL_DIGITS.getValue()));
                    }
                }));
            }

            if (StringUtils.equalsAny(qualifier, "javax.validation.constraints.Positive")) {
                result.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.POSITIVE.getValue()));
            }

            if (StringUtils.equalsAny(qualifier, "javax.validation.constraints.Negative")) {
                result.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.NEGATIVE.getValue()));
            }

            if (StringUtils.equalsAny(qualifier, "javax.validation.constraints.Pattern")) {
                annotation.ifNormalAnnotationExpr(nae -> nae.getPairs().forEach(pair -> {
                    if (nameOf(pair, "regexp")) {
                        result.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.REGEX.getValue())
                                .setNote(pair.getValue().asStringLiteralExpr().asString()));
                    }
                }));
            }
        }
        return nullToEmpty(result);
    }

    private boolean nameIsValue(MemberValuePair pair) {
        return nameOf(pair, "value");
    }

    private boolean nameOf(MemberValuePair pair, String value) {
        return pair.getNameAsString().equals(value);
    }

    private Collection<ValidatorDto> nullToEmpty(Collection<ValidatorDto> dtos) {
        dtos.forEach(one -> {
            if (one.getNote() == null) {
                one.setNote("");
            }
        });
        return dtos;
    }

}
