package com.spldeolin.allison1875.common.dto;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
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

    String mvcHandlerUrl;

    String description;

    String reqBodyDTOType;

    String respBodyDTOType;

    String injectedServiceVarName;

    String serviceMethodName;

    ClassOrInterfaceDeclaration mvcController;

    Boolean isHttpGet;

    List<VariableDeclarator> reqParams;

    public void validate() {
        List<String> invalids = Lists.newArrayList();
        if (StringUtils.isEmpty(mvcHandlerUrl)) {
            invalids.add("GenerateMvcHandlerArgs.mvcHandlerUrl must not be empty");
        }
        if (StringUtils.isEmpty(injectedServiceVarName)) {
            invalids.add("GenerateMvcHandlerArgs.injectedServiceVarName must not be empty");
        }
        if (StringUtils.isEmpty(serviceMethodName)) {
            invalids.add("GenerateMvcHandlerArgs.serviceMethodName must not be empty");
        }
        if (!invalids.isEmpty()) {
            throw new Allison1875Exception(String.join(", ", invalids));
        }
    }

}
