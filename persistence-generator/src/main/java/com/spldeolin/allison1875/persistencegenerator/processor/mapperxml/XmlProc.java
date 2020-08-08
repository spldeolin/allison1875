package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;

/**
 * @author Deolin 2020-08-08
 */
public abstract class XmlProc {

    public abstract Collection<String> getSourceCodeLines();

    protected String getParameterType(PropertyDto propertyDto) {
        return propertyDto.getJavaType().getName().replaceFirst("java\\.lang\\.", "");
    }

}
