package com.spldeolin.allison1875.docanalyzer.javabean;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-09-12
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnumCodeAndTitleDto {

    String code;

    String title;

}