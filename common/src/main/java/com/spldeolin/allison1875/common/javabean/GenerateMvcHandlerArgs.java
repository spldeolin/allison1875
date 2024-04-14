package com.spldeolin.allison1875.common.javabean;

import javax.validation.constraints.NotEmpty;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-02-17
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenerateMvcHandlerArgs {

    @NotEmpty String mvcHandlerUrl;

    String description;

    String serviceParamType;

    String serviceResultType;

    @NotEmpty String injectedServiceVarName;

    @NotEmpty String serviceMethodName;

    ClassOrInterfaceDeclaration mvcController;

}