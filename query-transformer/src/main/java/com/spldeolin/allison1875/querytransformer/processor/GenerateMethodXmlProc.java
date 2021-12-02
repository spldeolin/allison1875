package com.spldeolin.allison1875.querytransformer.processor;

import static com.spldeolin.allison1875.base.constant.BaseConstant.DOUBLE_INDENT;
import static com.spldeolin.allison1875.base.constant.BaseConstant.SINGLE_INDENT;
import static com.spldeolin.allison1875.base.constant.BaseConstant.TREBLE_INDENT;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.github.javaparser.ast.type.PrimitiveType;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.ast.MavenPathResolver;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.querytransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.querytransformer.enums.PredicateEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnClassifyEnum;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParameterTransformationDto;
import com.spldeolin.allison1875.querytransformer.javabean.PhraseDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultTransformationDto;
import jodd.io.FileUtil;
import jodd.util.StringUtil;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-10-11
 */
@Singleton
@Log4j2
public class GenerateMethodXmlProc {

    public static final String SINGLE_INDENT_WITH_AND = SINGLE_INDENT + "  AND ";

    public void process(AstForest astForest, DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ParameterTransformationDto parameterTransformation, ResultTransformationDto resultTransformation) {
        File mapperXml = MavenPathResolver.findMavenModule(astForest.getPrimaryClass())
                .resolve(designMeta.getMapperRelativePath()).toFile();

        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add("");
        if (chainAnalysis.getChainMethod() == ChainMethodEnum.query) {
            // QUERY
            xmlLines.add(concatLotNoComment(chainAnalysis));
            String startTag = this.concatSelectStartTag(designMeta, chainAnalysis, parameterTransformation,
                    resultTransformation);
            xmlLines.add(startTag);
            xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
            xmlLines.add(SINGLE_INDENT + "SELECT");
            if (chainAnalysis.getQueryPhrases().size() == 0) {
                xmlLines.add(DOUBLE_INDENT + "<include refid='all' />");
            } else {
                for (PhraseDto queryPhrase : chainAnalysis.getQueryPhrases()) {
                    xmlLines.add(
                            DOUBLE_INDENT + MoreStringUtils.lowerCamelToUnderscore(queryPhrase.getSubjectPropertyName())
                                    + " AS " + queryPhrase.getSubjectPropertyName() + ",");
                }
                // 删除最后一个语句中，最后的逗号
                int last = xmlLines.size() - 1;
                xmlLines.set(last, StringUtil.cutSuffix(xmlLines.get(last), ","));
            }
            xmlLines.add(SINGLE_INDENT + "FROM");
            xmlLines.add(DOUBLE_INDENT + designMeta.getTableName());
            xmlLines.addAll(concatWhereSection(designMeta, chainAnalysis));
            if (chainAnalysis.getOrderPhrases().size() > 0) {
                xmlLines.add(SINGLE_INDENT + "ORDER BY");
                for (PhraseDto orderPhrase : chainAnalysis.getOrderPhrases()) {
                    PropertyDto property = designMeta.getProperties().get(orderPhrase.getSubjectPropertyName());
                    xmlLines.add(
                            DOUBLE_INDENT + property.getColumnName() + (orderPhrase.getPredicate() == PredicateEnum.DESC
                                    ? " DESC," : ","));
                }
                // 删除最后一个语句中，最后的逗号
                int last = xmlLines.size() - 1;
                xmlLines.set(last, StringUtil.cutSuffix(xmlLines.get(last), ","));
            }
            if (chainAnalysis.getReturnClassify() == ReturnClassifyEnum.one) {
                xmlLines.add(SINGLE_INDENT + "LIMIT 1");
            }
            xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
            xmlLines.add("</select>");
        } else if (chainAnalysis.getChainMethod() == ChainMethodEnum.update) {
            // UPDATE
            xmlLines.add(concatLotNoComment(chainAnalysis));
            String startTag = concatUpdateStartTag(chainAnalysis, parameterTransformation);
            xmlLines.add(startTag);
            xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
            xmlLines.add(SINGLE_INDENT + "UPDATE " + designMeta.getTableName());
            xmlLines.add(SINGLE_INDENT + "SET");
            for (PhraseDto updatePhrase : chainAnalysis.getUpdatePhrases()) {
                PropertyDto property = designMeta.getProperties().get(updatePhrase.getSubjectPropertyName());
                xmlLines.add(DOUBLE_INDENT + property.getColumnName() + " = #{" + updatePhrase.getVarName() + "},");
            }
            // 删除最后一个语句中，最后的逗号
            int last = xmlLines.size() - 1;
            xmlLines.set(last, StringUtil.cutSuffix(xmlLines.get(last), ","));
            xmlLines.addAll(concatWhereSection(designMeta, chainAnalysis));
            xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
            xmlLines.add("</update>");
        } else if (chainAnalysis.getChainMethod() == ChainMethodEnum.drop) {
            // DROP
            xmlLines.add(concatLotNoComment(chainAnalysis));
            String startTag = concatDeleteStartTag(chainAnalysis, parameterTransformation);
            xmlLines.add(startTag);
            if (chainAnalysis.getByPhrases().size() > 0) {
                xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
            }
            xmlLines.add(SINGLE_INDENT + "DELETE FROM " + designMeta.getTableName());
            xmlLines.addAll(concatWhereSection(designMeta, chainAnalysis));
            if (chainAnalysis.getByPhrases().size() > 0) {
                xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
            }
            xmlLines.add("</delete>");
        } else {
            throw new RuntimeException("impossible unless bug");
        }

        List<String> newLines = Lists.newArrayList();
        try {
            List<String> lines = Arrays.asList(FileUtil.readLines(mapperXml));
            Collections.reverse(lines);
            for (String line : lines) {
                newLines.add(line);
                if (line.contains("</mapper>")) {
                    Collections.reverse(xmlLines);
                    for (String xmlLine : xmlLines) {
                        if (StringUtil.isNotBlank(xmlLine)) {
                            newLines.add(SINGLE_INDENT + xmlLine);
                        }
                    }
                    newLines.add("");
                }
            }
            Collections.reverse(newLines);
            FileUtil.writeString(mapperXml, Joiner.on('\n').join(newLines));
        } catch (IOException e) {
            log.error(e);
        }
    }

    private List<String> concatWhereSection(DesignMeta designMeta, ChainAnalysisDto chainAnalysis) {
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(SINGLE_INDENT + "WHERE TRUE");
        if (designMeta.getNotDeletedSql() != null) {
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
                        xmlLines.add(SINGLE_INDENT_WITH_AND + property.getColumnName() + " = " + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND " + property.getColumnName() + " = " + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case NOT_EQUALS:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + property.getColumnName() + " != " + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND " + property.getColumnName() + " != " + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case IN:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + property.getColumnName() + " IN (<foreach collection='"
                                + varName + "' item='one' separator=','>#{one}</foreach>)");
                    } else {
                        xmlLines.add(SINGLE_INDENT + "<if test=\"" + varName + " != null\">");
                        xmlLines.add(DOUBLE_INDENT + "<if test=\"" + varName + ".size() > 0\">");
                        xmlLines.add(TREBLE_INDENT + "AND " + property.getColumnName() + " IN (<foreach collection='"
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
                        xmlLines.add(
                                SINGLE_INDENT_WITH_AND + property.getColumnName() + " NOT IN (<foreach collection='"
                                        + varName + "' item='one' separator=','>#{one}</foreach>)");
                    } else {
                        xmlLines.add(
                                SINGLE_INDENT + String.format("<if test=\"%s != null and %s.size() > 0\">", varName,
                                        varName));
                        xmlLines.add(
                                DOUBLE_INDENT + "AND " + property.getColumnName() + " NOT IN (<foreach collection='"
                                        + varName + "' item='one' separator=','>#{one}</foreach>)");
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case GREATER_THEN:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + property.getColumnName() + " > " + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND " + property.getColumnName() + " > " + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case GREATER_OR_EQUALS:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + property.getColumnName() + " >= " + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND " + property.getColumnName() + " >= " + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case LESS_THEN:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + property.getColumnName() + " &lt; " + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND " + property.getColumnName() + " &lt; " + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case LESS_OR_EQUALS:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + property.getColumnName() + " &lt;= " + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND " + property.getColumnName() + " &lt;= " + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case NOT_NULL:
                    xmlLines.add(SINGLE_INDENT + "  AND " + property.getColumnName() + " IS NOT NULL");
                    break;
                case IS_NULL:
                    xmlLines.add(SINGLE_INDENT + "  AND " + property.getColumnName() + " IS NULL");
                    break;
                case LIKE:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(
                                SINGLE_INDENT_WITH_AND + property.getColumnName() + " LIKE CONCAT('%', " + dollarVar
                                        + ", '%')");
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(
                                DOUBLE_INDENT + "AND " + property.getColumnName() + " LIKE CONCAT('%', " + dollarVar
                                        + ", '%')");
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
            }
        }
        return xmlLines;
    }

    private String concatLotNoComment(ChainAnalysisDto chainAnalysis) {
        return chainAnalysis.getLotNo().asXmlComment();
    }

    private String concatSelectStartTag(DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ParameterTransformationDto parameterTransformation, ResultTransformationDto resultTransformation) {
        String startTag = "<select id='" + chainAnalysis.getMethodName() + "'";
        if (parameterTransformation != null && parameterTransformation.getParameters().size() == 1) {
            startTag += " parameterType='" + parameterTransformation.getImports().get(0) + "'";
        }
        if (resultTransformation.getElementTypeQualifier() != null && !resultTransformation.getElementTypeQualifier()
                .equals(designMeta.getEntityQualifier())) {
            startTag += " resultType='" + resultTransformation.getElementTypeQualifier() + "'>";
        } else if (!resultTransformation.getResultType().equals(PrimitiveType.intType())) {
            startTag += " resultMap='all'>";
        } else {
            startTag += ">";
        }
        return startTag;
    }

    private String concatUpdateStartTag(ChainAnalysisDto chainAnalysis,
            ParameterTransformationDto parameterTransformation) {
        String startTag = "<update id='" + chainAnalysis.getMethodName() + "'";
        if (parameterTransformation != null && parameterTransformation.getParameters().size() == 1) {
            startTag += " parameterType='" + parameterTransformation.getImports().get(0) + "'";
        }
        startTag += ">";
        return startTag;
    }

    private String concatDeleteStartTag(ChainAnalysisDto chainAnalysis,
            ParameterTransformationDto parameterTransformation) {
        String startTag = "<delete id='" + chainAnalysis.getMethodName() + "'";
        if (parameterTransformation != null && parameterTransformation.getParameters().size() == 1) {
            startTag += " parameterType='" + parameterTransformation.getImports().get(0) + "'";
        }
        startTag += ">";
        return startTag;
    }

}