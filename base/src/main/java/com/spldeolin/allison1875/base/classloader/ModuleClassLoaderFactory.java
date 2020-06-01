package com.spldeolin.allison1875.base.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.BaseConfig;
import com.spldeolin.allison1875.base.BaseConfig.ProjectModule;
import lombok.extern.log4j.Log4j2;

/**
 * 为module提供java.lang.ClassLoader 对象的工厂
 *
 * @author Deolin 2019-12-27
 */
@Log4j2
public class ModuleClassLoaderFactory {

    private static final Map<Path, ClassLoader> cache = Maps.newHashMap();

    public static ClassLoader getClassLoader(Path sourceRootPath) {
        ClassLoader classLoader = cache.get(sourceRootPath);
        if (classLoader != null) {
            return classLoader;
        }

        List<URL> urls = Lists.newArrayList();

        try {
            // classes
            ProjectModule projectModule = BaseConfig.getInstace().getProjectModulesMap().get(sourceRootPath);
            if (projectModule == null) {
                return null;
            }
            urls.add(projectModule.getClassesPath().toUri().toURL());

            // external jars
            File directory = projectModule.getExternalJarsPath().toFile();
            if (directory.isDirectory()) {
                Iterator<File> jarItr = FileUtils.iterateFiles(directory, new String[]{"jar"}, true);
                while (jarItr.hasNext()) {
                    File jar = jarItr.next();
                    urls.add(jar.toURI().toURL());
                }
            }
        } catch (MalformedURLException e) {
            log.error(e);
        }

        classLoader = new URLClassLoader(urls.toArray(new URL[0]));
        cache.put(sourceRootPath, classLoader);
        return classLoader;
    }

    public static ClassLoader getClassLoader(SourceRoot sourceRoot) {
        return getClassLoader(sourceRoot.getRoot());
    }

}
