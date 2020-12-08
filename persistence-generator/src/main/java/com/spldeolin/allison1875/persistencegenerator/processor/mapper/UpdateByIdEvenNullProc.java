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
 * 根据主键更新，即便属性的值为null，也更新为null
 *
 * @author Deolin 2020-07-18
 */
public class UpdateByIdEvenNullProc extends MapperProc {

    public String process(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (PersistenceGenerator.CONFIG.get().getDisableUpdateByIdEvenNull()) {
            return null;
        }
        String methodName = null;
        if (persistence.getIdProperties().size() > 0) {
            methodName = calcMethodName(mapper, "updateByIdEvenNull");
            MethodDeclaration updateByIdEvenNull = new MethodDeclaration();
            Javadoc javadoc = new JavadocComment("根据ID更新数据，为null的属性会被更新为null" + Constant.PROHIBIT_MODIFICATION_JAVADOC)
                    .parse();
            javadoc.addBlockTag("param", "entity", persistence.getDescrption());
            javadoc.addBlockTag("return", "更新条数");
            updateByIdEvenNull.setJavadocComment(javadoc);
            updateByIdEvenNull.setType(PrimitiveType.intType());
            updateByIdEvenNull.setName(methodName);
            updateByIdEvenNull.addParameter(persistence.getEntityName(), "entity");
            updateByIdEvenNull.setBody(null);
            mapper.getMembers().addLast(updateByIdEvenNull);
        }
        return methodName;
    }

}