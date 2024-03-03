package com.spldeolin.allison1875.common.config;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2023-05-05
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class CommonConfig {

    /**
     * 控制层@RequestBody类型所在包的包名
     */
    @NotEmpty String reqDtoPackage;

    /**
     * 控制层@ResponseBody业务数据部分类型所在包的包名
     */
    @NotEmpty String respDtoPackage;

    /**
     * 业务层Service接口所在包的包名
     */
    @NotEmpty String servicePackage;

    /**
     * 业务层ServiceImpl类所在包的包名
     */
    @NotEmpty String serviceImplPackage;

    /**
     * 持久层mapper接口所在包的包名
     */
    @NotEmpty String mapperPackage;

    /**
     * 持久层Entity类所在包的包名
     */
    @NotEmpty String entityPackage;

    /**
     * Design类所在包的包名
     */
    @NotEmpty String designPackage;

    /**
     * 持久层Mapper方法签名中Cond类所在包的包名
     */
    @NotEmpty String condPackage;

    /**
     * 持久层Mapper方法签名中Record类所在包的包名
     */
    @NotEmpty String recordPackage;

    /**
     * WholeDto类所在包的包名
     */
    @NotEmpty String wholeDtoPackage;

    /**
     * mapper.xml所在目录的相对路径（相对于Module Root）
     */
    @NotEmpty List<@NotNull String> mapperXmlDirectoryPaths;

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty String author;

    /**
     * 生成的Javabean是否实现java.io.Serializable接口
     */
    @NotNull Boolean isJavabeanSerializable;

    /**
     * 生成的Javabean是否实现java.lang.Cloneable接口
     */
    @NotNull Boolean isJavabeanCloneable;

}