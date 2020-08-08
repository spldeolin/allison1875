package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;
import lombok.Getter;

/**
 * 根据外键查询，表中每有几个外键，这个Proc就生成几个方法
 *
 * @author Deolin 2020-07-19
 */
public class QueryByFkXmlProc implements XmlProc {

    private final PersistenceDto persistence;

    @Getter
    private Collection<String> sourceCodeLines;

    public QueryByFkXmlProc(PersistenceDto persistence) {
        this.persistence = persistence;
    }

    public QueryByFkXmlProc process() {
        if (persistence.getFkProperties().size() > 0) {
            sourceCodeLines = Lists.newArrayList();
            for (PropertyDto fk : persistence.getFkProperties()) {
                Element queryByIdTag = new DefaultElement("select");
                queryByIdTag.addAttribute("id", "queryBy" + StringUtils.upperFirstLetter(fk.getPropertyName()));
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
                queryByIdTag.addText(fk.getColumnName() + " = #{" + fk.getPropertyName() + "}");
                sourceCodeLines.addAll(StringUtils.splitLineByLine(Dom4jUtils.toSourceCode(queryByIdTag)));
            }

        }
        return this;
    }

}