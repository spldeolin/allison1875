package com.spldeolin.allison1875.sqlapigenerator;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.common.ancestor.Allison1875Config;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
import com.spldeolin.allison1875.common.util.ValidUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-01-20
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SqlapiGeneratorConfig extends Allison1875Config {

    /**
     * 共用配置
     */
    @NotNull @Valid CommonConfig commonConfig;

    /**
     * 方法名
     */
    @NotEmpty String methodName;

    /**
     * select返回List还是单个？
     */
    @NotNull Boolean selectListOrOne;

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