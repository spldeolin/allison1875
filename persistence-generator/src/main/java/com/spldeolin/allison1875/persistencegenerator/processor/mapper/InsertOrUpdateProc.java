package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

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
public class InsertOrUpdateProc extends MapperProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public String process(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableInsertOrUpdate()) {
            return null;
        }
        String methodName = super.calcMethodName(mapper, "insertOrUpdate");
        MethodDeclaration insert = new MethodDeclaration();
        Javadoc javadoc = new JavadocComment(
                "尝试插入，若指定了id并存在，则更新（INSERT ON DUPLICATE KEY UPDATE）" + Constant.PROHIBIT_MODIFICATION_JAVADOC).parse();
        javadoc.addBlockTag("param", "entity", persistence.getDescrption());
        javadoc.addBlockTag("return", "插入或更新条数");
        insert.setJavadocComment(javadoc);
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(persistence.getEntityName(), "entity");
        insert.setBody(null);
        mapper.getMembers().addLast(insert);
        return methodName;
    }

}