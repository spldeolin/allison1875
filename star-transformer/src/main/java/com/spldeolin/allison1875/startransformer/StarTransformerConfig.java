package com.spldeolin.allison1875.startransformer;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.common.ancestor.Allison1875Config;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2023-05-05
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class StarTransformerConfig extends Allison1875Config {

    /**
     * QueryDesign类的包名（根据目标工程的情况填写）
     */
    @NotEmpty String designPackage;

    /**
     * WholeDto类的包名（根据目标工程的情况填写）
     */
    @NotEmpty String wholeDtoPackage;

    /**
     * 是否为WholeDto类实现java.io.Serializable接口
     */
    @NotNull Boolean enableImplementSerializable;

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty String author;

    /**
     * 是否在该生成的地方生成诸如 Allison 1875 Lot No: ST1000S-967D9357 的声明
     */
    @NotNull Boolean enableLotNoAnnounce;

    /**
     * Whole DTO的后缀
     */
    @NotNull String wholeDtoNamePostfix;

}