package com.spldeolin.allison1875.base.builder;

import java.util.LinkedHashSet;
import java.util.Set;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.Sets;

/**
 * @author Deolin 2021-01-10
 */
public class ServiceCuBuilder {

    private SourceRoot sourceRoot;

    private PackageDeclaration packageDeclaration;

    private final Set<ImportDeclaration> importDeclarations = Sets.newLinkedHashSet();

    private ClassOrInterfaceDeclaration coid;

    private Javadoc javadoc;

    private final Set<AnnotationExpr> annotationExprs = Sets.newLinkedHashSet();

    private String serviceName;

    private final LinkedHashSet<FieldDeclaration> fieldDeclarations = Sets.newLinkedHashSet();

    private ClassOrInterfaceDeclaration service;

}