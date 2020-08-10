package com.spldeolin.allison1875.pg.processor.mapperxml;

import java.util.Collection;
import org.dom4j.Element;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.pg.javabean.PropertyDto;

/**
 * @author Deolin 2020-08-08
 */
public abstract class XmlProc {

    public abstract Collection<String> getSourceCodeLines();

    protected void addParameterType(Element stmt, PropertyDto propertyDto) {
        stmt.addAttribute("parameterType", propertyDto.getJavaType().getName().replaceFirst("java\\.lang\\.", ""));
    }

    protected void newLineWithIndent(Element stmt) {
        stmt.addText(BaseConstant.NEW_LINE).addText(BaseConstant.SINGLE_INDENT);
    }

}
