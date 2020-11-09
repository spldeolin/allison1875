package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;

/**
 * ResultMap
 *
 * @author Deolin 2020-07-19
 */
public class ResultMapXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final String entityName;

    private Collection<String> sourceCodeLines;

    public ResultMapXmlProc(PersistenceDto persistence, String entityName) {
        this.persistence = persistence;
        this.entityName = entityName;
    }

    public ResultMapXmlProc process() {
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<resultMap id=\"all\" type=\"%s\">", entityName));
        for (PropertyDto id : persistence.getIdProperties()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + String
                    .format("<id column=\"%s\" property=\"%s\"/>", id.getColumnName(), id.getPropertyName()));
        }
        for (PropertyDto nonId : persistence.getNonIdProperties()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + String
                    .format(" <result column=\"%s\" property=\"%s\"/>", nonId.getColumnName(),
                            nonId.getPropertyName()));
        }
        xmlLines.add("</resultMap>");
        sourceCodeLines = xmlLines;
        sourceCodeLines.add("");
        return this;
    }

    public Collection<String> getSourceCodeLines() {
        return this.sourceCodeLines;
    }

}