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
        insertTag.addText(Constant.newLine).addText(Constant.singleIndent);
        insertTag.addText("INSERT INTO ").addText(persistence.getTableName()).addText(" (");
        insertTag.addElement("include").addAttribute("refid", "all");
        insertTag.addText(") VALUES (");
        insertTag.addText(Constant.newLine).addText(Constant.singleIndent);
        insertTag.addText(persistence.getProperties().stream().map(prop -> "#{" + prop.getPropertyName() + "}")
                .collect(Collectors.joining(", ")));
        insertTag.addText(")");

        sourceCodeLines = StringUtils.splitLineByLine(Dom4jUtils.toSourceCode(insertTag));
        return this;
    }

}