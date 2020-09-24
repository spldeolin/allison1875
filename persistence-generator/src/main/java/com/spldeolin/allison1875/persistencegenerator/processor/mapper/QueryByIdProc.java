package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import static com.github.javaparser.StaticJavaParser.parseParameter;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
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
 * 根据主键查询
 *
 * @author Deolin 2020-07-18
 */
public class QueryByIdProc extends MapperProc {

    private final PersistenceDto persistence;

    private final ClassOrInterfaceDeclaration mapper;

    @Getter
    private String methodName;

    public QueryByIdProc(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        this.persistence = persistence;
        this.mapper = mapper;
    }

    public QueryByIdProc process() {
        if (PersistenceGeneratorConfig.getInstance().getDisableQueryById()) {
            return this;
        }
        if (persistence.getIdProperties().size() > 0) {
            methodName = super.calcMethodName(mapper, "queryById");
            MethodDeclaration queryById = new MethodDeclaration();
            Javadoc javadoc = new JavadocComment("根据ID查询" + Constant.PROHIBIT_MODIFICATION_JAVADOC).parse();
            queryById.setType(new ClassOrInterfaceType().setName(persistence.getEntityName()));
            queryById.setName(methodName);

            if (persistence.getIdProperties().size() == 1) {
                PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getIdProperties());
                String varName = StringUtils.lowerFirstLetter(onlyPk.getPropertyName());
                Parameter parameter = parseParameter(onlyPk.getJavaType().getSimpleName() + " " + varName);
                queryById.addParameter(parameter);
                javadoc.addBlockTag("param", varName, onlyPk.getDescription());
            } else {
                Imports.ensureImported(mapper, "org.apache.ibatis.annotations.Param");
                for (PropertyDto pk : persistence.getIdProperties()) {
                    String varName = StringUtils.lowerFirstLetter(pk.getPropertyName());
                    Parameter parameter = parseParameter(
                            "@Param(\"" + varName + "\")" + pk.getJavaType().getSimpleName() + " " + varName);
                    queryById.addParameter(parameter);
                    javadoc.addBlockTag("param", varName, pk.getDescription());
                }
            }
            javadoc.addBlockTag("return", "（多个）" + persistence.getDescrption());
            queryById.setJavadocComment(javadoc);
            queryById.setBody(null);
            mapper.getMembers().addLast(queryById);
        }
        return this;
    }

}