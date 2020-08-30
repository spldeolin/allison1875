package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.PrimitiveType;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * 插入
 *
 * @author Deolin 2020-07-18
 */
@Log4j2
public class InsertProc extends MapperProc {

    private final PersistenceDto persistence;

    private final ClassOrInterfaceDeclaration mapper;

    @Getter
    private String methodName;

    public InsertProc(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        this.persistence = persistence;
        this.mapper = mapper;
    }

    public InsertProc process() {
        if (PersistenceGeneratorConfig.getInstance().getDisableInsert()) {
            return this;
        }
        methodName = super.calcMethodName(mapper, "insert");
        MethodDeclaration insert = new MethodDeclaration();
        insert.setJavadocComment(new JavadocComment("插入" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(persistence.getEntityName(), "entity");
        insert.setBody(null);
        mapper.getMembers().addLast(insert);
        return this;
    }

}