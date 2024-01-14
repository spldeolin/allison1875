package com.spldeolin.allison1875.persistencegenerator.service.impl;

import static com.github.javaparser.StaticJavaParser.parseAnnotation;
import static com.github.javaparser.StaticJavaParser.parseParameter;
import static com.github.javaparser.StaticJavaParser.parseType;

import java.util.Collection;
import java.util.List;
import org.atteo.evo.inflector.English;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.constant.ImportConstant;
import com.spldeolin.allison1875.common.exception.CuAbsentException;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.common.util.ast.JavadocDescriptions;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.QueryByKeysDto;
import com.spldeolin.allison1875.persistencegenerator.service.MapperService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-08-13
 */
@Log4j2
@Singleton
public class MapperServiceImpl implements MapperService {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    @Override
    public String batchInsertEvenNull(PersistenceDto persistence, JavabeanGeneration javabeanGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableBatchInsertEvenNull()) {
            return null;
        }
        String methodName = this.calcMethodName(mapper, "batchInsertEvenNull");
        MethodDeclaration insert = new MethodDeclaration();
        String comment = concatMapperMethodComment(persistence, "批量插入，为null的属性会被作为null插入");
        insert.setJavadocComment(comment);
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(StaticJavaParser.parseParameter(
                "@Param(\"entities\") Collection<" + javabeanGeneration.getJavabeanName() + "> entities"));
        insert.setBody(null);
        mapper.getMembers().addLast(insert);
        return methodName;
    }

    @Override
    public String batchInsert(PersistenceDto persistence, JavabeanGeneration javabeanGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableBatchInsert()) {
            return null;
        }
        String methodName = this.calcMethodName(mapper, "batchInsert");
        MethodDeclaration insert = new MethodDeclaration();
        String comment = concatMapperMethodComment(persistence, "批量插入");
        insert.setJavadocComment(comment);
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(StaticJavaParser.parseParameter(
                "@Param(\"entities\") Collection<" + javabeanGeneration.getJavabeanName() + "> entities"));
        insert.setBody(null);
        mapper.getMembers().addLast(insert);
        return methodName;
    }

    @Override
    public String batchUpdateEvenNull(PersistenceDto persistence, JavabeanGeneration javabeanGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableBatchUpdateEvenNull()) {
            return null;
        }

        String methodName = this.calcMethodName(mapper, "batchUpdateEvenNull");
        MethodDeclaration update = new MethodDeclaration();
        String comment = concatMapperMethodComment(persistence, "批量根据ID更新数据，为null对应的字段会被更新为null");
        update.setJavadocComment(comment);
        update.setType(PrimitiveType.intType());
        update.setName(methodName);
        update.addParameter(StaticJavaParser.parseParameter(
                "@Param(\"entities\") Collection<" + javabeanGeneration.getJavabeanName() + "> entities"));
        update.setBody(null);
        mapper.getMembers().addLast(update);
        return methodName;
    }

    @Override
    public String batchUpdate(PersistenceDto persistence, JavabeanGeneration javabeanGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableBatchUpdate()) {
            return null;
        }

        String methodName = this.calcMethodName(mapper, "batchUpdate");
        MethodDeclaration update = new MethodDeclaration();
        String comment = concatMapperMethodComment(persistence, "批量根据ID更新数据");
        update.setJavadocComment(comment);
        update.setType(PrimitiveType.intType());
        update.setName(methodName);
        update.addParameter(StaticJavaParser.parseParameter(
                "@Param(\"entities\") Collection<" + javabeanGeneration.getJavabeanName() + "> entities"));
        update.setBody(null);
        mapper.getMembers().addLast(update);
        return methodName;
    }

    @Override
    public String deleteByKey(PersistenceDto persistence, PropertyDto key, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableDeleteByKey() || !persistence.getIsDeleteFlagExist()) {
            return null;
        }
        String methodName = calcMethodName(mapper,
                "deleteBy" + MoreStringUtils.upperFirstLetter(key.getPropertyName()));
        MethodDeclaration method = new MethodDeclaration();
        String varName = MoreStringUtils.lowerFirstLetter(key.getPropertyName());
        String comment = concatMapperMethodComment(persistence, "根据「" + key.getDescription() + "」删除");
        method.setJavadocComment(comment);
        method.setType(PrimitiveType.intType());
        method.setName(methodName);

        Parameter parameter = parseParameter(key.getJavaType().getSimpleName() + " " + varName);
        method.addParameter(parameter);
        method.setBody(null);
        mapper.getMembers().addLast(method);
        return methodName;
    }

    @Override
    public String insertOrUpdate(PersistenceDto persistence, JavabeanGeneration javabeanGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableInsertOrUpdate()) {
            return null;
        }
        String methodName = this.calcMethodName(mapper, "insertOrUpdate");
        MethodDeclaration insert = new MethodDeclaration();
        String comment = concatMapperMethodComment(persistence,
                "尝试插入，若指定了id并存在，则更新，即INSERT ON DUPLICATE KEY UPDATE");
        insert.setJavadocComment(comment);
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(javabeanGeneration.getJavabeanName(), "entity");
        insert.setBody(null);
        mapper.getMembers().addLast(insert);
        return methodName;
    }

    @Override
    public String insert(PersistenceDto persistence, JavabeanGeneration javabeanGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableInsert()) {
            return null;
        }
        String methodName = this.calcMethodName(mapper, "insert");
        MethodDeclaration insert = new MethodDeclaration();
        String comment = concatMapperMethodComment(persistence, "插入");
        insert.setJavadocComment(comment);
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(javabeanGeneration.getJavabeanName(), "entity");
        insert.setBody(null);
        mapper.getMembers().addLast(insert);
        return methodName;
    }

    @Override
    public String listAll(PersistenceDto persistence, JavabeanGeneration javabeanGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableListAll()) {
            return null;
        }
        String methodName = null;
        if (persistence.getIdProperties().size() > 0) {
            methodName = this.calcMethodName(mapper, "listAll");
            MethodDeclaration listAll = new MethodDeclaration();
            String comment = concatMapperMethodComment(persistence, "获取全部");
            listAll.setType(StaticJavaParser.parseType("List<" + javabeanGeneration.getJavabeanName() + ">"));
            listAll.setName(methodName);
            listAll.setJavadocComment(comment);
            listAll.setBody(null);
            mapper.getMembers().addLast(listAll);
        }
        return methodName;
    }

    @Override
    public String queryByEntity(PersistenceDto persistence, JavabeanGeneration javabeanGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableQueryByEntity()) {
            return null;
        }

        String methodName = this.calcMethodName(mapper, "queryByEntity");
        MethodDeclaration queryByEntity = new MethodDeclaration();
        String comment = concatMapperMethodComment(persistence, "根据实体内的属性查询");
        mapper.findCompilationUnit().orElseThrow(CuAbsentException::new).addImport(ImportConstant.JAVA_UTIL);
        queryByEntity.setType(parseType("List<" + javabeanGeneration.getJavabeanName() + ">"));
        queryByEntity.setName(methodName);
        queryByEntity.addParameter(javabeanGeneration.getJavabeanName(), "entity");
        queryByEntity.setBody(null);
        queryByEntity.setJavadocComment(comment);
        mapper.getMembers().addLast(queryByEntity);
        return methodName;
    }

    @Override
    public String queryById(PersistenceDto persistence, JavabeanGeneration javabeanGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableQueryById()) {
            return null;
        }
        String methodName = null;
        if (persistence.getIdProperties().size() > 0) {
            methodName = this.calcMethodName(mapper, "queryById");
            MethodDeclaration queryById = new MethodDeclaration();
            String comment = concatMapperMethodComment(persistence, "根据ID查询");
            queryById.setType(new ClassOrInterfaceType().setName(javabeanGeneration.getJavabeanName()));
            queryById.setName(methodName);

            if (persistence.getIdProperties().size() == 1) {
                PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getIdProperties());
                String varName = MoreStringUtils.lowerFirstLetter(onlyPk.getPropertyName());
                Parameter parameter = parseParameter(onlyPk.getJavaType().getSimpleName() + " " + varName);
                queryById.addParameter(parameter);
            } else {
                mapper.findCompilationUnit().orElseThrow(CuAbsentException::new)
                        .addImport(ImportConstant.APACHE_IBATIS);
                for (PropertyDto pk : persistence.getIdProperties()) {
                    String varName = MoreStringUtils.lowerFirstLetter(pk.getPropertyName());
                    Parameter parameter = parseParameter(
                            "@Param(\"" + varName + "\")" + pk.getJavaType().getSimpleName() + " " + varName);
                    queryById.addParameter(parameter);
                }
            }
            queryById.setJavadocComment(comment);
            queryById.setBody(null);
            mapper.getMembers().addLast(queryById);
        }
        return methodName;
    }

    @Override
    public String queryByIdsEachId(PersistenceDto persistence, JavabeanGeneration javabeanGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableQueryByIdsEachId()) {
            return null;
        }
        String methodName = null;
        if (persistence.getIdProperties().size() == 1) {
            methodName = calcMethodName(mapper, "queryByIdsEachId");
            MethodDeclaration queryByIdsEachId = new MethodDeclaration();
            String comment = concatMapperMethodComment(persistence, "根据多个ID查询，并以ID作为key映射到Map");
            CompilationUnit cu = mapper.findCompilationUnit().orElseThrow(CuAbsentException::new);
            cu.addImport(ImportConstant.APACHE_IBATIS);
            cu.addImport(ImportConstant.JAVA_UTIL);
            PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getIdProperties());
            String varName = MoreStringUtils.lowerFirstLetter(onlyPk.getPropertyName());
            String pkTypeName = onlyPk.getJavaType().getSimpleName();
            queryByIdsEachId.addAnnotation(parseAnnotation("@MapKey(\"" + varName + "\")"));
            queryByIdsEachId.setType(
                    parseType("Map<" + pkTypeName + ", " + javabeanGeneration.getJavabeanName() + ">"));
            queryByIdsEachId.setName(methodName);
            String varsName = English.plural(varName);
            queryByIdsEachId.addParameter(
                    parseParameter("@Param(\"" + varsName + "\") Collection<" + pkTypeName + "> " + varsName));
            queryByIdsEachId.setBody(null);
            queryByIdsEachId.setJavadocComment(comment);
            mapper.getMembers().addLast(queryByIdsEachId);
        }
        return methodName;
    }

    @Override
    public String queryByIds(PersistenceDto persistence, JavabeanGeneration javabeanGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableQueryByIds()) {
            return null;
        }
        String methodName = null;
        if (persistence.getIdProperties().size() == 1) {
            methodName = calcMethodName(mapper, "queryByIds");
            MethodDeclaration queryByIds = new MethodDeclaration();
            String comment = concatMapperMethodComment(persistence, "根据多个ID查询");
            CompilationUnit cu = mapper.findCompilationUnit().orElseThrow(CuAbsentException::new);
            cu.addImport(ImportConstant.APACHE_IBATIS);
            cu.addImport(ImportConstant.JAVA_UTIL);
            PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getIdProperties());
            queryByIds.setType(parseType("List<" + javabeanGeneration.getJavabeanName() + ">"));
            queryByIds.setName(methodName);
            String varsName = English.plural(MoreStringUtils.lowerFirstLetter(onlyPk.getPropertyName()));
            Parameter parameter = parseParameter(
                    "@Param(\"" + varsName + "\") Collection<" + onlyPk.getJavaType().getSimpleName() + "> "
                            + varsName);
            queryByIds.addParameter(parameter);
            queryByIds.setBody(null);
            queryByIds.setJavadocComment(comment);
            mapper.getMembers().addLast(queryByIds);
        }
        return methodName;
    }

    @Override
    public String queryByKey(PersistenceDto persistence, JavabeanGeneration javabeanGeneration, PropertyDto key,
            ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableQueryByKey()) {
            return null;
        }
        String methodName = calcMethodName(mapper, "queryBy" + MoreStringUtils.upperFirstLetter(key.getPropertyName()));
        MethodDeclaration method = new MethodDeclaration();
        String comment = concatMapperMethodComment(persistence, "根据「" + key.getDescription() + "」查询");
        mapper.findCompilationUnit().orElseThrow(CuAbsentException::new).addImport(ImportConstant.JAVA_UTIL);
        method.setType(parseType("List<" + javabeanGeneration.getJavabeanName() + ">"));
        method.setName(methodName);
        String varName = MoreStringUtils.lowerFirstLetter(key.getPropertyName());
        Parameter parameter = parseParameter(key.getJavaType().getSimpleName() + " " + varName);
        method.addParameter(parameter);
        method.setBody(null);
        method.setJavadocComment(comment);
        mapper.getMembers().addLast(method);
        return methodName;
    }

    @Override
    public QueryByKeysDto queryByKeys(PersistenceDto persistence, JavabeanGeneration javabeanGeneration,
            PropertyDto key, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableQueryByKeys()) {
            return null;
        }
        String methodName = calcMethodName(mapper,
                "queryBy" + English.plural(MoreStringUtils.upperFirstLetter(key.getPropertyName())));
        MethodDeclaration method = new MethodDeclaration();
        String comment = concatMapperMethodComment(persistence, "根据多个「" + key.getDescription() + "」查询");
        mapper.findCompilationUnit().orElseThrow(CuAbsentException::new).addImport(ImportConstant.JAVA_UTIL);
        method.setType(parseType("List<" + javabeanGeneration.getJavabeanName() + ">"));
        method.setName(methodName);
        String typeName = "Collection<" + key.getJavaType().getSimpleName() + ">";
        String varsName = English.plural(MoreStringUtils.lowerFirstLetter(key.getPropertyName()));
        String paramAnno = "@Param(\"" + varsName + "\")";
        Parameter parameter = parseParameter(paramAnno + " " + typeName + " " + varsName);
        method.addParameter(parameter);
        method.setBody(null);
        method.setJavadocComment(comment);
        mapper.getMembers().addLast(method);
        return new QueryByKeysDto().setKey(key).setMethodName(methodName).setVarsName(varsName);
    }

    @Override
    public String updateByIdEvenNull(PersistenceDto persistence, JavabeanGeneration javabeanGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableUpdateByIdEvenNull()) {
            return null;
        }
        String methodName = null;
        if (persistence.getIdProperties().size() > 0) {
            methodName = calcMethodName(mapper, "updateByIdEvenNull");
            MethodDeclaration updateByIdEvenNull = new MethodDeclaration();
            String comment = concatMapperMethodComment(persistence,
                    "根据ID更新数据，为null属性对应的字段会被更新为null");
            updateByIdEvenNull.setJavadocComment(comment);
            updateByIdEvenNull.setType(PrimitiveType.intType());
            updateByIdEvenNull.setName(methodName);
            updateByIdEvenNull.addParameter(javabeanGeneration.getJavabeanName(), "entity");
            updateByIdEvenNull.setBody(null);
            mapper.getMembers().addLast(updateByIdEvenNull);
        }
        return methodName;
    }

    @Override
    public String updateById(PersistenceDto persistence, JavabeanGeneration javabeanGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableUpdateById()) {
            return null;
        }
        String methodName = null;
        if (persistence.getIdProperties().size() > 0) {
            methodName = calcMethodName(mapper, "updateById");
            MethodDeclaration updateById = new MethodDeclaration();
            String comment = concatMapperMethodComment(persistence, "根据ID更新数据，忽略值为null的属性");
            updateById.setJavadocComment(comment);
            updateById.setType(PrimitiveType.intType());
            updateById.setName(methodName);
            updateById.addParameter(javabeanGeneration.getJavabeanName(), "entity");
            updateById.setBody(null);
            mapper.getMembers().addLast(updateById);
        }
        return methodName;
    }

    private String calcMethodName(ClassOrInterfaceDeclaration mapper, String expectMethodName) {
        int v = 2;
        while (true) {
            if (existDeclared(mapper, expectMethodName)) {
                String newName = expectMethodName + "V" + v;
                log.warn("[{}]中已声明了的名为[{}]方法，待生成的方法重命名为[{}]", mapper.getNameAsString(),
                        expectMethodName, newName);
                expectMethodName = newName;
                v++;
            } else {
                return expectMethodName;
            }
        }
    }

    private boolean existDeclared(ClassOrInterfaceDeclaration mapper, String methodName) {
        List<MethodDeclaration> methods = mapper.getMethodsByName(methodName);
        for (MethodDeclaration method : methods) {
            Collection<String> descriptionLines = JavadocDescriptions.getAsLines(method);
            if (descriptionLines.stream().anyMatch(line -> line.contains(BaseConstant.LOT_NO_ANNOUNCE_PREFIXION))) {
                method.remove();
            }
        }
        return mapper.getMethodsByName(methodName).size() > 0;
    }

    private String concatMapperMethodComment(PersistenceDto persistence, String methodDescription) {
        String result = methodDescription;
        if (persistenceGeneratorConfig.getEnableNoModifyAnnounce()
                || persistenceGeneratorConfig.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE;
        }
        if (persistenceGeneratorConfig.getEnableNoModifyAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.NO_MODIFY_ANNOUNCE;
        }
        if (persistenceGeneratorConfig.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + persistence.getLotNo();
        }
        return result;
    }

}