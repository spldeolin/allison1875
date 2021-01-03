package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
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
            Long listAllLimit = persistenceGeneratorConfig.getListAllLimit();
            Javadoc javadoc = new JavadocComment(
                    String.format("获取全部\n\n出于性能考虑，这个方法只会返回最多%s条数据\n事实上，只建议对数据量不大于%s的配置表或常量表使用该方法，否则无法返回“全部”数据",
                            listAllLimit, listAllLimit) + Constant.PROHIBIT_MODIFICATION_JAVADOC).parse();
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