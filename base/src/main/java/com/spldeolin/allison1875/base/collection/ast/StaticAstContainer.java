package com.spldeolin.allison1875.base.collection.ast;


import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.BaseConfig;
import lombok.extern.log4j.Log4j2;

/**
 * 存放抽象语法树节点的容器的静态访问口
 *
 * @author Deolin 2020-02-03
 */
@Log4j2
public class StaticAstContainer {

    private static AstContainer fromConfigPath = new AstContainer(BaseConfig.getInstace().getProjectPath());

    private static Map<Path, AstContainer> fromCustomPath = Maps.newHashMap();

    public static Path getPath() {
        return fromConfigPath.getPath();
    }

    public static void refresh() {
        log.info("reflash AstContainer from config path.");
        fromConfigPath = new AstContainer(BaseConfig.getInstace().getProjectPath());
    }

    public static Collection<CompilationUnit> getCompilationUnits() {
        return fromConfigPath.getCompilationUnits();
    }

    public static CompilationUnit getCompilationUnit(Path path) {
        return fromConfigPath.getCompilationUnit(path);
    }

    public static Collection<ClassOrInterfaceDeclaration> getClassOrInterfaceDeclarations() {
        return fromConfigPath.getClassOrInterfaceDeclarations();
    }

    public static ClassOrInterfaceDeclaration getClassOrInterfaceDeclaration(String qualifier) {
        return fromConfigPath.getClassOrInterfaceDeclaration(qualifier);
    }

    public static Collection<ClassOrInterfaceDeclaration> getClassOrInterfaceDeclarations(String name) {
        return fromConfigPath.getClassOrInterfaceDeclarations(name);
    }

    public static Collection<EnumDeclaration> getEnumDeclarations() {
        return fromConfigPath.getEnumDeclarations();
    }

    public static EnumDeclaration getEnumDeclaration(String qualifier) {
        return fromConfigPath.getEnumDeclaration(qualifier);
    }

    public static Collection<VariableDeclarator> getFieldVariableDeclarators() {
        return fromConfigPath.getFieldVariableDeclarators();
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
