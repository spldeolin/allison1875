package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.List;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;

/**
 * ResultMap
 *
 * @author Deolin 2020-07-19
 */
@Singleton
public class ResultMapXmlProc {

    public Collection<String> process(PersistenceDto persistence, String entityName) {
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<resultMap id=\"all\" type=\"%s\">", entityName));
        for (PropertyDto id : persistence.getIdProperties()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + String.format("<id column=\"%s\" property=\"%s\"/>",
                    id.getColumnName(), id.getPropertyName()));
        }
        for (PropertyDto nonId : persistence.getNonIdProperties()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + String.format("<result column=\"%s\" property=\"%s\"/>",
                    nonId.getColumnName(), nonId.getPropertyName()));
        }
        xmlLines.add("</resultMap>");
        xmlLines.add("");
        return xmlLines;
    }

}