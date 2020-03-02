package com.spldeolin.allison1875.base.util.ast;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;

/**
 * @author Deolin 2020-01-26
 */
public class ResolvedTypes {

    private ResolvedTypes() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * <pre>
     *     e.g.1: List_String> isOrLike java.util.Collection
     *     e.g.2: List_String> isOrLike java.util.List
     * </pre>
     */
    public static boolean isOrLike(ResolvedType resolvedType, String... typeQualifier) {
        if (typeQualifier == null) {
            throw new IllegalArgumentException("typeQualifier must not be null.");
        }
        if (!resolvedType.isReferenceType()) {
            return false;
        }
        ResolvedReferenceType referenceType = resolvedType.asReferenceType();
        if (StringUtils.equalsAny(referenceType.getId(), typeQualifier)) {
            return true;
        }
        return referenceType.getAllAncestors().stream()
                .anyMatch(ancestor -> StringUtils.equalsAny(ancestor.getId(), typeQualifier));
    }

}
