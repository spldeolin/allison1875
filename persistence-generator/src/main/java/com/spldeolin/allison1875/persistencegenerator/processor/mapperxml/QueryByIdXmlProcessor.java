package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import org.dom4j.Element;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;

/**
 * 删除可能存在的select(id=queryById)标签，并重新生成
 *
 * @author Deolin 2020-07-19
 */
public class QueryByIdXmlProcessor {

    private final PersistenceDto persistence;

    private final Element root;

    public QueryByIdXmlProcessor(PersistenceDto persistence, Element root) {
        this.persistence = persistence;
        this.root = root;
    }

    public QueryByIdXmlProcessor process() {
        if (persistence.getPkProperties().size() > 0) {
            root.addText(Constant.newLine);
            Element queryByIdTag = Dom4jUtils.findAndRebuildElement(root, "select", "id", "queryById");
            if (PersistenceGeneratorConfig.getInstace().getPrintAllison1875Message()) {
                queryByIdTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
            }
            queryByIdTag.addAttribute("resultMap", "all");
            queryByIdTag.addText(Constant.newLine + Constant.doubleIndex + "SELECT");
            queryByIdTag.addElement("include").addAttribute("refid", "all");
            StringBuilder sb = new StringBuilder(64);
            sb.append(Constant.newLine).append(Constant.doubleIndex).append("FROM ").append(persistence.getTableName());
            sb.append(Constant.newLine).append(Constant.doubleIndex).append("WHERE ");
            for (PropertyDto pk : persistence.getPkProperties()) {
                sb.append(pk.getColumnName()).append("=#{").append(pk.getPropertyName()).append("},");
            }
            String text = StringUtils.removeLast(sb, ",");
            sb.setLength(0);
            queryByIdTag.addText(text);
        }
        return this;
    }

}