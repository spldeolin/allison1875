package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.stream.Collectors;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;
import lombok.Getter;

/**
 * 删除可能存在的sql(id=all)标签，并重新生成
 *
 * @author Deolin 2020-07-19
 */
public class AllColumnSqlProcessor implements SourceCodeGetter {

    private final PersistenceDto persistence;

    @Getter
    private Collection<String> sourceCodeLines;

    public AllColumnSqlProcessor(PersistenceDto persistence) {
        this.persistence = persistence;
    }

    public AllColumnSqlProcessor process() {
        Element sqlTag = new DefaultElement("sql");
        sqlTag.addAttribute("id", "all");
        sqlTag.addText(Constant.newLine);
        sqlTag.addText(Constant.singleIndent);
        sqlTag.addText(
                persistence.getProperties().stream().map(PropertyDto::getColumnName).collect(Collectors.joining(", ")));
        sqlTag.addText(Constant.newLine);
        sourceCodeLines = StringUtils.splitLineByLine(Dom4jUtils.toSourceCode(sqlTag));
        return this;
    }

}