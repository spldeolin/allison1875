package com.spldeolin.allison1875.handlertransformer.javabean;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-12-22
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FirstLineDto {

    @JsonIgnore
    InitializerDeclaration init;

    String handlerUrl;

    String handlerName;

    String handlerDescription;

    final List<ImportDeclaration> importsFromController = Lists.newArrayList();

    String lotNo;

    @Override
    public String toString() {
        return handlerUrl + " " + handlerDescription;
    }

}