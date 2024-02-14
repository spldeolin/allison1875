package com.spldeolin.allison1875.sqlapigenerator.javabean;

import java.util.List;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.ast.FileFlush;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-01-21
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenerateServiceImplMethodRetval {

    final List<FileFlush> flushes = Lists.newArrayList();

    MethodDeclaration methodImpl;

}