package com.spldeolin.allison1875.docanalyzer.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-02-12
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnalyzeMvcHandlerRetval {

    /**
     * 直接分类
     */
    String directCategory;

    /**
     * 层级分类
     */
    List<String> hierarchicalCategories;

    List<String> descriptionLines;

    String deprecatedDescription;

    String sinceVersion;

    String author;

    String sourceCode;

    Object moreInfo;

}