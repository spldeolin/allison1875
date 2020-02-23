package com.spldeolin.allison1875.si.vo;

import java.time.LocalDateTime;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.util.Cus;
import com.spldeolin.allison1875.base.util.Locations;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-02-22
 */
@Data
@Accessors(chain = true)
public class LawlessVo {

    private String codeSource;

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

    public LawlessVo(Node node, String qualifier) {
        codeSource = Locations.getRelativePathWithLineNo(node);
        this.qualifier = qualifier;
        author = Cus.getAuthor(node.findCompilationUnit().orElseThrow(CuAbsentException::new));
    }

    public LawlessVo(Node node) {
        codeSource = Locations.getRelativePathWithLineNo(node);
        author = Cus.getAuthor(node.findCompilationUnit().orElseThrow(CuAbsentException::new));
    }

}
