package com.spldeolin.allison1875.handlertransformer;

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
 * Allison1875[handler-transformer]的配置
 *
 * @author Deolin 2020-08-25
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class HandlerTransformerConfig extends Allison1875Config {

    /**
     * 包配置
     */
    @NotNull @Valid PackageConfig packageConfig;

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty String author;

    /**
     * 分页对象的全限定名
     */
    @NotEmpty String pageTypeQualifier;

    /**
     * 是否在该生成的地方生成诸如 Allison 1875 Lot No: HT1000S-967D9357 的声明
     */
    @NotNull Boolean enableLotNoAnnounce;

}