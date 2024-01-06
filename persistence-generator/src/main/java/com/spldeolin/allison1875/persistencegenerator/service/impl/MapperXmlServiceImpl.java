package com.spldeolin.allison1875.persistencegenerator.service.impl;

import static com.spldeolin.allison1875.common.constant.BaseConstant.SINGLE_INDENT;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.atteo.evo.inflector.English;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.KeyMethodNameDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.QueryByKeysDto;
import com.spldeolin.allison1875.persistencegenerator.service.MapperXmlService;
import com.spldeolin.allison1875.persistencegenerator.util.TextUtils;

/**
 * @author Deolin 2023-12-24
 */
@Singleton
public class MapperXmlServiceImpl implements MapperXmlService {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    /**
     * <sql id="all"></sql> 标签
     */
    @Override
    public Collection<String> allCloumnSqlXml(PersistenceDto persistence) {
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add("<sql id=\"all\">");
        xmlLines.addAll(TextUtils.formatLines(BaseConstant.SINGLE_INDENT,
                persistence.getProperties().stream().map(one -> "`" + one.getColumnName() + "`")
                        .collect(Collectors.toList()), 120));
        xmlLines.add("</sql>");
        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public Collection<String> batchInsertEvenNullXml(PersistenceDto persistence, String methodName) {
        if (persistenceGeneratorConfig.getDisableBatchInsertEvenNull()) {
            return null;
        }
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<insert id=\"%s\">", methodName));
        xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        xmlLines.add(BaseConstant.SINGLE_INDENT + String.format("INSERT INTO `%s`", persistence.getTableName()));
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "( <include refid=\"all\"/> )");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "VALUES");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "<foreach collection=\"entities\" item=\"one\" separator=\",\">(");
        xmlLines.addAll(TextUtils.formatLines(BaseConstant.TREBLE_INDENT,
                persistence.getProperties().stream().map(p -> "#{one." + p.getPropertyName() + "}")
                        .collect(Collectors.toList()), 120));
        xmlLines.add(BaseConstant.DOUBLE_INDENT + ")</foreach>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        xmlLines.add("</insert>");
        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public Collection<String> batchInsertXml(PersistenceDto persistence, String methodName) {
        if (persistenceGeneratorConfig.getDisableBatchInsert()) {
            return null;
        }
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<insert id=\"%s\">", methodName));
        xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<foreach collection=\"entities\" item=\"one\" separator=\";\">");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "INSERT INTO `" + persistence.getTableName() + "`");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDto property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.TREBLE_INDENT + String.format("<if test=\"one.%s!=null\"> `%s`, </if>",
                    property.getPropertyName(), property.getColumnName()));
        }
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "</trim>");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "<trim prefix=\"VALUE (\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDto property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.TREBLE_INDENT + String.format("<if test=\"one.%s!=null\"> #{one.%s}, </if>",
                    property.getPropertyName(), property.getPropertyName()));
        }
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "</trim>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</foreach>;");
        xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        xmlLines.add("</insert>");
        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public Collection<String> batchUpdateEvenNullXml(PersistenceDto persistence, String methodName) {
        if (persistenceGeneratorConfig.getDisableBatchUpdateEvenNull()) {
            return null;
        }
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<update id=\"%s\">", methodName));
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<foreach collection=\"entities\" item=\"one\" separator=\";\">");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "UPDATE `" + persistence.getTableName() + "`");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "SET");
        for (PropertyDto nonId : persistence.getNonIdProperties()) {
            xmlLines.add(
                    BaseConstant.TREBLE_INDENT + "`" + nonId.getColumnName() + "` = #{one." + nonId.getPropertyName()
                            + "},");
        }
        // 删除最后一个语句中，最后的逗号
        if (xmlLines.size() > 0) {
            int last = xmlLines.size() - 1;
            xmlLines.set(last, MoreStringUtils.replaceLast(xmlLines.get(last), ",", ""));
        }
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "WHERE TRUE");
        if (persistence.getIsDeleteFlagExist()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "  AND " + persistenceGeneratorConfig.getNotDeletedSql());
        }
        for (PropertyDto idProperty : persistence.getIdProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "  AND `" + idProperty.getColumnName() + "` = #{one."
                    + idProperty.getPropertyName() + "}");
        }
        xmlLines.add(BaseConstant.DOUBLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</foreach>");
        xmlLines.add("</update>");
        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public Collection<String> batchUpdateXml(PersistenceDto persistence, String methodName) {
        if (persistenceGeneratorConfig.getDisableBatchUpdate()) {
            return null;
        }
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<update id=\"%s\">", methodName));
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<foreach collection=\"entities\" item=\"one\" separator=\";\">");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "UPDATE `" + persistence.getTableName() + "`");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "<set>");
        for (PropertyDto nonId : persistence.getNonIdProperties()) {
            xmlLines.add(
                    BaseConstant.TREBLE_INDENT + String.format("<if test=\"one.%s!=null\"> `%s` = #{one.%s}, </if>",
                            nonId.getPropertyName(), nonId.getColumnName(), nonId.getPropertyName()));
        }
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "</set>");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "WHERE TRUE");
        if (persistence.getIsDeleteFlagExist()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "  AND " + persistenceGeneratorConfig.getNotDeletedSql());
        }
        for (PropertyDto idProperty : persistence.getIdProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "  AND `" + idProperty.getColumnName() + "` = #{one."
                    + idProperty.getPropertyName() + "}");
        }
        xmlLines.add(BaseConstant.DOUBLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</foreach>");
        xmlLines.add("</update>");
        xmlLines.add("");
        return xmlLines;
    }

    /**
     * 根据外键删除
     *
     * 表中每有几个外键，这个Proc就生成几个方法，以_id结尾的字段算作外键
     */
    @Override
    public Collection<String> deleteByKeyXml(PersistenceDto persistence,
            Collection<KeyMethodNameDto> KeyAndMethodNames) {
        if (persistenceGeneratorConfig.getDisableDeleteByKey() || !persistence.getIsDeleteFlagExist()) {
            return Lists.newArrayList();
        }
        Collection<String> result = Lists.newArrayList();
        for (KeyMethodNameDto KeyAndMethodName : KeyAndMethodNames) {
            List<String> xmlLines = Lists.newArrayList();
            PropertyDto key = KeyAndMethodName.getKey();
            xmlLines.add(String.format("<update id=\"%s\" parameterType=\"%s\">", KeyAndMethodName.getMethodName(),
                    key.getJavaType().getQualifier().replaceFirst("java\\.lang\\.", "")));
            xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
            xmlLines.add(SINGLE_INDENT + "UPDATE `" + persistence.getTableName() + "`");
            xmlLines.add(SINGLE_INDENT + "SET " + persistenceGeneratorConfig.getDeletedSql());
            xmlLines.add(SINGLE_INDENT + "WHERE `" + key.getColumnName() + "` = #{" + key.getPropertyName() + "}");
            xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
            xmlLines.add("</update>");
            result.addAll(xmlLines);
            result.add("");
        }
        return result;
    }

    @Override
    public Collection<String> insertOrUpdateXml(PersistenceDto persistence, String entityName, String methodName) {
        if (persistenceGeneratorConfig.getDisableInsertOrUpdate()) {
            return null;
        }
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<insert id=\"%s\" parameterType=\"%s\">", methodName, entityName));
        xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        xmlLines.add(BaseConstant.SINGLE_INDENT + "INSERT INTO `" + persistence.getTableName() + "`");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDto property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + String.format("<if test=\"%s!=null\"> `%s`, </if>",
                    property.getPropertyName(), property.getColumnName()));
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</trim>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<trim prefix=\"VALUES (\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDto property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + String.format("<if test=\"%s!=null\"> #{%s}, </if>",
                    property.getPropertyName(), property.getPropertyName()));
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</trim>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "ON DUPLICATE KEY UPDATE");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<trim suffixOverrides=\",\">");
        for (PropertyDto nonId : persistence.getNonIdProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + String.format("<if test=\"%s!=null\"> `%s` = #{%s}, </if>",
                    nonId.getPropertyName(), nonId.getColumnName(), nonId.getPropertyName()));
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</trim>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        xmlLines.add("</insert>");

        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public Collection<String> insertXml(PersistenceDto persistence, String entityName, String methodName) {
        if (persistenceGeneratorConfig.getDisableInsert()) {
            return null;
        }
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<insert id=\"%s\" parameterType=\"%s\">", methodName, entityName));
        xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        xmlLines.add(BaseConstant.SINGLE_INDENT + "INSERT INTO `" + persistence.getTableName() + "`");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDto property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + String.format("<if test=\"%s!=null\"> `%s`, </if>",
                    property.getPropertyName(), property.getColumnName()));
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</trim>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<trim prefix=\"VALUES (\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDto property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + String.format("<if test=\"%s!=null\"> #{%s}, </if>",
                    property.getPropertyName(), property.getPropertyName()));
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</trim>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        xmlLines.add("</insert>");

        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public Collection<String> listAllXml(PersistenceDto persistence, String methodName) {
        if (persistenceGeneratorConfig.getDisableListAll()) {
            return null;
        }
        Collection<String> result = Lists.newArrayList();
        String firstLine = "<select id=\"" + methodName + "\" ";
        firstLine += "resultMap=\"all\">";
        result.add(firstLine);
        result.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        result.add(BaseConstant.SINGLE_INDENT + "SELECT");
        result.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
        result.add(BaseConstant.SINGLE_INDENT + "FROM `" + persistence.getTableName() + "`");
        result.add(BaseConstant.SINGLE_INDENT + "WHERE TRUE");
        if (persistence.getIsDeleteFlagExist()) {
            result.add(BaseConstant.SINGLE_INDENT + "  AND " + persistenceGeneratorConfig.getNotDeletedSql());
        }
        result.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        result.add("</select>");
        result.add("");
        return result;
    }

    @Override
    public Collection<String> queryByEntityXml(PersistenceDto persistence, String entityName, String methodName) {
        if (persistenceGeneratorConfig.getDisableQueryByEntity()) {
            return null;
        }

        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(
                String.format("<select id=\"%s\" parameterType=\"%s\" resultMap=\"all\">", methodName, entityName));
        xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM `" + persistence.getTableName() + "`");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE TRUE");
        if (persistence.getIsDeleteFlagExist()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + persistenceGeneratorConfig.getNotDeletedSql());
        }
        for (PropertyDto property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + String.format("  <if test=\"%s!=null\"> AND `%s` = #{%s} </if>",
                    property.getPropertyName(), property.getColumnName(), property.getPropertyName()));
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        xmlLines.add("</select>");
        xmlLines.add("");
        return xmlLines;
    }

    /**
     * 这个Proc生成2中方法：
     * 1. 根据主键列表查询
     * 2. 根据主键列表查询，并把结果集以主键为key，映射到Map中
     */
    @Override
    public Collection<String> queryByIdsXml(PersistenceDto persistence, String methodName) {
        if (methodName == null) {
            return null;
        }
        List<String> xmlLines = Lists.newArrayList();
        if (persistence.getIdProperties().size() == 1) {
            PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getIdProperties());
            xmlLines.add(String.format("<select id=\"%s\" parameterType=\"%s\" resultMap=\"all\">", methodName,
                    onlyPk.getJavaType().getQualifier().replaceFirst("java\\.lang\\.", "")));
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
            xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT");
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM `" + persistence.getTableName() + "`");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE TRUE");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + persistenceGeneratorConfig.getNotDeletedSql());
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND `" + onlyPk.getColumnName() + String.format(
                    "` IN (<foreach collection=\"%s\" item=\"one\" separator=\",\">#{one}</foreach>)",
                    English.plural(MoreStringUtils.lowerFirstLetter(onlyPk.getPropertyName()))));
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
            xmlLines.add("</select>");
            xmlLines.add("");
        }
        return xmlLines;
    }

    @Override
    public Collection<String> queryByIdXml(PersistenceDto persistence, String methodName) {
        if (persistenceGeneratorConfig.getDisableQueryById()) {
            return null;
        }
        List<String> xmlLines = Lists.newArrayList();
        if (persistence.getIdProperties().size() > 0) {
            String firstLine = "<select id=\"" + methodName + "\" ";
            if (persistence.getIdProperties().size() == 1) {
                firstLine += "parameterType=\"" + Iterables.getOnlyElement(persistence.getIdProperties()).getJavaType()
                        .getQualifier().replaceFirst("java\\.lang\\.", "") + "\" ";
            }
            firstLine += "resultMap=\"all\">";
            xmlLines.add(firstLine);
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
            xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT");
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM `" + persistence.getTableName() + "`");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE TRUE");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + persistenceGeneratorConfig.getNotDeletedSql());
            }
            for (PropertyDto idProperty : persistence.getIdProperties()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND `" + idProperty.getColumnName() + "` = #{"
                        + idProperty.getPropertyName() + "}");
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
            xmlLines.add("</select>");
            xmlLines.add("");
        }
        return xmlLines;
    }

    /**
     * 根据外键列表查询，表中每有几个外键，这个Proc就生成几个方法
     */
    @Override
    public Collection<String> queryByKeysXml(PersistenceDto persistence, Collection<QueryByKeysDto> queryByKeysDtos) {
        if (persistenceGeneratorConfig.getDisableQueryByKeys()) {
            return null;
        }
        Collection<String> sourceCodeLines = Lists.newArrayList();
        for (QueryByKeysDto queryByKeysDto : queryByKeysDtos) {
            List<String> xmlLines = Lists.newArrayList();
            PropertyDto key = queryByKeysDto.getKey();
            xmlLines.add(String.format("<select id=\"%s\" parameterType=\"%s\" resultMap=\"all\">",
                    queryByKeysDto.getMethodName(),
                    key.getJavaType().getQualifier().replaceFirst("java\\.lang\\.", "")));
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
            xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT");
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM `" + persistence.getTableName() + "`");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE TRUE");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + persistenceGeneratorConfig.getNotDeletedSql());
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND `" + key.getColumnName() + String.format(
                    "` IN (<foreach collection=\"%s\" item=\"one\" separator=\",\">#{one}</foreach>)",
                    queryByKeysDto.getVarsName()));
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
            xmlLines.add("</select>");
            sourceCodeLines.addAll(xmlLines);
            sourceCodeLines.add("");
        }
        return sourceCodeLines;
    }

    @Override
    public Collection<String> queryByKeyXml(PersistenceDto persistence,
            Collection<KeyMethodNameDto> keyAndMethodNames) {
        if (persistenceGeneratorConfig.getDisableQueryByKey()) {
            return null;
        }
        Collection<String> result = Lists.newArrayList();
        for (KeyMethodNameDto keyAndMethodName : keyAndMethodNames) {
            List<String> xmlLines = Lists.newArrayList();
            PropertyDto key = keyAndMethodName.getKey();
            xmlLines.add(String.format("<select id=\"%s\" parameterType=\"%s\" resultMap=\"all\">",
                    keyAndMethodName.getMethodName(),
                    key.getJavaType().getQualifier().replaceFirst("java\\.lang\\.", "")));
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
            xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT");
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM `" + persistence.getTableName() + "`");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE TRUE");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + persistenceGeneratorConfig.getNotDeletedSql());
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND `" + key.getColumnName() + "` = #{" + key.getPropertyName()
                    + "}");
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
            xmlLines.add("</select>");
            result.addAll(xmlLines);
            result.add("");
        }
        return result;
    }

    @Override
    public Collection<String> resultMapXml(PersistenceDto persistence, String entityName) {
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<resultMap id=\"all\" type=\"%s\">", entityName));
        for (PropertyDto id : persistence.getIdProperties()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + String.format("<id column=\"%s\" property=\"%s\"/>",
                    id.getColumnName(), id.getPropertyName()));
        }
        for (PropertyDto nonId : persistence.getNonIdProperties()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + String.format("<result column=\"%s\" property=\"%s\"/>",
                    nonId.getColumnName(), nonId.getPropertyName()));
        }
        xmlLines.add("</resultMap>");
        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public Collection<String> updateByIdEvenNullXml(PersistenceDto persistence, String entityName, String methodName) {
        if (persistenceGeneratorConfig.getDisableUpdateByIdEvenNull()) {
            return null;
        }
        List<String> xmlLines = Lists.newArrayList();
        if (persistence.getIdProperties().size() > 0) {
            xmlLines.add(String.format("<update id=\"%s\" parameterType=\"%s\">", methodName, entityName));
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
            xmlLines.add(BaseConstant.SINGLE_INDENT + "UPDATE `" + persistence.getTableName() + "`");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "SET");
            for (PropertyDto nonId : persistence.getNonIdProperties()) {
                xmlLines.add(
                        BaseConstant.DOUBLE_INDENT + "`" + nonId.getColumnName() + "` = #{" + nonId.getPropertyName()
                                + "},");
            }
            // 删除最后一个语句中，最后的逗号
            if (xmlLines.size() > 0) {
                int last = xmlLines.size() - 1;
                xmlLines.set(last, MoreStringUtils.replaceLast(xmlLines.get(last), ",", ""));
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE TRUE");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + persistenceGeneratorConfig.getNotDeletedSql());
            }
            for (PropertyDto idProperty : persistence.getIdProperties()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND `" + idProperty.getColumnName() + "` = #{"
                        + idProperty.getPropertyName() + "}");
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
            xmlLines.add("</update>");
            xmlLines.add("");
        }
        return xmlLines;
    }

    @Override
    public Collection<String> updateByIdXml(PersistenceDto persistence, String entityName, String methodName) {
        if (persistenceGeneratorConfig.getDisableUpdateById()) {
            return null;
        }
        List<String> xmlLines = Lists.newArrayList();
        if (persistence.getIdProperties().size() > 0) {
            xmlLines.add(String.format("<update id=\"%s\" parameterType=\"%s\">", methodName, entityName));
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
            xmlLines.add(BaseConstant.SINGLE_INDENT + "UPDATE `" + persistence.getTableName() + "`");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "<set>");
            for (PropertyDto nonId : persistence.getNonIdProperties()) {
                xmlLines.add(BaseConstant.DOUBLE_INDENT + String.format("<if test=\"%s!=null\"> `%s` = #{%s}, </if>",
                        nonId.getPropertyName(), nonId.getColumnName(), nonId.getPropertyName()));
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "</set>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE TRUE");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + persistenceGeneratorConfig.getNotDeletedSql());
            }
            for (PropertyDto id : persistence.getIdProperties()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + String.format("  AND `%s` = #{%s}", id.getColumnName(),
                        id.getPropertyName()));
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
            xmlLines.add(BaseConstant.SINGLE_INDENT + "</update>");
            xmlLines.add("");
        }
        return xmlLines;
    }

}
