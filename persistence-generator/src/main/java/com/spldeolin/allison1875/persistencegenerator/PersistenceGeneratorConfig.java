package com.spldeolin.allison1875.persistencegenerator;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.common.ancestor.Allison1875Config;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-07-11
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class PersistenceGeneratorConfig extends Allison1875Config {

    /**
     * 共用配置
     */
    @NotNull @Valid CommonConfig commonConfig;

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
     * 指定schema
     */
    @NotEmpty String schema;

    /**
     * 指定table，非必填，未填写时代表schema下所有的table
     */
    @NotNull List<String> tables;

    /**
     * 是否为[query-transformer]生成Design类
     */
    @NotNull Boolean enableGenerateDesign;

    /**
     * 生成出的Entity类是否以Entity作为类名的结尾
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
     * 如果生成的Entity需要指定父类，指定父类的Class对象
     */
    Class<?> superEntity;

    /**
     * 生成Entity时，文件已存在的解决方式
     */
    @NotNull FileExistenceResolutionEnum entityExistenceResolution;

}