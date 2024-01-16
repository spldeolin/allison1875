package com.spldeolin.allison1875.inspector.javabean;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.javaparser.ast.Node;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.ast.Locations;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-02-22
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LawlessDto {

    @JsonProperty("源码位置")
    String sourceCode;

    /**
     * - type qualifier
     * - field qualifier
     * - method qualifier
     * - or else null
     */
    @JsonProperty("全限定名")
    String qualifier;

    @JsonProperty("规约号")
    String statuteNo;

    @JsonProperty("详细信息")
    String message;

    @JsonProperty("作者")
    String author;

    @JsonProperty("修复者")
    String fixer;

    @JsonProperty("修复时间")
    LocalDateTime fixedAt;

    public LawlessDto(Node node, String qualifier, String message) {
        this.sourceCode = Locations.getAbsolutePathWithLineNo(node);
        this.qualifier = qualifier;
        this.message = message;
        this.author = JavadocUtils.getAuthor(node);
    }

}
