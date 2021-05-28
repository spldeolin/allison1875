package com.spldeolin.allison1875.base.factory.javabean;

import java.util.List;
import java.util.function.BiConsumer;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.ast.AstForest;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2021-05-26
 */
@Data
@Accessors(chain = true)
public class JavabeanArg {

    @NotNull
    private AstForest astForest;

    @NotBlank
    private String packageName;

    @NotBlank
    private String className;

    private String description;

    private String authorName;

    @NotNull
    private List<FieldArg> fieldArgs = Lists.newArrayList();

    private BiConsumer<CompilationUnit, ClassOrInterfaceDeclaration> more4Javabean;

}