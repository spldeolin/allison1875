package com.spldeolin.allison1875.base.constant;

import java.util.regex.Pattern;

/**
 * 正则表达式Pattern对象 常量一览
 *
 * @author Deolin 2021-09-04
 */
public interface RegexPatternConstants {

    Pattern LOT_NO_PATTERN = Pattern.compile("Allison 1875 Lot No: ((QT|HT|PG|DA)\\d{4}(S|N)-[A-Z0-9]{8})");

}
