package com.spldeolin.allison1875.persistencegenerator.facade.javabean;

import java.util.Map;
import lombok.Data;

/**
 * @author Deolin 2020-10-06
 */
@Data
public class DesignMeta {

    private String entityQualifier;

    private String entityName;

    private String mapperQualifier;

    private String mapperName;

    private String mapperRelativePath;

    private Map<String, PropertyDto> properties;

    private String tableName;

}