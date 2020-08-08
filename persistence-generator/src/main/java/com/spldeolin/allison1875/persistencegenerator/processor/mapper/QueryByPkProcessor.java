package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import static com.github.javaparser.StaticJavaParser.parseParameter;

import java.util.List;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;

/**
 * 根据主键查询
 *
 * @author Deolin 2020-07-18
 */
public class QueryByPkProcessor {

    private final PersistenceDto persistence;

    private final ClassOrInterfaceDeclaration mapper;

    public QueryByPkProcessor(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        this.persistence = persistence;
        this.mapper = mapper;
    }

    public QueryByPkProcessor process() {
        if (persistence.getPkProperties().size() > 0) {
            List<MethodDeclaration> methods = mapper.getMethodsByName("queryById");
            methods.forEach(Node::remove);
            MethodDeclaration queryById = new MethodDeclaration();
            queryById.setJavadocComment(new JavadocComment("根据ID查询" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
            queryById.setType(new ClassOrInterfaceType().setName(persistence.getEntityName()));
            queryById.setName("queryById");
            Imports.ensureImported(mapper, "org.apache.ibatis.annotations.Param");
            for (PropertyDto pk : persistence.getPkProperties()) {
                String varName = StringUtils.lowerFirstLetter(pk.getPropertyName());
                Parameter parameter = parseParameter(pk.getJavaType().getSimpleName() + " " + varName);
                queryById.addParameter(parameter);
            }
            queryById.setBody(null);
            mapper.getMembers().addLast(queryById);
        }
        return this;
    }

}