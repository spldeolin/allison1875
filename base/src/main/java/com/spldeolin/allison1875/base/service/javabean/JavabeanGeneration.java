package com.spldeolin.allison1875.base.service.javabean;

import java.nio.file.Path;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.spldeolin.allison1875.base.ast.FileFlush;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2023-12-30
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JavabeanGeneration {

    CompilationUnit cu;

    FileFlush fileFlush;

    String javabeanName;

    String javabeanQualifier;

    ClassOrInterfaceDeclaration coid;

    Path path;

}