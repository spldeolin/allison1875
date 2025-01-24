package com.spldeolin.allison1875.common.util;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.google.common.collect.Lists;

/**
 * Javadoc工具类
 *
 * @author Deolin 2021-03-05
 */
public class JavadocUtils {

    private JavadocUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * 为参数node设置Javadoc
     */
    public static Javadoc setJavadoc(NodeWithJavadoc<?> node, String comment, String author) {
        Javadoc javadoc = new JavadocComment(comment).parse();
        if (StringUtils.isNotBlank(author)) {
            javadoc.addBlockTag(new JavadocBlockTag(JavadocBlockTag.Type.AUTHOR, author));
        }
        node.setJavadocComment(javadoc);
        return javadoc;
    }

    /**
     * 获取参数node的Javadoc中的comment部分，返回值至少是Empty String
     */
    public static String getComment(NodeWithJavadoc<?> node) {
        return node.getJavadoc().map(javadoc -> javadoc.getDescription().toText()).orElse("");
    }

    /**
     * 获取参数node的Javadoc中的comment部分的每一行
     */
    public static List<String> getCommentAsLines(NodeWithJavadoc<?> node) {
        return MoreStringUtils.splitLineByLine(getComment(node));
    }

    /**
     * 获取参数node的作者信息
     *
     * @return 如果有多个作者，trim、distinct后拼接为半角逗号分隔的文本
     */
    public static String getAuthor(Node node) {
        List<String> authors = getEveryAuthor(node);
        return authors.stream().map(String::trim).distinct().collect(Collectors.joining(", "));
    }

    /**
     * 获取每个参数blockTagType的标签内容部分的每一行
     */
    public static List<String> getEveryLineByTag(NodeWithJavadoc<?> node, JavadocBlockTag.Type tagType,
            String tagName) {
        if (!node.getJavadoc().isPresent()) {
            return Lists.newArrayList();
        }
        List<String> result = Lists.newArrayList();
        for (JavadocBlockTag blockTag : node.getJavadoc().get().getBlockTags()) {
            if (blockTag.getType() != tagType) {
                continue;
            }
            if (tagName != null) { // 如果指定了tagName
                if (!blockTag.getName().filter(n -> n.equals(tagName)).isPresent()) { // 但这个tag的name不匹配
                    continue; // 则跳过，否则视为“匹配”
                }
            } // 不指定tagName视为“匹配了tagName”
            List<String> lines = MoreStringUtils.splitLineByLine(blockTag.getContent().toText());
            result.addAll(lines);
        }
        return result;
    }


    /**
     * 尽可能获取一个Node的所有作者信息
     */
    private static List<String> getEveryAuthor(Node node) {
        // 本节点withJavadoc，并且能获取到可见的@author内容时，直接返回
        if (node instanceof NodeWithJavadoc) {
            List<String> authors = getEveryLineByTag((NodeWithJavadoc<?>) node, JavadocBlockTag.Type.AUTHOR, null);
            if (CollectionUtils.isNotEmpty(authors)) {
                return authors;
            }
        }

        // 本节点没有有效的author信息时，尝试递归地寻找父节点的author信息
        Optional<Node> parentWithJavadoc = getParentWithJavadoc(node);
        if (parentWithJavadoc.isPresent()) {
            return getEveryAuthor(parentWithJavadoc.get());
        }

        // 递归到找不到withJavadoc的父节点时，返回empty
        return Lists.newArrayList();
    }

    /**
     * 获取属于NodeWithJavadoc的父节点
     */
    private static Optional<Node> getParentWithJavadoc(Node node) {
        Optional<Node> parentOpt = node.getParentNode();
        if (parentOpt.isPresent()) {
            Node parent = parentOpt.get();
            if (parent instanceof NodeWithJavadoc) {
                return Optional.of(parent);
            } else {
                return getParentWithJavadoc(parent);
            }
        }
        return Optional.empty();
    }

}