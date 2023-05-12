package com.spldeolin.allison1875.startransformer;

import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author Deolin 2023-05-05
 */
@Data
public final class StarTransformerConfig {

    /**
     * QueryDesign类的包名（根据目标工程的情况填写）
     */
    @NotEmpty
    private String designPackage;

    /**
     * WholeDto类的包名（根据目标工程的情况填写）
     */
    @NotEmpty
    private String wholeDtoPackge;

}