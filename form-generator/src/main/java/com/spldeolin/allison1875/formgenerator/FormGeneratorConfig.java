package com.spldeolin.allison1875.formgenerator;

import javax.validation.constraints.NotEmpty;
import com.spldeolin.allison1875.common.config.Allison1875Config;
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
public class FormGeneratorConfig extends Allison1875Config {

    @NotEmpty
    String dsl;

}