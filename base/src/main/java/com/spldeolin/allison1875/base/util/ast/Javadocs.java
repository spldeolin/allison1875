package com.spldeolin.allison1875.base.util.ast;

import java.util.Collection;
import java.util.List;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.StringUtils;

/**
 * @author Deolin 2019-12-23
 */
public class Javadocs {

    private Javadocs() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * 获取Javadoc中注释部分的第一行
     *
     * @return trim()后的字符串，或者""
     */
    public static String getFirstLine(Javadoc javadoc) {
        Collection<String> strings = getEveryLine(javadoc);
        return Iterables.getFirst(strings, "");
    }

    /**
     * 获取Javadoc中注释部分的第一行
     *
     * @return trim()后的字符串，或者""
     */
    public static String getFirstLine(NodeWithJavadoc<?> node) {
        return node.getJavadoc().map(Javadocs::getFirstLine).orElse("");
    }

    /**
     * 获取Javadoc中注释部分的每一行，并使用参数sep拼接成一行
     *
     * @return trim()后的字符串，或者""
     */
    public static String getEveryLine(Javadoc javadoc, String sep) {
        return Joiner.on(sep).join(getEveryLine(javadoc));
    }

    /**
     * 获取Javadoc中注释部分的每一行，并使用参数sep拼接成一行
     *
     * @return trim()后的字符串，或者""
     */
    public static String getEveryLine(NodeWithJavadoc<?> node, String sep) {
        return node.getJavadoc().map(javadoc -> getEveryLine(javadoc, sep)).orElse("");
    }

    /**
     * 获取Javadoc中注释部分的每一行
     *
     * @retrun 每个String均已trim()
     */
    public static Collection<String> getEveryLine(Javadoc javadoc) {
        JavadocDescription description = javadoc.getDescription();
        String rawComment = description.toText();
        List<String> lines = StringUtils.splitLineByLine(rawComment);
        lines.replaceAll(String::trim);
        return lines;
    }

    /**
     * 获取Javadoc中注释部分的每一行
     *
     * @retrun 每个String均已trim()
     */
    public static Collection<String> getEveryLine(NodeWithJavadoc<?> node) {
        return node.getJavadoc().map(Javadocs::getEveryLine).orElse(Lists.newArrayList());
    }

}
