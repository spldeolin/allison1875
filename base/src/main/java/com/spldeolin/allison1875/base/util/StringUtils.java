package com.spldeolin.allison1875.base.util;

import java.util.List;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.google.common.collect.Lists;

/**
 * @author Deolin 2019-12-03
 */
public class StringUtils {

    private static final Converter<String, String> lowerUnderscore2UpperCamel = CaseFormat.LOWER_UNDERSCORE
            .converterTo(CaseFormat.UPPER_CAMEL);

    private static final Converter<String, String> lowerUnderscore2LowerCamel = CaseFormat.LOWER_UNDERSCORE
            .converterTo(CaseFormat.LOWER_CAMEL);

    private StringUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * @see org.apache.commons.lang3.StringUtils#isEmpty(java.lang.CharSequence)
     */
    public static boolean isEmpty(CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isEmpty(cs);
    }

    /**
     * @see org.apache.commons.lang3.StringUtils#isNotBlank(java.lang.CharSequence)
     */
    public static boolean isNotBlank(CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(cs);
    }

    /**
     * @see org.apache.commons.lang3.StringUtils#equalsAny(java.lang.CharSequence, java.lang.CharSequence...)
     */
    public static boolean equalsAny(final CharSequence string, final CharSequence... searchStrings) {
        return org.apache.commons.lang3.StringUtils.equalsAny(string, searchStrings);
    }

    /**
     * @see org.apache.commons.lang3.StringUtils#isBlank(java.lang.CharSequence)
     */
    public static boolean isBlank(CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isBlank(cs);
    }

    /**
     * @see org.apache.commons.lang3.StringUtils#containsAny(java.lang.CharSequence, java.lang.CharSequence...)
     */
    public static boolean containsAny(CharSequence cs, CharSequence... searchCharSequences) {
        return org.apache.commons.lang3.StringUtils.containsAny(cs, searchCharSequences);
    }

    /**
     * @see org.apache.commons.lang3.StringUtils#lowerCase(java.lang.String)
     */
    public static String lowerCase(String str) {
        return org.apache.commons.lang3.StringUtils.lowerCase(str);
    }

    public static List<String> splitLineByLine(String string) {
        if (string == null) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(string.split("\\r?\\n"));
    }

    public static String replaceLast(String string, String target, String replacement) {
        int ind = string.lastIndexOf(target);
        return string.substring(0, ind) + replacement + string.substring(ind + 1);
    }

    public static String capture(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static String upperFirstLetter(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String lowerFirstLetter(String s) {
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }

    public static String removeFirstLetterAndTrim(String s) {
        if (org.apache.commons.lang3.StringUtils.isBlank(s)) {
            return s;
        }
        return s.substring(1).trim();
    }

    public static String limitLength(CharSequence s, int limit) {
        if (s == null) {
            return null;
        }
        if (s.length() <= limit) {
            return s.toString();
        }
        return s.toString().substring(0, limit);
    }

    public static String underscoreToUpperCamel(String string) {
        return lowerUnderscore2UpperCamel.convert(string);
    }

    public static String underscoreToLowerCamel(String string) {
        return lowerUnderscore2LowerCamel.convert(string);
    }

}
