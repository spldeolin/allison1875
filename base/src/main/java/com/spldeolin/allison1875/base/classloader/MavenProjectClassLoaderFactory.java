package com.spldeolin.allison1875.base.classloader;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.BaseConfig;
import com.spldeolin.allison1875.base.BaseConfig.ProjectModule;
import javassist.ClassPool;
import javassist.NotFoundException;
import lombok.extern.log4j.Log4j2;

/**
 * get a classpath and jars classloader for sourceRoot
 *
 * @author Deolin 2019-12-27
 */
@Log4j2
public class MavenProjectClassLoaderFactory {

    private static final Map<Path, ClassLoader> cache = Maps.newHashMap();

    public static ClassLoader getClassLoader(Path sourceRootPath) {
        ClassLoader classLoader = cache.get(sourceRootPath);
        if (classLoader != null) {
            return classLoader;
        }

        // target classpath
        ClassPool classPool = ClassPool.getDefault();
        ProjectModule projectModule = BaseConfig.getInstace().getProjectModulesMap().get(sourceRootPath);
        if (projectModule == null) {
            return null;
        }
        appendToClassPool(classPool, projectModule.getClassesPath());

        // target jars path
        Iterator<File> jarItr = FileUtils
                .iterateFiles(projectModule.getExternalJarsPath().toFile(), new String[]{"jar"}, true);
        while (jarItr.hasNext()) {
            File jar = jarItr.next();
            appendToClassPool(classPool, jar.toPath());
        }

        classLoader = new javassist.Loader(classPool);
        cache.put(sourceRootPath, classLoader);
        return classLoader;
    }

    private static void appendToClassPool(ClassPool classPool, Path path) {
        try {
            classPool.appendClassPath(path.toString());
        } catch (NotFoundException e) {
            log.warn("[{}] not found.", path);
        }
    }

}
