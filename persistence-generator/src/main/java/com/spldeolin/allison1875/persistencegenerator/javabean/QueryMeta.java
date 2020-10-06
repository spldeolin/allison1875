package com.spldeolin.allison1875.persistencegenerator.javabean;

/**
 * @author Deolin 2020-10-06
 */
public class QueryMeta {

    private String entityQualifier;

    private String entityName;

    private String mapperQualifier;

    private String mapperName;

    private String mapperRelativePath;

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

}