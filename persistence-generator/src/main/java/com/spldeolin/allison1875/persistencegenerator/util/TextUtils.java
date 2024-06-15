package com.spldeolin.allison1875.persistencegenerator.util;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.util.CollectionUtils;

/**
 * @author Deolin 2021-08-30
 */
public class TextUtils {

    private TextUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static List<String> formatLines(String linePrefix, List<String> lines, int x) {
        if (CollectionUtils.isEmpty(lines)) {
            return Lists.newArrayList();
        }

        List<String> words = Lists.newArrayList();
        lines.forEach(line -> words.addAll(Lists.newArrayList(line.split(", "))));

        List<String> result = Lists.newArrayList(linePrefix);

        for (String word : words) {
            String line = Iterables.getLast(result);
            // try add to this line
            line += word + ", ";
            // length out of the x
            if (line.length() > x) {
                // add to next new line
                result.add(linePrefix + word + ", ");
            } else {
                result.set(result.size() - 1, line);
            }
        }

        // trim all lines end
        result.replaceAll(line -> StringUtils.stripEnd(line, null));

        // remove last line last comma
        result.set(result.size() - 1, result.get(result.size() - 1).replaceAll(",$", ""));

        return result;
    }

}