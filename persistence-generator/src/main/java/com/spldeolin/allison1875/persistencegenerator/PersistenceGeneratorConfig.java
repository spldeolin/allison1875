package com.spldeolin.allison1875.persistencegenerator;

import java.util.Collection;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.Configs;
import com.spldeolin.allison1875.base.util.YamlUtils;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-11
 */
@Data
@Log4j2
public class PersistenceGeneratorConfig {

    @Getter
    private static final PersistenceGeneratorConfig instance = YamlUtils
            .toObjectAndThen("persistence-generator-config.yml", PersistenceGeneratorConfig.class, Configs::validate);

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

    private Boolean disableInsert = false;

    private Boolean disableQueryById = false;

    private Boolean disableUpdateById = false;

    private Boolean disableUpdateByIdEvenNull = false;

    private Boolean disableQueryByIds = false;

    private Boolean disableQueryByIdsEachId = false;

    private Boolean disableQueryByKey = false;

    private Boolean disableDeleteByKey = false;

    private Boolean disableQueryByKeys = false;

    private PersistenceGeneratorConfig() {
    }

}