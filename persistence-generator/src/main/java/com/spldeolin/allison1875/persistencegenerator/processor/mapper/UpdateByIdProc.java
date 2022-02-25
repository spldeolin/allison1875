package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.javadoc.Javadoc;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;

/**
 * 根据ID更新数据，忽略值为null的属性
 *
 * @author Deolin 2020-07-18
 */
@Singleton
public class UpdateByIdProc extends MapperProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public String process(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableUpdateById()) {
            return null;
        }
        String methodName = null;
        if (persistence.getIdProperties().size() > 0) {
            methodName = calcMethodName(mapper, "updateById");
            MethodDeclaration updateById = new MethodDeclaration();
            String lotNoText = persistenceGeneratorConfig.getMapperInterfaceMethodPrintLotNo() ? persistence.getLotNo()
                    .asJavadocDescription() : "";
            Javadoc javadoc = new JavadocComment("根据ID更新数据，忽略值为null的属性" + lotNoText).parse();
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