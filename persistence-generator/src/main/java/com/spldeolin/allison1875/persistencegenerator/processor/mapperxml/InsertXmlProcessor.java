package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.stream.Collectors;
import org.dom4j.Element;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;

/**
 * 删除可能存在的insert(id=insert)标签，并重新生成
 *
 * @author Deolin 2020-07-19
 */
public class InsertXmlProcessor {

    private final PersistenceDto persistence;

    private final String entityName;

    private final Element root;

    public InsertXmlProcessor(PersistenceDto persistence, String entityName, Element root) {
        this.persistence = persistence;
        this.entityName = entityName;
        this.root = root;
    }

    public InsertXmlProcessor process() {
        root.addText(Constant.newLine);
        Element insertTag = Dom4jUtils.findAndRebuildElement(root, "insert", "id", "insert");
        if (PersistenceGeneratorConfig.getInstace().getPrintAllison1875Message()) {
            insertTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
        }
        insertTag.addAttribute("parameterType", entityName);
        if (persistence.getPkProperties().size() > 0) {
            insertTag.addAttribute("useGeneratedKeys", "true");
            String keyProperty = persistence.getPkProperties().stream().map(PropertyDto::getColumnName)
                    .collect(Collectors.joining(","));
            insertTag.addAttribute("keyProperty", keyProperty);
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append(Constant.newLine).append(Constant.doubleIndex);
        sb.append("INSERT INTO ").append(persistence.getTableName()).append(" (");
        insertTag.addText(sb.toString());
        insertTag.addElement("include").addAttribute("refid", "all");
        sb.setLength(0);
        sb.append(") VALUES (");
        for (PropertyDto property : persistence.getProperties()) {
            sb.append("#{").append(property.getPropertyName()).append("},");
        }
        sb.deleteCharAt(sb.lastIndexOf(",")).append(")");
        insertTag.addText(sb.toString());
        return this;
    }

}