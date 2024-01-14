package com.spldeolin.allison1875.common.javabean;

import java.util.List;
import java.util.function.BiConsumer;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
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
public class JavabeanArg {

    @NotNull AstForest astForest;

    @NotBlank String packageName;

    @NotBlank String className;

    String description;

    @NotBlank String authorName;

    @Valid
    final List<FieldArg> fieldArgs = Lists.newArrayList();

    BiConsumer<CompilationUnit, ClassOrInterfaceDeclaration> more4Javabean;

    @NotNull FileExistenceResolutionEnum javabeanExistenceResolution;

}