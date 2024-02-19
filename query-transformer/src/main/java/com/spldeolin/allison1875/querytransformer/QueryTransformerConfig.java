package com.spldeolin.allison1875.querytransformer;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.common.ancestor.Allison1875Config;
import com.spldeolin.allison1875.common.config.PackageConfig;
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
     * 包配置
     */
    @NotNull @Valid PackageConfig packageConfig;

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty String author;

    /**
     * 是否在该生成的地方生成诸如 Allison 1875 Lot No: QT1000S-967D9357 的声明
     */
    @NotNull Boolean enableLotNoAnnounce;

}