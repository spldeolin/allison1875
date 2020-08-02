package com.spldeolin.allison1875.persistencegenerator.constant;

import com.google.common.base.Strings;

/**
 * @author Deolin 2020-07-12
 */
public class Constant {

    public static final String PROHIBIT_MODIFICATION_XML = " 该标签由Allison1875生成，请勿人为修改 ";

    public static final String PROHIBIT_MODIFICATION_JAVADOC =
            "\r\n" + "\r\n" + "<strong>该方法由Allison1875生成，请勿人为修改</strong>";

    public static final String singleIndent = "    ";

    public static final String doubleIndex = Strings.repeat(singleIndent, 2);

    public static final String trebleIndex = Strings.repeat(singleIndent, 3);

    public static final String newLine = "\r\n";

}