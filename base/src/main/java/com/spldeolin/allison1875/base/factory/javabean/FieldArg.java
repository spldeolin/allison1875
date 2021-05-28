package com.spldeolin.allison1875.base.factory.javabean;

import java.util.function.BiConsumer;
import javax.validation.constraints.NotBlank;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2021-05-26
 */
@Data
@Accessors(chain = true)
public class FieldArg {

    private String typeQualifier;

    private String description;

    @NotBlank
    private String typeName;

    @NotBlank
    private String fieldName;

    private BiConsumer<ClassOrInterfaceDeclaration, FieldDeclaration> more4Field;

}