package com.spldeolin.allison1875.persistencegenerator.service.impl;

import static com.github.javaparser.StaticJavaParser.parseAnnotation;
import static com.github.javaparser.StaticJavaParser.parseParameter;
import static com.github.javaparser.StaticJavaParser.parseType;

import org.atteo.evo.inflector.English;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.QueryByKeysDto;
import com.spldeolin.allison1875.persistencegenerator.service.MapperService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-08-13
 */
@Slf4j
@Singleton
public class MapperServiceImpl implements MapperService {

    @Inject
    private PersistenceGeneratorConfig config;

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Override
    public String batchInsertEvenNull(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (config.getDisableBatchInsertEvenNull()) {
            return null;
        }
        String methodName = antiDuplicationService.getNewMethodNameIfExist("batchInsertEvenNull", mapper);
        MethodDeclaration insert = new MethodDeclaration();
        String comment = concatMapperMethodComment(persistence, "批量插入，为null的属性会被作为null插入");
        insert.setJavadocComment(comment);
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(StaticJavaParser.parseParameter(
                "@Param(\"entities\") List<" + entityGeneration.getJavabeanQualifier() + "> entities"));
        insert.setBody(null);
        mapper.getMembers().addLast(insert);
        return methodName;
    }

    @Override
    public String batchInsert(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (config.getDisableBatchInsert()) {
            return null;
        }
        String methodName = antiDuplicationService.getNewMethodNameIfExist("batchInsert", mapper);
        MethodDeclaration insert = new MethodDeclaration();
        String comment = concatMapperMethodComment(persistence, "批量插入");
        insert.setJavadocComment(comment);
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(StaticJavaParser.parseParameter(
                "@Param(\"entities\") List<" + entityGeneration.getJavabeanQualifier() + "> entities"));
        insert.setBody(null);
        mapper.getMembers().addLast(insert);
        return methodName;
    }

    @Override
    public String batchUpdateEvenNull(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (config.getDisableBatchUpdateEvenNull()) {
            return null;
        }

        String methodName = antiDuplicationService.getNewMethodNameIfExist("batchUpdateEvenNull", mapper);
        MethodDeclaration update = new MethodDeclaration();
        String comment = concatMapperMethodComment(persistence, "批量根据ID更新数据，为null对应的字段会被更新为null");
        update.setJavadocComment(comment);
        update.setType(PrimitiveType.intType());
        update.setName(methodName);
        update.addParameter(StaticJavaParser.parseParameter(
                "@Param(\"entities\") List<" + entityGeneration.getJavabeanQualifier() + "> entities"));
        update.setBody(null);
        mapper.getMembers().addLast(update);
        return methodName;
    }

    @Override
    public String batchUpdate(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (config.getDisableBatchUpdate()) {
            return null;
        }

        String methodName = antiDuplicationService.getNewMethodNameIfExist("batchUpdate", mapper);
        MethodDeclaration update = new MethodDeclaration();
        String comment = concatMapperMethodComment(persistence, "批量根据ID更新数据");
        update.setJavadocComment(comment);
        update.setType(PrimitiveType.intType());
        update.setName(methodName);
        update.addParameter(StaticJavaParser.parseParameter(
                "@Param(\"entities\") List<" + entityGeneration.getJavabeanQualifier() + "> entities"));
        update.setBody(null);
        mapper.getMembers().addLast(update);
        return methodName;
    }

    @Override
    public String deleteByKey(PersistenceDto persistence, PropertyDto key, ClassOrInterfaceDeclaration mapper) {
        if (config.getDisableDeleteByKey() || !persistence.getIsDeleteFlagExist()) {
            return null;
        }
        String methodName = antiDuplicationService.getNewMethodNameIfExist(
                "deleteBy" + MoreStringUtils.upperFirstLetter(key.getPropertyName()), mapper);
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
    public String insertOrUpdate(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (config.getDisableInsertOrUpdate()) {
            return null;
        }
        String methodName = antiDuplicationService.getNewMethodNameIfExist("insertOrUpdate", mapper);
        MethodDeclaration insert = new MethodDeclaration();
        String comment = concatMapperMethodComment(persistence,
                "尝试插入，若指定了id并存在，则更新，即INSERT ON DUPLICATE KEY UPDATE");
        insert.setJavadocComment(comment);
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(entityGeneration.getJavabeanQualifier(), "entity");
        insert.setBody(null);
        mapper.getMembers().addLast(insert);
        return methodName;
    }

    @Override
    public String insert(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (config.getDisableInsert()) {
            return null;
        }
        String methodName = antiDuplicationService.getNewMethodNameIfExist("insert", mapper);
        MethodDeclaration insert = new MethodDeclaration();
        String comment = concatMapperMethodComment(persistence, "插入");
        insert.setJavadocComment(comment);
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(entityGeneration.getJavabeanQualifier(), "entity");
        insert.setBody(null);
        mapper.getMembers().addLast(insert);
        return methodName;
    }

    @Override
    public String listAll(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (config.getDisableListAll()) {
            return null;
        }
        String methodName = null;
        if (CollectionUtils.isNotEmpty(persistence.getIdProperties())) {
            methodName = antiDuplicationService.getNewMethodNameIfExist("listAll", mapper);
            MethodDeclaration listAll = new MethodDeclaration();
            String comment = concatMapperMethodComment(persistence, "获取全部");
            listAll.setType(StaticJavaParser.parseType("List<" + entityGeneration.getJavabeanQualifier() + ">"));
            listAll.setName(methodName);
            listAll.setJavadocComment(comment);
            listAll.setBody(null);
            mapper.getMembers().addLast(listAll);
        }
        return methodName;
    }

    @Override
    public String queryByEntity(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (config.getDisableQueryByEntity()) {
            return null;
        }

        String methodName = antiDuplicationService.getNewMethodNameIfExist("queryByEntity", mapper);
        MethodDeclaration queryByEntity = new MethodDeclaration();
        String comment = concatMapperMethodComment(persistence, "根据实体内的属性查询");
        queryByEntity.setType(parseType("java.util.List<" + entityGeneration.getJavabeanQualifier() + ">"));
        queryByEntity.setName(methodName);
        queryByEntity.addParameter(entityGeneration.getJavabeanQualifier(), "entity");
        queryByEntity.setBody(null);
        queryByEntity.setJavadocComment(comment);
        mapper.getMembers().addLast(queryByEntity);
        return methodName;
    }

    @Override
    public String queryById(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (config.getDisableQueryById()) {
            return null;
        }

        String methodName = null;
        if (CollectionUtils.isNotEmpty(persistence.getIdProperties())) {
            methodName = antiDuplicationService.getNewMethodNameIfExist("queryById", mapper);
            MethodDeclaration queryById = new MethodDeclaration();
            String comment = concatMapperMethodComment(persistence, "根据ID查询");
            queryById.setType(new ClassOrInterfaceType().setName(entityGeneration.getJavabeanQualifier()));
            queryById.setName(methodName);

            if (persistence.getIdProperties().size() == 1) {
                PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getIdProperties());
                String varName = MoreStringUtils.lowerFirstLetter(onlyPk.getPropertyName());
                Parameter parameter = parseParameter(onlyPk.getJavaType().getSimpleName() + " " + varName);
                queryById.addParameter(parameter);
            } else {
                for (PropertyDto pk : persistence.getIdProperties()) {
                    String varName = MoreStringUtils.lowerFirstLetter(pk.getPropertyName());
                    Parameter parameter = parseParameter(
                            "@org.apache.ibatis.annotations.Param(\"" + varName + "\")" + pk.getJavaType()
                                    .getSimpleName() + " " + varName);
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
    public String queryByIdsEachId(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (config.getDisableQueryByIdsEachId()) {
            return null;
        }

        String methodName = null;
        if (persistence.getIdProperties().size() == 1) {
            methodName = antiDuplicationService.getNewMethodNameIfExist("queryByIdsEachId", mapper);
            MethodDeclaration queryByIdsEachId = new MethodDeclaration();
            String comment = concatMapperMethodComment(persistence, "根据多个ID查询，并以ID作为key映射到Map");
            PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getIdProperties());
            String varName = MoreStringUtils.lowerFirstLetter(onlyPk.getPropertyName());
            String pkTypeName = onlyPk.getJavaType().getSimpleName();
            queryByIdsEachId.addAnnotation(
                    parseAnnotation("@org.apache.ibatis.annotations.MapKey(\"" + varName + "\")"));
            queryByIdsEachId.setType(
                    parseType("java.util.Map<" + pkTypeName + ", " + entityGeneration.getJavabeanQualifier() + ">"));
            queryByIdsEachId.setName(methodName);
            String varsName = English.plural(varName);
            queryByIdsEachId.addParameter(parseParameter(
                    "@org.apache.ibatis.annotations.Param(\"" + varsName + "\") List<" + pkTypeName + "> " + varsName));
            queryByIdsEachId.setBody(null);
            queryByIdsEachId.setJavadocComment(comment);
            mapper.getMembers().addLast(queryByIdsEachId);
        }
        return methodName;
    }

    @Override
    public String queryByIds(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (config.getDisableQueryByIds()) {
            return null;
        }

        String methodName = null;
        if (persistence.getIdProperties().size() == 1) {
            methodName = antiDuplicationService.getNewMethodNameIfExist("queryByIds", mapper);
            MethodDeclaration queryByIds = new MethodDeclaration();
            String comment = concatMapperMethodComment(persistence, "根据多个ID查询");
            PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getIdProperties());
            queryByIds.setType(parseType("java.util.List<" + entityGeneration.getJavabeanQualifier() + ">"));
            queryByIds.setName(methodName);
            String varsName = English.plural(MoreStringUtils.lowerFirstLetter(onlyPk.getPropertyName()));
            Parameter parameter = parseParameter(
                    "@org.apache.ibatis.annotations.Param(\"" + varsName + "\") java.util.List<" + onlyPk.getJavaType()
                            .getSimpleName() + "> " + varsName);
            queryByIds.addParameter(parameter);
            queryByIds.setBody(null);
            queryByIds.setJavadocComment(comment);
            mapper.getMembers().addLast(queryByIds);
        }
        return methodName;
    }

    @Override
    public String queryByKey(PersistenceDto persistence, JavabeanGeneration entityGeneration, PropertyDto key,
            ClassOrInterfaceDeclaration mapper) {
        if (config.getDisableQueryByKey()) {
            return null;
        }

        String methodName = antiDuplicationService.getNewMethodNameIfExist(
                "queryBy" + MoreStringUtils.upperFirstLetter(key.getPropertyName()), mapper);
        MethodDeclaration method = new MethodDeclaration();
        String comment = concatMapperMethodComment(persistence, "根据「" + key.getDescription() + "」查询");
        method.setType(parseType("java.util.List<" + entityGeneration.getJavabeanQualifier() + ">"));
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
    public QueryByKeysDto queryByKeys(PersistenceDto persistence, JavabeanGeneration entityGeneration, PropertyDto key,
            ClassOrInterfaceDeclaration mapper) {
        if (config.getDisableQueryByKeys()) {
            return null;
        }

        String methodName = antiDuplicationService.getNewMethodNameIfExist(
                "queryBy" + English.plural(MoreStringUtils.upperFirstLetter(key.getPropertyName())), mapper);
        MethodDeclaration method = new MethodDeclaration();
        String comment = concatMapperMethodComment(persistence, "根据多个「" + key.getDescription() + "」查询");
        method.setType(parseType("java.util.List<" + entityGeneration.getJavabeanQualifier() + ">"));
        method.setName(methodName);
        String typeName = "java.util.List<" + key.getJavaType().getSimpleName() + ">";
        String varsName = English.plural(MoreStringUtils.lowerFirstLetter(key.getPropertyName()));
        String paramAnno = "@org.apache.ibatis.annotations.Param(\"" + varsName + "\")";
        Parameter parameter = parseParameter(paramAnno + " " + typeName + " " + varsName);
        method.addParameter(parameter);
        method.setBody(null);
        method.setJavadocComment(comment);
        mapper.getMembers().addLast(method);
        return new QueryByKeysDto().setKey(key).setMethodName(methodName).setVarsName(varsName);
    }

    @Override
    public String updateByIdEvenNull(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (config.getDisableUpdateByIdEvenNull()) {
            return null;
        }
        String methodName = null;
        if (CollectionUtils.isNotEmpty(persistence.getIdProperties())) {
            methodName = antiDuplicationService.getNewMethodNameIfExist("updateByIdEvenNull", mapper);
            MethodDeclaration updateByIdEvenNull = new MethodDeclaration();
            String comment = concatMapperMethodComment(persistence,
                    "根据ID更新数据，为null属性对应的字段会被更新为null");
            updateByIdEvenNull.setJavadocComment(comment);
            updateByIdEvenNull.setType(PrimitiveType.intType());
            updateByIdEvenNull.setName(methodName);
            updateByIdEvenNull.addParameter(entityGeneration.getJavabeanQualifier(), "entity");
            updateByIdEvenNull.setBody(null);
            mapper.getMembers().addLast(updateByIdEvenNull);
        }
        return methodName;
    }

    @Override
    public String updateById(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper) {
        if (config.getDisableUpdateById()) {
            return null;
        }
        String methodName = null;
        if (CollectionUtils.isNotEmpty(persistence.getIdProperties())) {
            methodName = antiDuplicationService.getNewMethodNameIfExist("updateById", mapper);
            MethodDeclaration updateById = new MethodDeclaration();
            String comment = concatMapperMethodComment(persistence, "根据ID更新数据，忽略值为null的属性");
            updateById.setJavadocComment(comment);
            updateById.setType(PrimitiveType.intType());
            updateById.setName(methodName);
            updateById.addParameter(entityGeneration.getJavabeanQualifier(), "entity");
            updateById.setBody(null);
            mapper.getMembers().addLast(updateById);
        }
        return methodName;
    }

    private String concatMapperMethodComment(PersistenceDto persistence, String methodDescription) {
        String result = methodDescription;
        if (config.getEnableNoModifyAnnounce() || config.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE;
        }
        if (config.getEnableNoModifyAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.NO_MODIFY_ANNOUNCE;
        }
        if (config.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + persistence.getLotNo();
        }
        return result;
    }

}