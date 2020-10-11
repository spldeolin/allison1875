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
import org.apache.logging.log4j.Logger;
import org.atteo.evo.inflector.English;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.querytransformer.enums.OperatorEnum;
import com.spldeolin.allison1875.querytransformer.javabean.CriterionDto;
import com.spldeolin.allison1875.querytransformer.javabean.QueryMeta;

/**
 * @author Deolin 2020-10-11
 */
class GenerateMapperXmlQueryMethodProc {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(AnalyzeCriterionProc.class);

    private final AstForest astForest;

    private final QueryMeta queryMeta;

    private final String queryMethodName;

    private final Collection<CriterionDto> criterions;

    GenerateMapperXmlQueryMethodProc(AstForest astForest, QueryMeta queryMeta, String queryMethodName,
            Collection<CriterionDto> criterions) {
        this.astForest = astForest;
        this.queryMeta = queryMeta;
        this.queryMethodName = queryMethodName;
        this.criterions = criterions;
    }

    void process() {
        File mapperXml = astForest.getHost().resolve(queryMeta.getMapperRelativePath()).toFile();

        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<select id='%s' resultMap='all'>", queryMethodName));
        xmlLines.add(SINGLE_INDENT + "SELECT");
        xmlLines.add(DOUBLE_INDENT + "<include refid='all' />");
        xmlLines.add(SINGLE_INDENT + "FROM");
        xmlLines.add(DOUBLE_INDENT + queryMeta.getTableName());
        if (criterions.size() > 0) {
            xmlLines.add(SINGLE_INDENT + "WHERE TRUE");
            for (CriterionDto cond : criterions) {
                OperatorEnum operator = OperatorEnum.of(cond.operator());
                String ifTag = SINGLE_INDENT + "<if test=\"" + cond.propertyName() + " != null";
                if ("String".equals(cond.propertyType())) {
                    ifTag += " and " + cond.propertyName() + " != ''";
                }
                ifTag += "\">";
                if (operator == OperatorEnum.EQUALS) {
                    xmlLines.add(ifTag);
                    xmlLines.add(DOUBLE_INDENT + "AND " + cond.columnName() + " = " + cond.dollarVar());
                    xmlLines.add(SINGLE_INDENT + "</if>");
                }
                if (operator == OperatorEnum.NOT_EQUALS) {
                    xmlLines.add(ifTag);
                    xmlLines.add(DOUBLE_INDENT + "AND " + cond.columnName() + " != " + cond.dollarVar());
                    xmlLines.add(SINGLE_INDENT + "</if>");
                }
                if (operator == OperatorEnum.IN || operator == OperatorEnum.NOT_IN) {
                    String argumentName = English.plural(cond.propertyName());
                    xmlLines.add(SINGLE_INDENT + "<if test=\"" + argumentName + " != null\">");
                    xmlLines.add(DOUBLE_INDENT + "<if test=\"" + argumentName + ".size() != 0\">");
                    xmlLines.add(
                            TREBLE_INDENT + "AND " + cond.columnName() + (operator == OperatorEnum.NOT_IN ? " NOT" : "")
                                    + " IN (<foreach collection='" + argumentName
                                    + "' item='one' separator=','>#{one}</foreach>)");
                    xmlLines.add(DOUBLE_INDENT + "</if>");
                    xmlLines.add(DOUBLE_INDENT + "<if test=\"" + argumentName + ".size() == 0\">");
                    xmlLines.add(TREBLE_INDENT + "AND FALSE");
                    xmlLines.add(DOUBLE_INDENT + "</if>");
                    xmlLines.add(SINGLE_INDENT + "</if>");
                }
                if (operator == OperatorEnum.GREATER_THEN) {
                    xmlLines.add(ifTag);
                    xmlLines.add(DOUBLE_INDENT + "AND " + cond.columnName() + " > " + cond.dollarVar());
                    xmlLines.add(SINGLE_INDENT + "</if>");
                }
                if (operator == OperatorEnum.GREATER_OR_EQUALS) {
                    xmlLines.add(ifTag);
                    xmlLines.add(DOUBLE_INDENT + "AND " + cond.columnName() + " >= " + cond.dollarVar());
                    xmlLines.add(SINGLE_INDENT + "</if>");
                }
                if (operator == OperatorEnum.LESS_THEN) {
                    xmlLines.add(ifTag);
                    xmlLines.add(DOUBLE_INDENT + "AND " + cond.columnName() + " &lt; " + cond.dollarVar());
                    xmlLines.add(SINGLE_INDENT + "</if>");
                }
                if (operator == OperatorEnum.LESS_OR_EQUALS) {
                    xmlLines.add(ifTag);
                    xmlLines.add(DOUBLE_INDENT + "AND " + cond.columnName() + " &lt;= " + cond.dollarVar());
                    xmlLines.add(SINGLE_INDENT + "</if>");
                }
                if (operator == OperatorEnum.NOT_NULL) {
                    xmlLines.add(SINGLE_INDENT + "AND " + cond.columnName() + " IS NOT NULL");
                }
                if (operator == OperatorEnum.IS_NULL) {
                    xmlLines.add(SINGLE_INDENT + "AND " + cond.columnName() + " IS NULL");
                }
                if (operator == OperatorEnum.LIKE) {
                    xmlLines.add(ifTag);
                    xmlLines.add(DOUBLE_INDENT + "AND " + cond.columnName() + " LIKE CONCAT('%', '" + cond.dollarVar()
                            + "', '%')");
                    xmlLines.add(SINGLE_INDENT + "</if>");
                }
            }
        }
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