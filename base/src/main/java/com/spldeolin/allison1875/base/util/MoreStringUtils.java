package com.spldeolin.allison1875.base.util;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import jodd.util.StringUtil;

/**
 * @author Deolin 2019-12-03
 */
public class MoreStringUtils {

    private MoreStringUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static List<String> splitLineByLine(String string) {
        if (string == null) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(string.split("\\r?\\n"));
    }

    /**
     * 将<code>from</code>中最后一次出现的<code>target</code>替换成<code>replacement</code>，如果<code>from</code>不包含<code>target
     * </code>，则无事发生
     */
    public static String replaceLast(String from, String target, String replacement) {
        return StringUtil.replaceLast(from, target, replacement);
    }

    public static String upperFirstLetter(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String lowerFirstLetter(String s) {
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }

    public static String underscoreToUpperCamel(String string) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, string);
    }

    public static String underscoreToLowerCamel(String string) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, string);
    }

    public static String lowerCamelToUnderscore(String string) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, string);
    }

    public static String upperCamelToLowerCamel(String string) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, string);
    }

    public static String slashToLowerCamel(String string) {
        StringBuilder sb = new StringBuilder(64);
        for (String part : string.split("/")) {
            sb.append(StringUtils.capitalize(part));
        }
        return lowerFirstLetter(sb.toString());
    }

    public static boolean endsWithIgnoreCase(String string, String suffix) {
        return string.toUpperCase().endsWith(suffix.toUpperCase());
    }

}
