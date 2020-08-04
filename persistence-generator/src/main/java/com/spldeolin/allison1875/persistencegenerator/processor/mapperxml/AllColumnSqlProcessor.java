package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.stream.Collectors;
import org.dom4j.Element;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;

/**
 * 删除可能存在的sql(id=all)标签，并重新生成
 *
 * @author Deolin 2020-07-19
 */
public class AllColumnSqlProcessor {

    private final PersistenceDto persistence;

    private final Element root;

    public AllColumnSqlProcessor(PersistenceDto persistence, Element root) {
        this.persistence = persistence;
        this.root = root;
    }

    public AllColumnSqlProcessor process() {
        root.addText(Constant.newLine);
        Element sqlTag = Dom4jUtils.findAndRebuildElement(root, "sql", "id", "all");
        if (PersistenceGeneratorConfig.getInstace().getPrintAllison1875Message()) {
            sqlTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
        }
        sqlTag.addText(Constant.newLine + Constant.doubleIndex + persistence.getProperties().stream()
                .map(PropertyDto::getColumnName).collect(Collectors.joining(",")));
        return this;
    }

}