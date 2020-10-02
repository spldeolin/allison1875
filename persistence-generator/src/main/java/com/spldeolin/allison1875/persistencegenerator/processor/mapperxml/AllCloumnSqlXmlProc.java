package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.stream.Collectors;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;

/**
 * <sql id="all"></sql> 标签
 *
 * @author Deolin 2020-07-19
 */
public class AllCloumnSqlXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private Collection<String> sourceCodeLines;

    public AllCloumnSqlXmlProc(PersistenceDto persistence) {
        this.persistence = persistence;
    }

    public AllCloumnSqlXmlProc process() {
        Element sqlTag = new DefaultElement("sql");
        sqlTag.addAttribute("id", "all");
        newLineWithIndent(sqlTag);
        sqlTag.addText(
                persistence.getProperties().stream().map(PropertyDto::getColumnName).collect(Collectors.joining(", ")));
        sqlTag.addText(BaseConstant.NEW_LINE);
        sourceCodeLines = Dom4jUtils.toSourceCodeLines(sqlTag);
        return this;
    }

    public Collection<String> getSourceCodeLines() {
        return this.sourceCodeLines;
    }

}