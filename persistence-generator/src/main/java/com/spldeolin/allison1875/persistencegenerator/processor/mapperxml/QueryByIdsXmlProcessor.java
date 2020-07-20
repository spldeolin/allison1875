package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import org.dom4j.Element;
import com.google.common.collect.Iterables;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;

/**
 * 删除可能存在的select(id=xxx)标签，并重新生成
 *
 * @author Deolin 2020-07-19
 */
public class QueryByIdsXmlProcessor {

    private final PersistenceDto persistence;

    private final Element root;

    private final String tagId;

    public QueryByIdsXmlProcessor(PersistenceDto persistence, Element root, String tagId) {
        this.persistence = persistence;
        this.root = root;
        this.tagId = tagId;
    }

    public QueryByIdsXmlProcessor process() {
        if (persistence.getPkProperties().size() == 1) {
            root.addText(Constant.newLine);
            Element queryByIdsTag = Dom4jUtils.findAndRebuildElement(root, "select", "id", tagId);
            queryByIdsTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
            queryByIdsTag.addAttribute("resultMap", "all");
            queryByIdsTag.addText(Constant.newLine + Constant.doubleIndex + "SELECT");
            queryByIdsTag.addElement("include").addAttribute("refid", "all");
            StringBuilder sb = new StringBuilder(64);
            sb.append(Constant.newLine).append(Constant.doubleIndex).append("FROM ").append(persistence.getTableName());
            sb.append(Constant.newLine).append(Constant.doubleIndex).append("WHERE ");
            PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getPkProperties());
            sb.append(onlyPk.getColumnName()).append(" IN (");
            queryByIdsTag.addText(sb.toString());
            sb.setLength(0);
            Element foreachTag = queryByIdsTag.addElement("foreach");
            foreachTag.addAttribute("collection", "ids");
            foreachTag.addAttribute("item", "id");
            foreachTag.addAttribute("separator", ",");
            foreachTag.addText("#{id}");
            queryByIdsTag.addText(")");
        }
        return this;
    }

}