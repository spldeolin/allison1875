package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.javadoc.Javadoc;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;

/**
 * 批量更新
 *
 * @author Deolin 2020-12-18
 */
@Singleton
public class BatchUpdateProc extends MapperProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public String process(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableBatchUpdate()) {
            return null;
        }

        String methodName = super.calcMethodName(mapper, "batchUpdate");
        MethodDeclaration update = new MethodDeclaration();
        Javadoc javadoc = new JavadocComment("（批量版）根据ID更新数据" + Constant.PROHIBIT_MODIFICATION_JAVADOC).parse();
        javadoc.addBlockTag("param", "entities", "（多个）" + persistence.getDescrption());
        javadoc.addBlockTag("return", "更新条数");
        update.setJavadocComment(javadoc);
        update.setType(PrimitiveType.intType());
        update.setName(methodName);
        update.addParameter(StaticJavaParser
                .parseParameter("@Param(\"entities\") Collection<" + persistence.getEntityName() + "> entities"));
        update.setBody(null);
        mapper.getMembers().addLast(update);
        return methodName;
    }

}