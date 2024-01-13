package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.List;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;
import org.springframework.core.annotation.AnnotatedElementUtils;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.docanalyzer.enums.ValidatorTypeEnum;
import com.spldeolin.allison1875.docanalyzer.javabean.ValidatorDto;
import com.spldeolin.allison1875.docanalyzer.service.AnalyzeCustomValidationService;
import com.spldeolin.allison1875.docanalyzer.service.ValidService;

/**
 * 校验项
 *
 * @author Deolin 2019-12-09
 */
@Singleton
public class ValidServiceImpl implements ValidService {

    @Inject
    private AnalyzeCustomValidationService analyzeCustomValidationService;

    @Override
    public List<ValidatorDto> analyzeValid(AnnotatedElement annotatedElement) {
        List<ValidatorDto> valids = Lists.newArrayList();
        NotNull notNull = find(annotatedElement, NotNull.class);
        if (notNull != null) {
            valids.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.NOT_NULL.getValue()));
        }

        if (find(annotatedElement, javax.validation.constraints.NotEmpty.class) != null
                || find(annotatedElement, org.hibernate.validator.constraints.NotEmpty.class) != null) {
            valids.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.NOT_EMPTY.getValue()));
        }

        if (find(annotatedElement, javax.validation.constraints.NotBlank.class) != null
                || find(annotatedElement, org.hibernate.validator.constraints.NotBlank.class) != null) {
            valids.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.NOT_BLANK.getValue()));
        }

        Size size = find(annotatedElement, Size.class);
        if (size != null) {
            valids.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.MIN_SIZE.getValue())
                    .setNote(String.valueOf(size.min())));
            valids.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.MAX_SIZE.getValue())
                    .setNote(String.valueOf(size.max())));
        }

        Length length = find(annotatedElement, Length.class);
        if (length != null) {
            valids.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.MIN_SIZE.getValue())
                    .setNote(String.valueOf(length.min())));
            valids.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.MAX_SIZE.getValue())
                    .setNote(String.valueOf(length.max())));
        }

        Min min = find(annotatedElement, Min.class);
        if (min != null) {
            valids.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.MIN_NUMBER.getValue())
                    .setNote(String.valueOf(min.value())));
        }

        DecimalMin decimalMin = find(annotatedElement, DecimalMin.class);
        if (decimalMin != null) {
            valids.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.MIN_NUMBER.getValue())
                    .setNote(decimalMin.value()));
        }

        Max max = find(annotatedElement, Max.class);
        if (max != null) {
            valids.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.MAX_NUMBER.getValue())
                    .setNote(String.valueOf(max.value())));
        }

        DecimalMax decimalMax = find(annotatedElement, DecimalMax.class);
        if (decimalMax != null) {
            valids.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.MAX_NUMBER.getValue())
                    .setNote(decimalMax.value()));
        }

        Future future = find(annotatedElement, Future.class);
        if (future != null) {
            valids.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.FUTURE.getValue()));
        }

        FutureOrPresent futureOrPresent = find(annotatedElement, FutureOrPresent.class);
        if (futureOrPresent != null) {
            valids.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.FUTURE_OR_PRESENT.getValue()));
        }

        Past past = find(annotatedElement, Past.class);
        if (past != null) {
            valids.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.PAST.getValue()));
        }

        PastOrPresent pastOrPresent = find(annotatedElement, PastOrPresent.class);
        if (pastOrPresent != null) {
            valids.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.PAST_OR_PRESENT.getValue()));
        }

        Digits digits = find(annotatedElement, Digits.class);
        if (digits != null) {
            valids.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.MAX_INTEGRAL_DIGITS.getValue())
                    .setNote(String.valueOf(digits.integer())));
            valids.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.MAX_FRACTIONAL_DIGITS.getValue())
                    .setNote(String.valueOf(digits.fraction())));
        }

        Positive positive = find(annotatedElement, Positive.class);
        if (positive != null) {
            valids.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.POSITIVE.getValue()));
        }

        Negative negative = find(annotatedElement, Negative.class);
        if (negative != null) {
            valids.add(new ValidatorDto().setValidatorType(ValidatorTypeEnum.NEGATIVE.getValue()));
        }

        Pattern pattern = find(annotatedElement, Pattern.class);
        if (pattern != null) {
            valids.add(
                    new ValidatorDto().setValidatorType(ValidatorTypeEnum.REGEX.getValue()).setNote(pattern.regexp()));
        }

        valids.addAll(analyzeCustomValidationService.analyzeCustomValidation(annotatedElement));
        nullToEmpty(valids);
        return valids;
    }

    private <A extends Annotation> A find(AnnotatedElement field, Class<A> annotationType) {
        return AnnotatedElementUtils.findMergedAnnotation(field, annotationType);
    }

    private void nullToEmpty(Collection<ValidatorDto> dtos) {
        dtos.forEach(one -> {
            if (one.getNote() == null) {
                one.setNote("");
            }
        });
    }

}
