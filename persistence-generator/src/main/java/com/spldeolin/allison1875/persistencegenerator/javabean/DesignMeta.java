package com.spldeolin.allison1875.persistencegenerator.javabean;

import java.util.Collection;

/**
 * @author Deolin 2020-10-06
 */
public class DesignMeta {

    private String entityQualifier;

    private String entityName;

    private String mapperQualifier;

    private String mapperName;

    private String mapperRelativePath;

    private Collection<String> propertyNames;

    private String tableName;

    public String getEntityQualifier() {
        return entityQualifier;
    }

    public void setEntityQualifier(String entityQualifier) {
        this.entityQualifier = entityQualifier;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getMapperQualifier() {
        return mapperQualifier;
    }

    public void setMapperQualifier(String mapperQualifier) {
        this.mapperQualifier = mapperQualifier;
    }

    public String getMapperName() {
        return mapperName;
    }

    public void setMapperName(String mapperName) {
        this.mapperName = mapperName;
    }

    public String getMapperRelativePath() {
        return mapperRelativePath;
    }

    public void setMapperRelativePath(String mapperRelativePath) {
        this.mapperRelativePath = mapperRelativePath;
    }

    public Collection<String> getPropertyNames() {
        return propertyNames;
    }

    public void setPropertyNames(Collection<String> propertyNames) {
        this.propertyNames = propertyNames;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

}