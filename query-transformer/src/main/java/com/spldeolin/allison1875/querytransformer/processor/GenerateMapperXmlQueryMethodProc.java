package com.spldeolin.allison1875.querytransformer.processor;

import static com.spldeolin.allison1875.base.constant.BaseConstant.DOUBLE_INDENT;
import static com.spldeolin.allison1875.base.constant.BaseConstant.SINGLE_INDENT;
import static com.spldeolin.allison1875.base.constant.BaseConstant.TREBLE_INDENT;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.atteo.evo.inflector.English;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.ast.MavenPathResolver;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.enums.VerbEnum;
import com.spldeolin.allison1875.querytransformer.javabean.CriterionDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-10-11
 */
@Singleton
@Log4j2
public class GenerateMapperXmlQueryMethodProc {

    public void process(AstForest astForest, DesignMeta queryMeta, String queryMethodName,
            Collection<CriterionDto> criterions) {
        File mapperXml = MavenPathResolver.findMavenModule(astForest.getPrimaryClass())
                .resolve(queryMeta.getMapperRelativePath()).toFile();

        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<select id='%s' resultMap='all'>", queryMethodName));
        xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        xmlLines.add(SINGLE_INDENT + "SELECT");
        xmlLines.add(DOUBLE_INDENT + "<include refid='all' />");
        xmlLines.add(SINGLE_INDENT + "FROM");
        xmlLines.add(DOUBLE_INDENT + queryMeta.getTableName());
        if (criterions.size() > 0) {
            xmlLines.add(SINGLE_INDENT + "WHERE TRUE");
            for (CriterionDto cond : criterions) {
                VerbEnum operator = VerbEnum.of(cond.getOperator());
                String ifTag = SINGLE_INDENT + "<if test=\"" + cond.getParameterName() + " != null";
                if ("String".equals(cond.getParameterType())) {
                    ifTag += " and " + cond.getParameterName() + " != ''";
                }
                ifTag += "\">";
                if (operator == VerbEnum.EQUALS) {
                    xmlLines.add(ifTag);
                    xmlLines.add(DOUBLE_INDENT + "AND " + cond.getColumnName() + " = " + cond.getDollarParameterName());
                    xmlLines.add(SINGLE_INDENT + "</if>");
                }
                if (operator == VerbEnum.NOT_EQUALS) {
                    xmlLines.add(ifTag);
                    xmlLines.add(
                            DOUBLE_INDENT + "AND " + cond.getColumnName() + " != " + cond.getDollarParameterName());
                    xmlLines.add(SINGLE_INDENT + "</if>");
                }
                if (operator == VerbEnum.IN) {
                    String argumentName = English.plural(cond.getParameterName());
                    xmlLines.add(SINGLE_INDENT + "<if test=\"" + argumentName + " != null\">");
                    xmlLines.add(DOUBLE_INDENT + "<if test=\"" + argumentName + ".size() > 0\">");
                    xmlLines.add(
                            TREBLE_INDENT + "AND " + cond.getColumnName() + " IN (<foreach collection='" + argumentName
                                    + "' item='one' separator=','>#{one}</foreach>)");
                    xmlLines.add(DOUBLE_INDENT + "</if>");
                    xmlLines.add(DOUBLE_INDENT + "<if test=\"" + argumentName + ".size() == 0\">");
                    xmlLines.add(TREBLE_INDENT + "AND FALSE");
                    xmlLines.add(DOUBLE_INDENT + "</if>");
                    xmlLines.add(SINGLE_INDENT + "</if>");
                }
                if (operator == VerbEnum.NOT_IN) {
                    String argumentName = English.plural(cond.getParameterName());
                    xmlLines.add(SINGLE_INDENT + String
                            .format("<if test=\"%s != null and %s.size() > 0\">", argumentName, argumentName));
                    xmlLines.add(DOUBLE_INDENT + "AND " + cond.getColumnName() + " NOT IN (<foreach collection='"
                            + argumentName + "' item='one' separator=','>#{one}</foreach>)");
                    xmlLines.add(SINGLE_INDENT + "</if>");
                }
                if (operator == VerbEnum.GREATER_THEN) {
                    xmlLines.add(ifTag);
                    xmlLines.add(DOUBLE_INDENT + "AND " + cond.getColumnName() + " > " + cond.getDollarParameterName());
                    xmlLines.add(SINGLE_INDENT + "</if>");
                }
                if (operator == VerbEnum.GREATER_OR_EQUALS) {
                    xmlLines.add(ifTag);
                    xmlLines.add(
                            DOUBLE_INDENT + "AND " + cond.getColumnName() + " >= " + cond.getDollarParameterName());
                    xmlLines.add(SINGLE_INDENT + "</if>");
                }
                if (operator == VerbEnum.LESS_THEN) {
                    xmlLines.add(ifTag);
                    xmlLines.add(
                            DOUBLE_INDENT + "AND " + cond.getColumnName() + " &lt; " + cond.getDollarParameterName());
                    xmlLines.add(SINGLE_INDENT + "</if>");
                }
                if (operator == VerbEnum.LESS_OR_EQUALS) {
                    xmlLines.add(ifTag);
                    xmlLines.add(
                            DOUBLE_INDENT + "AND " + cond.getColumnName() + " &lt;= " + cond.getDollarParameterName());
                    xmlLines.add(SINGLE_INDENT + "</if>");
                }
                if (operator == VerbEnum.NOT_NULL) {
                    xmlLines.add(SINGLE_INDENT + "AND " + cond.getColumnName() + " IS NOT NULL");
                }
                if (operator == VerbEnum.IS_NULL) {
                    xmlLines.add(SINGLE_INDENT + "AND " + cond.getColumnName() + " IS NULL");
                }
                if (operator == VerbEnum.LIKE) {
                    xmlLines.add(ifTag);
                    xmlLines.add(DOUBLE_INDENT + "AND " + cond.getColumnName() + " LIKE CONCAT('%', " + cond
                            .getDollarParameterName() + ", '%')");
                    xmlLines.add(SINGLE_INDENT + "</if>");
                }
            }
        }
        xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        xmlLines.add("</select>");

        List<String> newLines = Lists.newArrayList();
        try {
            List<String> lines = FileUtils.readLines(mapperXml, StandardCharsets.UTF_8);
            Collections.reverse(lines);
            for (String line : lines) {
                newLines.add(line);
                if (line.contains("</mapper>")) {
                    Collections.reverse(xmlLines);
                    for (String xmlLine : xmlLines) {
                        newLines.add(SINGLE_INDENT + xmlLine);
                    }
                }
            }
            Collections.reverse(newLines);

            FileUtils.writeLines(mapperXml, newLines);
        } catch (IOException e) {
            log.error(e);
        }
    }

}