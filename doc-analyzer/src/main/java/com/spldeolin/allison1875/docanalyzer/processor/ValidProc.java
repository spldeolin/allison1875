package com.spldeolin.allison1875.docanalyzer.processor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import javax.validation.constraints.AssertTrue;
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
import com.spldeolin.allison1875.docanalyzer.dto.ValidatorDto;
import com.spldeolin.allison1875.docanalyzer.enums.ValidTypeEnum;
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
public class ValidProc {

    private final AnalyzeCustomValidationStrategy analyzeCustomValidationStrategy;

    public ValidProc(AnalyzeCustomValidationStrategy analyzeCustomValidationStrategy) {
        this.analyzeCustomValidationStrategy = analyzeCustomValidationStrategy;
    }

    public Collection<ValidatorDto> process(AnnotatedElement annotatedElement) {
        Collection<ValidatorDto> result = Lists.newArrayList();
        NotNull notNull = find(annotatedElement, NotNull.class);
        if (notNull != null) {
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.NOT_NULL.getValue()));
        }

        if (find(annotatedElement, javax.validation.constraints.NotEmpty.class) != null
                || find(annotatedElement, org.hibernate.validator.constraints.NotEmpty.class) != null) {
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.NOT_EMPTY.getValue()));
        }

        if (find(annotatedElement, javax.validation.constraints.NotBlank.class) != null
                || find(annotatedElement, org.hibernate.validator.constraints.NotBlank.class) != null) {
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.NOT_BLANK.getValue()));
        }

        Size size = find(annotatedElement, Size.class);
        if (size != null) {
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.MIN_SIZE.getValue())
                    .setNote(String.valueOf(size.min())));
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.MAX_SIZE.getValue())
                    .setNote(String.valueOf(size.max())));
        }

        Length length = find(annotatedElement, Length.class);
        if (length != null) {
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.MIN_SIZE.getValue())
                    .setNote(String.valueOf(length.min())));
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.MAX_SIZE.getValue())
                    .setNote(String.valueOf(length.max())));
        }

        Min min = find(annotatedElement, Min.class);
        if (min != null) {
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.MIN_NUMBER.getValue())
                    .setNote(String.valueOf(min.value())));
        }

        DecimalMin decimalMin = find(annotatedElement, DecimalMin.class);
        if (decimalMin != null) {
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.MIN_NUMBER.getValue())
                    .setNote(decimalMin.value()));
        }

        Max max = find(annotatedElement, Max.class);
        if (max != null) {
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.MAX_NUMBER.getValue())
                    .setNote(String.valueOf(max.value())));
        }

        DecimalMax decimalMax = find(annotatedElement, DecimalMax.class);
        if (decimalMin != null) {
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.MAX_NUMBER.getValue())
                    .setNote(decimalMax.value()));
        }

        Future future = find(annotatedElement, Future.class);
        if (future != null) {
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.FUTURE.getValue()));
        }

        FutureOrPresent futureOrPresent = find(annotatedElement, FutureOrPresent.class);
        if (futureOrPresent != null) {
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.FUTURE_OR_PRESENT.getValue()));
        }

        Past past = find(annotatedElement, Past.class);
        if (past != null) {
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.PAST.getValue()));
        }

        PastOrPresent pastOrPresent = find(annotatedElement, PastOrPresent.class);
        if (pastOrPresent != null) {
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.PAST_OR_PRESENT.getValue()));
        }

        Digits digits = find(annotatedElement, Digits.class);
        if (digits != null) {
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.MAX_INTEGRAL_DIGITS.getValue())
                    .setNote(String.valueOf(digits.integer())));
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.MAX_FRACTIONAL_DIGITS.getValue())
                    .setNote(String.valueOf(digits.fraction())));
        }

        Positive positive = find(annotatedElement, Positive.class);
        if (positive != null) {
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.POSITIVE.getValue()));
        }

        Negative negative = find(annotatedElement, Negative.class);
        if (negative != null) {
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.NEGATIVE.getValue()));
        }

        Pattern pattern = find(annotatedElement, Pattern.class);
        if (pattern != null) {
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.REGEX.getValue()).setNote(pattern.regexp()));
        }

        AssertTrue assertTrue = find(annotatedElement, AssertTrue.class);
        if (assertTrue != null) {
            result.add(new ValidatorDto().setValidatorType(ValidTypeEnum.FIELD_CROSSING.getValue())
                    .setNote(assertTrue.message()));
        }

        result.addAll(analyzeCustomValidationStrategy.analyzeCustomValidation(annotatedElement));
        return nullToEmpty(result);
    }

    private <A extends Annotation> A find(AnnotatedElement field, Class<A> annotationType) {
        return AnnotatedElementUtils.findMergedAnnotation(field, annotationType);
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
