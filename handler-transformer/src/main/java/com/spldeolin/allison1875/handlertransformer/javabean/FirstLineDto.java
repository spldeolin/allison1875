package com.spldeolin.allison1875.handlertransformer.javabean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.javaparser.ast.body.InitializerDeclaration;
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

    String presentServiceQualifier;

    String serviceName;

    String lotNo;

    @Override
    public String toString() {
        return handlerUrl + " " + handlerDescription;
    }

}