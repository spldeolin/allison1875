package com.spldeolin.allison1875.base;

import com.github.javaparser.utils.CollectionStrategy;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.spldeolin.allison1875.base.classloader.ClassLoaderCollectionStrategy;
import com.spldeolin.allison1875.base.classloader.WarOrFatJarClassLoaderFactory;

/**
 * 全局CU收集策略，每个工具都可以根据需要通过这个类进行配置（故没有通过config.yml进行配置）
 *
 * @author Deolin 2020-02-14
 */
public class GlobalCollectionStrategy {

    private static CollectionStrategy collectionStrategy;

    private static boolean doNotCollectWithLoadingClass = false;

    /**
     * 是否在收集时不再类加载。
     * 禁用类加载可以加快速度，但是AST将会不再支持resolved、calculateResolvedType等方法
     */
    public static void setDoNotCollectWithLoadingClass(boolean doNotCollectWithLoadingClass) {
        GlobalCollectionStrategy.doNotCollectWithLoadingClass = doNotCollectWithLoadingClass;
    }

    /**
     * @return 收集策略
     */
    public static CollectionStrategy getCollectionStrategy() {
        if (collectionStrategy == null) {
            if (doNotCollectWithLoadingClass) {
                collectionStrategy = new ParserCollectionStrategy();
            } else {
                collectionStrategy = new ClassLoaderCollectionStrategy(WarOrFatJarClassLoaderFactory.getClassLoader());
            }
        }
        return collectionStrategy;
    }

}
