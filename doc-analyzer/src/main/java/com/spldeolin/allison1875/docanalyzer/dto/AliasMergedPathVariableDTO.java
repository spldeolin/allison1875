package com.spldeolin.allison1875.docanalyzer.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2025-01-25
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AliasMergedPathVariableDTO {

    String name;

    boolean required = true;

}
