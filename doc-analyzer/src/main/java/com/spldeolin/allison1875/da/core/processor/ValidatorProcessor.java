package com.spldeolin.allison1875.da.core.processor;

import static com.spldeolin.allison1875.da.core.enums.ValidatorTypeEnum.enumValue;
import static com.spldeolin.allison1875.da.core.enums.ValidatorTypeEnum.future;
import static com.spldeolin.allison1875.da.core.enums.ValidatorTypeEnum.maxFloat;
import static com.spldeolin.allison1875.da.core.enums.ValidatorTypeEnum.maxFractionalDigits;
import static com.spldeolin.allison1875.da.core.enums.ValidatorTypeEnum.maxInteger;
import static com.spldeolin.allison1875.da.core.enums.ValidatorTypeEnum.maxIntegralDigits;
import static com.spldeolin.allison1875.da.core.enums.ValidatorTypeEnum.maxSize;
import static com.spldeolin.allison1875.da.core.enums.ValidatorTypeEnum.minFloat;
import static com.spldeolin.allison1875.da.core.enums.ValidatorTypeEnum.minInteger;
import static com.spldeolin.allison1875.da.core.enums.ValidatorTypeEnum.minSize;
import static com.spldeolin.allison1875.da.core.enums.ValidatorTypeEnum.notBlank;
import static com.spldeolin.allison1875.da.core.enums.ValidatorTypeEnum.notEmpty;
import static com.spldeolin.allison1875.da.core.enums.ValidatorTypeEnum.past;
import static com.spldeolin.allison1875.da.core.enums.ValidatorTypeEnum.positive;
import static com.spldeolin.allison1875.da.core.enums.ValidatorTypeEnum.regex;

import java.util.Collection;
import java.util.NoSuchElementException;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.da.core.domain.ValidatorDomain;
import com.spldeolin.allison1875.da.core.util.Javadocs;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * 校验项
 *
 * @author Deolin 2019-12-09
 */
@Log4j2
@Accessors(fluent = true)
class ValidatorProcessor {

    @Setter
    private NodeWithAnnotations<?> nodeWithAnnotations;

    @Getter
    private Collection<ValidatorDomain> validators = Lists.newLinkedList();

    ValidatorProcessor process() {
        this.calcValidators(nodeWithAnnotations.getAnnotations());

        if (validators.removeIf(validator -> enumValue == validator.validatorType())) {
            String enumValueNote = this.calcEnumValueSpecially(nodeWithAnnotations).toString();
            validators.add(new ValidatorDomain().validatorType(enumValue).note(enumValueNote));
        }

        return this;
    }

    private StringBuilder calcEnumValueSpecially(NodeWithAnnotations<?> node) {
        StringBuilder result = new StringBuilder(64);
        node.getAnnotationByName("ValidEnumValue").ifPresent(anno -> {
            result.append("（");
            anno.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                if (nameOf(pair, "enumType")) {
                    ResolvedType resolvedType = pair.getValue().calculateResolvedType();
                    String qualifier = resolvedType.asReferenceType().getTypeParametersMap().get(0).b.describe();
                    EnumDeclaration enumDeclaration = StaticAstContainer.getEnumDeclaration(qualifier);
//                            EnumContainer.getInstance().getByQualifier().get(qualifier);

                    Collection<String> parts = Lists.newLinkedList();
                    try {
                        enumDeclaration.getEntries().forEach(entry -> {
                            if (entry.getArguments().size() > 0) {
                                // 约定第1个作为参数绑定的value
                                parts.add(entry.getArgument(0).toString());
                                parts.add(Javadocs.extractFirstLine(entry));
                            } else {
                                // 类似于 public enum Gender {male, female;}
                            }
                        });
                    } catch (NoSuchElementException e) {
                        log.warn("找不到enum[{}]", qualifier);
                        return;
                    }
                    Joiner.on("、").appendTo(result, parts);
                }
            });
        });
        return result;
    }

    private void calcValidators(NodeList<AnnotationExpr> annos) {
        annos.forEach(anno -> {
            switch (anno.getNameAsString()) {
                case "NotEmpty":
                    validators.add(new ValidatorDomain().validatorType(notEmpty));
                    break;
                case "NotBlank":
                    validators.add(new ValidatorDomain().validatorType(notBlank));
                    break;
                case "Size":
                case "Length":
                    anno.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                        ValidatorDomain validator = new ValidatorDomain().note(pair.getValue().toString());
                        if (nameOf(pair, "min")) {
                            validators.add(validator.validatorType(minSize));
                        }
                        if (nameOf(pair, "max")) {
                            validators.add(validator.validatorType(maxSize));
                        }
                    });
                    break;
                case "Max":
                    anno.ifSingleMemberAnnotationExpr(singleAnno -> validators
                            .add(new ValidatorDomain().validatorType(maxInteger)
                                    .note(singleAnno.getMemberValue().toString())));
                    anno.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> validators.add(new ValidatorDomain().validatorType(maxInteger)
                                            .note(pair.getValue().toString()))));
                    break;
                case "Min":
                    anno.ifSingleMemberAnnotationExpr(singleAnno -> validators
                            .add(new ValidatorDomain().validatorType(minInteger)
                                    .note(singleAnno.getMemberValue().toString())));
                    anno.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> validators.add(new ValidatorDomain().validatorType(minInteger)
                                            .note(pair.getValue().toString()))));
                    break;
                case "DecimalMax":
                    anno.ifSingleMemberAnnotationExpr(singleAnno -> validators
                            .add(new ValidatorDomain().validatorType(maxFloat)
                                    .note(singleAnno.getMemberValue().toString())));
                    anno.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> validators.add(new ValidatorDomain().validatorType(maxFloat)
                                            .note(pair.getValue().toString()))));
                    break;
                case "DecimalMin":
                    anno.ifSingleMemberAnnotationExpr(singleAnno -> validators
                            .add(new ValidatorDomain().validatorType(minFloat)
                                    .note(singleAnno.getMemberValue().toString())));
                    anno.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> validators.add(new ValidatorDomain().validatorType(minFloat)
                                            .note(pair.getValue().toString()))));
                    break;
                case "Future":
                    validators.add(new ValidatorDomain().validatorType(future));
                    break;
                case "Past":
                    validators.add(new ValidatorDomain().validatorType(past));
                    break;
                case "Digits":
                    anno.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                        ValidatorDomain validator = new ValidatorDomain().note(pair.getValue().toString());
                        if (nameOf(pair, "integer")) {
                            validators.add(validator.validatorType(maxIntegralDigits));
                        }
                        if (nameOf(pair, "fraction")) {
                            validators.add(validator.validatorType(maxFractionalDigits));
                        }
                    });
                    break;
                case "Positive":
                    validators.add(new ValidatorDomain().validatorType(positive));
                    break;
                case "Pattern":
                    anno.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                        if (nameOf(pair, "regexp")) {
                            validators.add(new ValidatorDomain().validatorType(regex)
                                    .note(pair.getValue().asStringLiteralExpr().asString()));
                        }
                    });
                    break;
                case "ValidEnumValue":
                    validators.add(new ValidatorDomain().validatorType(enumValue));
                    break;
            }
        });
    }

    private boolean nameIsValue(MemberValuePair pair) {
        return nameOf(pair, "value");
    }

    private boolean nameOf(MemberValuePair pair, String value) {
        return pair.getNameAsString().equals(value);
    }

}
