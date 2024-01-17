package com.spldeolin.allison1875.common.util;

import java.io.File;
import java.util.LinkedHashSet;
import org.apache.commons.io.FileUtils;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

/**
 * @author Deolin 2021-06-11
 */
public class FileTraverseUtils {

    private FileTraverseUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static LinkedHashSet<File> listFilesRecursively(File directory, String extension) {
        LinkedHashSet<File> result = Sets.newLinkedHashSet();
        FileUtils.iterateFiles(directory, new String[]{extension}, true).forEachRemaining(file -> {
            if (extension.equals(Files.getFileExtension(file.getPath()))) {
                result.add(file);
            }
        });
        return result;
    }

}