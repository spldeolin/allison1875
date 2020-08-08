package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.stream.Collectors;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;
import lombok.Getter;

/**
 * 删除可能存在的select(id=queryById)标签，并重新生成
 *
 * @author Deolin 2020-07-19
 */
public class QueryByIdXmlProcessor implements SourceCodeGetter {

    private final PersistenceDto persistence;

    @Getter
    private Collection<String> sourceCodeLines;

    public QueryByIdXmlProcessor(PersistenceDto persistence) {
        this.persistence = persistence;
    }

    public QueryByIdXmlProcessor process() {
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
            queryByIdTag.addText(persistence.getPkProperties().stream()
                    .map(pk -> pk.getColumnName() + "=#{" + pk.getPropertyName() + "}")
                    .collect(Collectors.joining(", ")));
            sourceCodeLines = StringUtils.splitLineByLine(Dom4jUtils.toSourceCode(queryByIdTag));
        }
        return this;
    }

}