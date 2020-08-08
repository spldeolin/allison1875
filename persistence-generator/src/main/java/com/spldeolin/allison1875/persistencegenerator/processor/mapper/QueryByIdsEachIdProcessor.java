package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import static com.github.javaparser.StaticJavaParser.parseParameter;
import static com.github.javaparser.StaticJavaParser.parseType;

import java.util.List;
import org.atteo.evo.inflector.English;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.google.common.collect.Iterables;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;

/**
 * 删除所有queryByIds方法，再在头部插入List<BizEntity> queryByIds(@Param Collection<PkType> ids);
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
            List<MethodDeclaration> methods = mapper.getMethodsByName("queryByIdsEachId");
            methods.forEach(Node::remove);
            MethodDeclaration queryByIdsEachId = new MethodDeclaration();
            queryByIdsEachId.setJavadocComment(
                    new JavadocComment("根据ID查询，并以ID为key映射到Map" + Constant.PROHIBIT_MODIFICATION_JAVADOC));
            Imports.ensureImported(mapper, "org.apache.ibatis.annotations.MapKey");
            Imports.ensureImported(mapper, "java.util.Map");
            Imports.ensureImported(mapper, "java.util.Collection");
            Imports.ensureImported(mapper, "org.apache.ibatis.annotations.Param");
            PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getPkProperties());
            String varName = StringUtils.lowerFirstLetter(onlyPk.getPropertyName());
            String pkTypeName = onlyPk.getJavaType().getSimpleName();
            queryByIdsEachId.setType(parseType(
                    "@MapKey(\"" + varName + "\")" + "Map<" + pkTypeName + ", " + persistence.getEntityName() + ">"));
            queryByIdsEachId.setName("queryByIdsEachId");
            String varsName = English.plural(varName);
            queryByIdsEachId.addParameter(
                    parseParameter("@Param(\"" + varsName + "\") Collection<" + pkTypeName + "> " + varsName));
            queryByIdsEachId.setBody(null);
            mapper.getMembers().addLast(queryByIdsEachId);
        }
        return this;
    }

}