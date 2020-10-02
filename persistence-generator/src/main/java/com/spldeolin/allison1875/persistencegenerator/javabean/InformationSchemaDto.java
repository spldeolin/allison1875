package com.spldeolin.allison1875.persistencegenerator.javabean;

/**
 * @author Deolin 2020-07-11
 */
public class InformationSchemaDto {

    private String tableName;

    private String tableComment;

    private String columnName;

    private String dataType;

    private String columnType;

    private String columnComment;

    private String columnKey;

    private Long characterMaximumLength;

    private String isNullable; // YES NO

    private String columnDefault;

    public InformationSchemaDto() {
    }

    public String getTableName() {
        return this.tableName;
    }

    public String getTableComment() {
        return this.tableComment;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public String getDataType() {
        return this.dataType;
    }

    public String getColumnType() {
        return this.columnType;
    }

    public String getColumnComment() {
        return this.columnComment;
    }

    public String getColumnKey() {
        return this.columnKey;
    }

    public Long getCharacterMaximumLength() {
        return this.characterMaximumLength;
    }

    public String getIsNullable() {
        return this.isNullable;
    }

    public String getColumnDefault() {
        return this.columnDefault;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public void setColumnKey(String columnKey) {
        this.columnKey = columnKey;
    }

    public void setCharacterMaximumLength(Long characterMaximumLength) {
        this.characterMaximumLength = characterMaximumLength;
    }

    public void setIsNullable(String isNullable) {
        this.isNullable = isNullable;
    }

    public void setColumnDefault(String columnDefault) {
        this.columnDefault = columnDefault;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof InformationSchemaDto)) {
            return false;
        }
        final InformationSchemaDto other = (InformationSchemaDto) o;
        if (!other.canEqual((Object) this)) {
            return false;
        }
        final Object this$tableName = this.getTableName();
        final Object other$tableName = other.getTableName();
        if (this$tableName == null ? other$tableName != null : !this$tableName.equals(other$tableName)) {
            return false;
        }
        final Object this$tableComment = this.getTableComment();
        final Object other$tableComment = other.getTableComment();
        if (this$tableComment == null ? other$tableComment != null : !this$tableComment.equals(other$tableComment)) {
            return false;
        }
        final Object this$columnName = this.getColumnName();
        final Object other$columnName = other.getColumnName();
        if (this$columnName == null ? other$columnName != null : !this$columnName.equals(other$columnName)) {
            return false;
        }
        final Object this$dataType = this.getDataType();
        final Object other$dataType = other.getDataType();
        if (this$dataType == null ? other$dataType != null : !this$dataType.equals(other$dataType)) {
            return false;
        }
        final Object this$columnType = this.getColumnType();
        final Object other$columnType = other.getColumnType();
        if (this$columnType == null ? other$columnType != null : !this$columnType.equals(other$columnType)) {
            return false;
        }
        final Object this$columnComment = this.getColumnComment();
        final Object other$columnComment = other.getColumnComment();
        if (this$columnComment == null ? other$columnComment != null
                : !this$columnComment.equals(other$columnComment)) {
            return false;
        }
        final Object this$columnKey = this.getColumnKey();
        final Object other$columnKey = other.getColumnKey();
        if (this$columnKey == null ? other$columnKey != null : !this$columnKey.equals(other$columnKey)) {
            return false;
        }
        final Object this$characterMaximumLength = this.getCharacterMaximumLength();
        final Object other$characterMaximumLength = other.getCharacterMaximumLength();
        if (this$characterMaximumLength == null ? other$characterMaximumLength != null
                : !this$characterMaximumLength.equals(other$characterMaximumLength)) {
            return false;
        }
        final Object this$isNullable = this.getIsNullable();
        final Object other$isNullable = other.getIsNullable();
        if (this$isNullable == null ? other$isNullable != null : !this$isNullable.equals(other$isNullable)) {
            return false;
        }
        final Object this$columnDefault = this.getColumnDefault();
        final Object other$columnDefault = other.getColumnDefault();
        if (this$columnDefault == null ? other$columnDefault != null
                : !this$columnDefault.equals(other$columnDefault)) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof InformationSchemaDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $tableName = this.getTableName();
        result = result * PRIME + ($tableName == null ? 43 : $tableName.hashCode());
        final Object $tableComment = this.getTableComment();
        result = result * PRIME + ($tableComment == null ? 43 : $tableComment.hashCode());
        final Object $columnName = this.getColumnName();
        result = result * PRIME + ($columnName == null ? 43 : $columnName.hashCode());
        final Object $dataType = this.getDataType();
        result = result * PRIME + ($dataType == null ? 43 : $dataType.hashCode());
        final Object $columnType = this.getColumnType();
        result = result * PRIME + ($columnType == null ? 43 : $columnType.hashCode());
        final Object $columnComment = this.getColumnComment();
        result = result * PRIME + ($columnComment == null ? 43 : $columnComment.hashCode());
        final Object $columnKey = this.getColumnKey();
        result = result * PRIME + ($columnKey == null ? 43 : $columnKey.hashCode());
        final Object $characterMaximumLength = this.getCharacterMaximumLength();
        result = result * PRIME + ($characterMaximumLength == null ? 43 : $characterMaximumLength.hashCode());
        final Object $isNullable = this.getIsNullable();
        result = result * PRIME + ($isNullable == null ? 43 : $isNullable.hashCode());
        final Object $columnDefault = this.getColumnDefault();
        result = result * PRIME + ($columnDefault == null ? 43 : $columnDefault.hashCode());
        return result;
    }

    public String toString() {
        return "InformationSchemaDto(tableName=" + this.getTableName() + ", tableComment=" + this.getTableComment()
                + ", columnName=" + this.getColumnName() + ", dataType=" + this.getDataType() + ", columnType=" + this
                .getColumnType() + ", columnComment=" + this.getColumnComment() + ", columnKey=" + this.getColumnKey()
                + ", characterMaximumLength=" + this.getCharacterMaximumLength() + ", isNullable=" + this
                .getIsNullable() + ", columnDefault=" + this.getColumnDefault() + ")";
    }

}