package com.spldeolin.allison1875.base.util;

import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-05-31
 */
@Log4j2
public class LoadClassUtils {

    /**
     * 使用参数classloader对参数name进行类加载
     *
     * 如果name中有泛型部分，泛型部分将会被去除
     * 如果抛出ClassNotFoundException，这个方法将会把最后一位的'.'符号替换为'$'并递归自己
     */
    public static Class<?> loadClass(String name, ClassLoader classLoader) throws ClassNotFoundException {
        name = name.replaceAll("<[^>]+>", "");
        try {
            return Class.forName(name, false, classLoader);
        } catch (ClassNotFoundException e) {
            try {
                return loadClassConsideringInnerClassRecursively(name, classLoader);
            } catch (ClassNotFoundException ex) {
                log.error("类[{}]无法被加载", name);
                throw e;
            }
        }
    }

    private static Class<?> loadClassConsideringInnerClassRecursively(String name, ClassLoader classLoader)
            throws ClassNotFoundException {
        try {
            return classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            int lastDotIndex = name.lastIndexOf('.');
            if (lastDotIndex == -1) {
                throw e;
            }
            String newName = name.substring(0, lastDotIndex) + '$' + name.substring(lastDotIndex + 1);
            return loadClassConsideringInnerClassRecursively(newName, classLoader);
        }
    }

}
