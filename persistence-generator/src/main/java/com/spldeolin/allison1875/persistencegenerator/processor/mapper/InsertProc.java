package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.javadoc.Javadoc;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.processor.PersistenceGenerator;

/**
 * 插入
 *
 * @author Deolin 2020-07-18
 */
public class InsertProc extends MapperProc {

    public String process(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (PersistenceGenerator.CONFIG.get().getDisableInsert()) {
            return null;
        }
        String methodName = super.calcMethodName(mapper, "insert");
        MethodDeclaration insert = new MethodDeclaration();
        Javadoc javadoc = new JavadocComment("插入" + Constant.PROHIBIT_MODIFICATION_JAVADOC).parse();
        javadoc.addBlockTag("param", "entity", persistence.getDescrption());
        javadoc.addBlockTag("return", "插入条数");
        insert.setJavadocComment(javadoc);
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(persistence.getEntityName(), "entity");
        insert.setBody(null);
        mapper.getMembers().addLast(insert);
        return methodName;
    }

}