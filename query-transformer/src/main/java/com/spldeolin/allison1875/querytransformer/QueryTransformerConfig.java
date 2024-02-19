package com.spldeolin.allison1875.querytransformer;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.common.ancestor.Allison1875Config;
import com.spldeolin.allison1875.common.config.CommonConfig;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-08-09
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class QueryTransformerConfig extends Allison1875Config {

    /**
     * 共用配置
     */
    @NotNull @Valid CommonConfig commonConfig;

    /**
     * 是否在该生成的地方生成诸如 Allison 1875 Lot No: QT1000S-967D9357 的声明
     */
    @NotNull Boolean enableLotNoAnnounce;

}