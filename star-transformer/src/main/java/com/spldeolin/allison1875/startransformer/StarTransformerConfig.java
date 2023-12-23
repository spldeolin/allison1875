package com.spldeolin.allison1875.startransformer;

import javax.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2023-05-05
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class StarTransformerConfig {

    /**
     * QueryDesign类的包名（根据目标工程的情况填写）
     */
    @NotEmpty String designPackage;

    /**
     * WholeDto类的包名（根据目标工程的情况填写）
     */
    @NotEmpty String wholeDtoPackge;

    /**
     * 是否为entity实现java.io.Serializable接口
     */
    Boolean enableEntityImplementSerializable;

}