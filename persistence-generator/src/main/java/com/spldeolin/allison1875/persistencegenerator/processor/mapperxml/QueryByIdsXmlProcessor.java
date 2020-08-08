package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.google.common.collect.Iterables;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;
import lombok.Getter;

/**
 * 删除可能存在的select(id=xxx)标签，并重新生成
 *
 * @author Deolin 2020-07-19
 */
public class QueryByIdsXmlProcessor implements SourceCodeGetter {

    private final PersistenceDto persistence;

    private final String tagId;

    @Getter
    private Collection<String> sourceCodeLines;

    public QueryByIdsXmlProcessor(PersistenceDto persistence, String tagId) {
        this.persistence = persistence;
        this.tagId = tagId;
    }

    public QueryByIdsXmlProcessor process() {
        if (persistence.getPkProperties().size() == 1) {
            Element queryByIdsTag = new DefaultElement("select");
            queryByIdsTag.addAttribute("id", tagId);
            queryByIdsTag.addAttribute("resultMap", "all");
            queryByIdsTag.addText(Constant.newLine).addText(Constant.singleIndent);
            queryByIdsTag.addText("SELECT");
            queryByIdsTag.addElement("include").addAttribute("refid", "all");
            queryByIdsTag.addText(Constant.newLine).addText(Constant.singleIndent);
            queryByIdsTag.addText("FROM ").addText(persistence.getTableName());
            queryByIdsTag.addText(Constant.newLine).addText(Constant.singleIndent);
            queryByIdsTag.addText("WHERE ");
            queryByIdsTag.addText(Constant.newLine).addText(Constant.singleIndent);
            PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getPkProperties());
            queryByIdsTag.addText(onlyPk.getColumnName()).addText(" IN (");
            queryByIdsTag.addElement("foreach").addAttribute("collection", "ids").addAttribute("item", "id")
                    .addAttribute("separator", ",").addText("#{id}");
            queryByIdsTag.addText(")");
            sourceCodeLines = StringUtils.splitLineByLine(Dom4jUtils.toSourceCode(queryByIdsTag));
        }
        return this;
    }

}