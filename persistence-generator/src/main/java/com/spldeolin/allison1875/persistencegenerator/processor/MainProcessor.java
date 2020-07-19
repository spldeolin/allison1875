package com.spldeolin.allison1875.persistencegenerator.processor;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.InsertProcessor;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByIdProcessor;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByIdsEachIdProcessor;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByIdsProcessor;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.UpdateByIdEvenNullProcessor;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.UpdateByIdProcessor;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-11
 */
@Log4j2
public class MainProcessor {

    private static final String singleIndent = "    ";

    private static final String doubleIndex = Strings.repeat(singleIndent, 2);

    private static final String trebleIndex = Strings.repeat(singleIndent, 3);

    private static final String newLine = "\r\n";

    public static void main(String[] args) {
        Collection<CompilationUnit> toSave = Lists.newArrayList();

        // 构建并遍历 PersistenceDto对象
        for (PersistenceDto persistence : new BuildPersistenceDtoProcessor().process().getPersistences()) {

            // 重新生成Entity
            EntityProcessor entityProcessor = new EntityProcessor(persistence).process();
            CuCreator entityCuCreator = entityProcessor.getEntityCuCreator();
            toSave.add(entityCuCreator.create(false));

            // 寻找或创建Mapper
            ClassOrInterfaceDeclaration mapper;
            try {
                FindOrCreateMapperProcessor findOrCreateMapper = new FindOrCreateMapperProcessor(persistence,
                        entityCuCreator).process();
                mapper = findOrCreateMapper.getMapper();
                toSave.add(findOrCreateMapper.getCu());
            } catch (Exception e) {
                log.error("寻找或创建Mapper时发生异常 persistence={}", persistence, e);
                continue;
            }

            // 在Mapper中生成基础方法
            new InsertProcessor(persistence, mapper).process();
            new QueryByIdsEachIdProcessor(persistence, mapper).process();
            new UpdateByIdProcessor(persistence, mapper).process();
            new UpdateByIdEvenNullProcessor(persistence, mapper).process();
            new QueryByIdProcessor(persistence, mapper).process();
            new QueryByIdsProcessor(persistence, mapper).process();
            new QueryByIdsEachIdProcessor(persistence, mapper).process();

            // 在Mapper.xml中生成基础方法

            File mapperXmlFile = Paths.get(PersistenceGeneratorConfig.getInstace().getMapperXmlPath(),
                    persistence.getMapperName() + ".xml").toFile();
            Element root = findDom4jRootOrElseCreate(mapperXmlFile);
            if (root == null) {
                continue;
            }
            overwriteNamespace(mapper, root);

            // 删除可能存在的resultMap(id=all)标签，并重新生成
            root.addText(newLine);
            Element resultMapTag = Dom4jUtils.findAndRebuildElement(root, "resultMap", "id", "all");
            resultMapTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
            resultMapTag.addAttribute("type", getEntityNameInXml(entityCuCreator));
            for (PropertyDto pk : persistence.getPkProperties()) {
                Element resultTag = resultMapTag.addElement("id");
                resultTag.addAttribute("column", pk.getColumnName());
                resultTag.addAttribute("property", pk.getPropertyName());
            }
            for (PropertyDto nonPk : persistence.getNonPkProperties()) {
                Element resultTag = resultMapTag.addElement("result");
                resultTag.addAttribute("column", nonPk.getColumnName());
                resultTag.addAttribute("property", nonPk.getPropertyName());
            }

            // 删除可能存在的sql(id=all)标签，并重新生成
            root.addText(newLine);
            Element sqlTag = Dom4jUtils.findAndRebuildElement(root, "sql", "id", "all");
            sqlTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
            sqlTag.addText(newLine + doubleIndex + persistence.getProperties().stream().map(PropertyDto::getColumnName)
                    .collect(Collectors.joining(",")));

            // Mapper.xml#insert
            root.addText(newLine);
            Element insertTag = Dom4jUtils.findAndRebuildElement(root, "insert", "id", "insert");
            insertTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
            insertTag.addAttribute("parameterType", getEntityNameInXml(entityCuCreator));
            if (persistence.getPkProperties().size() > 0) {
                insertTag.addAttribute("useGeneratedKeys", "true");
                String keyProperty = persistence.getPkProperties().stream().map(PropertyDto::getColumnName)
                        .collect(Collectors.joining(","));
                insertTag.addAttribute("keyProperty", keyProperty);
            }
            final StringBuilder sql = new StringBuilder(64);
            sql.append(newLine).append(doubleIndex);
            sql.append("INSERT INTO ").append(persistence.getTableName()).append(" (");
            insertTag.addText(sql.toString());
            insertTag.addElement("include").addAttribute("refid", "all");
            sql.setLength(0);
            sql.append(") VALUES (");
            for (PropertyDto property : persistence.getProperties()) {
                sql.append("#{").append(property.getPropertyName()).append("},");
            }
            sql.deleteCharAt(sql.lastIndexOf(",")).append(")");
            insertTag.addText(sql.toString());
            sql.setLength(0);

            // Mapper.xml#updateById
            if (persistence.getPkProperties().size() > 0) {
                root.addText(newLine);
                Element updateByIdTag = Dom4jUtils.findAndRebuildElement(root, "update", "id", "updateById");
                updateByIdTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
                updateByIdTag.addAttribute("parameterType", getEntityNameInXml(entityCuCreator));
                updateByIdTag.addText(newLine + doubleIndex + "UPDATE " + persistence.getTableName());

                Element setTag = updateByIdTag.addElement("set");
                for (PropertyDto nonPk : persistence.getNonPkProperties()) {
                    Element ifTag = setTag.addElement("if");
                    String ifTest = nonPk.getPropertyName() + "!=null";
                    if (String.class == nonPk.getJavaType()) {
                        ifTest += " and " + nonPk.getPropertyName() + "!=''";
                    }
                    ifTag.addAttribute("test", ifTest);
                    ifTag.addText(newLine + Strings.repeat(singleIndent, 4) + nonPk.getColumnName() + "=#{" + nonPk
                            .getPropertyName() + "},\r\n" + trebleIndex);
                }

                sql.append(" WHERE ");
                for (PropertyDto pk : persistence.getPkProperties()) {
                    sql.append(pk.getColumnName()).append("=#{").append(pk.getPropertyName()).append("} AND ");
                }
                String text = StringUtils.removeLast(sql.toString(), " AND ");
                sql.setLength(0);
                updateByIdTag.addText(text);
            }

            // Mapper.xml#updateByIdEvenNull
            if (persistence.getPkProperties().size() > 0) {
                root.addText(newLine);
                Element updateByIdEvenNullTag = Dom4jUtils
                        .findAndRebuildElement(root, "update", "id", "updateByIdEvenNull");
                updateByIdEvenNullTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
                updateByIdEvenNullTag.addAttribute("parameterType", getEntityNameInXml(entityCuCreator));
                sql.append(newLine).append(doubleIndex).append("UPDATE ").append(persistence.getTableName());
                sql.append(newLine).append(doubleIndex).append("SET ");
                for (PropertyDto nonPk : persistence.getNonPkProperties()) {
                    sql.append(nonPk.getColumnName()).append("=#{").append(nonPk.getPropertyName()).append("},");
                }
                sql.deleteCharAt(sql.lastIndexOf(","));
                sql.append(newLine).append(doubleIndex).append("WHERE ");
                for (PropertyDto pk : persistence.getPkProperties()) {
                    sql.append(pk.getColumnName()).append("=#{").append(pk.getPropertyName()).append("} AND ");
                }
                String text = StringUtils.removeLast(sql, " AND ");
                sql.setLength(0);
                updateByIdEvenNullTag.addText(text);
            }

            // Mapper.xml#queryById
            if (persistence.getPkProperties().size() > 0) {
                root.addText(newLine);
                Element queryByIdTag = Dom4jUtils.findAndRebuildElement(root, "select", "id", "queryById");
                queryByIdTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
                queryByIdTag.addAttribute("resultMap", "all");
                queryByIdTag.addText(newLine + doubleIndex + "SELECT");
                queryByIdTag.addElement("include").addAttribute("refid", "all");
                sql.append(newLine).append(doubleIndex).append("FROM ").append(persistence.getTableName());
                sql.append(newLine).append(doubleIndex).append("WHERE ");
                for (PropertyDto pk : persistence.getPkProperties()) {
                    sql.append(pk.getColumnName()).append("=#{").append(pk.getPropertyName()).append("},");
                }
                String text = StringUtils.removeLast(sql, ",");
                sql.setLength(0);
                queryByIdTag.addText(text);
            }

            // Mapper.xml#queryByIds
            if (persistence.getPkProperties().size() == 1) {
                queryByIdsXml(persistence, root, sql, "queryByIds");
            }

            // Mapper.xml#queryByIdsEachId
            if (persistence.getPkProperties().size() == 1) {
                queryByIdsXml(persistence, root, sql, "queryByIdsEachId");
            }

            Dom4jUtils.write(mapperXmlFile, root);
        }

        toSave.forEach(Saves::prettySave);
    }

    private static void queryByIdsXml(PersistenceDto persistence, Element root, StringBuilder sql, String queryByIds2) {
        root.addText(newLine);
        Element queryByIdsTag = Dom4jUtils.findAndRebuildElement(root, "select", "id", queryByIds2);
        queryByIdsTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
        queryByIdsTag.addAttribute("resultMap", "all");
        queryByIdsTag.addText(newLine + doubleIndex + "SELECT");
        queryByIdsTag.addElement("include").addAttribute("refid", "all");
        sql.append(newLine).append(doubleIndex).append("FROM ").append(persistence.getTableName());
        sql.append(newLine).append(doubleIndex).append("WHERE ");
        PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getPkProperties());
        sql.append(onlyPk.getColumnName()).append(" IN (");
        queryByIdsTag.addText(sql.toString());
        sql.setLength(0);
        Element foreachTag = queryByIdsTag.addElement("foreach");
        foreachTag.addAttribute("collection", "ids");
        foreachTag.addAttribute("item", "id");
        foreachTag.addAttribute("separator", ",");
        foreachTag.addText("#{id}");
        queryByIdsTag.addText(")");
    }

    private static String getEntityNameInXml(CuCreator entityCuCreator) {
        if (PersistenceGeneratorConfig.getInstace().getIsEntityUsingAlias()) {
            return entityCuCreator.getPrimaryTypeName();
        } else {
            return entityCuCreator.getPrimaryTypeQualifier();
        }
    }

    private static void overwriteNamespace(ClassOrInterfaceDeclaration mapper, Element root) {
        root.addAttribute("namespace", mapper.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new));
    }

    private static Element findDom4jRootOrElseCreate(File mapperXmlFile) {
        Document document;
        if (mapperXmlFile.exists()) {
            try {
                document = new SAXReader().read(mapperXmlFile);
                Element rootElement = document.getRootElement();
                if (rootElement == null) {
                    rootElement = document.addElement("mapper");
                }
                return rootElement;
            } catch (DocumentException e) {
                log.error("xml parse failed, mapperXmlFile={}", mapperXmlFile, e);
                return null;
            }
        } else {
            document = DocumentHelper.createDocument();
            document.addDocType("mapper",
                    "-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd", null);
            return document.addElement("mapper");
        }
    }

}