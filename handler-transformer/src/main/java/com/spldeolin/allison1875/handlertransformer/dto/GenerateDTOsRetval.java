package com.spldeolin.allison1875.handlertransformer.dto;

import java.util.List;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-01-12
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenerateDTOsRetval {

    String reqBodyDTOType = null;

    String respBodyDTOType = null;

    Boolean isHttpGet;

    final List<VariableDeclarator> reqParams = Lists.newArrayList();

}