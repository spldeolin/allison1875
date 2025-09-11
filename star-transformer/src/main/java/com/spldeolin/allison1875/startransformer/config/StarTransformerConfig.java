package com.spldeolin.allison1875.startransformer.config;

import javax.validation.constraints.NotNull;
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
public class StarTransformerConfig {

    /**
     * Whole DTO的后缀
     */
    @NotNull
    String wholeDTONamePostfix = "WholeDTO";

}