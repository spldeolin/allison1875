package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;

/**
 * <sql id="all"></sql> 标签
 *
 * @author Deolin 2020-07-19
 */
@Singleton
public class AllCloumnSqlXmlProc {

    public Collection<String> process(PersistenceDto persistence) {
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add("<sql id=\"all\">");
        xmlLines.add(BaseConstant.SINGLE_INDENT + persistence.getProperties().stream().map(PropertyDto::getColumnName)
                .collect(Collectors.joining(", ")));
        xmlLines.add("</sql>");
        xmlLines.add("");
        return xmlLines;
    }

}