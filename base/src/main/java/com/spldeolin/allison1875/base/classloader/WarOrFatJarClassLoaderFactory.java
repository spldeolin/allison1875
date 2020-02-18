package com.spldeolin.allison1875.base.classloader;


import static com.spldeolin.allison1875.base.BaseConfig.CONFIG;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;

/**
 * web app war or Spring Boot fat jar class loader.
 *
 * @author Deolin 2019-12-27
 */
@Log4j2
public class WarOrFatJarClassLoaderFactory {

    private static ClassLoader cache;

    public static ClassLoader getClassLoader() {
        if (cache != null) {
            return cache;
        }
        List<URL> urls = Lists.newLinkedList();
        try {
            Path tempDir = decompressToTempDir();
            Path webInf = getWebInfPath(tempDir);

            // lib
            Iterator<File> lib = FileUtils.iterateFiles(webInf.resolve("lib").toFile(), new String[]{"jar"}, true);
            while (lib.hasNext()) {
                urls.add(lib.next().toURI().toURL());
            }

            // classes
            urls.add(webInf.resolve("classes").toUri().toURL());

            // extra
            urls.add(Paths.get(
                    WarOrFatJarClassLoaderFactory.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
                    "classpathex").toUri().toURL());

        } catch (IOException e) {
            log.error("something wasn't right here.", e);
            System.exit(-1);
        }
        cache = new URLClassLoader(urls.toArray(new URL[0]));
        return cache;
    }

    private static Path decompressToTempDir() throws IOException {
        Path tempDir = Files.createTempDirectory("docgen" + LocalDateTime.now().toString());
        tempDir.toFile().deleteOnExit();

        try (ZipFile zip = new ZipFile(CONFIG.getWarOrFatJarPath().toFile())) {
            for (ZipEntry zipEntry : Collections.list(zip.entries())) {
                File dest = tempDir.resolve(zipEntry.getName()).toFile();
                // mkdir or copy
                if (zipEntry.isDirectory()) {
                    if (!dest.mkdir()) {
                        log.error("mkdir [{}] error", dest.getPath());
                    }
                } else {
                    try (InputStream jarEntryIs = zip.getInputStream(zipEntry)) {
                        FileUtils.copyInputStreamToFile(jarEntryIs, dest);
                    }
                }
            }
        }
        log.info("Decompressed [{}] to [{}]", CONFIG.getProjectPath().relativize(CONFIG.getWarOrFatJarPath()), tempDir);
        return tempDir;
    }

    private static Path getWebInfPath(Path tempDir) {
        String extension = FilenameUtils.getExtension(CONFIG.getWarOrFatJarPath().getFileName().toString());
        if ("war".equalsIgnoreCase(extension)) {
            return tempDir.resolve("WEB-INF");
        } else if ("jar".equalsIgnoreCase(extension)) {
            return tempDir.resolve("BOOT-INF");
        } else {
            throw new IllegalArgumentException();
        }
    }

}
