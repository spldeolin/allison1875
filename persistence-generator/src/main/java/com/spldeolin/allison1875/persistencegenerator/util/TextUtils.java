package com.spldeolin.allison1875.persistencegenerator.util;

import java.util.List;
import java.util.ListIterator;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Deolin 2021-08-30
 */
public class TextUtils {

    public static List<String> formatLines(String everyLinePrefix, List<String> snippets, int maxLineLength) {
        final String sep = ", ";
        List<String> lines = Lists.newArrayList();
        int currentLineLength = 0;
        for (String snippet : snippets) {
            // 如果添加下个元素，长度会是nextLength
            int nextLength = currentLineLength + snippet.length() + 1;

            if (nextLength > maxLineLength) {
                // 如果添加下个元素，长度将会超过最大，添加到下一行，并清空currentLineLength
                String thisLine = snippet + sep;
                lines.add(thisLine);
                currentLineLength = 0;
            } else {
                // 如果添加下个元素，长度将不会超过最大
                String thisLine = Iterables.getLast(lines, "");
                thisLine = thisLine + snippet + sep;
                if (lines.size() == 0) {
                    lines.add(thisLine);
                } else {
                    lines.set(lines.size() - 1, thisLine);
                }
            }
            // 为本行累加长度
            currentLineLength += snippet.length() + sep.length();
        }

        // 为每一行执行trim，并添加前缀
        ListIterator<String> itr = lines.listIterator();
        while (itr.hasNext()) {
            itr.set(everyLinePrefix + itr.next().trim());
        }

        // 删除最后一行 最后的,
        String lastLine = Iterables.getLast(lines);
        lastLine = StringUtils.stripEnd(lastLine, ",");
        lines.set(lines.size() - 1, lastLine);

        return lines;
    }

}