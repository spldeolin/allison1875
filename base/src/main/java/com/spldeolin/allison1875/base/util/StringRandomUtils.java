package com.spldeolin.allison1875.base.util;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * 随机字符串
 *
 * @author Deolin
 */
public class StringRandomUtils {

    private static final char[] LOW_EN_NUM = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9'};

    private static final char[] EASY_READ_AND_SPEAK = new char[]{'3', '4', '5', '7', '8', 'a', 'c', 'f', 'g', 'h', 'j',
            'k', 'p', 's', 't', 'u', 'v', 'w', 'x', 'y'};

    private StringRandomUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * 随机选取可见ASCII字符，生成字符串
     *
     * @param stringLength 字符串长度
     * @return 随机字符串
     */
    public static String generateVisibleAscii(int stringLength) {
        return RandomStringUtils.randomAscii(stringLength);
    }

    /**
     * 随机选取大小写英文数字，生成字符串
     *
     * @param stringLength 字符串长度
     * @return 随机字符串
     */
    public static String generateEnNum(int stringLength) {
        return RandomStringUtils.random(stringLength, true, true);
    }

    /**
     * 随机选取小写英文数字，生成字符串
     *
     * @param stringLength 字符串长度
     * @return 随机字符串
     */
    public static String generateLowEnNum(int stringLength) {
        return RandomStringUtils.random(stringLength, LOW_EN_NUM);
    }

    /**
     * 随机选取数字，生成字符串
     *
     * @param stringLength 字符串长度
     * @return 随机字符串
     */
    public static String generateNum(int stringLength) {
        return RandomStringUtils.randomNumeric(stringLength);
    }

    /**
     * 随机选取除了o、0、l、i、1、q、9、z、2、b、6、m、n、b、d、r、e的小写英文字母和数字，生成易区分和易读的字符串
     *
     * @param stringLength 字符串长度
     * @return 随机字符串
     */
    public static String generateEasyReadAndSpeakChar(int stringLength) {
        return RandomStringUtils.random(stringLength, EASY_READ_AND_SPEAK);
    }

}
