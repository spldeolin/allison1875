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
 * 插入
 *
 * @author Deolin 2020-07-18
 */
@Singleton
public class BatchInsertEvenNullProc extends MapperProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public String process(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableBatchInsertEvenNull()) {
            return null;
        }
        String methodName = super.calcMethodName(mapper, "batchInsertEvenNull");
        MethodDeclaration insert = new MethodDeclaration();
        Javadoc javadoc = new JavadocComment("批量插入，为null的属性会被作为null插入" + Constant.PROHIBIT_MODIFICATION_JAVADOC)
                .parse();
        javadoc.addBlockTag("param", "entities", "（多个）" + persistence.getDescrption());
        javadoc.addBlockTag("return", "插入条数");
        insert.setJavadocComment(javadoc);
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(StaticJavaParser
                .parseParameter("@Param(\"entities\") Collection<" + persistence.getEntityName() + "> entities"));
        insert.setBody(null);
        mapper.getMembers().addLast(insert);
        return methodName;
    }

}