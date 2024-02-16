package com.spldeolin.allison1875.persistencegenerator;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.common.ancestor.Allison1875Config;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-07-11
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class PersistenceGeneratorConfig extends Allison1875Config {

    /**
     * 数据库连接
     */
    @NotEmpty String jdbcUrl;

    /**
     * 数据库用户名
     */
    @NotEmpty String userName;

    /**
     * 数据库密码
     */
    @NotEmpty String password;

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty String author;

    /**
     * 指定schema
     */
    @NotEmpty String schema;

    /**
     * 指定table，非必填，未填写时代表schema下所有的table
     */
    @NotNull List<String> tables;

    /**
     * mapper.xml所在目录的相对路径（相对于Module Root）
     */
    @NotEmpty List<String> mapperXmlDirectoryPaths;

    /**
     * mapper接口的包名（根据目标工程的情况填写）
     */
    @NotEmpty String mapperPackage;

    /**
     * Entity类的包名（根据目标工程的情况填写）
     */
    @NotEmpty String entityPackage;

    /**
     * 是否为[query-transformer]生成Design类
     */
    @NotNull Boolean enableGenerateDesign;

    /**
     * QueryDesign类的包名（根据目标工程的情况填写）
     */
    @NotEmpty String designPackage;

    /**
     * mapper.xml的标签中，是否使用别名来引用Entity类
     */
    @NotNull Boolean isEntityUsingAlias;

    /**
     * # 生成出的Entity类是否以Entity作为类名的结尾
     */
    @NotNull Boolean isEntityEndWithEntity;

    /**
     * 如果有逻辑删除，怎么样算作“数据被删”，非必填，只支持等式SQL
     */
    String deletedSql;

    /**
     * 如果有逻辑删除，怎么样算作“数据未被删”，非必填，只支持等式SQL
     */
    String notDeletedSql;

    /**
     * 对项目隐藏，仅在数据库中可见的表字段
     */
    @NotNull List<String> hiddenColumns;

    /**
     * 即便符合persistence-generator对外键的定义，也不会被当作外键的表字段（一般用于忽略为创建人ID和更新人ID生成query方法）
     */
    @NotNull List<String> notKeyColumns;

    /**
     * Entity父类的全限定名
     */
    String superEntityQualifier;

    /**
     * 已在Entit父类中声明，无需在具体Entity中再次声明的表字段
     */
    @NotNull List<String> alreadyInSuperEntity;

    /**
     * 是否在该生成的地方生成 Any modifications may be overwritten by future code generations. 声明
     */
    @NotNull Boolean enableNoModifyAnnounce;

    /**
     * 是否在该生成的地方生成诸如 Allison 1875 Lot No: PG1000S-967D9357 的声明
     */
    @NotNull Boolean enableLotNoAnnounce;

    /**
     * 是否为entity实现java.io.Serializable接口
     */
    @NotNull Boolean enableEntityImplementSerializable;

    /**
     * 是否为entity实现java.lang.Cloneable接口
     */
    @NotNull Boolean enableEntityImplementCloneable;

    /**
     * 生成Entity时，文件已存在的解决方式
     */
    @NotNull FileExistenceResolutionEnum entityExistenceResolution;

    @NotNull Boolean disableInsert;

    @NotNull Boolean disableBatchInsert;

    @NotNull Boolean disableBatchInsertEvenNull;

    @NotNull Boolean disableBatchUpdate;

    @NotNull Boolean disableBatchUpdateEvenNull;

    @NotNull Boolean disableQueryById;

    @NotNull Boolean disableUpdateById;

    @NotNull Boolean disableUpdateByIdEvenNull;

    @NotNull Boolean disableQueryByIds;

    @NotNull Boolean disableQueryByIdsEachId;

    @NotNull Boolean disableQueryByKey;

    @NotNull Boolean disableDeleteByKey;

    @NotNull Boolean disableQueryByKeys;

    @NotNull Boolean disableQueryByEntity;

    @NotNull Boolean disableListAll;

    @NotNull Boolean disableInsertOrUpdate;

}