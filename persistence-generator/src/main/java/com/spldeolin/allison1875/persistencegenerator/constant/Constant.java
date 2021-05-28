package com.spldeolin.allison1875.persistencegenerator.constant;

import com.spldeolin.allison1875.base.constant.BaseConstant;

/**
 * @author Deolin 2020-07-12
 */
public interface Constant {

    String PROHIBIT_MODIFICATION_XML_BEGIN =
            "<!-- <${leftAnchor}> 从本行到锚点<${rightAnchor}>之间的内容，" + BaseConstant.BY_ALLISON_1875 + " -->";

    String PROHIBIT_MODIFICATION_XML_END =
            "<!-- 从锚点<${leftAnchor}>到本行之间的内容，" + BaseConstant.BY_ALLISON_1875 + " <${rightAnchor}> -->";

    String PROHIBIT_MODIFICATION_JAVADOC = "\r\n" + "\r\n" + "<strong>该方法" + BaseConstant.BY_ALLISON_1875 + "</strong>";

}