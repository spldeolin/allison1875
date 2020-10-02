package com.spldeolin.allison1875.persistencegenerator.javabean;

import java.util.Collection;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-07-12
 */
@Accessors(chain = true)
public class PersistenceDto {

    private String tableName;

    private String entityName;

    private String mapperName;

    private String descrption;

    /**
     * 主键字段（存在联合主键的可能）
     */
    private Collection<PropertyDto> idProperties;

    /**
     * 非主键字段
     */
    private Collection<PropertyDto> nonIdProperties;

    /**
     * 所有字段
     */
    private Collection<PropertyDto> properties;

    /**
     * 逻辑外键字段（id结尾的字段算做逻辑外键）
     */
    private Collection<PropertyDto> keyProperties;

    /**
     * 存在逻辑删除标识符
     */
    private Boolean isDeleteFlagExist = false;

    public PersistenceDto() {
    }

    public String getTableName() {
        return this.tableName;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public String getMapperName() {
        return this.mapperName;
    }

    public String getDescrption() {
        return this.descrption;
    }

    public Collection<PropertyDto> getIdProperties() {
        return this.idProperties;
    }

    public Collection<PropertyDto> getNonIdProperties() {
        return this.nonIdProperties;
    }

    public Collection<PropertyDto> getProperties() {
        return this.properties;
    }

    public Collection<PropertyDto> getKeyProperties() {
        return this.keyProperties;
    }

    public Boolean getIsDeleteFlagExist() {
        return this.isDeleteFlagExist;
    }

    public PersistenceDto setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public PersistenceDto setEntityName(String entityName) {
        this.entityName = entityName;
        return this;
    }

    public PersistenceDto setMapperName(String mapperName) {
        this.mapperName = mapperName;
        return this;
    }

    public PersistenceDto setDescrption(String descrption) {
        this.descrption = descrption;
        return this;
    }

    public PersistenceDto setIdProperties(Collection<PropertyDto> idProperties) {
        this.idProperties = idProperties;
        return this;
    }

    public PersistenceDto setNonIdProperties(Collection<PropertyDto> nonIdProperties) {
        this.nonIdProperties = nonIdProperties;
        return this;
    }

    public PersistenceDto setProperties(Collection<PropertyDto> properties) {
        this.properties = properties;
        return this;
    }

    public PersistenceDto setKeyProperties(Collection<PropertyDto> keyProperties) {
        this.keyProperties = keyProperties;
        return this;
    }

    public PersistenceDto setIsDeleteFlagExist(Boolean isDeleteFlagExist) {
        this.isDeleteFlagExist = isDeleteFlagExist;
        return this;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PersistenceDto)) {
            return false;
        }
        final PersistenceDto other = (PersistenceDto) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$tableName = this.getTableName();
        final Object other$tableName = other.getTableName();
        if (this$tableName == null ? other$tableName != null : !this$tableName.equals(other$tableName)) {
            return false;
        }
        final Object this$entityName = this.getEntityName();
        final Object other$entityName = other.getEntityName();
        if (this$entityName == null ? other$entityName != null : !this$entityName.equals(other$entityName)) {
            return false;
        }
        final Object this$mapperName = this.getMapperName();
        final Object other$mapperName = other.getMapperName();
        if (this$mapperName == null ? other$mapperName != null : !this$mapperName.equals(other$mapperName)) {
            return false;
        }
        final Object this$descrption = this.getDescrption();
        final Object other$descrption = other.getDescrption();
        if (this$descrption == null ? other$descrption != null : !this$descrption.equals(other$descrption)) {
            return false;
        }
        final Object this$idProperties = this.getIdProperties();
        final Object other$idProperties = other.getIdProperties();
        if (this$idProperties == null ? other$idProperties != null : !this$idProperties.equals(other$idProperties)) {
            return false;
        }
        final Object this$nonIdProperties = this.getNonIdProperties();
        final Object other$nonIdProperties = other.getNonIdProperties();
        if (this$nonIdProperties == null ? other$nonIdProperties != null
                : !this$nonIdProperties.equals(other$nonIdProperties)) {
            return false;
        }
        final Object this$properties = this.getProperties();
        final Object other$properties = other.getProperties();
        if (this$properties == null ? other$properties != null : !this$properties.equals(other$properties)) {
            return false;
        }
        final Object this$keyProperties = this.getKeyProperties();
        final Object other$keyProperties = other.getKeyProperties();
        if (this$keyProperties == null ? other$keyProperties != null
                : !this$keyProperties.equals(other$keyProperties)) {
            return false;
        }
        final Object this$isDeleteFlagExist = this.getIsDeleteFlagExist();
        final Object other$isDeleteFlagExist = other.getIsDeleteFlagExist();
        return this$isDeleteFlagExist == null ? other$isDeleteFlagExist == null
                : this$isDeleteFlagExist.equals(other$isDeleteFlagExist);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PersistenceDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $tableName = this.getTableName();
        result = result * PRIME + ($tableName == null ? 43 : $tableName.hashCode());
        final Object $entityName = this.getEntityName();
        result = result * PRIME + ($entityName == null ? 43 : $entityName.hashCode());
        final Object $mapperName = this.getMapperName();
        result = result * PRIME + ($mapperName == null ? 43 : $mapperName.hashCode());
        final Object $descrption = this.getDescrption();
        result = result * PRIME + ($descrption == null ? 43 : $descrption.hashCode());
        final Object $idProperties = this.getIdProperties();
        result = result * PRIME + ($idProperties == null ? 43 : $idProperties.hashCode());
        final Object $nonIdProperties = this.getNonIdProperties();
        result = result * PRIME + ($nonIdProperties == null ? 43 : $nonIdProperties.hashCode());
        final Object $properties = this.getProperties();
        result = result * PRIME + ($properties == null ? 43 : $properties.hashCode());
        final Object $keyProperties = this.getKeyProperties();
        result = result * PRIME + ($keyProperties == null ? 43 : $keyProperties.hashCode());
        final Object $isDeleteFlagExist = this.getIsDeleteFlagExist();
        result = result * PRIME + ($isDeleteFlagExist == null ? 43 : $isDeleteFlagExist.hashCode());
        return result;
    }

    public String toString() {
        return "PersistenceDto(tableName=" + this.getTableName() + ", entityName=" + this.getEntityName()
                + ", mapperName=" + this.getMapperName() + ", descrption=" + this.getDescrption() + ", idProperties="
                + this.getIdProperties() + ", nonIdProperties=" + this.getNonIdProperties() + ", properties=" + this
                .getProperties() + ", keyProperties=" + this.getKeyProperties() + ", isDeleteFlagExist=" + this
                .getIsDeleteFlagExist() + ")";
    }

}