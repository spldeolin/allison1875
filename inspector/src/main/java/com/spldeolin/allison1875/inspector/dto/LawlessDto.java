package com.spldeolin.allison1875.inspector.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.javaparser.ast.Node;
import com.spldeolin.allison1875.base.util.ast.Authors;
import com.spldeolin.allison1875.base.util.ast.Locations;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-02-22
 */
@Data
@Accessors(chain = true)
public class LawlessDto {

    @JsonProperty("源码位置")
    private String sourceCode;

    /**
     * - type qualifier
     * - field qualifier
     * - method qualifier
     * - or else null
     */
    @JsonProperty("全限定名")
    private String qualifier;

    @JsonProperty("规约号")
    private String statuteNo;

    @JsonProperty("详细信息")
    private String message;

    @JsonProperty("作者")
    private String author;

    @JsonProperty("修复者")
    private String fixer;

    @JsonProperty("修复时间")
    private LocalDateTime fixedAt;

    public LawlessDto(Node node, String qualifier, String message) {
        this.sourceCode = Locations.getRelativePathWithLineNo(node);
        this.qualifier = qualifier;
        this.message = message;
        this.author = Authors.getAuthor(node);
    }

}
