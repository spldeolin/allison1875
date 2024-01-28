package com.spldeolin.allison1875.sqlapigenerator;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import com.spldeolin.allison1875.common.ancestor.Allison1875Config;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
import com.spldeolin.allison1875.common.util.ValidUtils;
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
     * Mapper方法签名中Condition类的包名
     */
    @NotEmpty String mapperConditionPackage;

    /**
     * Mapper方法签名中Record类的包名
     */
    @NotEmpty String mapperRecordPackage;

    /**
     * mapper.xml所在目录的相对路径（相对于Module Root）
     */
    @NotEmpty List<String> mapperXmlDirectoryPaths;

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty String author;

    /**
     * 方法名
     */
    @NotEmpty String methodName;

    @NotEmpty String sql;

    @NotEmpty String mapperName;

    String serviceName;

    String controllerName;

    @Override
    public List<InvalidDto> invalidSelf() {
        List<InvalidDto> invalids = super.invalidSelf();
        if (controllerName != null) {
            if (serviceName == null) {
                invalids.add(new InvalidDto().setPath("serviceName").setValue(ValidUtils.formatValue(serviceName))
                        .setReason("must not be null when 'controllerName' is not null"));
            }
        }
        return invalids;
    }

}