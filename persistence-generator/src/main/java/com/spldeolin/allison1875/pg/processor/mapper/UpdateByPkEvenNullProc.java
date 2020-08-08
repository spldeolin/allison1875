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
 * 根据主键更新，即便属性的值为null，也更新为null
 *
 * @author Deolin 2020-07-18
 */
public class UpdateByPkEvenNullProc {

    private final PersistenceDto persistence;

    private final ClassOrInterfaceDeclaration mapper;

    public UpdateByPkEvenNullProc(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        this.persistence = persistence;
        this.mapper = mapper;
    }

    public UpdateByPkEvenNullProc process() {
        if (persistence.getPkProperties().size() > 0) {
            List<MethodDeclaration> methods = mapper.getMethodsByName("updateByIdEvenNull");
            methods.forEach(Node::remove);
            MethodDeclaration updateByIdEvenNull = new MethodDeclaration();
            updateByIdEvenNull.setJavadocComment(
                    new JavadocComment("根据ID更新数据，即便属性的值为null，也更新为null" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
            updateByIdEvenNull.setType(PrimitiveType.intType());
            updateByIdEvenNull.setName("updateByIdEvenNull");
            updateByIdEvenNull.addParameter(persistence.getEntityName(), "entity");
            updateByIdEvenNull.setBody(null);
            mapper.getMembers().addLast(updateByIdEvenNull);
        }
        return this;
    }

}