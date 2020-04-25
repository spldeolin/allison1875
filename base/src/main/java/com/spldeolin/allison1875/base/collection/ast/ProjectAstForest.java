package com.spldeolin.allison1875.base.collection.ast;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.Multimap;

/**
 * 一个项目映射的抽象语法森林
 *
 * @author Deolin 2020-02-03
 */
public class ProjectAstForest {

    private final Path projectPath;

    private final Collection<SourceRoot> sourceRoots;

    private Collection<CompilationUnit> cus;

    private Collection<ClassOrInterfaceDeclaration> coids;

    private Map<String, ClassOrInterfaceDeclaration> coidsByQualifier;

    private Multimap<String, ClassOrInterfaceDeclaration> coidsByName;

    private Collection<EnumDeclaration> enums;

    private Map<String, EnumDeclaration> enumsByQualifier;

    private Collection<VariableDeclarator> fieldVars;

    private Map<String, VariableDeclarator> fieldVarsByQualifier;

    ProjectAstForest(Path projectPath, Collection<SourceRoot> sourceRoots) {
        this.projectPath = projectPath;
        this.sourceRoots = sourceRoots;
    }

    public Path getProjectPath() {
        return projectPath;
    }

    public Collection<CompilationUnit> getCompilationUnits() {
        cus = new CompilationUnitCollector().sourceRoots(sourceRoots).collectIntoCollection().list();
        return cus;
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

    public Collection<ClassOrInterfaceDeclaration> getClassOrInterfaceDeclarations(String name) {
        if (coidsByName == null) {
            TypeDeclarationByNameCollector<ClassOrInterfaceDeclaration> collector =
                    new TypeDeclarationByNameCollector<>(
                    ClassOrInterfaceDeclaration.class);
            if (coids == null) {
                coidsByName = collector.collectIntoMapByCompilationUnits(cus);
            } else {
                coidsByName = collector.collectIntoMapByCollectedOnes(coids);
            }
        }
        return coidsByName.get(name);
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
