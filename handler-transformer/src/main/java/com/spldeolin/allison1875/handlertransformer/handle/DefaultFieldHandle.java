package com.spldeolin.allison1875.handlertransformer.handle;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.handlertransformer.enums.JavabeanTypeEnum;
import com.spldeolin.allison1875.handlertransformer.handle.javabean.BeforeJavabeanCuBuildResult;

/**
 * @author Deolin 2021-01-29
 */
@Singleton
public class DefaultFieldHandle implements FieldHandle {

    @Override
    public BeforeJavabeanCuBuildResult beforeJavabeanCuBuild(FieldDeclaration field, JavabeanTypeEnum javabeanType) {
        return new BeforeJavabeanCuBuildResult().setField(field);
    }

}