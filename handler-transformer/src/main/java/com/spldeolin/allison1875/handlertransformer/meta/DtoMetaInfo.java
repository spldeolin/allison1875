package com.spldeolin.allison1875.handlertransformer.meta;

import java.util.Collection;
import org.apache.commons.lang3.tuple.Pair;
import com.github.javaparser.ast.ImportDeclaration;
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

    private Collection<ImportDeclaration> imports = Lists.newArrayList();

    private String typeQualifier;

    private String typeName;

    private String dtoName;

    private Pair<String, String> asVariableDeclarator;

    private Collection<Pair<String, String>> variableDeclarators = Lists.newArrayList();

}