package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import static com.github.javaparser.StaticJavaParser.parseParameter;
import static com.github.javaparser.StaticJavaParser.parseType;

import org.atteo.evo.inflector.English;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;

/**
 * 根据主键列表查询
 *
 * 表是联合主键时，这个Proc不生成方法
 *
 * @author Deolin 2020-07-18
 */
@Singleton
public class QueryByIdsProc extends MapperProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public String process(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableQueryByIds()) {
            return null;
        }
        String methodName = null;
        if (persistence.getIdProperties().size() == 1) {
            methodName = calcMethodName(mapper, "queryByIds");
            MethodDeclaration queryByIds = new MethodDeclaration();
            Javadoc javadoc = new JavadocComment("根据多个ID查询" + Constant.PROHIBIT_MODIFICATION_JAVADOC).parse();
            Imports.ensureImported(mapper, "java.util.List");
            Imports.ensureImported(mapper, "java.util.Collection");
            Imports.ensureImported(mapper, "org.apache.ibatis.annotations.Param");
            PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getIdProperties());
            queryByIds.setType(parseType("List<" + persistence.getEntityName() + ">"));
            queryByIds.setName(methodName);
            String varsName = English.plural(MoreStringUtils.lowerFirstLetter(onlyPk.getPropertyName()));
            Parameter parameter = parseParameter(
                    "@Param(\"" + varsName + "\") Collection<" + onlyPk.getJavaType().getSimpleName() + "> "
                            + varsName);
            queryByIds.addParameter(parameter);
            queryByIds.setBody(null);
            javadoc.addBlockTag("param", varsName, "（多个）" + onlyPk.getDescription());
            javadoc.addBlockTag("return", "（多个）" + persistence.getDescrption());
            queryByIds.setJavadocComment(javadoc);
            mapper.getMembers().addLast(queryByIds);
        }
        return methodName;
    }

}