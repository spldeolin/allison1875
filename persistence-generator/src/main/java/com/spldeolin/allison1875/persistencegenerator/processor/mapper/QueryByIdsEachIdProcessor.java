package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import static com.github.javaparser.StaticJavaParser.parseParameter;
import static com.github.javaparser.StaticJavaParser.parseType;

import java.util.List;
import org.atteo.evo.inflector.English;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.google.common.collect.Iterables;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;

/**
 * 删除所有queryByIdsAsMap方法
 * 再在头部插入@MapKey("id") Map<Long, BizEntity> queryByIdsEachId(Collection<Long> ids);
 * 只有单主键时才会生成
 *
 * @author Deolin 2020-07-18
 */
public class QueryByIdsEachIdProcessor {

    private final PersistenceDto persistence;

    private final ClassOrInterfaceDeclaration mapper;

    public QueryByIdsEachIdProcessor(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        this.persistence = persistence;
        this.mapper = mapper;
    }

    public QueryByIdsEachIdProcessor process() {
        if (persistence.getPkProperties().size() == 1) {
            List<MethodDeclaration> methods = mapper.getMethodsByName("queryByIds");
            methods.forEach(Node::remove);
            MethodDeclaration queryByIds = new MethodDeclaration();
            queryByIds.setJavadocComment(new JavadocComment("根据ID查询数据" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
            Imports.ensureImported(mapper, "java.util.List");
            Imports.ensureImported(mapper, "java.util.Collection");
            Imports.ensureImported(mapper, "org.apache.ibatis.annotations.Param");
            PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getPkProperties());
            queryByIds.setType(parseType("List<" + persistence.getEntityName() + ">"));
            queryByIds.setName("queryByIds");
            String varsName = English.plural(StringUtils.lowerFirstLetter(onlyPk.getPropertyName()));
            Parameter parameter = parseParameter(
                    "@Param(\"" + varsName + "\") Collection<" + onlyPk.getJavaType().getSimpleName() + "> "
                            + varsName);
            queryByIds.addParameter(parameter);
            queryByIds.setBody(null);
            mapper.getMembers().addFirst(queryByIds);
        }
        return this;
    }

}