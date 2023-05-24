package com.spldeolin.allison1875.base.util;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

/**
 * @author Deolin 2021-06-11
 */
public class FileFindUtils {

    private FileFindUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static void recursively(Path directory, String extension, Consumer<Path> action) {
        for (File file : Files.fileTraverser().depthFirstPreOrder(directory.toFile())) {
            if (extension.equals(Files.getFileExtension(file.getPath()))) {
                action.accept(file.toPath());
            }
        }
    }

    public static Set<File> asFilesRecursively(Path directory, String extension) {
        Set<File> result = Sets.newHashSet();
        for (File file : Files.fileTraverser().depthFirstPreOrder(directory.toFile())) {
            if (extension.equals(Files.getFileExtension(file.getPath()))) {
                result.add(file);
            }
        }
        return result;
    }

    public static Set<Path> asPathsRecursively(Path directory, String extension) {
        return asFilesRecursively(directory, extension).stream().map(File::toPath).collect(Collectors.toSet());
    }

}