package com.spldeolin.allison1875.common.util.ast;

import java.util.List;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.google.common.collect.Lists;

/**
 * 获取Javadoc标签部分
 *
 * @author Deolin 2020-06-19
 */
public class JavadocTags {

    private JavadocTags() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * 获取每个参数blockTagType的标签内容部分的每一行
     *
     * @return 每个String均已trim()
     */
    public static List<String> getEveryLineByTag(Javadoc javadoc, Type blockTagType) {
        List<String> result = Lists.newArrayList();
        for (JavadocBlockTag blockTag : javadoc.getBlockTags()) {
            if (blockTag.getType() == blockTagType) {
                JavadocDescription content = blockTag.getContent();
                List<String> lines = JavadocDescriptions.getAsLines(content);
                result.addAll(lines);
            }
        }
        return result;
    }

    /**
     * 重载自 {@linkplain JavadocTags#getEveryLineByTag(Javadoc,
     * Type)}
     */
    public static List<String> getEveryLineByTag(NodeWithJavadoc<?> node, Type blockTagType) {
        return node.getJavadoc().map(javadoc -> getEveryLineByTag(javadoc, blockTagType)).orElse(Lists.newArrayList());
    }

}
