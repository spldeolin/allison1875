package com.spldeolin.allison1875.persistencegenerator;

import java.util.Collection;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.exception.ConfigInvalidException;
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
    private static final PersistenceGeneratorConfig instace = YamlUtils
            .toObjectAndThen("persistence-generator-config.yml", PersistenceGeneratorConfig.class,
                    PersistenceGeneratorConfig::validate);

    private static void validate(PersistenceGeneratorConfig config) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<PersistenceGeneratorConfig>> valids = validator.validate(config);
        valids.forEach(valid -> log.error("配置项校验未通过：{}{}", valid.getPropertyPath(), valid.getMessage()));
        if (valids.size() > 0) {
            throw new ConfigInvalidException();
        }
    }

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
     * Maven工程源码的布局（一般不需要改动此项）
     */
    @NotEmpty
    private String javaDirectoryLayout;

    /**
     * mapper.xml所在目录的相对路径（根据目标工程的情况填写，一般不需要改动此项）
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

    private Boolean disableInsert = true;

    private Boolean disableQueryById = true;

    private Boolean disableUpdateById = true;

    private Boolean disableUpdateByIdEvenNull = true;

    private Boolean disableQueryByIds = true;

    private Boolean disableQueryByIdsEachId = true;

    private Boolean disableQueryByKey = true;

    private Boolean disableDeleteByKey = true;

    private Boolean disableQueryByKeys = true;

    private PersistenceGeneratorConfig() {
    }

}