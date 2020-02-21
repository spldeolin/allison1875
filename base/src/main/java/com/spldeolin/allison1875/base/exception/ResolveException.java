package com.spldeolin.allison1875.base.exception;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.spldeolin.allison1875.base.util.Locations;
import lombok.Getter;

/**
 * 捕获调用resolve的rt异常后需要抛出的异常
 *
 * @author Deolin 2020-02-21
 */
@Getter
public class ResolveException extends Exception {

    private Node node;

    private String name;

    private String codeSource;

    public ResolveException(Node node, Throwable cause) {
        super(cause);
        this.node = node;
        this.codeSource = Locations.getRelativePathWithLineNo(node);
        if (node instanceof NodeWithSimpleName) {
            this.name = ((NodeWithSimpleName<?>) node).getNameAsString();
        }
    }

    private static final long serialVersionUID = -5388920054996993904L;

}
