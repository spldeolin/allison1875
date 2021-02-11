package com.spldeolin.allison1875.handlertransformer.handle;


import com.github.javaparser.ast.body.FieldDeclaration;
import com.spldeolin.allison1875.handlertransformer.enums.JavabeanTypeEnum;
import com.spldeolin.allison1875.handlertransformer.handle.javabean.BeforeJavabeanCuBuildResult;

/**
 * 对Javabean中Field追加额外的定制操作handle
 *
 * @author Deolin 2021-01-29
 */
public interface FieldHandle {

    /**
     * 所在Javabean的Cu构建前 追加额外操作
     */
    BeforeJavabeanCuBuildResult beforeJavabeanCuBuild(FieldDeclaration field, JavabeanTypeEnum javabeanType);

}
