package com.spldeolin.allison1875.base.util.ast;

import java.util.List;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.google.common.base.Joiner;
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
     * 如果Javadoc内有多行注释，提取注释的第一行
     */
    public static String extractFirstLine(Javadoc javadoc) {
        JavadocDescription description = javadoc.getDescription();
        List<String> lines = StringUtils.splitLineByLine(description.toText());
        if (lines.size() == 0) {
            return "";
        } else {
            return lines.get(0);
        }
    }

    /**
     * 如果Javadoc内有多行注释，提取注释的第一行
     */
    public static String extractFirstLine(NodeWithJavadoc<?> node) {
        return node.getJavadoc().map(Javadocs::extractFirstLine).orElse("");
    }

    /**
     * 如果Javadoc内有多行注释，每行注释用分隔符拼接成一行后返回
     */
    public static String extractEveryLine(Javadoc javadoc, String sep) {
        JavadocDescription description = javadoc.getDescription();
        List<String> lines = StringUtils.splitLineByLine(description.toText());
        return Joiner.on(sep).join(lines);
    }

    /**
     * 如果Javadoc内有多行注释，每行注释用分隔符拼接成一行后返回
     */
    public static String extractEveryLine(NodeWithJavadoc<?> node, String sep) {
        return node.getJavadoc().map(javadoc -> extractEveryLine(javadoc, sep)).orElse("");
    }

    /**
     * 如果Javadoc内有多行注释，则提取每一行
     */
    public static List<String> extractEveryLine(Javadoc javadoc) {
        JavadocDescription description = javadoc.getDescription();
        List<String> lines = StringUtils.splitLineByLine(description.toText());
        return lines;
    }

    /**
     * 如果Javadoc内有多行注释，则提取每一行
     */
    public static List<String> extractEveryLine(NodeWithJavadoc<?> node) {
        return node.getJavadoc().map(Javadocs::extractEveryLine).orElse(Lists.newArrayList());
    }

}
