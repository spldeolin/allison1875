package com.spldeolin.allison1875.docanalyzer.dto;

import java.util.List;
import com.spldeolin.allison1875.docanalyzer.enums.ValueTypeEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2025-01-24
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PathParamDTO {

    String name;

    ValueTypeEnum type;

    List<String> descriptionLines;

}