package com.spldeolin.allison1875.querytransformer;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.common.ancestor.Allison1875Config;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-08-09
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class QueryTransformerConfig extends Allison1875Config {

    /**
     * Mapper方法签名中Condition类的包名
     */
    @NotEmpty String mapperConditionPackage;

    /**
     * Mapper方法签名中Record类的包名
     */
    @NotEmpty String mapperRecordPackage;

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty String author;

    /**
     * Design类的包名
     */
    @NotEmpty String designPackage;

    /**
     * 是否在该生成的地方生成诸如 Allison 1875 Lot No: QT1000S-967D9357 的声明
     */
    @NotNull Boolean enableLotNoAnnounce;

}