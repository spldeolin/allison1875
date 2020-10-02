package com.spldeolin.allison1875.persistencegenerator.javabean;

/**
 * @author Deolin 2020-07-12
 */
public class PropertyDto {

    private String columnName;

    private String propertyName;

    private Class<?> javaType;

    private String description;

    private Long length;

    private Boolean notnull;

    private String defaultV;

    public PropertyDto() {
    }

    public String getColumnName() {
        return this.columnName;
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    public Class<?> getJavaType() {
        return this.javaType;
    }

    public String getDescription() {
        return this.description;
    }

    public Long getLength() {
        return this.length;
    }

    public Boolean getNotnull() {
        return this.notnull;
    }

    public String getDefaultV() {
        return this.defaultV;
    }

    public PropertyDto setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public PropertyDto setPropertyName(String propertyName) {
        this.propertyName = propertyName;
        return this;
    }

    public PropertyDto setJavaType(Class<?> javaType) {
        this.javaType = javaType;
        return this;
    }

    public PropertyDto setDescription(String description) {
        this.description = description;
        return this;
    }

    public PropertyDto setLength(Long length) {
        this.length = length;
        return this;
    }

    public PropertyDto setNotnull(Boolean notnull) {
        this.notnull = notnull;
        return this;
    }

    public PropertyDto setDefaultV(String defaultV) {
        this.defaultV = defaultV;
        return this;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PropertyDto)) {
            return false;
        }
        final PropertyDto other = (PropertyDto) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$columnName = this.getColumnName();
        final Object other$columnName = other.getColumnName();
        if (this$columnName == null ? other$columnName != null : !this$columnName.equals(other$columnName)) {
            return false;
        }
        final Object this$propertyName = this.getPropertyName();
        final Object other$propertyName = other.getPropertyName();
        if (this$propertyName == null ? other$propertyName != null : !this$propertyName.equals(other$propertyName)) {
            return false;
        }
        final Object this$javaType = this.getJavaType();
        final Object other$javaType = other.getJavaType();
        if (this$javaType == null ? other$javaType != null : !this$javaType.equals(other$javaType)) {
            return false;
        }
        final Object this$description = this.getDescription();
        final Object other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description)) {
            return false;
        }
        final Object this$length = this.getLength();
        final Object other$length = other.getLength();
        if (this$length == null ? other$length != null : !this$length.equals(other$length)) {
            return false;
        }
        final Object this$notnull = this.getNotnull();
        final Object other$notnull = other.getNotnull();
        if (this$notnull == null ? other$notnull != null : !this$notnull.equals(other$notnull)) {
            return false;
        }
        final Object this$defaultV = this.getDefaultV();
        final Object other$defaultV = other.getDefaultV();
        return this$defaultV == null ? other$defaultV == null : this$defaultV.equals(other$defaultV);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PropertyDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $columnName = this.getColumnName();
        result = result * PRIME + ($columnName == null ? 43 : $columnName.hashCode());
        final Object $propertyName = this.getPropertyName();
        result = result * PRIME + ($propertyName == null ? 43 : $propertyName.hashCode());
        final Object $javaType = this.getJavaType();
        result = result * PRIME + ($javaType == null ? 43 : $javaType.hashCode());
        final Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        final Object $length = this.getLength();
        result = result * PRIME + ($length == null ? 43 : $length.hashCode());
        final Object $notnull = this.getNotnull();
        result = result * PRIME + ($notnull == null ? 43 : $notnull.hashCode());
        final Object $defaultV = this.getDefaultV();
        result = result * PRIME + ($defaultV == null ? 43 : $defaultV.hashCode());
        return result;
    }

    public String toString() {
        return "PropertyDto(columnName=" + this.getColumnName() + ", propertyName=" + this.getPropertyName()
                + ", javaType=" + this.getJavaType() + ", description=" + this.getDescription() + ", length=" + this
                .getLength() + ", notnull=" + this.getNotnull() + ", defaultV=" + this.getDefaultV() + ")";
    }

}