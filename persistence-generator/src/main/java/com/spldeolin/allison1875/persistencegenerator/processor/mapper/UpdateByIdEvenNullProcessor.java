package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import java.util.List;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.PrimitiveType;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;

/**
 * 删除所有updateByIdEvenNull方法，再在头部插入int updateByIdEvenNull(XxxEntity xxx);
 *
 * @author Deolin 2020-07-18
 */
public class UpdateByIdEvenNullProcessor {

    private final PersistenceDto persistence;

    private final ClassOrInterfaceDeclaration mapper;

    public UpdateByIdEvenNullProcessor(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        this.persistence = persistence;
        this.mapper = mapper;
    }

    public UpdateByIdEvenNullProcessor process() {
        if (persistence.getPkProperties().size() > 0) {
            List<MethodDeclaration> methods = mapper.getMethodsByName("updateByIdEvenNull");
            methods.forEach(Node::remove);
            MethodDeclaration updateByIdEvenNull = new MethodDeclaration();
            updateByIdEvenNull.setJavadocComment(
                    new JavadocComment("根据ID更新数据，值为null的属性强制更新为null" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
            updateByIdEvenNull.setType(PrimitiveType.intType());
            updateByIdEvenNull.setName("updateByIdEvenNull");
            updateByIdEvenNull.addParameter(persistence.getEntityName(), "entity");
            updateByIdEvenNull.setBody(null);
            mapper.getMembers().addFirst(updateByIdEvenNull);
        }
        return this;
    }

}