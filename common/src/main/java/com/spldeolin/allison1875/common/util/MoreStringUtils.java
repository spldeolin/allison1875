package com.spldeolin.allison1875.common.util;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.constant.BaseConstant;

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
        return Lists.newArrayList(string.split(BaseConstant.NEW_LINE_FOR_MATCHING));
    }

    /**
     * 将<code>from</code>中最后一次出现的<code>target</code>替换成<code>replacement</code>，如果<code>from</code>不包含<code>target
     * </code>，则无事发生
     */
    public static String replaceLast(String from, String target, String replacement) {
        int lastIndex = from.lastIndexOf(target);
        if (lastIndex != -1) {
            return from.substring(0, lastIndex) + replacement + from.substring(lastIndex + target.length());
        } else {
            return from;
        }
    }

    public static boolean isFirstLetterUpperCase(String s) {
        return Character.isUpperCase(s.charAt(0));
    }

    public static String upperFirstLetter(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static boolean isFirstLetterLowerCase(String s) {
        return Character.isLowerCase(s.charAt(0));
    }

    /**
     * 任何分隔形式转化为UpperCamel形式
     */
    public static String toUpperCamel(String string) {
        String lowerCamel = toLowerCamel(string);
        String upperCamel = lowerCamel.substring(0, 1).toUpperCase() + lowerCamel.substring(1);
        return upperCamel;
    }

    /**
     * 任何分隔形式转化为lowerCamel形式
     *
     * @param string 参数字符串中只支持含有数字+字母字符
     */
    public static String toLowerCamel(String string) {
        if (string == null || string.trim().isEmpty()) {
            return string;
        }
        StringBuilder sb = new StringBuilder(64);
        boolean firstNotEmptyPart = true;
        for (String part : string.split("[^a-zA-Z0-9]+")) {
            if ("".equals(part)) {
                // e.g.: /user/create
                continue;
            }
            if (part.toUpperCase().equals(part)) {
                // e.g.: AAAA_BBB
                part = part.toLowerCase();
            }
            if (firstNotEmptyPart) {
                // e.g.: AaaaBbb
                part = part.substring(0, 1).toLowerCase() + part.substring(1);
                firstNotEmptyPart = false;
            } else {
                part = part.substring(0, 1).toUpperCase() + part.substring(1);
            }
            sb.append(part);
        }
        return sb.toString();
    }

    public static String compressConsecutiveSpaces(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("\\s+", " ");
    }

    public static String removeNewLine(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll(BaseConstant.NEW_LINE_FOR_MATCHING, " ");
    }

    public static String splitAndGetLastPart(String text, String separator) {
        String[] parts = StringUtils.split(text, separator);
        return parts[parts.length - 1];
    }

}
