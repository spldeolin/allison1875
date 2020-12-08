package com.spldeolin.allison1875.persistencegenerator.javabean;

import java.nio.file.Path;
import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.spldeolin.allison1875.base.creator.CuCreator;
import lombok.Data;

/**
 * @author Deolin 2020-12-08
 */
@Data
public class GenerateEntityResultDto {

    private Path entityPath;

    private CuCreator entityCuCreator;

    private Collection<CompilationUnit> toCreate;

}