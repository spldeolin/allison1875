package com.spldeolin.allison1875.base.classloader;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.BaseConfig;
import com.spldeolin.allison1875.base.BaseConfig.ProjectModule;
import javassist.ClassPool;
import javassist.NotFoundException;
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

        // classes
        ClassPool classPool = ClassPool.getDefault();
        ProjectModule projectModule = BaseConfig.getInstace().getProjectModulesMap().get(sourceRootPath);
        if (projectModule == null) {
            return null;
        }
        appendToClassPool(classPool, projectModule.getClassesPath());

        // external jars
        File directory = projectModule.getExternalJarsPath().toFile();
        if (directory.isDirectory()) {
            Iterator<File> jarItr = FileUtils.iterateFiles(directory, new String[]{"jar"}, true);
            while (jarItr.hasNext()) {
                File jar = jarItr.next();
                appendToClassPool(classPool, jar.toPath());
            }
        }

        classLoader = new javassist.Loader(classPool);
        cache.put(sourceRootPath, classLoader);
        return classLoader;
    }

    public static ClassLoader getClassLoader(SourceRoot sourceRoot) {
        return getClassLoader(sourceRoot.getRoot());
    }

    private static void appendToClassPool(ClassPool classPool, Path path) {
        try {
            classPool.appendClassPath(path.toString());
        } catch (NotFoundException e) {
            log.warn("[{}] not found.", path);
        }
    }

}
