package com.spldeolin.allison1875.base.classloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.BaseConfig;
import javassist.ClassPool;
import javassist.NotFoundException;
import lombok.extern.log4j.Log4j2;

/**
 * build a [target/classes] and dependency jars class loader.
 *
 * @author Deolin 2019-12-27
 */
@Log4j2
public class MavenProjectClassLoaderFactory {

    private static ClassLoader cache;

    private static final Collection<Path> classUrlsCache = Sets.newHashSet();

    private static final Collection<String> libUrlsCache = Sets.newHashSet();

    public static ClassLoader getClassLoader() {
        if (cache == null) {
            refresh();
        }
        return cache;
    }

    public static void refresh() {
        BaseConfig.getInstace().getProjectPaths().forEach(projectPath -> {
            try {
                // classes
                ProjectRoot projectRoot = new ParserCollectionStrategy().collect(projectPath);
                for (SourceRoot sourceRoot : projectRoot.getSourceRoots()) {
                    classUrlsCache
                            .add(sourceRoot.getRoot().resolve(BaseConfig.getInstace().getClasspathRelativePath()));
                }

                // lib
                File dependencyTmpFile = new File(projectPath + File.separator + ".dependency-jar-paths.tmp");
                if (!dependencyTmpFile.exists()) {
                    log.warn("[{}] is absent.", dependencyTmpFile.getPath());
                    return;
                }
                LineIterator lineIterator = FileUtils.lineIterator(dependencyTmpFile);
                while (lineIterator.hasNext()) {
                    libUrlsCache.add(lineIterator.next());
                }

            } catch (IOException e) {
                log.error("something wasn't right here.", e);
                System.exit(-1);
            }
        });

        ClassPool pool = ClassPool.getDefault();
        classUrlsCache.forEach(path -> {
            try {
                pool.insertClassPath(path.toString());
            } catch (NotFoundException e) {
                log.warn("[{}] not found.", path);
            }
        });
        libUrlsCache.forEach(path -> {
            try {
                pool.insertClassPath(path);
            } catch (NotFoundException e) {
                log.warn("[{}] not found.", path);
            }
        });

        cache = new javassist.Loader(pool);
    }

}
