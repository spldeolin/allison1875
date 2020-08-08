package com.spldeolin.allison1875.pg.constant;

import com.google.common.base.Strings;
import com.spldeolin.allison1875.base.constant.BaseConstant;

/**
 * @author Deolin 2020-07-12
 */
public interface Constant {

    String PROHIBIT_MODIFICATION_XML_BEGIN = "<!-- <%s> 从本行到锚点<%s>之间的内容，" + BaseConstant.BY_ALLISON_1875 + " -->";

    String PROHIBIT_MODIFICATION_XML_END = "<!-- 从锚点<%s>到本行之间的内容，" + BaseConstant.BY_ALLISON_1875 + " <%s> -->";

    String PROHIBIT_MODIFICATION_JAVADOC = "\r\n" + "\r\n" + "<strong>该方法" + BaseConstant.BY_ALLISON_1875 + "</strong>";

    String singleIndent = "    ";

    String doubleIndex = Strings.repeat(singleIndent, 2);

    String trebleIndex = Strings.repeat(singleIndent, 3);

    String newLine = "\r\n";

}