package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import static com.github.javaparser.StaticJavaParser.parseType;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;

/**
 * @author Deolin 2020-10-27
 */
@Singleton
public class QueryByEntityProc extends MapperProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public String process(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableQueryByEntity()) {
            return null;
        }

        String methodName = super.calcMethodName(mapper, "queryByEntity");
        MethodDeclaration queryByEntity = new MethodDeclaration();
        Javadoc javadoc = new JavadocComment("根据实体内的属性查询" + persistence.getLotNo().asJavadocDescription()).parse();
        Imports.ensureImported(mapper, "java.util.List");
        queryByEntity.setType(parseType("List<" + persistence.getEntityName() + ">"));
        queryByEntity.setName(methodName);
        queryByEntity.addParameter(persistence.getEntityName(), "entity");
        queryByEntity.setBody(null);
        javadoc.addBlockTag("param", "entity", "条件");
        javadoc.addBlockTag("return", "（多个）" + persistence.getDescrption());
        queryByEntity.setJavadocComment(javadoc);
        mapper.getMembers().addLast(queryByEntity);
        return methodName;
    }

}