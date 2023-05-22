package com.spldeolin.allison1875.base.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import jodd.io.FileNameUtil;

/**
 * @author Deolin 2021-06-11
 */
public class FileFindUtils {

    private FileFindUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static void recursively(Path directory, String extension, Consumer<Path> action) {
        try {
            Files.find(directory, Integer.MAX_VALUE,
                            (filePath, fileAttr) -> extension.equals(FileNameUtil.getExtension(filePath.toString())))
                    .forEach(action);
        } catch (IOException e) {
            throw new RuntimeException("fail to find files from [" + directory + "]", e);
        }
    }

    public static Set<File> asFilesRecursively(Path directory, String extension) {
        try {
            return Files.find(directory, Integer.MAX_VALUE,
                            (filePath, fileAttr) -> extension.equals(FileNameUtil.getExtension(filePath.toString())))
                    .map(Path::toFile).collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException("fail to find files from [" + directory + "]", e);
        }
    }

    public static Set<Path> asPathsRecursively(Path directory, String extension) {
        try {
            return Files.find(directory, Integer.MAX_VALUE,
                            (filePath, fileAttr) -> extension.equals(FileNameUtil.getExtension(filePath.toString())))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException("fail to find files from [" + directory + "]", e);
        }
    }

}