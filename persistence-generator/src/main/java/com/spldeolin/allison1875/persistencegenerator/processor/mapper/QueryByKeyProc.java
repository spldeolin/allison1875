package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import static com.github.javaparser.StaticJavaParser.parseParameter;
import static com.github.javaparser.StaticJavaParser.parseType;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;

/**
 * 根据外键查询，表中每有几个外键，这个Proc就生成几个方法
 *
 * 以_id结尾的字段算作外键
 *
 * @author Deolin 2020-08-08
 */
@Singleton
public class QueryByKeyProc extends MapperProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public String process(PersistenceDto persistence, PropertyDto key, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableQueryByKey()) {
            return null;
        }
        String methodName = calcMethodName(mapper, "queryBy" + MoreStringUtils.upperFirstLetter(key.getPropertyName()));
        MethodDeclaration method = new MethodDeclaration();
        Javadoc javadoc = new JavadocComment(
                "根据" + key.getDescription() + "查询" + persistence.getLotNo().asJavadocDescription()).parse();

        Imports.ensureImported(mapper, "java.util.List");
        method.setType(parseType("List<" + persistence.getEntityName() + ">"));
        method.setName(methodName);
        String varName = MoreStringUtils.lowerFirstLetter(key.getPropertyName());
        Parameter parameter = parseParameter(key.getJavaType().getSimpleName() + " " + varName);
        method.addParameter(parameter);
        method.setBody(null);
        javadoc.addBlockTag("param", varName, key.getDescription());
        javadoc.addBlockTag("return", persistence.getDescrption());
        method.setJavadocComment(javadoc);
        mapper.getMembers().addLast(method);
        return methodName;
    }

}