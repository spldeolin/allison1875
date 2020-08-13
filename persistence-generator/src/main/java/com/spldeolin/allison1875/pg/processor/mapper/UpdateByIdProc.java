package com.spldeolin.allison1875.pg.processor.mapper;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.PrimitiveType;
import com.spldeolin.allison1875.pg.constant.Constant;
import com.spldeolin.allison1875.pg.javabean.PersistenceDto;
import lombok.Getter;

/**
 * 根据ID更新数据，忽略值为null的属性
 *
 * @author Deolin 2020-07-18
 */
public class UpdateByIdProc extends MapperProc {

    private final PersistenceDto persistence;

    private final ClassOrInterfaceDeclaration mapper;

    @Getter
    private Boolean generateOrNot = true;

    public UpdateByIdProc(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        this.persistence = persistence;
        this.mapper = mapper;
    }

    public UpdateByIdProc process() {
        if (persistence.getPkProperties().size() > 0) {
            if (super.existDeclared(mapper, "updateById")) {
                generateOrNot = false;
                return this;
            }
            MethodDeclaration updateById = new MethodDeclaration();
            updateById.setJavadocComment(
                    new JavadocComment("根据ID更新数据，忽略值为null的属性" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
            updateById.setType(PrimitiveType.intType());
            updateById.setName("updateById");
            updateById.addParameter(persistence.getEntityName(), "entity");
            updateById.setBody(null);
            mapper.getMembers().addLast(updateById);
        }
        return this;
    }

}