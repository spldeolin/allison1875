package com.spldeolin.allison1875.persistencegenerator.processor;

import java.nio.file.Path;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.processor.impl.FindMethodNamingOffsetServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(FindMethodNamingOffsetServiceImpl.class)
public interface FindMethodNamingOffsetService {

    FieldDeclaration findMethodNamingOffsetField(Path designPath);

}