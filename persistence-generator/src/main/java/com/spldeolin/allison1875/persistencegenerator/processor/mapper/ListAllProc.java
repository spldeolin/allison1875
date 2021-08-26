package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;

/**
 * 根据主键查询
 *
 * @author Deolin 2020-07-18
 */
@Singleton
public class ListAllProc extends MapperProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public String process(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableListAll()) {
            return null;
        }
        String methodName = null;
        if (persistence.getIdProperties().size() > 0) {
            methodName = super.calcMethodName(mapper, "listAll");
            MethodDeclaration listAll = new MethodDeclaration();
            Javadoc javadoc = new JavadocComment("获取全部" + persistence.getLotNo().asJavadocDescription()).parse();
            listAll.setType(StaticJavaParser.parseType("List<" + persistence.getEntityName() + ">"));
            listAll.setName(methodName);

            javadoc.addBlockTag("return", "（多个）" + persistence.getDescrption());
            listAll.setJavadocComment(javadoc);
            listAll.setBody(null);
            mapper.getMembers().addLast(listAll);
        }
        return methodName;
    }

}