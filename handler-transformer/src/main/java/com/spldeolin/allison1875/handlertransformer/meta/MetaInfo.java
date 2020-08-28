package com.spldeolin.allison1875.handlertransformer.meta;

import java.nio.file.Path;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.ImmutableList;
import com.spldeolin.allison1875.base.util.StringUtils;
import lombok.Builder;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-06-26
 */
@Data
@Builder
@Log4j2
public class MetaInfo {

    private final String location;

    private final Path sourceRoot;

    private final ClassOrInterfaceDeclaration controller;

    private final String handlerName;

    private final String handlerDescription;

    private final DtoMetaInfo reqBody;

    private final DtoMetaInfo respBody;

    private final ImmutableList<DtoMetaInfo> dtos;

    public boolean isLack() {
        if (StringUtils.isBlank(handlerName)) {
            log.warn("Blueprint[{}]缺少hanlderName", location);
            return true;
        }
        if (StringUtils.isBlank(handlerDescription)) {
            log.warn("Blueprint[{}]缺少handlerDescription", location);
            return true;
        }
        return false;
    }

}