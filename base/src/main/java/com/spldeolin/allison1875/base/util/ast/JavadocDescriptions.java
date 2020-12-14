package com.spldeolin.allison1875.base.util.ast;

import java.util.Collection;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.MoreStringUtils;

/**
 * 获取Javadoc注释部分，若Javadoc不存在时，所有方法均返回""
 *
 * @author Deolin 2019-12-23
 */
public class JavadocDescriptions {

    private JavadocDescriptions() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * 获取原始的description
     */
    static String getRaw(JavadocDescription javadocDescription) {
        return javadocDescription.toText();
    }

    /**
     * 获取原始的description
     */
    public static String getRaw(Javadoc javadoc) {
        return getRaw(javadoc.getDescription());
    }

    /**
     * 获取原始的description
     */
    public static String getRaw(NodeWithJavadoc<?> node) {
        return node.getJavadoc().map(JavadocDescriptions::getRaw).orElse("");
    }

    /**
     * 获取description，并按行分割到Collection中
     */
    static Collection<String> getAsLines(JavadocDescription javadocDescription) {
        return MoreStringUtils.splitLineByLine(getRaw(javadocDescription));
    }

    /**
     * 获取description，并按行分割到Collection中
     */
    public static Collection<String> getAsLines(Javadoc javadoc) {
        return getAsLines(javadoc.getDescription());
    }

    /**
     * 获取description，并按行分割到Collection中
     */
    public static Collection<String> getAsLines(NodeWithJavadoc<?> node) {
        return node.getJavadoc().map(JavadocDescriptions::getAsLines).orElse(Lists.newArrayList());
    }

    /**
     * 获取description的第一行
     */
    static String getFirstLine(JavadocDescription javadocDescription) {
        return Iterables.getFirst(getAsLines(javadocDescription), "");
    }

    /**
     * 获取description的第一行
     */
    public static String getFirstLine(Javadoc javadoc) {
        return getFirstLine(javadoc.getDescription());
    }

    /**
     * 获取description的第一行
     */
    public static String getFirstLine(NodeWithJavadoc<?> node) {
        return node.getJavadoc().map(JavadocDescriptions::getFirstLine).orElse("");
    }

}
