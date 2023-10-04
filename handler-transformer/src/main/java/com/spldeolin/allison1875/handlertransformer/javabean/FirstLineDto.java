package com.spldeolin.allison1875.handlertransformer.javabean;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.LotNo;
import lombok.Data;

/**
 * @author Deolin 2020-12-22
 */
@Data
public class FirstLineDto {

    @JsonIgnore
    private InitializerDeclaration init;

    private String handlerUrl;

    private String handlerName;

    private String handlerDescription;

    private String presentServiceQualifier;

    private String serviceName;

    private final Map<String, Object> more = Maps.newHashMap();

    private LotNo lotNo;

    @Override
    public String toString() {
        return handlerUrl + " " + handlerDescription;
    }

}