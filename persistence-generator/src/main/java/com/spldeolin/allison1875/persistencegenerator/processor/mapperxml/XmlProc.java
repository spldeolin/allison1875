package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import org.dom4j.Element;
import com.spldeolin.allison1875.base.constant.BaseConstant;

/**
 * @author Deolin 2020-08-08
 */
public abstract class XmlProc {

    public abstract Collection<String> getSourceCodeLines();

    protected void newLineWithIndent(Element stmt) {
        stmt.addText(BaseConstant.NEW_LINE).addText(BaseConstant.SINGLE_INDENT);
    }

}
