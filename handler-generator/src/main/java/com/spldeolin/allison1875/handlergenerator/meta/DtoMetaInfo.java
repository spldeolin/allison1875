package com.spldeolin.allison1875.handlergenerator.meta;

import java.util.Collection;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-06-26
 */
@Data
@Accessors(fluent = true)
public class DtoMetaInfo {

    private String packageName;

    private Collection<String> imports = Lists.newArrayList();

    private String typeQualifier;

    private String typeName;

    private String dtoName;

    private String asVariableDeclarator;

    private Collection<String> variableDeclarators = Lists.newArrayList();

}