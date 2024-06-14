package com.spldeolin.allison1875.startransformer;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.common.ancestor.Allison1875Config;
import com.spldeolin.allison1875.common.config.CommonConfig;
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
public final class StarTransformerConfig extends Allison1875Config {

    /**
     * 共用配置
     */
    @NotNull @Valid CommonConfig commonConfig;

    /**
     * Whole DTO的后缀
     */
    @NotNull String wholeDtoNamePostfix;

}