package com.spldeolin.allison1875.docanalyzer.util;

/**
 * 简易 Markdown → HTML 转换工具。
 *
 * <p>仅覆盖 doc-analyzer 中用到的 Markdown 特性：
 * <ul>
 *   <li>{@code **text**} → {@code <strong>text</strong>}</li>
 *   <li>{@code > text} → {@code <blockquote>text</blockquote>}</li>
 *   <li>{@code ##### text} → {@code <h5>text</h5>}</li>
 *   <li>{@code ---} → {@code <hr />}</li>
 * </ul>
 *
 * @author Deolin 2020-08-01
 */
public class MarkdownUtils {

    private MarkdownUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static String convertToHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }

        // 统一换行符
        String normalized = markdown.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = normalized.split("\n", -1);

        StringBuilder html = new StringBuilder();
        StringBuilder paragraphBuf = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith("> ")) {
                flushParagraph(html, paragraphBuf);
                html.append("<blockquote>");
                html.append(processInline(line.substring(2)));
                html.append("</blockquote>");
            } else if (line.startsWith("##### ")) {
                flushParagraph(html, paragraphBuf);
                html.append("<h5>").append(processInline(line.substring(6))).append("</h5>");
            } else if (line.equals("---")) {
                flushParagraph(html, paragraphBuf);
                html.append("<hr />");
            } else if (line.isEmpty()) {
                flushParagraph(html, paragraphBuf);
            } else {
                // 普通文本，累积为一个段落
                if (paragraphBuf.length() > 0) {
                    paragraphBuf.append("<br>");
                }
                paragraphBuf.append(processInline(line));
            }
        }
        flushParagraph(html, paragraphBuf);

        return html.toString();
    }

    private static void flushParagraph(StringBuilder html, StringBuilder paragraphBuf) {
        if (paragraphBuf.length() > 0) {
            html.append("<p>").append(paragraphBuf).append("</p>");
            paragraphBuf.setLength(0);
        }
    }

    private static String processInline(String text) {
        return text.replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>");
    }

}