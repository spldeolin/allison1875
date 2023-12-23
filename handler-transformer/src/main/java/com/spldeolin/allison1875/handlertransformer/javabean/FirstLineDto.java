package com.spldeolin.allison1875.handlertransformer.javabean;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.LotNo;
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

    final Map<String, Object> more = Maps.newHashMap();

    LotNo lotNo;

    @Override
    public String toString() {
        return handlerUrl + " " + handlerDescription;
    }

}