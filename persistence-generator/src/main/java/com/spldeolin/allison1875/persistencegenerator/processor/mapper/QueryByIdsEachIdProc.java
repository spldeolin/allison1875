package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import static com.github.javaparser.StaticJavaParser.parseAnnotation;
import static com.github.javaparser.StaticJavaParser.parseParameter;
import static com.github.javaparser.StaticJavaParser.parseType;

import org.atteo.evo.inflector.English;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.google.common.collect.Iterables;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import lombok.Getter;

/**
 * 根据主键列表查询，并把结果集以主键为key，映射到Map中
 *
 * 表是联合主键时，这个Proc不生成方法
 *
 * @author Deolin 2020-07-18
 */
public class QueryByIdsEachIdProc extends MapperProc {

    private final PersistenceDto persistence;

    private final ClassOrInterfaceDeclaration mapper;

    @Getter
    private String methodName;

    public QueryByIdsEachIdProc(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        this.persistence = persistence;
        this.mapper = mapper;
    }

    public QueryByIdsEachIdProc process() {
        if (PersistenceGeneratorConfig.getInstance().getDisableQueryByIdsEachId()) {
            return this;
        }
        if (persistence.getIdProperties().size() == 1) {
            methodName = calcMethodName(mapper, "queryByIdsEachId");
            MethodDeclaration queryByIdsEachId = new MethodDeclaration();

            Javadoc javadoc = new JavadocComment("根据多个ID查询，并以ID作为key映射到Map" + Constant.PROHIBIT_MODIFICATION_JAVADOC)
                    .parse();

            queryByIdsEachId.setJavadocComment(javadoc);
            Imports.ensureImported(mapper, "org.apache.ibatis.annotations.MapKey");
            Imports.ensureImported(mapper, "java.util.Map");
            Imports.ensureImported(mapper, "java.util.Collection");
            Imports.ensureImported(mapper, "org.apache.ibatis.annotations.Param");
            PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getIdProperties());
            String varName = StringUtils.lowerFirstLetter(onlyPk.getPropertyName());
            String pkTypeName = onlyPk.getJavaType().getSimpleName();
            queryByIdsEachId.addAnnotation(parseAnnotation("@MapKey(\"" + varName + "\")"));
            queryByIdsEachId.setType(parseType("Map<" + pkTypeName + ", " + persistence.getEntityName() + ">"));
            queryByIdsEachId.setName(methodName);
            String varsName = English.plural(varName);
            queryByIdsEachId.addParameter(
                    parseParameter("@Param(\"" + varsName + "\") Collection<" + pkTypeName + "> " + varsName));
            queryByIdsEachId.setBody(null);
            javadoc.addBlockTag("param", varsName, "（多个）" + onlyPk.getDescription());
            javadoc.addBlockTag("return", "（多个）（以ID为key）" + onlyPk.getDescription());
            mapper.getMembers().addLast(queryByIdsEachId);
        }
        return this;
    }

}