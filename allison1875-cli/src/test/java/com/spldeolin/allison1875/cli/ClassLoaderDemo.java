package com.spldeolin.allison1875.cli;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import com.spldeolin.allison1875.cli.util.MavenProjectClassLoaderUtils;

/**
 * 演示为指定的Maven模块项目路径构建ClassLoader，并尝试加载类来验证功能正确性。
 *
 * <p>使用方式：修改 {@code MAVEN_MODULE_DIR} 和 {@code CLASS_TO_LOAD} 为目标Maven模块的实际路径和要加载的类名，然后运行main方法。
 *
 * @author Deolin 2026-05-09
 */
@Slf4j
public class ClassLoaderDemo {

    /**
     * 目标Maven模块的根目录路径（包含pom.xml），可替换为任意本地Maven项目路径
     */
    private static final String MAVEN_MODULE_DIR = ".";

    /**
     * 要尝试加载的类的全限定名，用于验证ClassLoader是否能正确加载依赖中的类
     */
    private static final String CLASS_TO_LOAD = "com.spldeolin.allison1875.startransformer.StarTransformer";

    public static void main(String[] args) {
        File moduleDir = new File(MAVEN_MODULE_DIR);
        log.info("target maven module dir: {}", moduleDir.getAbsolutePath());

        // 构建ClassLoader
        log.info("building ClassLoader...");
        ClassLoader classLoader = MavenProjectClassLoaderUtils.buildClassLoader(moduleDir);
        log.info("ClassLoader built successfully: {}", classLoader);

        // 尝试加载类
        log.info("attempting to load class: {}", CLASS_TO_LOAD);
        try {
            Class<?> clazz = classLoader.loadClass(CLASS_TO_LOAD);
            log.info("class loaded successfully: {}", clazz);
            log.info("class location: {}", clazz.getProtectionDomain().getCodeSource().getLocation());
        } catch (ClassNotFoundException e) {
            log.error("class not found: {}. "
                    + "Possible reasons: 1) the target module does not depend on this class, "
                    + "2) the module has not been compiled yet (target/classes is empty)", CLASS_TO_LOAD, e);
        }
    }

}
