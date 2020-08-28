package com.spldeolin.allison1875.handlertransformer.javabean;

import org.apache.commons.lang3.tuple.Pair;
import com.github.javaparser.ast.ImportDeclaration;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Data;

/**
 * @author Deolin 2020-06-26
 */
@Data
@Builder
public class DtoMetaInfo {

    private final String packageName;

    private final String typeQualifier;

    private final String typeName;

    private final String dtoName;

    private final Pair<String, String> asVariableDeclarator;

    private final ImmutableList<ImportDeclaration> imports;

    private final ImmutableList<Pair<String, String>> variableDeclarators;

}