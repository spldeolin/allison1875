package com.spldeolin.allison1875.base.classloader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.BaseConfig;
import lombok.extern.log4j.Log4j2;

/**
 * Maven project class loader.
 *
 * @author Deolin 2019-12-27
 */
@Log4j2
public class MavenProjectClassLoaderFactory {

    private static ClassLoader cache;

    private static final Collection<URL> classUrlsCache = Sets.newHashSet();

    private static final Collection<URL> libUrlsCache = Sets.newHashSet();

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
                    classUrlsCache.add(sourceRoot.getRoot().resolve(BaseConfig.getInstace().getClasspathRelativePath())
                            .toUri().toURL());
                }

                // lib
                File dependencyTmpFile = new File(projectPath + File.separator + ".dependency-jar-paths.tmp");
                if (!dependencyTmpFile.exists()) {
                    log.warn("[{}] is absent.", dependencyTmpFile.getPath());
                }
                LineIterator lineIterator = FileUtils.lineIterator(dependencyTmpFile);
                while (lineIterator.hasNext()) {
                    libUrlsCache.add(Paths.get(lineIterator.next()).toUri().toURL());
                }

            } catch (IOException e) {
                log.error("something wasn't right here.", e);
                System.exit(-1);
            }
        });

        cache = new URLClassLoader(concatAndToArray());
    }

    private static URL[] concatAndToArray() {
        Collection<URL> concat = Lists.newArrayListWithCapacity(classUrlsCache.size() + libUrlsCache.size());
        concat.addAll(classUrlsCache);
        concat.addAll(libUrlsCache);
        return concat.toArray(new URL[0]);

    }

}
