package com.spldeolin.allison1875.common.config;

import java.util.List;
import javax.validation.constraints.NotEmpty;
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
public final class PackageConfig {

    /**
     * 控制层 @RequestBody类型所在包的包名
     */
    @NotEmpty String reqDtoPackage;

    /**
     * 控制层 @ResponseBody业务数据部分类型所在包的包名
     */
    @NotEmpty String respDtoPackage;

    /**
     * 业务层 Service接口所在包的包名
     */
    @NotEmpty String servicePackage;

    /**
     * 业务 ServiceImpl类所在包的包名
     */
    @NotEmpty String serviceImplPackage;

    /**
     * mapper接口的包名（根据目标工程的情况填写）
     */
    @NotEmpty String mapperPackage;

    /**
     * Entity类的包名（根据目标工程的情况填写）
     */
    @NotEmpty String entityPackage;

    /**
     * QueryDesign类的包名（根据目标工程的情况填写）
     */
    @NotEmpty String designPackage;

    /**
     * Mapper方法签名中Condition类的包名
     */
    @NotEmpty String condPackage;

    /**
     * Mapper方法签名中Record类的包名
     */
    @NotEmpty String recordPackage;

    /**
     * WholeDto类的包名（根据目标工程的情况填写）
     */
    @NotEmpty String wholeDtoPackage;

    /**
     * mapper.xml所在目录的相对路径（相对于Module Root）
     */
    @NotEmpty List<String> mapperXmlDirectoryPaths;

}