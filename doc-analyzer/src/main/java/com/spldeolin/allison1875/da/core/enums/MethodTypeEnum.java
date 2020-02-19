package com.spldeolin.allison1875.da.core.enums;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2019-12-28
 */
@AllArgsConstructor
@Getter
public enum MethodTypeEnum {

    DELETE("DELETE", QualifierConstants.DELETE_MAPPING, "RequestMethod.DELETE"),

    GET("GET", QualifierConstants.GET_MAPPING, "RequestMethod.GET"),

    PATCH("PATCH", QualifierConstants.PATCH_MAPPING, "RequestMethod.PATCH"),

    POST("POST", QualifierConstants.POST_MAPPING, "RequestMethod.POST"),

    PUT("PUT", QualifierConstants.PUT_MAPPING, "RequestMethod.PUT");


    private String value;

    private String annotationQualifier;

    private String fieldAccessExpr;

    public Collection<MethodTypeEnum> inCollection() {
        return Lists.newArrayList(this);
    }

    public static Optional<MethodTypeEnum> ofValue(String value) {
        return Arrays.stream(values()).filter(one -> one.getValue().equals(value)).findFirst();
    }

    public static Optional<MethodTypeEnum> ofAnnotationQualifier(String annotationQualifier) {
        return Arrays.stream(values()).filter(one -> one.getAnnotationQualifier().equals(annotationQualifier))
                .findFirst();
    }

    public static Optional<MethodTypeEnum> ofFieldAccessExpr(String fieldAccessExpr) {
        return Arrays.stream(values()).filter(one -> one.getFieldAccessExpr().equals(fieldAccessExpr)).findFirst();
    }

}
