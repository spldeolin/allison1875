package com.spldeolin.allison1875.docanalyzer.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2025-03-15
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategorizedMarkdownDTO {

    List<String> hierarchicalCategories;

    String directCategory;

    String content;

}
