package com.spldeolin.allison1875.base.util.ast;

import java.util.Collection;
import java.util.Optional;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.github.javaparser.javadoc.description.JavadocDescriptionElement;
import com.github.javaparser.javadoc.description.JavadocSnippet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.JsonUtils;

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
     * 获取一个CU的作者信息，
     * 如果有与文件名同名的类型声明，则获取这个类型声明的作者信息，
     * 否则获取所有最外层类型声明的作者信息的集合
     */
    public static String getAuthor(CompilationUnit cu) {
        Collection<String> authors;
        if (cu.getPrimaryType().isPresent()) {
            authors = listAuthors(cu.getPrimaryType().get());
        } else {
            authors = Lists.newLinkedList();
            cu.getTypes().forEach(type -> authors.addAll(Authors.listAuthors(type)));
        }
        return concat(authors);
    }

    /**
     * 获取一个Node的作者信息，
     * 1. 如果Node能声明Javadoc，则获取这个Node的作者信息
     * 2. 如果1. 没有收获，则尝试获取这个Node的第一个能声明Javadoc的祖先Node
     * 3. 递归1. 和2.
     */
    public static String getAuthor(Node node) {
        return concat(listAuthors(node));
    }

    /**
     * Node是否没有作者信息
     */
    public static boolean isAuthorAbsent(Node node) {
        return listAuthors(node).size() == 0;
    }

    private static Collection<String> listAuthors(Node node) {
        Collection<String> authors = Lists.newArrayList();
        if (node instanceof NodeWithJavadoc) {
            ((NodeWithJavadoc<?>) node).getJavadoc().ifPresent(
                    value -> value.getBlockTags().stream().filter(Authors::isAuthorTag).forEach(
                            tag -> tag.getContent().getElements().stream().filter(Authors::isJavadocSnippet)
                                    .forEach(ele -> {
                                        authors.add(ele.toText());
                                    })));
        }

        // 没有有效的author信息时，寻找父节点的author信息
        if (authors.size() == 0) {
            Optional<Node> parentWithJavadoc = getParentWithJavadoc(node);
            if (parentWithJavadoc.isPresent()) {
                return listAuthors(parentWithJavadoc.get());
            }
        }

        return authors;
    }

    private static String concat(Collection<String> authors) {
        if (authors.size() == 0) {
            return "Unknown author";
        } else if (authors.size() == 1) {
            return Iterables.getOnlyElement(authors);
        } else {
            return JsonUtils.toJson(authors);
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

    private static boolean isAuthorTag(JavadocBlockTag tag) {
        return Type.AUTHOR == tag.getType();
    }

    private static boolean isJavadocSnippet(JavadocDescriptionElement ele) {
        return ele instanceof JavadocSnippet;
    }

}
