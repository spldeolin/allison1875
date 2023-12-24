package com.spldeolin.allison1875.handlertransformer.javabean;

import java.util.Collection;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-01-29
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BeforeJavabeanCuBuildResult {

    FieldDeclaration field;

    /**
     * 待追加的import声明
     */
    Collection<String> appendImports = Lists.newArrayList();

}