package com.spldeolin.allison1875.common.util;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.function.Predicate;
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

    public static HashSet<File> listFilesRecursively(Path directory, String extension) {
        HashSet<File> result = Sets.newLinkedHashSet();
        FileUtils.iterateFiles(directory.toFile(), new String[]{extension}, true).forEachRemaining(file -> {
            if (extension.equals(Files.getFileExtension(file.getPath()))) {
                result.add(file);
            }
        });
        return result;
    }

    public static HashSet<File> listFilesRecursively(Path directory, String extension, Predicate<File> predicate) {
        HashSet<File> result = Sets.newLinkedHashSet();
        FileUtils.iterateFiles(directory.toFile(), new String[]{extension}, true).forEachRemaining(file -> {
            if (extension.equals(Files.getFileExtension(file.getPath()))) {
                if (predicate.test(file)) {

                    result.add(file);
                }
            }
        });
        return result;
    }

}