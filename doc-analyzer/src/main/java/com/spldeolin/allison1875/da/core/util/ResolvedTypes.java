package com.spldeolin.allison1875.da.core.util;

import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;

/**
 * @author Deolin 2020-01-26
 */
public class ResolvedTypes {

    public static boolean isOrLike(ResolvedType resolvedType, String typeQualifier) {
        if (typeQualifier == null) {
            throw new IllegalArgumentException("typeQualifier must not be null.");
        }
        if (!resolvedType.isReferenceType()) {
            return false;
        }
        ResolvedReferenceType referenceType = resolvedType.asReferenceType();
        if (typeQualifier.equals(referenceType.getId())) {
            return true;
        }
        return referenceType.getAllAncestors().stream().anyMatch(ancestor -> typeQualifier.equals(ancestor.getId()));
    }

}
