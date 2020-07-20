package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import org.dom4j.Element;
import com.google.common.base.Strings;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;

/**
 * 删除可能存在的update(id=updateById)标签，并重新生成
 *
 * @author Deolin 2020-07-19
 */
public class UpdateByIdXmlProcessor {

    private final PersistenceDto persistence;

    private final String entityName;

    private final Element root;

    public UpdateByIdXmlProcessor(PersistenceDto persistence, String entityName, Element root) {
        this.persistence = persistence;
        this.entityName = entityName;
        this.root = root;
    }

    public UpdateByIdXmlProcessor process() {
        if (persistence.getPkProperties().size() > 0) {
            root.addText(Constant.newLine);
            Element updateByIdTag = Dom4jUtils.findAndRebuildElement(root, "update", "id", "updateById");
            updateByIdTag.addComment(Constant.PROHIBIT_MODIFICATION_XML);
            updateByIdTag.addAttribute("parameterType", entityName);
            updateByIdTag.addText(Constant.newLine + Constant.doubleIndex + "UPDATE " + persistence.getTableName());

            Element setTag = updateByIdTag.addElement("set");
            for (PropertyDto nonPk : persistence.getNonPkProperties()) {
                Element ifTag = setTag.addElement("if");
                String ifTest = nonPk.getPropertyName() + "!=null";
                if (String.class == nonPk.getJavaType()) {
                    ifTest += " and " + nonPk.getPropertyName() + "!=''";
                }
                ifTag.addAttribute("test", ifTest);
                ifTag.addText(
                        Constant.newLine + Strings.repeat(Constant.singleIndent, 4) + nonPk.getColumnName() + "=#{"
                                + nonPk.getPropertyName() + "},\r\n" + Constant.trebleIndex);
            }

            StringBuilder sb = new StringBuilder(64);
            sb.append(" WHERE ");
            for (PropertyDto pk : persistence.getPkProperties()) {
                sb.append(pk.getColumnName()).append("=#{").append(pk.getPropertyName()).append("} AND ");
            }
            String text = StringUtils.removeLast(sb.toString(), " AND ");
            sb.setLength(0);
            updateByIdTag.addText(text);
        }
        return this;
    }

}