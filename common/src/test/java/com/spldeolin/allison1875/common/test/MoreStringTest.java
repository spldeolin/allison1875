package com.spldeolin.allison1875.common.test;

import com.spldeolin.allison1875.common.util.MoreStringUtils;

/**
 * @author Deolin 2024-01-16
 */
public class MoreStringTest {

    public static void main(String[] args) {
        final String lf = "\n";
        final String cr = "\r";
        final String crlf = "\r\n";
        String demo = "a" + lf + "bb" + cr + "ccc" + crlf + "dddd";
        System.out.println(demo);
        System.out.println(MoreStringUtils.splitLineByLine(demo));
    }

}