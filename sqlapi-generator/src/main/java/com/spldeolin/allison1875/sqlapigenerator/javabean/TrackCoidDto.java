package com.spldeolin.allison1875.sqlapigenerator.javabean;

import java.io.File;
import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-01-21
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrackCoidDto {

    ClassOrInterfaceDeclaration controller;

    CompilationUnit controllerCu;

    ClassOrInterfaceDeclaration service;

    CompilationUnit serviceCu;

    final List<ClassOrInterfaceDeclaration> serviceImpls = Lists.newArrayList();

    final List<CompilationUnit> serviceImplCus = Lists.newArrayList();

    ClassOrInterfaceDeclaration mapper;

    CompilationUnit mapperCu;

    final List<File> mapperXmls = Lists.newArrayList();

}