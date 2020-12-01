package com.spldeolin.allison1875.persistencegenerator.handle;

import java.nio.file.Path;
import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;

/**
 * @author Deolin 2020-11-20
 */
public interface GenerateQueryDesignFieldHandle {

    Collection<CompilationUnit> handlerQueryDesignField(PropertyDto propertyDto, FieldDeclaration field,
            Path sourceRoot);

}
