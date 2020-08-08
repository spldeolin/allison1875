package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import static com.github.javaparser.StaticJavaParser.parseParameter;
import static com.github.javaparser.StaticJavaParser.parseType;

import java.util.List;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;

/**
 * 根据外键查询，表中每有几个外键，这个Proc就生成几个方法
 *
 * 以_id结尾的字段算作外键
 *
 * @author Deolin 2020-08-08
 */
public class QueryByFkProcessor {

    private final PersistenceDto persistence;

    private final ClassOrInterfaceDeclaration mapper;

    public QueryByFkProcessor(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        this.persistence = persistence;
        this.mapper = mapper;
    }

    public QueryByFkProcessor process() {
        for (PropertyDto fk : persistence.getFkProperties()) {
            String methodName = "queryBy" + StringUtils.upperFirstLetter(fk.getPropertyName());
            List<MethodDeclaration> methods = mapper.getMethodsByName(methodName);
            methods.forEach(Node::remove);
            MethodDeclaration method = new MethodDeclaration();
            method.setJavadocComment(
                    new JavadocComment("根据" + fk.getDescription() + "查询" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
            Imports.ensureImported(mapper, "java.util.List");
            Imports.ensureImported(mapper, "org.apache.ibatis.annotations.Param");
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