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
 * 删除所有updateById方法
 * 再在头部插入int updateById(BizEntity entity);
 *
 * @author Deolin 2020-07-18
 */
public class UpdateByIdProcessor {

    private final PersistenceDto persistence;

    private final ClassOrInterfaceDeclaration mapper;

    public UpdateByIdProcessor(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        this.persistence = persistence;
        this.mapper = mapper;
    }

    public UpdateByIdProcessor process() {
        if (persistence.getPkProperties().size() > 0) {
            List<MethodDeclaration> methods = mapper.getMethodsByName("updateById");
            methods.forEach(Node::remove);
            MethodDeclaration updateById = new MethodDeclaration();
            updateById.setJavadocComment(
                    new JavadocComment("根据ID更新数据，忽略值为null的属性" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
            updateById.setType(PrimitiveType.intType());
            updateById.setName("updateById");
            updateById.addParameter(persistence.getEntityName(), "entity");
            updateById.setBody(null);
            mapper.getMembers().addFirst(updateById);
        }
        return this;
    }

}