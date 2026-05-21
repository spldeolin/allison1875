package com.spldeolin.allison1875.common.dto;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-05-26
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataModelArg {

    @NotNull
    Path sourceRoot;

    @NotBlank
    String packageName;

    @NotBlank
    String className;

    String description;

    @NotBlank
    String author;

    @Valid
    final List<FieldArg> fieldArgs = Lists.newArrayList();

    BiConsumer<CompilationUnit, ClassOrInterfaceDeclaration> moreOperation;

    @NotNull
    FileExistenceResolutionEnum dataModelExistenceResolution;

}
