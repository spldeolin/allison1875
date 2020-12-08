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
 * 根据ID更新数据，忽略值为null的属性
 *
 * @author Deolin 2020-07-18
 */
public class UpdateByIdProc extends MapperProc {

    public String process(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (PersistenceGenerator.CONFIG.get().getDisableUpdateById()) {
            return null;
        }
        String methodName = null;
        if (persistence.getIdProperties().size() > 0) {
            methodName = calcMethodName(mapper, "updateById");
            MethodDeclaration updateById = new MethodDeclaration();
            Javadoc javadoc = new JavadocComment("根据ID更新数据，忽略值为null的属性" + Constant.PROHIBIT_MODIFICATION_JAVADOC)
                    .parse();
            javadoc.addBlockTag("param", "entity", persistence.getDescrption());
            javadoc.addBlockTag("return", "更新条数");
            updateById.setJavadocComment(javadoc);
            updateById.setType(PrimitiveType.intType());
            updateById.setName(methodName);
            updateById.addParameter(persistence.getEntityName(), "entity");
            updateById.setBody(null);
            mapper.getMembers().addLast(updateById);
        }
        return methodName;
    }

}