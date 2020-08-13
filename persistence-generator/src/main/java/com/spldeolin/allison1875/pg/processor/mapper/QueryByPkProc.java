package com.spldeolin.allison1875.pg.processor.mapper;

import static com.github.javaparser.StaticJavaParser.parseParameter;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.google.common.collect.Iterables;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.pg.constant.Constant;
import com.spldeolin.allison1875.pg.javabean.PersistenceDto;
import com.spldeolin.allison1875.pg.javabean.PropertyDto;
import lombok.Getter;

/**
 * 根据主键查询
 *
 * @author Deolin 2020-07-18
 */
public class QueryByPkProc extends MapperProc {

    private final PersistenceDto persistence;

    private final ClassOrInterfaceDeclaration mapper;

    @Getter
    private Boolean generateOrNot = true;

    public QueryByPkProc(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        this.persistence = persistence;
        this.mapper = mapper;
    }

    public QueryByPkProc process() {
        if (persistence.getPkProperties().size() > 0) {
            if (super.existDeclared(mapper, "queryById")) {
                generateOrNot = false;
                return this;
            }
            MethodDeclaration queryById = new MethodDeclaration();
            queryById.setJavadocComment(new JavadocComment("根据ID查询" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
            queryById.setType(new ClassOrInterfaceType().setName(persistence.getEntityName()));
            queryById.setName("queryById");

            if (persistence.getPkProperties().size() == 1) {
                PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getPkProperties());
                Parameter parameter = parseParameter(onlyPk.getJavaType().getSimpleName() + " " + StringUtils
                        .lowerFirstLetter(onlyPk.getPropertyName()));
                queryById.addParameter(parameter);
            } else {
                Imports.ensureImported(mapper, "org.apache.ibatis.annotations.Param");
                for (PropertyDto pk : persistence.getPkProperties()) {
                    String varName = StringUtils.lowerFirstLetter(pk.getPropertyName());
                    Parameter parameter = parseParameter(
                            "@Param(\"" + varName + "\")" + pk.getJavaType().getSimpleName() + " " + varName);
                    queryById.addParameter(parameter);
                }
            }
            queryById.setBody(null);
            mapper.getMembers().addLast(queryById);
        }
        return this;
    }

}