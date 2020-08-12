package com.spldeolin.allison1875.pg.processor.mapper;

import static com.github.javaparser.StaticJavaParser.parseParameter;

import java.util.List;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.PrimitiveType;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.pg.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.pg.constant.Constant;
import com.spldeolin.allison1875.pg.javabean.PersistenceDto;
import com.spldeolin.allison1875.pg.javabean.PropertyDto;

/**
 * 根据外键删除
 *
 * 表中每有几个外键，这个Proc就生成几个方法，以_id结尾的字段算作外键
 *
 * @author Deolin 2020-08-08
 */
public class DeleteByFkProc {

    private final PersistenceDto persistence;

    private final ClassOrInterfaceDeclaration mapper;

    public DeleteByFkProc(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        this.persistence = persistence;
        this.mapper = mapper;
    }

    public DeleteByFkProc process() {
        String deletedSql = PersistenceGeneratorConfig.getInstace().getDeletedSql();
        if (deletedSql != null) {
            for (PropertyDto fk : persistence.getFkProperties()) {
                String methodName = "deleteBy" + StringUtils.upperFirstLetter(fk.getPropertyName());
                List<MethodDeclaration> methods = mapper.getMethodsByName(methodName);
                methods.forEach(Node::remove);
                MethodDeclaration method = new MethodDeclaration();
                method.setJavadocComment(
                        new JavadocComment("根据" + fk.getDescription() + "删除" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
                method.setType(PrimitiveType.intType());
                method.setName(methodName);
                String varName = StringUtils.lowerFirstLetter(fk.getPropertyName());
                Parameter parameter = parseParameter(fk.getJavaType().getSimpleName() + " " + varName);
                method.addParameter(parameter);
                method.setBody(null);
                mapper.getMembers().addLast(method);
            }
        }
        return this;
    }

}