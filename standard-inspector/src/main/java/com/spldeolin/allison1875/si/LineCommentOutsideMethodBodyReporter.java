package com.spldeolin.allison1875.si;

import static com.spldeolin.allison1875.base.util.Locations.getRange;
import static com.spldeolin.allison1875.base.util.Locations.getRelativePath;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.spldeolin.allison1875.base.GlobalCollectionStrategy;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import lombok.extern.log4j.Log4j2;

/**
 * 报告出出现在方法体外的行注释（Line Comment）
 *
 * @author Deolin 2020-02-10
 */
@Log4j2
public class LineCommentOutsideMethodBodyReporter {

    public static void main(String[] args) {
        new LineCommentOutsideMethodBodyReporter().processor();
    }

    private void processor() {
        GlobalCollectionStrategy.setDoNotCollectWithLoadingClass(false);
        StaticAstContainer.forEachCompilationUnits(cu -> cu.findAll(LineComment.class).forEach(lineComment -> {

            // 不在大括号内，或是在类型声明的大括号内
            if (isNotInBlock(lineComment) || isDirectlyInTypeBlock(lineComment)) {
                // 忽略被注释掉的情况（这种情况往往开头都是空格）
                if (!lineComment.getContent().startsWith("  ")) {

                    log.info("{}:{}", getRelativePath(lineComment), getRange(lineComment).begin.line);
                }
            }
        }));

    }

    /**
     * e.g.
     * <pre>
     *     // argument Node
     *     public class A {}
     * </pre>
     */
    private boolean isNotInBlock(Node node) {
        return !node.getParentNode().filter(parent -> parent instanceof BlockStmt).isPresent();
    }

    /**
     * e.g.
     * <pre>
     *      public enum B {
     *
     *          // argument Node
     *          ONE_ELEMENT;
     *
     *      }
     * </pre>
     */
    private boolean isDirectlyInTypeBlock(LineComment lineComment) {
        return lineComment.getParentNode().filter(parent -> parent instanceof TypeDeclaration<?>).isPresent();
    }

}
