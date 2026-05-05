package com.spldeolin.allison1875.persistencegenerator.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.dto.DeleteByIndexMethodDTO;
import com.spldeolin.allison1875.persistencegenerator.dto.QueryByIndexMethodDTO;
import com.spldeolin.allison1875.persistencegenerator.dto.ReplaceMapperXmlMethodsArgs;
import com.spldeolin.allison1875.persistencegenerator.dto.TableAnalysisDTO;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.PropertyDTO;
import com.spldeolin.allison1875.persistencegenerator.service.MapperXmlService;
import com.spldeolin.allison1875.persistencegenerator.util.TextUtils;

/**
 * @author Deolin 2023-12-24
 */
@Singleton
public class MapperXmlServiceImpl implements MapperXmlService {

    private static final String startMark = "[START]";

    private static final String endMark = "[END]";

    @Inject
    private Config config;

    /**
     * <sql id="all"></sql> 标签
     */
    @Override
    public List<String> generateAllCloumnSql(TableAnalysisDTO persistence) {
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add("<sql id=\"all\">");
        xmlLines.addAll(TextUtils.formatLines(BaseConstant.SINGLE_INDENT,
                persistence.getProperties().stream().map(PropertyDTO::getColumnName).collect(Collectors.toList()),
                120));
        xmlLines.add("</sql>");
        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public List<String> generateBatchInsertEvenNullMethod(TableAnalysisDTO persistence, String methodName) {
        if (methodName == null) {
            return Lists.newArrayList();
        }
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<insert id=\"%s\">", methodName));
        xmlLines.add(BaseConstant.SINGLE_INDENT + String.format("INSERT INTO %s", persistence.getTableName()));
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "( <include refid=\"all\"/> )");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "VALUES");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "<foreach collection=\"entities\" item=\"one\" separator=\",\">(");
        xmlLines.addAll(TextUtils.formatLines(BaseConstant.TREBLE_INDENT,
                persistence.getProperties().stream().map(p -> "#{one." + p.getPropertyName() + "}")
                        .collect(Collectors.toList()), 120));
        xmlLines.add(BaseConstant.DOUBLE_INDENT + ")</foreach>");
        xmlLines.add("</insert>");
        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public List<String> generateBatchInsertMethod(TableAnalysisDTO persistence, String methodName) {
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<insert id=\"%s\">", methodName));
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<foreach collection=\"entities\" item=\"one\" separator=\";\">");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "INSERT INTO " + persistence.getTableName());
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDTO property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.TREBLE_INDENT + String.format("<if test=\"one.%s!=null\"> %s, </if>",
                    property.getPropertyName(), property.getColumnName()));
        }
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "</trim>");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "<trim prefix=\"VALUE (\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDTO property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.TREBLE_INDENT + String.format("<if test=\"one.%s!=null\"> #{one.%s}, </if>",
                    property.getPropertyName(), property.getPropertyName()));
        }
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "</trim>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</foreach>;");
        xmlLines.add("</insert>");
        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public List<String> generateInsertMethod(TableAnalysisDTO persistence, String entityName, String methodName) {
        List<String> xmlLines = Lists.newArrayList();
        String generatedKeyPart = "";
        if (persistence.getIdProperties().size() == 1) {
            PropertyDTO onlyId = persistence.getIdProperties().get(0);
            if (onlyId.getIsAutoIncrement()) {
                generatedKeyPart = " useGeneratedKeys=\"true\" keyProperty=\"" + onlyId.getPropertyName() + "\"";
            }
        }
        xmlLines.add(
                String.format("<insert id=\"%s\" parameterType=\"%s\"%s>", methodName, entityName, generatedKeyPart));
        xmlLines.add(BaseConstant.SINGLE_INDENT + "INSERT INTO " + persistence.getTableName());
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDTO property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + String.format("<if test=\"%s!=null\"> %s, </if>",
                    property.getPropertyName(), property.getColumnName()));
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</trim>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<trim prefix=\"VALUES (\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDTO property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + String.format("<if test=\"%s!=null\"> #{%s}, </if>",
                    property.getPropertyName(), property.getPropertyName()));
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</trim>");
        xmlLines.add("</insert>");

        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public List<String> generateListAllMethod(TableAnalysisDTO persistence, String methodName) {
        if (methodName == null) {
            return null;
        }
        List<String> result = Lists.newArrayList();
        String firstLine = "<select id=\"" + methodName + "\" ";
        firstLine += "resultMap=\"all\">";
        result.add(firstLine);
        result.add(BaseConstant.SINGLE_INDENT + "SELECT <include refid=\"all\"/>");
        result.add(BaseConstant.SINGLE_INDENT + "FROM " + persistence.getTableName());
        if (persistence.getIsDeleteFlagExist()) {
            result.add(config.getNotDeletedSql());
        }
        result.add("</select>");
        result.add("");
        return result;
    }

    /**
     * 这个Proc生成2中方法：
     * 1. 根据主键列表查询
     * 2. 根据主键列表查询，并把结果集以主键为key，映射到Map中
     */
    @Override
    public List<String> generateQueryByIdsMethod(TableAnalysisDTO persistence, String methodName) {
        if (methodName == null) {
            return null;
        }
        List<String> xmlLines = Lists.newArrayList();
        if (persistence.getIdProperties().size() == 1) {
            PropertyDTO onlyPk = Iterables.getOnlyElement(persistence.getIdProperties());
            xmlLines.add(String.format("<select id=\"%s\" parameterType=\"%s\" resultMap=\"all\">", methodName,
                    onlyPk.getJavaType().getQualifier().replaceFirst("java\\.lang\\.", "")));
            xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT <include refid=\"all\"/>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM " + persistence.getTableName());
            xmlLines.add(BaseConstant.SINGLE_INDENT + "<where>");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.DOUBLE_INDENT + "AND " + config.getNotDeletedSql());
            }
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "AND " + onlyPk.getColumnName() + String.format(
                    " IN (<foreach collection=\"%s\" item=\"one\" separator=\",\">#{one}</foreach>)",
                    English.plural(MoreStringUtils.toLowerCamel(onlyPk.getPropertyName()))));
            xmlLines.add(BaseConstant.SINGLE_INDENT + "</where>");
            xmlLines.add("</select>");
            xmlLines.add("");
        }
        return xmlLines;
    }

    @Override
    public List<String> generateQueryByIdMethod(TableAnalysisDTO persistence, String methodName) {
        List<String> xmlLines = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(persistence.getIdProperties())) {
            String firstLine = "<select id=\"" + methodName + "\" ";
            if (persistence.getIdProperties().size() == 1) {
                firstLine += "parameterType=\"" + Iterables.getOnlyElement(persistence.getIdProperties()).getJavaType()
                        .getQualifier().replaceFirst("java\\.lang\\.", "") + "\" ";
            }
            firstLine += "resultMap=\"all\">";
            xmlLines.add(firstLine);
            xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT <include refid=\"all\"/>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM " + persistence.getTableName());
            xmlLines.add(BaseConstant.SINGLE_INDENT + "<where>");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.DOUBLE_INDENT + "AND " + config.getNotDeletedSql());
            }
            for (PropertyDTO idProperty : persistence.getIdProperties()) {
                xmlLines.add(BaseConstant.DOUBLE_INDENT + "AND " + idProperty.getColumnName() + " = #{"
                        + idProperty.getPropertyName() + "}");
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "</where>");
            xmlLines.add("</select>");
            xmlLines.add("");
        }
        return xmlLines;
    }

    @Override
    public List<String> generateResultMap(TableAnalysisDTO persistence, String entityName) {
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<resultMap id=\"all\" type=\"%s\">", entityName));
        for (PropertyDTO id : persistence.getIdProperties()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + String.format("<id column=\"%s\" property=\"%s\"/>",
                    id.getColumnName(), id.getPropertyName()));
        }
        for (PropertyDTO nonId : persistence.getNonIdProperties()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + String.format("<result column=\"%s\" property=\"%s\"/>",
                    nonId.getColumnName(), nonId.getPropertyName()));
        }
        xmlLines.add("</resultMap>");
        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public List<String> generateUpdateByIdEvenNullMethod(TableAnalysisDTO persistence, String entityName,
            String methodName) {
        if (methodName == null) {
            return Lists.newArrayList();
        }
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<update id=\"%s\" parameterType=\"%s\">", methodName, entityName));
        xmlLines.add(BaseConstant.SINGLE_INDENT + "UPDATE " + persistence.getTableName());
        xmlLines.add(BaseConstant.SINGLE_INDENT + "SET");
        for (PropertyDTO nonId : persistence.getNonIdProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + nonId.getColumnName() + " = #{" + nonId.getPropertyName() + "},");
        }
        // 删除最后一个语句中，最后的逗号
        if (CollectionUtils.isNotEmpty(xmlLines)) {
            int last = xmlLines.size() - 1;
            xmlLines.set(last, MoreStringUtils.replaceLast(xmlLines.get(last), ",", ""));
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<where>");
        if (persistence.getIsDeleteFlagExist()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "AND " + config.getNotDeletedSql());
        }
        for (PropertyDTO idProperty : persistence.getIdProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "AND " + idProperty.getColumnName() + " = #{"
                    + idProperty.getPropertyName() + "}");
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</where>");
        xmlLines.add("</update>");
        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public List<String> generateUpdateByIdMethod(TableAnalysisDTO persistence, String entityName, String methodName) {
        List<String> xmlLines = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(persistence.getIdProperties())) {
            xmlLines.add(String.format("<update id=\"%s\" parameterType=\"%s\">", methodName, entityName));
            xmlLines.add(BaseConstant.SINGLE_INDENT + "UPDATE " + persistence.getTableName());
            xmlLines.add(BaseConstant.SINGLE_INDENT + "<set>");
            for (PropertyDTO nonId : persistence.getNonIdProperties()) {
                xmlLines.add(BaseConstant.DOUBLE_INDENT + String.format("<if test=\"%s!=null\"> %s = #{%s}, </if>",
                        nonId.getPropertyName(), nonId.getColumnName(), nonId.getPropertyName()));
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "</set>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "<where>");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.DOUBLE_INDENT + "AND " + config.getNotDeletedSql());
            }
            for (PropertyDTO id : persistence.getIdProperties()) {
                xmlLines.add(BaseConstant.DOUBLE_INDENT + String.format("AND %s = #{%s}", id.getColumnName(),
                        id.getPropertyName()));
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "</where>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "</update>");
            xmlLines.add("");
        }
        return xmlLines;
    }

    @Override
    public void replaceMapperXmlMethods(ReplaceMapperXmlMethodsArgs args) {
        try {
            // find
            File mapperXmlFile = args.getMapperXmlDirectory().resolve(args.getTableAnalysis().getMapperName() + ".xml")
                    .toFile();

            if (!mapperXmlFile.exists()) {
                // create new File
                List<String> sourceCodeLines = Lists.newArrayList();
                sourceCodeLines.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                sourceCodeLines.add("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis"
                        + ".org/dtd/mybatis-3-mapper.dtd\">");
                sourceCodeLines.add(String.format("<mapper namespace=\"%s\">", args.getMapper().getFullyQualifiedName()
                        .orElseThrow(() -> new Allison1875Exception(
                                "Node '" + args.getMapper().getName() + "' has no Qualifier"))));
                sourceCodeLines.add("</mapper>");
                FileUtils.writeLines(mapperXmlFile, StandardCharsets.UTF_8.name(), sourceCodeLines);
            }

            List<String> newLines = Lists.newArrayList();

            String content = FileUtils.readFileToString(mapperXmlFile, StandardCharsets.UTF_8);
            List<String> lines = MoreStringUtils.splitLineByLine(content);
            List<String> generatedLines = getGeneratedLines(args.getSourceCodes(), args.getTableAnalysis());

            if (StringUtils.containsAny(content, startMark, endMark)) {
                boolean inAnchorRange = false;
                for (String line : lines) {
                    if (!inAnchorRange) {
                        if (StringUtils.containsAny(line, startMark, endMark)) {
                            // 从 范围外 进入
                            inAnchorRange = true;
                        } else {
                            newLines.add(line);
                        }
                    } else {
                        if (StringUtils.containsAny(line, startMark, endMark)) {
                            // 从 范围内 离开
                            inAnchorRange = false;
                            newLines.addAll(generatedLines);
                        }
                    }
                }
            } else {
                Collections.reverse(lines);
                for (String line : lines) {
                    newLines.add(line);
                    if (line.contains("</mapper>")) {
                        Collections.reverse(generatedLines);
                        newLines.addAll(generatedLines);
                    }
                }
                Collections.reverse(newLines);
            }

            // writeXml
            try {
                FileUtils.writeStringToFile(mapperXmlFile, Joiner.on(BaseConstant.NEW_LINE).join(newLines),
                        StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<String> generateQueryByIndexMethod(TableAnalysisDTO tableAnalysis,
            List<QueryByIndexMethodDTO> queryByIndexMethods) {
        List<String> xmlLines = Lists.newArrayList();
        for (QueryByIndexMethodDTO queryByIndexMethod : queryByIndexMethods) {
            if (queryByIndexMethod.getMethodName() == null) {
                continue;
            }
            xmlLines.add(String.format("<select id=\"%s\" resultMap=\"all\">", queryByIndexMethod.getMethodName()));
            xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT <include refid=\"all\"/>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM " + tableAnalysis.getTableName());
            xmlLines.add(BaseConstant.SINGLE_INDENT + "<where>");
            if (tableAnalysis.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.DOUBLE_INDENT + "AND " + config.getNotDeletedSql());
            }
            for (PropertyDTO indexProperty : queryByIndexMethod.getIndexProperties()) {
                xmlLines.add(BaseConstant.DOUBLE_INDENT + "AND " + indexProperty.getColumnName() + " = #{"
                        + indexProperty.getPropertyName() + "}");
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "</where>");
            xmlLines.add("</select>");
            xmlLines.add("");
        }
        return xmlLines;
    }

    @Override
    public List<String> generateDeleteByIdMethod(TableAnalysisDTO persistence, String methodName) {
        List<String> xmlLines = Lists.newArrayList();

        if (!persistence.getIsDeleteFlagExist()) {
            String firstLine = "<delete id=\"" + methodName + "\" ";
            if (persistence.getIdProperties().size() == 1) {
                firstLine += "parameterType=\"" + Iterables.getOnlyElement(persistence.getIdProperties()).getJavaType()
                        .getQualifier().replaceFirst("java\\.lang\\.", "") + "\">";
            }
            xmlLines.add(firstLine);
            xmlLines.add(BaseConstant.SINGLE_INDENT + "DELETE FROM " + persistence.getTableName());
            xmlLines.add(BaseConstant.SINGLE_INDENT + "<where>");
            for (PropertyDTO idProperty : persistence.getIdProperties()) {
                xmlLines.add(BaseConstant.DOUBLE_INDENT + "AND " + idProperty.getColumnName() + " = #{"
                        + idProperty.getPropertyName() + "}");
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "</where>");
            xmlLines.add("</delete>");
        } else {
            String firstLine = "<update id=\"" + methodName + "\" ";
            if (persistence.getIdProperties().size() == 1) {
                firstLine += "parameterType=\"" + Iterables.getOnlyElement(persistence.getIdProperties()).getJavaType()
                        .getQualifier().replaceFirst("java\\.lang\\.", "") + "\">";
            }
            xmlLines.add(firstLine);
            xmlLines.add(BaseConstant.SINGLE_INDENT + "UPDATE " + persistence.getTableName());
            xmlLines.add(BaseConstant.SINGLE_INDENT + "SET " + config.getDeletedSql());
            xmlLines.add(BaseConstant.SINGLE_INDENT + "<where>");
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "AND " + config.getNotDeletedSql());
            for (PropertyDTO idProperty : persistence.getIdProperties()) {
                xmlLines.add(BaseConstant.DOUBLE_INDENT + "AND " + idProperty.getColumnName() + " = #{"
                        + idProperty.getPropertyName() + "}");
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "</where>");
            xmlLines.add("</update>");
        }
        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public List<String> generateDeleteByIndexMethod(TableAnalysisDTO tableAnalysis,
            List<DeleteByIndexMethodDTO> deleteByIndexMethodNames) {
        List<String> xmlLines = Lists.newArrayList();
        for (DeleteByIndexMethodDTO deleteByIndexMethod : deleteByIndexMethodNames) {
            if (deleteByIndexMethod.getMethodName() == null) {
                continue;
            }
            if (!tableAnalysis.getIsDeleteFlagExist()) {
                xmlLines.add(String.format("<delete id=\"%s\">", deleteByIndexMethod.getMethodName()));
                xmlLines.add(BaseConstant.SINGLE_INDENT + "DELETE FROM " + tableAnalysis.getTableName());
                xmlLines.add(BaseConstant.SINGLE_INDENT + "<where>");
                for (PropertyDTO indexProperty : deleteByIndexMethod.getIndexProperties()) {
                    xmlLines.add(BaseConstant.DOUBLE_INDENT + "AND " + indexProperty.getColumnName() + " = #{"
                            + indexProperty.getPropertyName() + "}");
                }
                xmlLines.add(BaseConstant.SINGLE_INDENT + "</where>");
                xmlLines.add("</delete>");
            } else {
                xmlLines.add(String.format("<update id=\"%s\">", deleteByIndexMethod.getMethodName()));
                xmlLines.add(BaseConstant.SINGLE_INDENT + "UPDATE " + tableAnalysis.getTableName());
                xmlLines.add(BaseConstant.SINGLE_INDENT + "SET " + config.getDeletedSql());
                xmlLines.add(BaseConstant.SINGLE_INDENT + "<where>");
                xmlLines.add(BaseConstant.DOUBLE_INDENT + "AND " + config.getNotDeletedSql());
                for (PropertyDTO indexProperty : deleteByIndexMethod.getIndexProperties()) {
                    xmlLines.add(BaseConstant.DOUBLE_INDENT + "AND " + indexProperty.getColumnName() + " = #{"
                            + indexProperty.getPropertyName() + "}");
                }
                xmlLines.add(BaseConstant.SINGLE_INDENT + "</where>");
                xmlLines.add("</update>");
            }
            xmlLines.add("");
        }
        return xmlLines;
    }

    @Override
    public List<String> generateQueryByBizIdsMethod(TableAnalysisDTO persistence,
            List<QueryByIndexMethodDTO> queryByBizIdsMethodNames) {
        List<String> xmlLines = Lists.newArrayList();
        for (QueryByIndexMethodDTO method : queryByBizIdsMethodNames) {
            if (persistence.getIdProperties().size() == 1) {
                PropertyDTO bizId = Iterables.getOnlyElement(method.getIndexProperties());
                xmlLines.add(String.format("<select id=\"%s\" parameterType=\"%s\" resultMap=\"all\">",
                        method.getMethodName(), bizId.getJavaType().getQualifier().replaceFirst("java\\.lang\\.", "")));
                xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT <include refid=\"all\"/>");
                xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM " + persistence.getTableName());
                xmlLines.add(BaseConstant.SINGLE_INDENT + "<where>");
                if (persistence.getIsDeleteFlagExist()) {
                    xmlLines.add(BaseConstant.DOUBLE_INDENT + "AND " + config.getNotDeletedSql());
                }
                xmlLines.add(BaseConstant.DOUBLE_INDENT + "AND " + bizId.getColumnName() + String.format(
                        " IN (<foreach collection=\"%s\" item=\"one\" separator=\",\">#{one}</foreach>)",
                        English.plural(MoreStringUtils.toLowerCamel(bizId.getPropertyName()))));
                xmlLines.add(BaseConstant.SINGLE_INDENT + "</where>");
                xmlLines.add("</select>");
                xmlLines.add("");
            }
        }
        return xmlLines;
    }

    private String concatXmlComment(TableAnalysisDTO persistence) {
        String result = "<!--";
        if (config.getEnableNoModifyAnnounce()) {
            result += " " + BaseConstant.NO_MODIFY_ANNOUNCE;
        }
        result += " -->";
        return result;
    }

    private List<String> getGeneratedLines(List<List<String>> sourceCodes, TableAnalysisDTO persistence) {
        List<String> auto = Lists.newArrayList();
        auto.add(BaseConstant.SINGLE_INDENT + concatXmlComment(persistence).replace("<!--", "<!-- " + startMark));
        auto.add("");
        for (List<String> sourceCode : sourceCodes) {
            if (CollectionUtils.isNotEmpty(sourceCode)) {
                for (String line : sourceCode) {
                    if (StringUtils.isNotBlank(line)) {
                        auto.add(BaseConstant.SINGLE_INDENT + line);
                    } else {
                        auto.add("");
                    }
                }
            }
        }
        if (StringUtils.isEmpty(auto.get(auto.size() - 1))) {
            auto.remove(auto.size() - 1);
        }
        auto.add("");
        auto.add(BaseConstant.SINGLE_INDENT + concatXmlComment(persistence).replace("<!--", "<!-- " + endMark));
        return auto;
    }

}
