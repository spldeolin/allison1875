package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import org.dom4j.Element;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;

/**
 * 删除可能存在的resultMap(id=all)标签，并重新生成
 *
 * @author Deolin 2020-07-19
 */
public class AllColumnResultMapProcessor {

    private final PersistenceDto persistence;

    private final String entityName;

    private final Element root;

    public AllColumnResultMapProcessor(PersistenceDto persistence, String entityName, Element root) {
        this.persistence = persistence;
        this.entityName = entityName;
        this.root = root;
    }

    public AllColumnResultMapProcessor process() {
        root.addText(Constant.newLine);
        Element resultMapTag = Dom4jUtils.findAndRebuildElement(root, "resultMap", "id", "all");
        resultMapTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
        resultMapTag.addAttribute("type", entityName);
        for (PropertyDto pk : persistence.getPkProperties()) {
            Element resultTag = resultMapTag.addElement("id");
            resultTag.addAttribute("column", pk.getColumnName());
            resultTag.addAttribute("property", pk.getPropertyName());
        }
        for (PropertyDto nonPk : persistence.getNonPkProperties()) {
            Element resultTag = resultMapTag.addElement("result");
            resultTag.addAttribute("column", nonPk.getColumnName());
            resultTag.addAttribute("property", nonPk.getPropertyName());
        }
        return this;
    }

}