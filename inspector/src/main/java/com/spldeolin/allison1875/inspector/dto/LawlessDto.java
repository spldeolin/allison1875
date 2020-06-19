package com.spldeolin.allison1875.inspector.dto;

import java.time.LocalDateTime;
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

    private String sourceCode;

    /**
     * - type qualifier
     * - field qualifier
     * - method qualifier
     * - or else null
     */
    private String qualifier;

    private String statuteNo;

    private String message;

    private String author;

    private String fixer;

    private LocalDateTime fixedAt;

    public LawlessDto(Node node, String qualifier) {
        sourceCode = Locations.getRelativePathWithLineNo(node);
        this.qualifier = qualifier;
        author = Authors.getAuthor(node);
    }

    public LawlessDto(Node node) {
        sourceCode = Locations.getRelativePathWithLineNo(node);
        author = Authors.getAuthor(node);
    }

}
