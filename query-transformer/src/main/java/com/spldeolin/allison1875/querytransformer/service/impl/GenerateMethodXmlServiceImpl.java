package com.spldeolin.allison1875.querytransformer.service.impl;

import static com.spldeolin.allison1875.common.constant.BaseConstant.DOUBLE_INDENT;
import static com.spldeolin.allison1875.common.constant.BaseConstant.SINGLE_INDENT;
import static com.spldeolin.allison1875.common.constant.BaseConstant.TREBLE_INDENT;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.type.PrimitiveType;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.service.AstForestResidenceService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.querytransformer.QueryTransformerConfig;
import com.spldeolin.allison1875.querytransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.querytransformer.enums.PredicateEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnClassifyEnum;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParamGenerationDto;
import com.spldeolin.allison1875.querytransformer.javabean.PhraseDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultGenerationDto;
import com.spldeolin.allison1875.querytransformer.service.GenerateMethodXmlService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-10-11
 */
@Singleton
@Slf4j
public class GenerateMethodXmlServiceImpl implements GenerateMethodXmlService {

    public static final String SINGLE_INDENT_WITH_AND = SINGLE_INDENT + "  AND ";

    @Inject
    private QueryTransformerConfig config;

    @Inject
    private AstForestResidenceService astForestResidenceService;

    @Override
    public List<FileFlush> generate(AstForest astForest, DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ParamGenerationDto paramGeneration, ResultGenerationDto resultGeneration) {
        List<FileFlush> result = Lists.newArrayList();

        for (String mapperRelativePath : designMeta.getMapperRelativePaths()) {
            File mapperXml = astForestResidenceService.findModuleRoot(astForest.getPrimaryClass())
                    .resolve(mapperRelativePath).toFile();

            List<String> xmlLines = Lists.newArrayList();
            xmlLines.add("");
            if (chainAnalysis.getChainMethod() == ChainMethodEnum.query) {
                // QUERY
                xmlLines.add(concatLotNoComment(chainAnalysis));
                String startTag = this.concatSelectStartTag(designMeta, chainAnalysis, paramGeneration,
                        resultGeneration);
                xmlLines.add(startTag);
                xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
                xmlLines.add(SINGLE_INDENT + "SELECT");
                if (chainAnalysis.getReturnClassify() == ReturnClassifyEnum.count) {
                    xmlLines.add(DOUBLE_INDENT + "COUNT(*)");
                } else if (CollectionUtils.isEmpty(chainAnalysis.getQueryPhrases())) {
                    xmlLines.add(DOUBLE_INDENT + "<include refid='all' />");
                } else {
                    for (PhraseDto queryPhrase : chainAnalysis.getQueryPhrases()) {
                        PropertyDto property = designMeta.getProperties().get(queryPhrase.getSubjectPropertyName());
                        xmlLines.add(
                                DOUBLE_INDENT + "`" + property.getColumnName() + "` AS " + property.getPropertyName()
                                        + ",");
                    }
                    // 删除最后一个语句中，最后的逗号
                    int last = xmlLines.size() - 1;
                    xmlLines.set(last, MoreStringUtils.replaceLast(xmlLines.get(last), ",", ""));
                }
                xmlLines.add(SINGLE_INDENT + "FROM");
                xmlLines.add(DOUBLE_INDENT + "`" + designMeta.getTableName() + "`");
                xmlLines.addAll(concatWhereSection(designMeta, chainAnalysis, true));
                if (CollectionUtils.isNotEmpty(chainAnalysis.getOrderPhrases())) {
                    xmlLines.add(SINGLE_INDENT + "ORDER BY");
                    for (PhraseDto orderPhrase : chainAnalysis.getOrderPhrases()) {
                        PropertyDto property = designMeta.getProperties().get(orderPhrase.getSubjectPropertyName());
                        xmlLines.add(DOUBLE_INDENT + "`" + property.getColumnName() + "`" + (
                                orderPhrase.getPredicate() == PredicateEnum.DESC ? " DESC," : ","));
                    }
                    // 删除最后一个语句中，最后的逗号
                    int last = xmlLines.size() - 1;
                    xmlLines.set(last, MoreStringUtils.replaceLast(xmlLines.get(last), ",", ""));
                }
                if (chainAnalysis.getReturnClassify() == ReturnClassifyEnum.one) {
                    xmlLines.add(SINGLE_INDENT + "LIMIT 1");
                }
                xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
                xmlLines.add("</select>");
            } else if (chainAnalysis.getChainMethod() == ChainMethodEnum.update) {
                // UPDATE
                xmlLines.add(concatLotNoComment(chainAnalysis));
                String startTag = concatUpdateStartTag(chainAnalysis, paramGeneration);
                xmlLines.add(startTag);
                xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
                xmlLines.add(SINGLE_INDENT + "UPDATE `" + designMeta.getTableName() + "`");
                xmlLines.add(SINGLE_INDENT + "SET");
                for (PhraseDto updatePhrase : chainAnalysis.getUpdatePhrases()) {
                    PropertyDto property = designMeta.getProperties().get(updatePhrase.getSubjectPropertyName());
                    xmlLines.add(DOUBLE_INDENT + "`" + property.getColumnName() + "` = #{" + updatePhrase.getVarName()
                            + "},");
                }
                // 删除最后一个语句中，最后的逗号
                int last = xmlLines.size() - 1;
                xmlLines.set(last, MoreStringUtils.replaceLast(xmlLines.get(last), ",", ""));
                xmlLines.addAll(concatWhereSection(designMeta, chainAnalysis, true));
                xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
                xmlLines.add("</update>");
            } else if (chainAnalysis.getChainMethod() == ChainMethodEnum.drop) {
                // DROP
                xmlLines.add(concatLotNoComment(chainAnalysis));
                String startTag = concatDeleteStartTag(chainAnalysis, paramGeneration);
                xmlLines.add(startTag);
                if (CollectionUtils.isNotEmpty(chainAnalysis.getByPhrases())) {
                    xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
                }
                xmlLines.add(SINGLE_INDENT + "DELETE FROM `" + designMeta.getTableName() + "`");
                xmlLines.addAll(concatWhereSection(designMeta, chainAnalysis, false));
                if (CollectionUtils.isNotEmpty(chainAnalysis.getByPhrases())) {
                    xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
                }
                xmlLines.add("</delete>");
            } else {
                throw new Allison1875Exception("unknown ChainMethodEnum [" + chainAnalysis.getChainMethod() + "]");
            }

            List<String> newLines = Lists.newArrayList();
            try {
                List<String> lines = FileUtils.readLines(mapperXml, StandardCharsets.UTF_8);
                Collections.reverse(lines);
                for (String line : lines) {
                    newLines.add(line);
                    if (line.contains("</mapper>")) {
                        Collections.reverse(xmlLines);
                        for (String xmlLine : xmlLines) {
                            if (StringUtils.isNotBlank(xmlLine)) {
                                newLines.add(SINGLE_INDENT + xmlLine);
                            }
                        }
                        newLines.add("");
                    }
                }
                Collections.reverse(newLines);

                result.add(FileFlush.build(mapperXml, Joiner.on('\n').join(newLines)));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return result;
    }

    private List<String> concatWhereSection(DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            boolean needNotDeletedSql) {
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(SINGLE_INDENT + "WHERE TRUE");
        if (needNotDeletedSql && designMeta.getNotDeletedSql() != null) {
            xmlLines.add(SINGLE_INDENT + "  AND " + designMeta.getNotDeletedSql());
        }
        for (PhraseDto byPhrase : chainAnalysis.getByPhrases()) {
            PropertyDto property = designMeta.getProperties().get(byPhrase.getSubjectPropertyName());
            String varName = byPhrase.getVarName();
            String dollarVar = "#{" + varName + "}";

            String ifTag = SINGLE_INDENT + "<if test=\"" + varName + " != null";
            if (property.getJavaType().getQualifier().equals("java.lang.String")) {
                ifTag += " and " + varName + " != ''";
            }
            ifTag += "\">";
            switch (byPhrase.getPredicate()) {
                case EQUALS:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + "`" + property.getColumnName() + "` = " + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND `" + property.getColumnName() + "` = " + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case NOT_EQUALS:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + "`" + property.getColumnName() + "` != " + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND `" + property.getColumnName() + "` != " + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case IN:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(
                                SINGLE_INDENT_WITH_AND + "`" + property.getColumnName() + "` IN (<foreach collection='"
                                        + varName + "' item='one' separator=','>#{one}</foreach>)");
                    } else {
                        xmlLines.add(SINGLE_INDENT + "<if test=\"" + varName + " != null\">");
                        xmlLines.add(DOUBLE_INDENT + "<if test=\"" + varName + ".size() > 0\">");
                        xmlLines.add(TREBLE_INDENT + "AND `" + property.getColumnName() + "` IN (<foreach collection='"
                                + varName + "' item='one' separator=','>#{one}</foreach>)");
                        xmlLines.add(DOUBLE_INDENT + "</if>");
                        xmlLines.add(DOUBLE_INDENT + "<if test=\"" + varName + ".size() == 0\">");
                        xmlLines.add(TREBLE_INDENT + "AND FALSE");
                        xmlLines.add(DOUBLE_INDENT + "</if>");
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case NOT_IN:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + "`" + property.getColumnName()
                                + "` NOT IN (<foreach collection='" + varName
                                + "' item='one' separator=','>#{one}</foreach>)");
                    } else {
                        xmlLines.add(
                                SINGLE_INDENT + String.format("<if test=\"%s != null and %s.size() > 0\">", varName,
                                        varName));
                        xmlLines.add(
                                DOUBLE_INDENT + "AND `" + property.getColumnName() + "` NOT IN (<foreach collection='"
                                        + varName + "' item='one' separator=','>#{one}</foreach>)");
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case GREATER_THEN:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + "`" + property.getColumnName() + "` > " + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND `" + property.getColumnName() + "` > " + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case GREATER_OR_EQUALS:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + "`" + property.getColumnName() + "` >= " + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND `" + property.getColumnName() + "` >= " + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case LESS_THEN:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + "`" + property.getColumnName() + "` &lt; " + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND `" + property.getColumnName() + "` &lt; " + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case LESS_OR_EQUALS:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + "`" + property.getColumnName() + "` &lt;= " + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND `" + property.getColumnName() + "` &lt;= " + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case NOT_NULL:
                    xmlLines.add(SINGLE_INDENT + "  AND `" + property.getColumnName() + "` IS NOT NULL");
                    break;
                case IS_NULL:
                    xmlLines.add(SINGLE_INDENT + "  AND `" + property.getColumnName() + "` IS NULL");
                    break;
                case LIKE:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + "`" + property.getColumnName() + "` LIKE CONCAT('%', "
                                + dollarVar + ", '%')");
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(
                                DOUBLE_INDENT + "AND `" + property.getColumnName() + "` LIKE CONCAT('%', " + dollarVar
                                        + ", '%')");
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
            }
        }
        return xmlLines;
    }

    private String concatLotNoComment(ChainAnalysisDto chainAnalysis) {
        if (config.getEnableLotNoAnnounce()) {
            return "<!-- " + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + chainAnalysis.getLotNo() + " -->";
        }
        return "";
    }

    private String concatSelectStartTag(DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ParamGenerationDto paramGeneration, ResultGenerationDto resultGeneration) {
        String startTag = "<select id='" + chainAnalysis.getMethodName() + "'";
        if (paramGeneration.getParameters().size() == 1) {
            startTag += " parameterType='" + paramGeneration.getImports().get(0) + "'";
        }
        if (resultGeneration.getElementTypeQualifier() != null && !resultGeneration.getElementTypeQualifier()
                .equals(designMeta.getEntityQualifier())) {
            startTag += " resultType='" + resultGeneration.getElementTypeQualifier() + "'>";
        } else if (!resultGeneration.getResultType().equals(PrimitiveType.intType())) {
            startTag += " resultMap='all'>";
        } else {
            startTag += " resultType='int'>";
        }
        return startTag;
    }

    private String concatUpdateStartTag(ChainAnalysisDto chainAnalysis, ParamGenerationDto paramGeneration) {
        String startTag = "<update id='" + chainAnalysis.getMethodName() + "'";
        if (paramGeneration.getParameters().size() == 1) {
            startTag += " parameterType='" + paramGeneration.getImports().get(0) + "'";
        }
        startTag += ">";
        return startTag;
    }

    private String concatDeleteStartTag(ChainAnalysisDto chainAnalysis, ParamGenerationDto paramGeneration) {
        String startTag = "<delete id='" + chainAnalysis.getMethodName() + "'";
        if (paramGeneration.getParameters().size() == 1) {
            startTag += " parameterType='" + paramGeneration.getImports().get(0) + "'";
        }
        startTag += ">";
        return startTag;
    }

}