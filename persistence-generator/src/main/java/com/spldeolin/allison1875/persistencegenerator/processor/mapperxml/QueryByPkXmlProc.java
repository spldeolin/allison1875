package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.stream.Collectors;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;
import lombok.Getter;

/**
 * 根据主键查询
 *
 * @author Deolin 2020-07-19
 */
public class QueryByPkXmlProc implements XmlProc {

    private final PersistenceDto persistence;

    @Getter
    private Collection<String> sourceCodeLines;

    public QueryByPkXmlProc(PersistenceDto persistence) {
        this.persistence = persistence;
    }

    public QueryByPkXmlProc process() {
        if (persistence.getPkProperties().size() > 0) {
            Element queryByIdTag = new DefaultElement("select");
            queryByIdTag.addAttribute("id", "queryById");
            queryByIdTag.addAttribute("resultMap", "all");
            queryByIdTag.addText(Constant.newLine).addText(Constant.singleIndent);
            queryByIdTag.addText("SELECT");
            queryByIdTag.addElement("include").addAttribute("refid", "all");
            queryByIdTag.addText(Constant.newLine).addText(Constant.singleIndent);
            queryByIdTag.addText("FROM ").addText(persistence.getTableName());
            queryByIdTag.addText(Constant.newLine).addText(Constant.singleIndent);
            queryByIdTag.addText("WHERE ");
            queryByIdTag.addText(Constant.newLine).addText(Constant.singleIndent);
            queryByIdTag.addText(PersistenceGeneratorConfig.getInstace().getNotDeletedSql());
            queryByIdTag.addText(Constant.newLine).addText(Constant.singleIndent);
            queryByIdTag.addText(persistence.getPkProperties().stream()
                    .map(pk -> pk.getColumnName() + " = #{" + pk.getPropertyName() + "}")
                    .collect(Collectors.joining(", ")));
            sourceCodeLines = StringUtils.splitLineByLine(Dom4jUtils.toSourceCode(queryByIdTag));
        }
        return this;
    }

}