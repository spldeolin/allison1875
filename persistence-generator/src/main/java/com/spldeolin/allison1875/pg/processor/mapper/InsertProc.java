package com.spldeolin.allison1875.pg.processor.mapper;

import java.util.List;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.PrimitiveType;
import com.spldeolin.allison1875.pg.constant.Constant;
import com.spldeolin.allison1875.pg.javabean.PersistenceDto;

/**
 * 插入
 *
 * @author Deolin 2020-07-18
 */
public class InsertProc {

    private final PersistenceDto persistence;

    private final ClassOrInterfaceDeclaration mapper;

    public InsertProc(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        this.persistence = persistence;
        this.mapper = mapper;
    }

    public InsertProc process() {
        List<MethodDeclaration> methods = mapper.getMethodsByName("insert");
        methods.forEach(Node::remove);
        MethodDeclaration insert = new MethodDeclaration();
        insert.setJavadocComment(new JavadocComment("插入" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
        insert.setType(PrimitiveType.intType());
        insert.setName("insert");
        insert.addParameter(persistence.getEntityName(), "entity");
        insert.setBody(null);
        mapper.getMembers().addLast(insert);
        return this;
    }

}