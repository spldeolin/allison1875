package com.spldeolin.allison1875.base.ast.collection;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.Config;

/**
 * 存放抽象语法树节点的容器的静态访问口
 *
 * @author Deolin 2020-02-03
 */
public class StaticAstContainer {

    private static AstContainer fromConfigPath = new AstContainer(Config.getProjectPath());

    private static Map<Path, AstContainer> fromCustomPath = Maps.newHashMap();

    public static Path getPath() {
        return fromConfigPath.getPath();
    }

    public static Collection<CompilationUnit> getCompilationUnits() {
        return fromConfigPath.getCompilationUnits();
    }

    public static void forEachCompilationUnits(Consumer<CompilationUnit> action) {
        fromConfigPath.getCompilationUnits().forEach(action);
    }

    public static Collection<ClassOrInterfaceDeclaration> getClassOrInterfaceDeclarations() {
        return fromConfigPath.getClassOrInterfaceDeclarations();
    }

    public static void forEachClassOrInterfaceDeclarations(Consumer<ClassOrInterfaceDeclaration> action) {
        fromConfigPath.getClassOrInterfaceDeclarations().forEach(action);
    }

    public static ClassOrInterfaceDeclaration getClassOrInterfaceDeclaration(String qualifier) {
        return fromConfigPath.getClassOrInterfaceDeclaration(qualifier);
    }

    public static Collection<EnumDeclaration> getEnumDeclarations() {
        return fromConfigPath.getEnumDeclarations();
    }

    public static void forEachEnumDeclarations(Consumer<EnumDeclaration> action) {
        fromConfigPath.getEnumDeclarations().forEach(action);
    }

    public static EnumDeclaration getEnumDeclaration(String qualifier) {
        return fromConfigPath.getEnumDeclaration(qualifier);
    }

    public static Collection<VariableDeclarator> getFieldVariableDeclarators() {
        return fromConfigPath.getFieldVariableDeclarators();
    }

    public static void forEachFieldVariableDeclarators(Consumer<VariableDeclarator> action) {
        fromConfigPath.getFieldVariableDeclarators().forEach(action);
    }

    public static VariableDeclarator getFieldVariableDeclarator(String qualifier) {
        return fromConfigPath.getFieldVariableDeclarator(qualifier);
    }

    public static AstContainer getAstContainerByCustomPath(Path path) {
        AstContainer container = fromCustomPath.get(path);
        if (container == null) {
            container = new AstContainer(path);
            fromCustomPath.put(path, container);
        }
        return container;
    }

}
