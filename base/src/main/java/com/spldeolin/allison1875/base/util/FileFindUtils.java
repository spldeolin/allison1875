package com.spldeolin.allison1875.base.util;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

/**
 * @author Deolin 2021-06-11
 */
public class FileFindUtils {

    private FileFindUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static Set<File> asFilesRecursively(Path directory, String extension) {
        Set<File> result = Sets.newHashSet();
        FileUtils.iterateFiles(directory.toFile(), new String[]{extension}, true).forEachRemaining(file -> {
            if (extension.equals(Files.getFileExtension(file.getPath()))) {
                result.add(file);
            }
        });
        return result;
    }

    public static Set<Path> asPathsRecursively(Path directory, String extension) {
        return asFilesRecursively(directory, extension).stream().map(File::toPath).collect(Collectors.toSet());
    }

}