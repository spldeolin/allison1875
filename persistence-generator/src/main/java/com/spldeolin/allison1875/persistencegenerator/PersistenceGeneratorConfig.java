package com.spldeolin.allison1875.persistencegenerator;

import java.util.Collection;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.apache.logging.log4j.Logger;
import com.google.common.collect.Lists;

/**
 * @author Deolin 2020-07-11
 */
public class PersistenceGeneratorConfig {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(PersistenceGeneratorConfig.class);

    private static final PersistenceGeneratorConfig instance = new PersistenceGeneratorConfig();

    /**
     * 数据库连接
     */
    @NotEmpty
    private String jdbcUrl;

    /**
     * 数据库用户名
     */
    @NotEmpty
    private String userName;

    /**
     * 数据库密码
     */
    @NotEmpty
    private String password;

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty
    private String author;

    /**
     * 指定schema
     */
    @NotEmpty
    private String schema;

    /**
     * 指定table，非必填，未填写时代表schema下所有的table
     */
    private Collection<String> tables = Lists.newArrayList();

    /**
     * mapper.xml所在目录的相对路径（根据目标工程的情况填写）
     */
    @NotEmpty
    private String mapperXmlDirectoryPath;

    /**
     * mapper接口的包名（根据目标工程的情况填写）
     */
    @NotEmpty
    private String mapperPackage;

    /**
     * Entity类的包名（根据目标工程的情况填写）
     */
    @NotEmpty
    private String entityPackage;

    /**
     * 是否为[query-transformer]生成Query类
     */
    @NotNull
    private Boolean enableGenerateQueryDesign;

    /**
     * QueryDesign类的包名（根据目标工程的情况填写）
     */
    @NotEmpty
    private String queryDesignPackage;

    /**
     * QueryPredicate类的全限定名（根据目标工程的情况填写）
     */
    private String queryPredicateQualifier;

    /**
     * mapper.xml的标签中，是否使用别名来引用Entity类
     */
    @NotNull
    private Boolean isEntityUsingAlias;

    /**
     * # 生成出的Entity类是否以Entity作为类名的结尾
     */
    @NotNull
    private Boolean isEntityEndWithEntity;

    /**
     * 如果有逻辑删除，怎么样算作“数据被删”，非必填，只支持等式SQL
     */
    private String deletedSql;

    /**
     * 如果有逻辑删除，怎么样算作“数据未被删”，非必填，只支持等式SQL
     */
    private String notDeletedSql;

    /**
     * 对项目隐藏，仅在数据库中可见的表字段
     */
    private Collection<String> hiddenColumns = Lists.newArrayList();

    /**
     * 即便符合persistence-generator对外键的定义，也不会被当作外键的表字段（一般用于忽略为创建人ID和更新人ID生成query方法）
     */
    private Collection<String> notKeyColumns = Lists.newArrayList();

    /**
     * Entity父类的全限定名
     */
    private String superEntityQualifier;

    /**
     * 已在Entit父类中声明，无需在具体Entity中再次声明的表字段
     */
    private Collection<String> alreadyInSuperEntity = Lists.newArrayList();

    private Boolean disableInsert = false;

    private Boolean disableQueryById = false;

    private Boolean disableUpdateById = false;

    private Boolean disableUpdateByIdEvenNull = false;

    private Boolean disableQueryByIds = false;

    private Boolean disableQueryByIdsEachId = false;

    private Boolean disableQueryByKey = false;

    private Boolean disableDeleteByKey = false;

    private Boolean disableQueryByKeys = false;

    private Boolean disableQueryByEntity = false;

    private PersistenceGeneratorConfig() {
    }

    public static PersistenceGeneratorConfig getInstance() {
        return PersistenceGeneratorConfig.instance;
    }

    public @NotEmpty String getJdbcUrl() {
        return this.jdbcUrl;
    }

    public @NotEmpty String getUserName() {
        return this.userName;
    }

    public @NotEmpty String getPassword() {
        return this.password;
    }

    public @NotEmpty String getAuthor() {
        return this.author;
    }

    public @NotEmpty String getSchema() {
        return this.schema;
    }

    public Collection<String> getTables() {
        return this.tables;
    }

    public @NotEmpty String getMapperXmlDirectoryPath() {
        return this.mapperXmlDirectoryPath;
    }

    public @NotEmpty String getMapperPackage() {
        return this.mapperPackage;
    }

    public @NotEmpty String getEntityPackage() {
        return this.entityPackage;
    }

    public @NotNull Boolean getEnableGenerateQueryDesign() {
        return this.enableGenerateQueryDesign;
    }

    public @NotEmpty String getQueryDesignPackage() {
        return this.queryDesignPackage;
    }

    public @NotEmpty String getQueryPredicateQualifier() {
        return this.queryPredicateQualifier;
    }

    public @NotNull Boolean getIsEntityUsingAlias() {
        return this.isEntityUsingAlias;
    }

    public @NotNull Boolean getIsEntityEndWithEntity() {
        return this.isEntityEndWithEntity;
    }

    public String getDeletedSql() {
        return this.deletedSql;
    }

    public String getNotDeletedSql() {
        return this.notDeletedSql;
    }

    public Collection<String> getHiddenColumns() {
        return this.hiddenColumns;
    }

    public Collection<String> getNotKeyColumns() {
        return this.notKeyColumns;
    }

    public String getSuperEntityQualifier() {
        return this.superEntityQualifier;
    }

    public Collection<String> getAlreadyInSuperEntity() {
        return this.alreadyInSuperEntity;
    }

    public Boolean getDisableInsert() {
        return this.disableInsert;
    }

    public Boolean getDisableQueryById() {
        return this.disableQueryById;
    }

    public Boolean getDisableUpdateById() {
        return this.disableUpdateById;
    }

    public Boolean getDisableUpdateByIdEvenNull() {
        return this.disableUpdateByIdEvenNull;
    }

    public Boolean getDisableQueryByIds() {
        return this.disableQueryByIds;
    }

    public Boolean getDisableQueryByIdsEachId() {
        return this.disableQueryByIdsEachId;
    }

    public Boolean getDisableQueryByKey() {
        return this.disableQueryByKey;
    }

    public Boolean getDisableDeleteByKey() {
        return this.disableDeleteByKey;
    }

    public Boolean getDisableQueryByKeys() {
        return this.disableQueryByKeys;
    }

    public Boolean getDisableQueryByEntity() {
        return this.disableQueryByEntity;
    }

    public void setJdbcUrl(@NotEmpty String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public void setUserName(@NotEmpty String userName) {
        this.userName = userName;
    }

    public void setPassword(@NotEmpty String password) {
        this.password = password;
    }

    public void setAuthor(@NotEmpty String author) {
        this.author = author;
    }

    public void setSchema(@NotEmpty String schema) {
        this.schema = schema;
    }

    public void setTables(Collection<String> tables) {
        this.tables = tables;
    }

    public void setMapperXmlDirectoryPath(@NotEmpty String mapperXmlDirectoryPath) {
        this.mapperXmlDirectoryPath = mapperXmlDirectoryPath;
    }

    public void setMapperPackage(@NotEmpty String mapperPackage) {
        this.mapperPackage = mapperPackage;
    }

    public void setEntityPackage(@NotEmpty String entityPackage) {
        this.entityPackage = entityPackage;
    }

    public void setEnableGenerateQueryDesign(@NotNull Boolean enableGenerateQueryDesign) {
        this.enableGenerateQueryDesign = enableGenerateQueryDesign;
    }

    public void setQueryDesignPackage(@NotEmpty String queryDesignPackage) {
        this.queryDesignPackage = queryDesignPackage;
    }

    public void setQueryPredicateQualifier(@NotEmpty String queryPredicateQualifier) {
        this.queryPredicateQualifier = queryPredicateQualifier;
    }

    public void setIsEntityUsingAlias(@NotNull Boolean isEntityUsingAlias) {
        this.isEntityUsingAlias = isEntityUsingAlias;
    }

    public void setIsEntityEndWithEntity(@NotNull Boolean isEntityEndWithEntity) {
        this.isEntityEndWithEntity = isEntityEndWithEntity;
    }

    public void setDeletedSql(String deletedSql) {
        this.deletedSql = deletedSql;
    }

    public void setNotDeletedSql(String notDeletedSql) {
        this.notDeletedSql = notDeletedSql;
    }

    public void setHiddenColumns(Collection<String> hiddenColumns) {
        this.hiddenColumns = hiddenColumns;
    }

    public void setNotKeyColumns(Collection<String> notKeyColumns) {
        this.notKeyColumns = notKeyColumns;
    }

    public void setSuperEntityQualifier(String superEntityQualifier) {
        this.superEntityQualifier = superEntityQualifier;
    }

    public void setAlreadyInSuperEntity(Collection<String> alreadyInSuperEntity) {
        this.alreadyInSuperEntity = alreadyInSuperEntity;
    }

    public void setDisableInsert(Boolean disableInsert) {
        this.disableInsert = disableInsert;
    }

    public void setDisableQueryById(Boolean disableQueryById) {
        this.disableQueryById = disableQueryById;
    }

    public void setDisableUpdateById(Boolean disableUpdateById) {
        this.disableUpdateById = disableUpdateById;
    }

    public void setDisableUpdateByIdEvenNull(Boolean disableUpdateByIdEvenNull) {
        this.disableUpdateByIdEvenNull = disableUpdateByIdEvenNull;
    }

    public void setDisableQueryByIds(Boolean disableQueryByIds) {
        this.disableQueryByIds = disableQueryByIds;
    }

    public void setDisableQueryByIdsEachId(Boolean disableQueryByIdsEachId) {
        this.disableQueryByIdsEachId = disableQueryByIdsEachId;
    }

    public void setDisableQueryByKey(Boolean disableQueryByKey) {
        this.disableQueryByKey = disableQueryByKey;
    }

    public void setDisableDeleteByKey(Boolean disableDeleteByKey) {
        this.disableDeleteByKey = disableDeleteByKey;
    }

    public void setDisableQueryByKeys(Boolean disableQueryByKeys) {
        this.disableQueryByKeys = disableQueryByKeys;
    }

    public void setDisableQueryByEntity(Boolean disableQueryByEntity) {
        this.disableQueryByEntity = disableQueryByEntity;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PersistenceGeneratorConfig)) {
            return false;
        }
        final PersistenceGeneratorConfig other = (PersistenceGeneratorConfig) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$jdbcUrl = this.getJdbcUrl();
        final Object other$jdbcUrl = other.getJdbcUrl();
        if (this$jdbcUrl == null ? other$jdbcUrl != null : !this$jdbcUrl.equals(other$jdbcUrl)) {
            return false;
        }
        final Object this$userName = this.getUserName();
        final Object other$userName = other.getUserName();
        if (this$userName == null ? other$userName != null : !this$userName.equals(other$userName)) {
            return false;
        }
        final Object this$password = this.getPassword();
        final Object other$password = other.getPassword();
        if (this$password == null ? other$password != null : !this$password.equals(other$password)) {
            return false;
        }
        final Object this$author = this.getAuthor();
        final Object other$author = other.getAuthor();
        if (this$author == null ? other$author != null : !this$author.equals(other$author)) {
            return false;
        }
        final Object this$schema = this.getSchema();
        final Object other$schema = other.getSchema();
        if (this$schema == null ? other$schema != null : !this$schema.equals(other$schema)) {
            return false;
        }
        final Object this$tables = this.getTables();
        final Object other$tables = other.getTables();
        if (this$tables == null ? other$tables != null : !this$tables.equals(other$tables)) {
            return false;
        }
        final Object this$mapperXmlDirectoryPath = this.getMapperXmlDirectoryPath();
        final Object other$mapperXmlDirectoryPath = other.getMapperXmlDirectoryPath();
        if (this$mapperXmlDirectoryPath == null ? other$mapperXmlDirectoryPath != null
                : !this$mapperXmlDirectoryPath.equals(other$mapperXmlDirectoryPath)) {
            return false;
        }
        final Object this$mapperPackage = this.getMapperPackage();
        final Object other$mapperPackage = other.getMapperPackage();
        if (this$mapperPackage == null ? other$mapperPackage != null
                : !this$mapperPackage.equals(other$mapperPackage)) {
            return false;
        }
        final Object this$entityPackage = this.getEntityPackage();
        final Object other$entityPackage = other.getEntityPackage();
        if (this$entityPackage == null ? other$entityPackage != null
                : !this$entityPackage.equals(other$entityPackage)) {
            return false;
        }
        final Object this$isEntityUsingAlias = this.getIsEntityUsingAlias();
        final Object other$isEntityUsingAlias = other.getIsEntityUsingAlias();
        if (this$isEntityUsingAlias == null ? other$isEntityUsingAlias != null
                : !this$isEntityUsingAlias.equals(other$isEntityUsingAlias)) {
            return false;
        }
        final Object this$isEntityEndWithEntity = this.getIsEntityEndWithEntity();
        final Object other$isEntityEndWithEntity = other.getIsEntityEndWithEntity();
        if (this$isEntityEndWithEntity == null ? other$isEntityEndWithEntity != null
                : !this$isEntityEndWithEntity.equals(other$isEntityEndWithEntity)) {
            return false;
        }
        final Object this$deletedSql = this.getDeletedSql();
        final Object other$deletedSql = other.getDeletedSql();
        if (this$deletedSql == null ? other$deletedSql != null : !this$deletedSql.equals(other$deletedSql)) {
            return false;
        }
        final Object this$notDeletedSql = this.getNotDeletedSql();
        final Object other$notDeletedSql = other.getNotDeletedSql();
        if (this$notDeletedSql == null ? other$notDeletedSql != null
                : !this$notDeletedSql.equals(other$notDeletedSql)) {
            return false;
        }
        final Object this$hiddenColumns = this.getHiddenColumns();
        final Object other$hiddenColumns = other.getHiddenColumns();
        if (this$hiddenColumns == null ? other$hiddenColumns != null
                : !this$hiddenColumns.equals(other$hiddenColumns)) {
            return false;
        }
        final Object this$notKeyColumns = this.getNotKeyColumns();
        final Object other$notKeyColumns = other.getNotKeyColumns();
        if (this$notKeyColumns == null ? other$notKeyColumns != null
                : !this$notKeyColumns.equals(other$notKeyColumns)) {
            return false;
        }
        final Object this$superEntityQualifier = this.getSuperEntityQualifier();
        final Object other$superEntityQualifier = other.getSuperEntityQualifier();
        if (this$superEntityQualifier == null ? other$superEntityQualifier != null
                : !this$superEntityQualifier.equals(other$superEntityQualifier)) {
            return false;
        }
        final Object this$alreadyInSuperEntity = this.getAlreadyInSuperEntity();
        final Object other$alreadyInSuperEntity = other.getAlreadyInSuperEntity();
        if (this$alreadyInSuperEntity == null ? other$alreadyInSuperEntity != null
                : !this$alreadyInSuperEntity.equals(other$alreadyInSuperEntity)) {
            return false;
        }
        final Object this$disableInsert = this.getDisableInsert();
        final Object other$disableInsert = other.getDisableInsert();
        if (this$disableInsert == null ? other$disableInsert != null
                : !this$disableInsert.equals(other$disableInsert)) {
            return false;
        }
        final Object this$disableQueryById = this.getDisableQueryById();
        final Object other$disableQueryById = other.getDisableQueryById();
        if (this$disableQueryById == null ? other$disableQueryById != null
                : !this$disableQueryById.equals(other$disableQueryById)) {
            return false;
        }
        final Object this$disableUpdateById = this.getDisableUpdateById();
        final Object other$disableUpdateById = other.getDisableUpdateById();
        if (this$disableUpdateById == null ? other$disableUpdateById != null
                : !this$disableUpdateById.equals(other$disableUpdateById)) {
            return false;
        }
        final Object this$disableUpdateByIdEvenNull = this.getDisableUpdateByIdEvenNull();
        final Object other$disableUpdateByIdEvenNull = other.getDisableUpdateByIdEvenNull();
        if (this$disableUpdateByIdEvenNull == null ? other$disableUpdateByIdEvenNull != null
                : !this$disableUpdateByIdEvenNull.equals(other$disableUpdateByIdEvenNull)) {
            return false;
        }
        final Object this$disableQueryByIds = this.getDisableQueryByIds();
        final Object other$disableQueryByIds = other.getDisableQueryByIds();
        if (this$disableQueryByIds == null ? other$disableQueryByIds != null
                : !this$disableQueryByIds.equals(other$disableQueryByIds)) {
            return false;
        }
        final Object this$disableQueryByIdsEachId = this.getDisableQueryByIdsEachId();
        final Object other$disableQueryByIdsEachId = other.getDisableQueryByIdsEachId();
        if (this$disableQueryByIdsEachId == null ? other$disableQueryByIdsEachId != null
                : !this$disableQueryByIdsEachId.equals(other$disableQueryByIdsEachId)) {
            return false;
        }
        final Object this$disableQueryByKey = this.getDisableQueryByKey();
        final Object other$disableQueryByKey = other.getDisableQueryByKey();
        if (this$disableQueryByKey == null ? other$disableQueryByKey != null
                : !this$disableQueryByKey.equals(other$disableQueryByKey)) {
            return false;
        }
        final Object this$disableDeleteByKey = this.getDisableDeleteByKey();
        final Object other$disableDeleteByKey = other.getDisableDeleteByKey();
        if (this$disableDeleteByKey == null ? other$disableDeleteByKey != null
                : !this$disableDeleteByKey.equals(other$disableDeleteByKey)) {
            return false;
        }
        final Object this$disableQueryByKeys = this.getDisableQueryByKeys();
        final Object other$disableQueryByKeys = other.getDisableQueryByKeys();
        return this$disableQueryByKeys == null ? other$disableQueryByKeys == null
                : this$disableQueryByKeys.equals(other$disableQueryByKeys);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PersistenceGeneratorConfig;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $jdbcUrl = this.getJdbcUrl();
        result = result * PRIME + ($jdbcUrl == null ? 43 : $jdbcUrl.hashCode());
        final Object $userName = this.getUserName();
        result = result * PRIME + ($userName == null ? 43 : $userName.hashCode());
        final Object $password = this.getPassword();
        result = result * PRIME + ($password == null ? 43 : $password.hashCode());
        final Object $author = this.getAuthor();
        result = result * PRIME + ($author == null ? 43 : $author.hashCode());
        final Object $schema = this.getSchema();
        result = result * PRIME + ($schema == null ? 43 : $schema.hashCode());
        final Object $tables = this.getTables();
        result = result * PRIME + ($tables == null ? 43 : $tables.hashCode());
        final Object $mapperXmlDirectoryPath = this.getMapperXmlDirectoryPath();
        result = result * PRIME + ($mapperXmlDirectoryPath == null ? 43 : $mapperXmlDirectoryPath.hashCode());
        final Object $mapperPackage = this.getMapperPackage();
        result = result * PRIME + ($mapperPackage == null ? 43 : $mapperPackage.hashCode());
        final Object $entityPackage = this.getEntityPackage();
        result = result * PRIME + ($entityPackage == null ? 43 : $entityPackage.hashCode());
        final Object $isEntityUsingAlias = this.getIsEntityUsingAlias();
        result = result * PRIME + ($isEntityUsingAlias == null ? 43 : $isEntityUsingAlias.hashCode());
        final Object $isEntityEndWithEntity = this.getIsEntityEndWithEntity();
        result = result * PRIME + ($isEntityEndWithEntity == null ? 43 : $isEntityEndWithEntity.hashCode());
        final Object $deletedSql = this.getDeletedSql();
        result = result * PRIME + ($deletedSql == null ? 43 : $deletedSql.hashCode());
        final Object $notDeletedSql = this.getNotDeletedSql();
        result = result * PRIME + ($notDeletedSql == null ? 43 : $notDeletedSql.hashCode());
        final Object $hiddenColumns = this.getHiddenColumns();
        result = result * PRIME + ($hiddenColumns == null ? 43 : $hiddenColumns.hashCode());
        final Object $notKeyColumns = this.getNotKeyColumns();
        result = result * PRIME + ($notKeyColumns == null ? 43 : $notKeyColumns.hashCode());
        final Object $superEntityQualifier = this.getSuperEntityQualifier();
        result = result * PRIME + ($superEntityQualifier == null ? 43 : $superEntityQualifier.hashCode());
        final Object $alreadyInSuperEntity = this.getAlreadyInSuperEntity();
        result = result * PRIME + ($alreadyInSuperEntity == null ? 43 : $alreadyInSuperEntity.hashCode());
        final Object $disableInsert = this.getDisableInsert();
        result = result * PRIME + ($disableInsert == null ? 43 : $disableInsert.hashCode());
        final Object $disableQueryById = this.getDisableQueryById();
        result = result * PRIME + ($disableQueryById == null ? 43 : $disableQueryById.hashCode());
        final Object $disableUpdateById = this.getDisableUpdateById();
        result = result * PRIME + ($disableUpdateById == null ? 43 : $disableUpdateById.hashCode());
        final Object $disableUpdateByIdEvenNull = this.getDisableUpdateByIdEvenNull();
        result = result * PRIME + ($disableUpdateByIdEvenNull == null ? 43 : $disableUpdateByIdEvenNull.hashCode());
        final Object $disableQueryByIds = this.getDisableQueryByIds();
        result = result * PRIME + ($disableQueryByIds == null ? 43 : $disableQueryByIds.hashCode());
        final Object $disableQueryByIdsEachId = this.getDisableQueryByIdsEachId();
        result = result * PRIME + ($disableQueryByIdsEachId == null ? 43 : $disableQueryByIdsEachId.hashCode());
        final Object $disableQueryByKey = this.getDisableQueryByKey();
        result = result * PRIME + ($disableQueryByKey == null ? 43 : $disableQueryByKey.hashCode());
        final Object $disableDeleteByKey = this.getDisableDeleteByKey();
        result = result * PRIME + ($disableDeleteByKey == null ? 43 : $disableDeleteByKey.hashCode());
        final Object $disableQueryByKeys = this.getDisableQueryByKeys();
        result = result * PRIME + ($disableQueryByKeys == null ? 43 : $disableQueryByKeys.hashCode());
        return result;
    }

    public String toString() {
        return "PersistenceGeneratorConfig(jdbcUrl=" + this.getJdbcUrl() + ", userName=" + this.getUserName()
                + ", password=" + this.getPassword() + ", author=" + this.getAuthor() + ", schema=" + this.getSchema()
                + ", tables=" + this.getTables() + ", mapperXmlDirectoryPath=" + this.getMapperXmlDirectoryPath()
                + ", mapperPackage=" + this.getMapperPackage() + ", entityPackage=" + this.getEntityPackage()
                + ", isEntityUsingAlias=" + this.getIsEntityUsingAlias() + ", isEntityEndWithEntity=" + this
                .getIsEntityEndWithEntity() + ", deletedSql=" + this.getDeletedSql() + ", notDeletedSql=" + this
                .getNotDeletedSql() + ", hiddenColumns=" + this.getHiddenColumns() + ", notKeyColumns=" + this
                .getNotKeyColumns() + ", superEntityQualifier=" + this.getSuperEntityQualifier()
                + ", alreadyInSuperEntity=" + this.getAlreadyInSuperEntity() + ", disableInsert=" + this
                .getDisableInsert() + ", disableQueryById=" + this.getDisableQueryById() + ", disableUpdateById=" + this
                .getDisableUpdateById() + ", disableUpdateByIdEvenNull=" + this.getDisableUpdateByIdEvenNull()
                + ", disableQueryByIds=" + this.getDisableQueryByIds() + ", disableQueryByIdsEachId=" + this
                .getDisableQueryByIdsEachId() + ", disableQueryByKey=" + this.getDisableQueryByKey()
                + ", disableDeleteByKey=" + this.getDisableDeleteByKey() + ", disableQueryByKeys=" + this
                .getDisableQueryByKeys() + ")";
    }

}