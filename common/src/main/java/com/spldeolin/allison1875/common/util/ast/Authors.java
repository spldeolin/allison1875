package com.spldeolin.allison1875.common.util.ast;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * 根据Javadoc的@author标签，获取作者信息
 *
 * @author Deolin 2020-02-29
 */
public class Authors {

    private Authors() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * 获取一个Node的所有作者
     * <p>
     * 获取方式参考{@link Authors#getEveryAuthor(Node)}
     *
     * @return 如果有多个作者，去重后每个作者信息为一行，每行均已trim
     */
    public static String getAuthor(Node node) {
        return distinctAndConcat(getEveryAuthor(node));
    }

    /**
     * 获取一个Node的作者信息，
     * 1. 如果Node能声明Javadoc，则获取这个Node的作者信息
     * 2. 如果1. 没有收获，则尝试获取这个Node的第一个能声明Javadoc的祖先Node
     * 3. 递归1. 和2.
     */
    private static Collection<String> getEveryAuthor(Node node) {
        // 本节点withJavadoc，并且能获取到可见的@author内容时，直接返回
        if (node instanceof NodeWithJavadoc) {
            Collection<String> authors = JavadocTags.getEveryLineByTag((NodeWithJavadoc<?>) node, Type.AUTHOR);
            if (authors.size() > 0) {
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

    public static void ensureAuthorExist(NodeWithJavadoc<?> node, String authorName) {
        if (StringUtils.isEmpty(authorName)) {
            throw new IllegalArgumentException("authorName cannot be empty");
        }
        Optional<Javadoc> javadocOpt = node.getJavadoc();
        if (javadocOpt.isPresent()) {
            Javadoc javadoc = javadocOpt.get();
            if (javadoc.getBlockTags().stream()
                    .noneMatch(javadocBlockTag -> javadocBlockTag.getType() == Type.AUTHOR)) {
                javadoc.addBlockTag(Type.AUTHOR.name(), authorName + " " + LocalDate.now());
            }
        } else {
            Javadoc javadoc = new JavadocComment("").parse();
            javadoc.addBlockTag(new JavadocBlockTag(Type.AUTHOR, authorName + " " + LocalDate.now()));
            node.setJavadocComment(javadoc);
        }
    }

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

    private static String distinctAndConcat(Collection<String> authors) {
        if (authors.size() == 0) {
            return "";
        } else if (authors.size() == 1) {
            return Iterables.getOnlyElement(authors);
        } else {
            List<String> distinctAuthors = authors.stream().distinct().collect(Collectors.toList());
            return Joiner.on('\n').join(distinctAuthors);
        }
    }

}
