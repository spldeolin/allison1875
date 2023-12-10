package com.spldeolin.allison1875.persistencegenerator.facade.javabean;

import java.util.List;
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

    private List<String> mapperRelativePaths;

    private Map<String, PropertyDto> properties;

    private String tableName;

    /**
     * 如果有逻辑删除，怎么样算作“数据未被删”
     * 如果properties中有逻辑删除标识，则值来自PersistenceGeneratorConfig#notDeletedSql
     */
    private String notDeletedSql;

}