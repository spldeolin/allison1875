package com.spldeolin.allison1875.pg.processor.mapperxml;

import java.util.Collection;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.pg.javabean.PersistenceDto;
import com.spldeolin.allison1875.pg.javabean.PropertyDto;
import com.spldeolin.allison1875.pg.util.Dom4jUtils;
import lombok.Getter;

/**
 * 插入
 *
 * @author Deolin 2020-07-19
 */
public class InsertXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final String entityName;

    @Getter
    private Collection<String> sourceCodeLines;

    public InsertXmlProc(PersistenceDto persistence, String entityName) {
        this.persistence = persistence;
        this.entityName = entityName;
    }

    public InsertXmlProc process() {
        Element insertTag = new DefaultElement("insert");
        insertTag.addAttribute("id", "insert");
        insertTag.addAttribute("parameterType", entityName);
//        if (persistence.getPkProperties().size() > 0) {
//            insertTag.addAttribute("useGeneratedKeys", "true");
//            String keyProperty = persistence.getPkProperties().stream().map(PropertyDto::getColumnName)
//                    .collect(Collectors.joining(", "));
//            insertTag.addAttribute("keyProperty", keyProperty);
//        }
        newLineWithIndent(insertTag);
        insertTag.addText("INSERT INTO ").addText(persistence.getTableName());
        Element trimTag1 = insertTag.addElement("trim").addAttribute("prefix", "(").addAttribute("suffix", ")")
                .addAttribute("suffixOverrides", ",");
        for (PropertyDto property : persistence.getProperties()) {
            Element ifTag = trimTag1.addElement("if");
            ifTag.addAttribute("test", property.getPropertyName() + "!=null");
            ifTag.addText(property.getColumnName() + ",");
        }
        Element trimTag2 = insertTag.addElement("trim").addAttribute("prefix", "VALUES (").addAttribute("suffix", ")")
                .addAttribute("suffixOverrides", ",");
        for (PropertyDto property : persistence.getProperties()) {
            Element ifTag = trimTag2.addElement("if");
            ifTag.addAttribute("test", property.getPropertyName() + "!=null");
            ifTag.addText("#{" + property.getPropertyName() + "},");
        }

        sourceCodeLines = StringUtils.splitLineByLine(Dom4jUtils.toSourceCode(insertTag));
        return this;
    }

}