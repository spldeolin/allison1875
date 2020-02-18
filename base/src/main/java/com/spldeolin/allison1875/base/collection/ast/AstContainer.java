package com.spldeolin.allison1875.base.collection.ast;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

/**
 * 存放抽象语法树节点的容器
 *
 * @author Deolin 2020-02-03
 */
public class AstContainer {

    private Path path;

    private Collection<CompilationUnit> cus;

    private Map<Path, CompilationUnit> cusByPath;

    private Collection<ClassOrInterfaceDeclaration> coids;

    private Map<String, ClassOrInterfaceDeclaration> coidsByQualifier;

    private Collection<EnumDeclaration> enums;

    private Map<String, EnumDeclaration> enumsByQualifier;

    private Collection<VariableDeclarator> fieldVars;

    private Map<String, VariableDeclarator> fieldVarsByQualifier;

    AstContainer(Path path) {
        this.path = path;
        this.cus = new CompilationUnitCollector().collectIntoCollection(path);
    }

    public Path getPath() {
        return path;
    }

    public Collection<CompilationUnit> getCompilationUnits() {
        return cus;
    }

    public CompilationUnit getCompilationUnit(Path path) {
        if (cusByPath == null) {
            cusByPath = new CompilationUnitCollector().collectIntoMap(cus);
        }
        return cusByPath.get(path);
    }

    public Collection<ClassOrInterfaceDeclaration> getClassOrInterfaceDeclarations() {
        if (coids == null) {
            TypeDeclarationCollector<ClassOrInterfaceDeclaration> collector = new TypeDeclarationCollector<>(
                    ClassOrInterfaceDeclaration.class);
            coids = collector.collectIntoCollection(cus);
        }
        return coids;
    }

    public ClassOrInterfaceDeclaration getClassOrInterfaceDeclaration(String qualifier) {
        if (coidsByQualifier == null) {
            TypeDeclarationCollector<ClassOrInterfaceDeclaration> collector = new TypeDeclarationCollector<>(
                    ClassOrInterfaceDeclaration.class);
            if (coids == null) {
                coidsByQualifier = collector.collectIntoMapByCompilationUnits(cus);
            } else {
                coidsByQualifier = collector.collectIntoMapByCollectedOnes(coids);
            }
        }
        return coidsByQualifier.get(qualifier);
    }

    public Collection<EnumDeclaration> getEnumDeclarations() {
        if (enums == null) {
            TypeDeclarationCollector<EnumDeclaration> collector = new TypeDeclarationCollector<>(EnumDeclaration.class);
            enums = collector.collectIntoCollection(cus);
        }
        return enums;
    }

    public EnumDeclaration getEnumDeclaration(String qualifier) {
        if (enumsByQualifier == null) {
            TypeDeclarationCollector<EnumDeclaration> collector = new TypeDeclarationCollector<>(EnumDeclaration.class);
            if (enums == null) {
                enumsByQualifier = collector.collectIntoMapByCompilationUnits(cus);
            } else {
                enumsByQualifier = collector.collectIntoMapByCollectedOnes(enums);
            }
        }
        return enumsByQualifier.get(qualifier);
    }

    public Collection<VariableDeclarator> getFieldVariableDeclarators() {
        if (fieldVars == null) {
            FieldVariableDeclaratorCollector collector = new FieldVariableDeclaratorCollector();
            fieldVars = collector.collectIntoCollection(cus);
        }
        return fieldVars;
    }

    public VariableDeclarator getFieldVariableDeclarator(String qualifier) {
        if (fieldVarsByQualifier == null) {
            FieldVariableDeclaratorCollector collector = new FieldVariableDeclaratorCollector();
            if (fieldVars == null) {
                fieldVarsByQualifier = collector.collectIntoMapByCompilationUnit(cus);
            } else {
                fieldVarsByQualifier = collector.collectIntoMapByCollectedOnes(fieldVars);
            }
        }
        return fieldVarsByQualifier.get(qualifier);
    }

}
