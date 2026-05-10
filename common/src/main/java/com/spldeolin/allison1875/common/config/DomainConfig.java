package com.spldeolin.allison1875.common.config;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * 业务领域配置，描述一个业务领域中各层次代码的模块位置和包名。
 * 支持从单模块到最复杂的垂直+水平拆分场景。
 *
 * @author Deolin 2026-04-28
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DomainConfig {

    /**
     * 业务领域名称（如 user、order），用于 -Ddomain 参数匹配
     */
    @NotEmpty
    String name;

    // ==================== 控制层 ====================

    /**
     * Controller类所在Maven模块的绝对路径
     */
    @NotEmpty
    String controllerModule;

    /**
     * 控制器所在包的包名
     */
    @NotEmpty
    String controllerPackage;

    // ==================== DTO层（reqDTO + respDTO 共用module） ====================

    /**
     * reqDTO和respDTO类所在Maven模块的绝对路径
     */
    @NotEmpty
    String dtoModule;

    /**
     * 控制层@RequestBody类型所在包的包名
     */
    @NotEmpty
    String reqDTOPackage;

    /**
     * 控制层@ResponseBody业务数据部分类型所在包的包名
     */
    @NotEmpty
    String respDTOPackage;

    // ==================== 枚举层 ====================

    /**
     * 枚举类所在Maven模块的绝对路径
     */
    @NotEmpty
    String enumModule;

    /**
     * 枚举所在包的包名
     */
    @NotEmpty
    String enumPackage;

    // ==================== 业务层 Service ====================

    /**
     * Service接口所在Maven模块的绝对路径
     */
    @NotEmpty
    String serviceModule;

    /**
     * 业务层Service接口所在包的包名
     */
    @NotEmpty
    String servicePackage;

    // ==================== 业务层 ServiceImpl ====================

    /**
     * ServiceImpl类所在Maven模块的绝对路径
     */
    @NotEmpty
    String serviceImplModule;

    /**
     * 业务层ServiceImpl类所在包的包名
     */
    @NotEmpty
    String serviceImplPackage;

    // ==================== 持久层（mapper + entity + design + paramDTO + recordDTO + mapperXmlDirs 共用module）
    // ====================

    /**
     * 持久层代码所在Maven模块的绝对路径
     */
    @NotEmpty
    String persistenceModule;

    /**
     * 持久层mapper接口所在包的包名
     */
    @NotEmpty
    String mapperPackage;

    /**
     * 持久层Entity类所在包的包名
     */
    @NotEmpty
    String entityPackage;

    /**
     * Design类所在包的包名
     */
    @NotEmpty
    String designPackage;

    /**
     * 持久层Mapper方法签名中Param类所在包的包名
     */
    @NotEmpty
    String paramDTOPackage;

    /**
     * 持久层Mapper方法签名中Record类所在包的包名
     */
    @NotEmpty
    String recordDTOPackage;

    /**
     * mapper.xml所在目录（相对于持久层module的basedir的相对路径 或 绝对路径 皆可）
     */
    @NotEmpty
    List<File> mapperXmlDirs = Lists.newArrayList(new File("src/main/resources/mapper"));

    /**
     * WholeDTO类所在包的包名
     */
    @NotEmpty
    String wholeDTOPackage;

    // ==================== 以下为运行时解析后的SourceRoot路径，由Mojo自动填充，不在yml中配置 ====================

    /**
     * Controller层的SourceRoot绝对路径
     */
    transient Path controllerSourceRoot;

    /**
     * DTO层的SourceRoot绝对路径
     */
    transient Path dtoSourceRoot;

    /**
     * 枚举层的SourceRoot绝对路径
     */
    transient Path enumSourceRoot;

    /**
     * Service接口的SourceRoot绝对路径
     */
    transient Path serviceSourceRoot;

    /**
     * ServiceImpl的SourceRoot绝对路径
     */
    transient Path serviceImplSourceRoot;

    /**
     * 持久层的SourceRoot绝对路径
     */
    transient Path persistenceSourceRoot;

}
