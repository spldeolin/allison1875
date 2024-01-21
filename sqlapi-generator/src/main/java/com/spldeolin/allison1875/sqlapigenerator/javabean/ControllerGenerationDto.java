package com.spldeolin.allison1875.sqlapigenerator.javabean;

import com.spldeolin.allison1875.common.ast.FileFlush;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-01-21
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ControllerGenerationDto {

    FileFlush flush;

}