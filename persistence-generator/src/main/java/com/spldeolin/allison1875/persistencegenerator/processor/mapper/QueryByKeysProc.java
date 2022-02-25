package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import static com.github.javaparser.StaticJavaParser.parseParameter;
import static com.github.javaparser.StaticJavaParser.parseType;

import org.atteo.evo.inflector.English;
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
import com.spldeolin.allison1875.persistencegenerator.javabean.QueryByKeysDto;

/**
 * 根据外键列表查询，表中每有几个外键，这个Proc就生成几个方法
 *
 * 以_id结尾的字段算作外键
 *
 * @author Deolin 2020-08-08
 */
@Singleton
public class QueryByKeysProc extends MapperProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public QueryByKeysDto process(PersistenceDto persistence, PropertyDto key, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableQueryByKeys()) {
            return null;
        }
        String methodName = calcMethodName(mapper,
                "queryBy" + English.plural(MoreStringUtils.upperFirstLetter(key.getPropertyName())));
        MethodDeclaration method = new MethodDeclaration();
        String lotNoText = persistenceGeneratorConfig.getMapperInterfaceMethodPrintLotNo() ? persistence.getLotNo()
                .asJavadocDescription() : "";
        Javadoc javadoc = new JavadocComment("根据多个「" + key.getDescription() + "」查询" + lotNoText).parse();
        Imports.ensureImported(mapper, "java.util.List");
        method.setType(parseType("List<" + persistence.getEntityName() + ">"));
        method.setName(methodName);
        String typeName = "Collection<" + key.getJavaType().getSimpleName() + ">";
        String varsName = English.plural(MoreStringUtils.lowerFirstLetter(key.getPropertyName()));
        String paramAnno = "@Param(\"" + varsName + "\")";
        Parameter parameter = parseParameter(paramAnno + " " + typeName + " " + varsName);
        method.addParameter(parameter);
        method.setBody(null);
        method.setJavadocComment(javadoc);
        mapper.getMembers().addLast(method);
        return new QueryByKeysDto().setKey(key).setMethodName(methodName).setVarsName(varsName);
    }

}