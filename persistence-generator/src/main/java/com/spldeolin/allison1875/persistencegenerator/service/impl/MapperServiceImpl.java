package com.spldeolin.allison1875.persistencegenerator.service.impl;

import static com.github.javaparser.StaticJavaParser.parseAnnotation;
import static com.github.javaparser.StaticJavaParser.parseParameter;
import static com.github.javaparser.StaticJavaParser.parseType;

import java.util.Collection;
import java.util.List;
import org.atteo.evo.inflector.English;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.javadoc.Javadoc;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.LotNo;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;
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
    public String batchInsertEvenNull(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableBatchInsertEvenNull()) {
            return null;
        }
        String methodName = this.calcMethodName(mapper, "batchInsertEvenNull");
        MethodDeclaration insert = new MethodDeclaration();
        String lotNoText = getLotNoText(persistenceGeneratorConfig, persistence);
        Javadoc javadoc = new JavadocComment("批量插入，为null的属性会被作为null插入" + lotNoText).parse();
        insert.setJavadocComment(javadoc);
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(StaticJavaParser.parseParameter(
                "@Param(\"entities\") Collection<" + persistence.getEntityName() + "> entities"));
        insert.setBody(null);
        mapper.getMembers().addLast(insert);
        return methodName;
    }

    @Override
    public String batchInsert(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableBatchInsert()) {
            return null;
        }
        String methodName = this.calcMethodName(mapper, "batchInsert");
        MethodDeclaration insert = new MethodDeclaration();
        String lotNoText = getLotNoText(persistenceGeneratorConfig, persistence);
        Javadoc javadoc = new JavadocComment("批量插入" + lotNoText).parse();
        insert.setJavadocComment(javadoc);
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(StaticJavaParser.parseParameter(
                "@Param(\"entities\") Collection<" + persistence.getEntityName() + "> entities"));
        insert.setBody(null);
        mapper.getMembers().addLast(insert);
        return methodName;
    }

    @Override
    public String batchUpdateEvenNull(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableBatchUpdateEvenNull()) {
            return null;
        }

        String methodName = this.calcMethodName(mapper, "batchUpdateEvenNull");
        MethodDeclaration update = new MethodDeclaration();
        String lotNoText = getLotNoText(persistenceGeneratorConfig, persistence);
        Javadoc javadoc = new JavadocComment("批量根据ID更新数据，为null对应的字段会被更新为null" + lotNoText).parse();
        update.setJavadocComment(javadoc);
        update.setType(PrimitiveType.intType());
        update.setName(methodName);
        update.addParameter(StaticJavaParser.parseParameter(
                "@Param(\"entities\") Collection<" + persistence.getEntityName() + "> entities"));
        update.setBody(null);
        mapper.getMembers().addLast(update);
        return methodName;
    }

    @Override
    public String batchUpdate(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableBatchUpdate()) {
            return null;
        }

        String methodName = this.calcMethodName(mapper, "batchUpdate");
        MethodDeclaration update = new MethodDeclaration();
        String lotNoText = getLotNoText(persistenceGeneratorConfig, persistence);
        Javadoc javadoc = new JavadocComment("批量根据ID更新数据" + lotNoText).parse();
        update.setJavadocComment(javadoc);
        update.setType(PrimitiveType.intType());
        update.setName(methodName);
        update.addParameter(StaticJavaParser.parseParameter(
                "@Param(\"entities\") Collection<" + persistence.getEntityName() + "> entities"));
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
        String lotNoText = getLotNoText(persistenceGeneratorConfig, persistence);
        Javadoc javadoc = new JavadocComment("根据「" + key.getDescription() + "」删除" + lotNoText).parse();
        method.setJavadocComment(javadoc);
        method.setType(PrimitiveType.intType());
        method.setName(methodName);

        Parameter parameter = parseParameter(key.getJavaType().getSimpleName() + " " + varName);
        method.addParameter(parameter);
        method.setBody(null);
        mapper.getMembers().addLast(method);
        return methodName;
    }

    @Override
    public String insertOrUpdate(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableInsertOrUpdate()) {
            return null;
        }
        String methodName = this.calcMethodName(mapper, "insertOrUpdate");
        MethodDeclaration insert = new MethodDeclaration();
        String lotNoText = getLotNoText(persistenceGeneratorConfig, persistence);
        Javadoc javadoc = new JavadocComment(
                "尝试插入，若指定了id并存在，则更新，即INSERT ON DUPLICATE KEY UPDATE" + lotNoText).parse();
        insert.setJavadocComment(javadoc);
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(persistence.getEntityName(), "entity");
        insert.setBody(null);
        mapper.getMembers().addLast(insert);
        return methodName;
    }

    @Override
    public String insert(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableInsert()) {
            return null;
        }
        String methodName = this.calcMethodName(mapper, "insert");
        MethodDeclaration insert = new MethodDeclaration();
        String lotNoText = getLotNoText(persistenceGeneratorConfig, persistence);
        Javadoc javadoc = new JavadocComment("插入" + lotNoText).parse();
        insert.setJavadocComment(javadoc);
        insert.setType(PrimitiveType.intType());
        insert.setName(methodName);
        insert.addParameter(persistence.getEntityName(), "entity");
        insert.setBody(null);
        mapper.getMembers().addLast(insert);
        return methodName;
    }

    @Override
    public String listAll(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableListAll()) {
            return null;
        }
        String methodName = null;
        if (persistence.getIdProperties().size() > 0) {
            methodName = this.calcMethodName(mapper, "listAll");
            MethodDeclaration listAll = new MethodDeclaration();
            String lotNoText = getLotNoText(persistenceGeneratorConfig, persistence);
            Javadoc javadoc = new JavadocComment("获取全部" + lotNoText).parse();
            listAll.setType(StaticJavaParser.parseType("List<" + persistence.getEntityName() + ">"));
            listAll.setName(methodName);
            listAll.setJavadocComment(javadoc);
            listAll.setBody(null);
            mapper.getMembers().addLast(listAll);
        }
        return methodName;
    }

    @Override
    public String queryByEntity(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableQueryByEntity()) {
            return null;
        }

        String methodName = this.calcMethodName(mapper, "queryByEntity");
        MethodDeclaration queryByEntity = new MethodDeclaration();
        String lotNoText = getLotNoText(persistenceGeneratorConfig, persistence);
        Javadoc javadoc = new JavadocComment("根据实体内的属性查询" + lotNoText).parse();
        Imports.ensureImported(mapper, "java.util.List");
        queryByEntity.setType(parseType("List<" + persistence.getEntityName() + ">"));
        queryByEntity.setName(methodName);
        queryByEntity.addParameter(persistence.getEntityName(), "entity");
        queryByEntity.setBody(null);
        queryByEntity.setJavadocComment(javadoc);
        mapper.getMembers().addLast(queryByEntity);
        return methodName;
    }

    @Override
    public String queryById(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableQueryById()) {
            return null;
        }
        String methodName = null;
        if (persistence.getIdProperties().size() > 0) {
            methodName = this.calcMethodName(mapper, "queryById");
            MethodDeclaration queryById = new MethodDeclaration();
            String lotNoText = getLotNoText(persistenceGeneratorConfig, persistence);
            Javadoc javadoc = new JavadocComment("根据ID查询" + lotNoText).parse();
            queryById.setType(new ClassOrInterfaceType().setName(persistence.getEntityName()));
            queryById.setName(methodName);

            if (persistence.getIdProperties().size() == 1) {
                PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getIdProperties());
                String varName = MoreStringUtils.lowerFirstLetter(onlyPk.getPropertyName());
                Parameter parameter = parseParameter(onlyPk.getJavaType().getSimpleName() + " " + varName);
                queryById.addParameter(parameter);
            } else {
                Imports.ensureImported(mapper, "org.apache.ibatis.annotations.Param");
                for (PropertyDto pk : persistence.getIdProperties()) {
                    String varName = MoreStringUtils.lowerFirstLetter(pk.getPropertyName());
                    Parameter parameter = parseParameter(
                            "@Param(\"" + varName + "\")" + pk.getJavaType().getSimpleName() + " " + varName);
                    queryById.addParameter(parameter);
                }
            }
            queryById.setJavadocComment(javadoc);
            queryById.setBody(null);
            mapper.getMembers().addLast(queryById);
        }
        return methodName;
    }

    @Override
    public String queryByIdsEachId(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableQueryByIdsEachId()) {
            return null;
        }
        String methodName = null;
        if (persistence.getIdProperties().size() == 1) {
            methodName = calcMethodName(mapper, "queryByIdsEachId");
            MethodDeclaration queryByIdsEachId = new MethodDeclaration();
            String lotNoText = getLotNoText(persistenceGeneratorConfig, persistence);
            Javadoc javadoc = new JavadocComment("根据多个ID查询，并以ID作为key映射到Map" + lotNoText).parse();
            Imports.ensureImported(mapper, "org.apache.ibatis.annotations.MapKey");
            Imports.ensureImported(mapper, "java.util.Map");
            Imports.ensureImported(mapper, "java.util.Collection");
            Imports.ensureImported(mapper, "org.apache.ibatis.annotations.Param");
            PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getIdProperties());
            String varName = MoreStringUtils.lowerFirstLetter(onlyPk.getPropertyName());
            String pkTypeName = onlyPk.getJavaType().getSimpleName();
            queryByIdsEachId.addAnnotation(parseAnnotation("@MapKey(\"" + varName + "\")"));
            queryByIdsEachId.setType(parseType("Map<" + pkTypeName + ", " + persistence.getEntityName() + ">"));
            queryByIdsEachId.setName(methodName);
            String varsName = English.plural(varName);
            queryByIdsEachId.addParameter(
                    parseParameter("@Param(\"" + varsName + "\") Collection<" + pkTypeName + "> " + varsName));
            queryByIdsEachId.setBody(null);
            queryByIdsEachId.setJavadocComment(javadoc);
            mapper.getMembers().addLast(queryByIdsEachId);
        }
        return methodName;
    }

    @Override
    public String queryByIds(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableQueryByIds()) {
            return null;
        }
        String methodName = null;
        if (persistence.getIdProperties().size() == 1) {
            methodName = calcMethodName(mapper, "queryByIds");
            MethodDeclaration queryByIds = new MethodDeclaration();
            String lotNoText = getLotNoText(persistenceGeneratorConfig, persistence);
            Javadoc javadoc = new JavadocComment("根据多个ID查询" + lotNoText).parse();
            Imports.ensureImported(mapper, "java.util.List");
            Imports.ensureImported(mapper, "java.util.Collection");
            Imports.ensureImported(mapper, "org.apache.ibatis.annotations.Param");
            PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getIdProperties());
            queryByIds.setType(parseType("List<" + persistence.getEntityName() + ">"));
            queryByIds.setName(methodName);
            String varsName = English.plural(MoreStringUtils.lowerFirstLetter(onlyPk.getPropertyName()));
            Parameter parameter = parseParameter(
                    "@Param(\"" + varsName + "\") Collection<" + onlyPk.getJavaType().getSimpleName() + "> "
                            + varsName);
            queryByIds.addParameter(parameter);
            queryByIds.setBody(null);
            queryByIds.setJavadocComment(javadoc);
            mapper.getMembers().addLast(queryByIds);
        }
        return methodName;
    }

    @Override
    public String queryByKey(PersistenceDto persistence, PropertyDto key, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableQueryByKey()) {
            return null;
        }
        String methodName = calcMethodName(mapper, "queryBy" + MoreStringUtils.upperFirstLetter(key.getPropertyName()));
        MethodDeclaration method = new MethodDeclaration();
        String lotNoText = getLotNoText(persistenceGeneratorConfig, persistence);
        Javadoc javadoc = new JavadocComment("根据「" + key.getDescription() + "」查询" + lotNoText).parse();

        Imports.ensureImported(mapper, "java.util.List");
        method.setType(parseType("List<" + persistence.getEntityName() + ">"));
        method.setName(methodName);
        String varName = MoreStringUtils.lowerFirstLetter(key.getPropertyName());
        Parameter parameter = parseParameter(key.getJavaType().getSimpleName() + " " + varName);
        method.addParameter(parameter);
        method.setBody(null);
        method.setJavadocComment(javadoc);
        mapper.getMembers().addLast(method);
        return methodName;
    }

    @Override
    public QueryByKeysDto queryByKeys(PersistenceDto persistence, PropertyDto key, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableQueryByKeys()) {
            return null;
        }
        String methodName = calcMethodName(mapper,
                "queryBy" + English.plural(MoreStringUtils.upperFirstLetter(key.getPropertyName())));
        MethodDeclaration method = new MethodDeclaration();
        String lotNoText = getLotNoText(persistenceGeneratorConfig, persistence);
        Javadoc javadoc = new JavadocComment("根据多个「" + key.getDescription() + "」查询" + lotNoText).parse();
        Imports.ensureImported(mapper, "java.util.List");
        method.setType(parseType("List<" + persistence.getEntityName() + ">"));
        method.setName(methodName);
        String typeName = "Collection<" + key.getJavaType().getSimpleName() + ">";
        String varsName = English.plural(MoreStringUtils.lowerFirstLetter(key.getPropertyName()));
        String paramAnno = "@Param(\"" + varsName + "\")";
        Parameter parameter = parseParameter(paramAnno + " " + typeName + " " + varsName);
        method.addParameter(parameter);
        method.setBody(null);
        method.setJavadocComment(javadoc);
        mapper.getMembers().addLast(method);
        return new QueryByKeysDto().setKey(key).setMethodName(methodName).setVarsName(varsName);
    }

    @Override
    public String updateByIdEvenNull(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableUpdateByIdEvenNull()) {
            return null;
        }
        String methodName = null;
        if (persistence.getIdProperties().size() > 0) {
            methodName = calcMethodName(mapper, "updateByIdEvenNull");
            MethodDeclaration updateByIdEvenNull = new MethodDeclaration();
            String lotNoText = getLotNoText(persistenceGeneratorConfig, persistence);
            Javadoc javadoc = new JavadocComment(
                    "根据ID更新数据，为null属性对应的字段会被更新为null" + lotNoText).parse();
            updateByIdEvenNull.setJavadocComment(javadoc);
            updateByIdEvenNull.setType(PrimitiveType.intType());
            updateByIdEvenNull.setName(methodName);
            updateByIdEvenNull.addParameter(persistence.getEntityName(), "entity");
            updateByIdEvenNull.setBody(null);
            mapper.getMembers().addLast(updateByIdEvenNull);
        }
        return methodName;
    }

    @Override
    public String updateById(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        if (persistenceGeneratorConfig.getDisableUpdateById()) {
            return null;
        }
        String methodName = null;
        if (persistence.getIdProperties().size() > 0) {
            methodName = calcMethodName(mapper, "updateById");
            MethodDeclaration updateById = new MethodDeclaration();
            String lotNoText = getLotNoText(persistenceGeneratorConfig, persistence);
            Javadoc javadoc = new JavadocComment("根据ID更新数据，忽略值为null的属性" + lotNoText).parse();
            updateById.setJavadocComment(javadoc);
            updateById.setType(PrimitiveType.intType());
            updateById.setName(methodName);
            updateById.addParameter(persistence.getEntityName(), "entity");
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
            if (descriptionLines.stream().anyMatch(line -> line.contains(LotNo.TAG_PREFIXION))) {
                method.remove();
            }
        }
        return mapper.getMethodsByName(methodName).size() > 0;
    }

    private String getLotNoText(PersistenceGeneratorConfig persistenceGeneratorConfig, PersistenceDto persistence) {
        String lotNoText = persistenceGeneratorConfig.getMapperInterfaceMethodPrintLotNo() ? persistence.getLotNo()
                .asJavadocDescription() : "\n\n<p>" + LotNo.NO_MANUAL_MODIFICATION;
        return lotNoText;
    }

}