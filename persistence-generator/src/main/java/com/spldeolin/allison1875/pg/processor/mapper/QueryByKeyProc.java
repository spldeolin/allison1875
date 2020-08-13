package com.spldeolin.allison1875.pg.processor.mapper;

import static com.github.javaparser.StaticJavaParser.parseParameter;
import static com.github.javaparser.StaticJavaParser.parseType;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.pg.constant.Constant;
import com.spldeolin.allison1875.pg.javabean.PersistenceDto;
import com.spldeolin.allison1875.pg.javabean.PropertyDto;
import lombok.Getter;

/**
 * 根据外键查询，表中每有几个外键，这个Proc就生成几个方法
 *
 * 以_id结尾的字段算作外键
 *
 * @author Deolin 2020-08-08
 */
public class QueryByKeyProc extends MapperProc {

    private final PersistenceDto persistence;

    private final ClassOrInterfaceDeclaration mapper;

    @Getter
    private Boolean generateOrNot = true;

    public QueryByKeyProc(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        this.persistence = persistence;
        this.mapper = mapper;
    }

    public QueryByKeyProc process() {
        for (PropertyDto fk : persistence.getFkProperties()) {
            String methodName = "queryBy" + StringUtils.upperFirstLetter(fk.getPropertyName());
            if (super.existDeclared(mapper, methodName)) {
                generateOrNot = false;
                return this;
            }
            MethodDeclaration method = new MethodDeclaration();
            method.setJavadocComment(
                    new JavadocComment("根据" + fk.getDescription() + "查询" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
            Imports.ensureImported(mapper, "java.util.List");
            method.setType(parseType("List<" + persistence.getEntityName() + ">"));
            method.setName(methodName);
            String varName = StringUtils.lowerFirstLetter(fk.getPropertyName());
            Parameter parameter = parseParameter(fk.getJavaType().getSimpleName() + " " + varName);
            method.addParameter(parameter);
            method.setBody(null);
            mapper.getMembers().addLast(method);
        }
        return this;
    }

}