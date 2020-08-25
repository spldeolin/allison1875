package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;
import lombok.Getter;

/**
 * ResultMap
 *
 * @author Deolin 2020-07-19
 */
public class ResultMapXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final String entityName;

    @Getter
    private Collection<String> sourceCodeLines;

    public ResultMapXmlProc(PersistenceDto persistence, String entityName) {
        this.persistence = persistence;
        this.entityName = entityName;
    }

    public ResultMapXmlProc process() {
        Element resultMapTag = new DefaultElement("resultMap");
        resultMapTag.addAttribute("id", "all");
        resultMapTag.addAttribute("type", entityName);
        for (PropertyDto pk : persistence.getIdProperties()) {
            Element resultTag = resultMapTag.addElement("id");
            resultTag.addAttribute("column", pk.getColumnName());
            resultTag.addAttribute("property", pk.getPropertyName());
        }
        for (PropertyDto nonPk : persistence.getNonIdProperties()) {
            Element resultTag = resultMapTag.addElement("result");
            resultTag.addAttribute("column", nonPk.getColumnName());
            resultTag.addAttribute("property", nonPk.getPropertyName());
        }
        sourceCodeLines = Dom4jUtils.toSourceCodeLines(resultMapTag);
        return this;
    }

}