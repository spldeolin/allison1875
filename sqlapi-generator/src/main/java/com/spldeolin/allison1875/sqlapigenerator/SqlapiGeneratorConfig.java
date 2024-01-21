package com.spldeolin.allison1875.sqlapigenerator;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import com.spldeolin.allison1875.common.ancestor.Allison1875Config;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-01-20
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SqlapiGeneratorConfig extends Allison1875Config {

    /**
     * 控制层 @RequestBody类型所在包的包名
     */
    @NotEmpty String reqDtoPackage;

    /**
     * 控制层 @ResponseBody业务数据部分类型所在包的包名
     */
    @NotEmpty String respDtoPackage;

    /**
     * mapper.xml所在目录的相对路径（相对于Module Root）
     */
    @NotEmpty List<String> mapperXmlDirectoryPaths;

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty String author;

    @NotEmpty String sql;

    @NotEmpty String controllerName;

    @NotEmpty String serviceName;

    @NotEmpty String mapperName;

}