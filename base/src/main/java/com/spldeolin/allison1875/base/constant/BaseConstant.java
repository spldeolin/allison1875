package com.spldeolin.allison1875.base.constant;

import com.google.common.base.Strings;

/**
 * @author Deolin 2020-08-08
 */
public interface BaseConstant {

    String BY_ALLISON_1875 = "由Allison 1875生成，请勿人为修改";

    String SINGLE_INDENT = "    ";

    String DOUBLE_INDENT = Strings.repeat(SINGLE_INDENT, 2);

    String TREBLE_INDENT = Strings.repeat(SINGLE_INDENT, 3);

    String NEW_LINE = "\r\n";

    String FORMATTER_OFF_MARKER = "<!-- @formatter:off -->";

    String FORMATTER_ON_MARKER = "<!-- @formatter:on -->";

}
