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
 * 获取Javadoc注释部分
 *
 * @author Deolin 2019-12-23
 */
public class JavadocDescriptions {

    private JavadocDescriptions() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * 获取Javadoc中注释部分的第一行（经trim）
     */
    public static String getTrimmedFirstLine(Javadoc javadoc, boolean emptyToNull) {
        Collection<String> strings = getEveryLine(javadoc);
        String result = Iterables.getFirst(strings, null);
        if (result != null) {
            result = result.trim();
            if (emptyToNull && result.length() == 0) {
                return null;
            }
        }
        return result;
    }

    /**
     * 重载自 {@linkplain JavadocDescriptions#getTrimmedFirstLine(Javadoc)}
     */
    public static String getTrimmedFirstLine(NodeWithJavadoc<?> node, boolean emptyToNull) {
        return node.getJavadoc().map(jd -> getTrimmedFirstLine(jd, emptyToNull)).orElse(null);
    }

    /**
     * 获取Javadoc中注释部分的每一行，并使用参数sep拼接成一行
     */
    public static String getEveryLineInOne(Javadoc javadoc, String sep) {
        return Joiner.on(sep).join(getEveryLine(javadoc));
    }

    /**
     * 重载自 {@linkplain JavadocDescriptions#getEveryLineInOne(Javadoc, String)}
     */
    public static String getEveryLineInOne(NodeWithJavadoc<?> node, String sep) {
        return node.getJavadoc().map(javadoc -> getEveryLineInOne(javadoc, sep)).orElse("");
    }

    /**
     * 获取Javadoc中注释部分的每一行
     */
    public static Collection<String> getEveryLine(Javadoc javadoc) {
        JavadocDescription description = javadoc.getDescription();
        return getEveryLine(description);
    }

    /**
     * 重载自 {@linkplain JavadocDescriptions#getEveryLine(Javadoc)}
     */
    public static Collection<String> getEveryLine(NodeWithJavadoc<?> node) {
        return node.getJavadoc().map(JavadocDescriptions::getEveryLine).orElse(Lists.newArrayList());
    }

    /**
     * 获取Javadoc中注释部分的每一行
     */
    public static Collection<String> getEveryLine(JavadocDescription description) {
        String rawComment = description.toText();
        List<String> lines = StringUtils.splitLineByLine(rawComment);
        return lines;
    }

}
