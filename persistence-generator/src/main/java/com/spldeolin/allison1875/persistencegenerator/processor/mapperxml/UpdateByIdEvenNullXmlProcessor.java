package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import org.dom4j.Element;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;

/**
 * 删除可能存在的update(id=updateByIdEvenNull)标签，并重新生成
 *
 * @author Deolin 2020-07-19
 */
public class UpdateByIdEvenNullXmlProcessor {

    private final PersistenceDto persistence;

    private final String entityName;

    private final Element root;

    public UpdateByIdEvenNullXmlProcessor(PersistenceDto persistence, String entityName, Element root) {
        this.persistence = persistence;
        this.entityName = entityName;
        this.root = root;
    }

    public UpdateByIdEvenNullXmlProcessor process() {
        if (persistence.getPkProperties().size() > 0) {
            root.addText(Constant.newLine);
            Element updateByIdEvenNullTag = Dom4jUtils
                    .findAndRebuildElement(root, "update", "id", "updateByIdEvenNull");
            if (PersistenceGeneratorConfig.getInstace().getPrintAllison1875Message()) {
                updateByIdEvenNullTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
            }
            updateByIdEvenNullTag.addAttribute("parameterType", entityName);
            StringBuilder sb = new StringBuilder(64);
            sb.append(Constant.newLine).append(Constant.doubleIndex).append("UPDATE ")
                    .append(persistence.getTableName());
            sb.append(Constant.newLine).append(Constant.doubleIndex).append("SET ");
            for (PropertyDto nonPk : persistence.getNonPkProperties()) {
                sb.append(nonPk.getColumnName()).append("=#{").append(nonPk.getPropertyName()).append("},");
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append(Constant.newLine).append(Constant.doubleIndex).append("WHERE ");
            for (PropertyDto pk : persistence.getPkProperties()) {
                sb.append(pk.getColumnName()).append("=#{").append(pk.getPropertyName()).append("} AND ");
            }
            String text = StringUtils.removeLast(sb, " AND ");
            sb.setLength(0);
            updateByIdEvenNullTag.addText(text);
        }
        return this;
    }

}