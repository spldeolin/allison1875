package com.spldeolin.allison1875.persistencegenerator;

import java.util.Collection;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Deolin 2020-07-11
 */
@Data
public final class PersistenceGeneratorConfig {

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
    @NotNull
    private Collection<String> tables;

    /**
     * mapper.xml所在目录的相对路径（根据目标工程的情况填写）
     */
    @NotEmpty
    private List<String> mapperXmlDirectoryPaths;

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
     * 是否为[query-transformer]生成Design类
     */
    @NotNull
    private Boolean enableGenerateDesign;

    /**
     * QueryDesign类的包名（根据目标工程的情况填写）
     */
    @NotEmpty
    private String designPackage;

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
    @NotNull
    private Collection<String> hiddenColumns;

    /**
     * 即便符合persistence-generator对外键的定义，也不会被当作外键的表字段（一般用于忽略为创建人ID和更新人ID生成query方法）
     */
    @NotNull
    private Collection<String> notKeyColumns;

    /**
     * Entity父类的全限定名
     */
    private String superEntityQualifier;

    /**
     * 已在Entit父类中声明，无需在具体Entity中再次声明的表字段
     */
    @NotNull
    private Collection<String> alreadyInSuperEntity;

    /**
     * mapper接口中的方法是否打印Lot No信息
     */
    private Boolean mapperInterfaceMethodPrintLotNo = true;

    /**
     * 是否为entity实现java.io.Serializable接口
     */
    private Boolean enableEntityImplementSerializable;

    /**
     * 是否为entity实现java.lang.Cloneable接口
     */
    private Boolean enableEntityImplementCloneable;

    @NotNull
    private Boolean disableInsert;

    @NotNull
    private Boolean disableBatchInsert;

    @NotNull
    private Boolean disableBatchInsertEvenNull;

    @NotNull
    private Boolean disableBatchUpdate;

    @NotNull
    private Boolean disableBatchUpdateEvenNull;

    @NotNull
    private Boolean disableQueryById;

    @NotNull
    private Boolean disableUpdateById;

    @NotNull
    private Boolean disableUpdateByIdEvenNull;

    @NotNull
    private Boolean disableQueryByIds;

    @NotNull
    private Boolean disableQueryByIdsEachId;

    @NotNull
    private Boolean disableQueryByKey;

    @NotNull
    private Boolean disableDeleteByKey;

    @NotNull
    private Boolean disableQueryByKeys;

    @NotNull
    private Boolean disableQueryByEntity;

    @NotNull
    private Boolean disableListAll;

    @NotNull
    private Boolean disableInsertOrUpdate;

    /**
     * 使用通配符的方式设置所有包名，通配符是<code>.-</code>
     *
     * <pre>
     * e.g.1:
     * input:
     *  com.company.orginization.project.-
     *
     * output:
     *  com.company.orginization.project.mapper
     *  com.company.orginization.project.entity
     *  com.company.orginization.project.design
     *
     *
     * e.g.2:
     * input:
     *  com.company.orginization.project.-.module.sub
     *
     * output:
     *  com.company.orginization.project.mapper.module.sub
     *  com.company.orginization.project.entity.module.sub
     *  com.company.orginization.project.design.module.sub
     *
     * </pre>
     */
    public void batchSetAllPackagesByWildcard(String packageNameWithWildcard) {
        if (packageNameWithWildcard != null && packageNameWithWildcard.contains(".-")) {
            this.mapperPackage = packageNameWithWildcard.replace(".-", ".mapper");
            this.entityPackage = packageNameWithWildcard.replace(".-", ".entity");
            this.designPackage = packageNameWithWildcard.replace(".-", ".design");
        }
    }

}