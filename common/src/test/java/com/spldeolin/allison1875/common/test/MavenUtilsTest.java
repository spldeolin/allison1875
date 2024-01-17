package com.spldeolin.allison1875.common.test;

import com.github.javaparser.utils.CodeGenerationUtils;
import com.spldeolin.allison1875.common.util.MavenUtils;

/**
 * @author Deolin 2024-01-17
 */
public class MavenUtilsTest {

    public static void main(String[] args) {
        System.out.println(MavenUtils.findMavenModule(MavenUtilsTest.class));
        System.out.println(MavenUtils.findMavenProject(MavenUtilsTest.class));
        System.out.println(CodeGenerationUtils.classLoaderRoot(MavenUtilsTest.class));
    }

}