package com.spldeolin.allison1875.persistencegenerator;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.config.Allison1875Config;
import com.spldeolin.allison1875.common.dto.InvalidDTO;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.util.ValidUtils;
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
public class PersistenceGeneratorConfig extends Allison1875Config {

    /**
     * 数据库连接
     */
    String jdbcUrl;

    /**
     * 数据库用户名
     */
    String userName;

    /**
     * 数据库密码
     */
    String password;

    /**
     * 指定schema
     */
    String schema;

    /**
     * 使用指定的DDL，在In-memory H2中构建表结构
     */
    String ddl;

    /**
     * 指定table，非必填，未填写时代表schema下所有的table
     */
    List<String> tables = Lists.newArrayList();

    /**
     * 是否为[query-transformer]生成Design类
     */
    @NotNull
    Boolean enableGenerateDesign = true;

    /**
     * 生成出的Entity类是否以Entity作为类名的结尾
     */
    @NotNull
    Boolean isEntityEndWithEntity = true;

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
    @NotNull
    FileExistenceResolutionEnum entityExistenceResolution = FileExistenceResolutionEnum.OVERWRITE;

    /**
     * 是否生成Intell IDEA的“Turn formatter on/off with makers in code comments”
     */
    @NotNull
    Boolean enableGenerateFormatterMarker = true;

    @Override
    public List<InvalidDTO> invalidSelf() {
        List<InvalidDTO> result = super.invalidSelf();
        if (StringUtils.isNotEmpty(jdbcUrl)) {
            if (StringUtils.isEmpty(userName)) {
                result.add(new InvalidDTO().setPath("userName").setValue(ValidUtils.formatValue(userName))
                        .setReason("must not be empty"));
            }
            if (StringUtils.isEmpty(password)) {
                result.add(new InvalidDTO().setPath("password").setValue(ValidUtils.formatValue(password))
                        .setReason("must not be empty"));
            }
            if (StringUtils.isEmpty(schema)) {
                result.add(new InvalidDTO().setPath("schema").setValue(ValidUtils.formatValue(schema))
                        .setReason("must not be empty"));
            }
        }
        return result;
    }

}