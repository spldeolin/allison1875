package com.spldeolin.allison1875.base.util.ast;

import java.util.List;
import java.util.Optional;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocDescriptionElement;
import com.github.javaparser.javadoc.description.JavadocSnippet;
import com.spldeolin.allison1875.base.util.Strings;

/**
 * @author Deolin 2019-12-23
 */
public class Javadocs {

    public static String extractFirstLine(Javadoc javadoc) {
        JavadocDescription description = javadoc.getDescription();
        return extractFirstLineFromDescription(description);
    }

    public static String extractFirstLine(NodeWithJavadoc<?> node) {
        return node.getJavadoc().map(Javadocs::extractFirstLine).orElse("");
    }

    public static String extractAuthorTag(Javadoc javadoc) {
        StringBuilder sb = new StringBuilder(64);
        javadoc.getBlockTags().stream().filter(tag -> Type.AUTHOR == tag.getType()).forEach(authorTag -> {
            JavadocDescription content = authorTag.getContent();
            sb.append(extractAllFromDescription(content));
        });
        return sb.toString();
    }

    public static String extractAuthorTag(NodeWithJavadoc<?> node) {
        return node.getJavadoc().map(Javadocs::extractAuthorTag).orElse("");
    }

    private static String extractFirstLineFromDescription(JavadocDescription description) {
        List<JavadocDescriptionElement> elements = description.getElements();
        if (elements.size() == 0) {
            return "";
        }

        Optional<JavadocDescriptionElement> firstSnippet = elements.stream()
                .filter(element -> element instanceof JavadocSnippet).findFirst();
        if (!firstSnippet.isPresent()) {
            return "";
        }

        List<String> lines = Strings.splitLineByLine(firstSnippet.get().toText());
        if (lines.size() == 0) {
            return "";
        }

        return lines.get(0);
    }

    private static String extractAllFromDescription(JavadocDescription description) {
        StringBuilder sb = new StringBuilder(64);
        description.getElements().stream().filter(ele -> ele instanceof JavadocSnippet)
                .forEach(snippet -> sb.append(snippet.toText()));
        return sb.toString();
    }

}
