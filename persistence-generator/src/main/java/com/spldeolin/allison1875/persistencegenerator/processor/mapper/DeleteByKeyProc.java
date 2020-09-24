package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import static com.github.javaparser.StaticJavaParser.parseParameter;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.javadoc.Javadoc;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import lombok.Getter;

/**
 * 根据外键删除
 *
 * 表中每有几个外键，这个Proc就生成几个方法，以_id结尾的字段算作外键
 *
 * @author Deolin 2020-08-08
 */
public class DeleteByKeyProc extends MapperProc {

    private final PersistenceDto persistence;

    @Getter
    private final PropertyDto key;

    private final ClassOrInterfaceDeclaration mapper;

    @Getter
    private String methodName;

    public DeleteByKeyProc(PersistenceDto persistence, PropertyDto key, ClassOrInterfaceDeclaration mapper) {
        this.persistence = persistence;
        this.key = key;
        this.mapper = mapper;
    }

    public DeleteByKeyProc process() {
        if (persistence.getIsDeleteFlagExist()) {
            methodName = calcMethodName(mapper, "deleteBy" + StringUtils.upperFirstLetter(key.getPropertyName()));
            MethodDeclaration method = new MethodDeclaration();
            String varName = StringUtils.lowerFirstLetter(key.getPropertyName());
            Javadoc javadoc = new JavadocComment(
                    "根据" + key.getDescription() + "删除" + Constant.PROHIBIT_MODIFICATION_JAVADOC).parse();
            javadoc.addBlockTag("param", varName, key.getDescription());
            javadoc.addBlockTag("return", "删除条数");
            method.setJavadocComment(javadoc);
            method.setType(PrimitiveType.intType());
            method.setName(methodName);

            Parameter parameter = parseParameter(key.getJavaType().getSimpleName() + " " + varName);
            method.addParameter(parameter);
            method.setBody(null);
            mapper.getMembers().addLast(method);
        }
        return this;
    }

}