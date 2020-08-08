package com.spldeolin.allison1875.pg.processor.mapperxml;

import java.util.Collection;
import java.util.stream.Collectors;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.pg.javabean.PersistenceDto;
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
        Element stmt = new DefaultElement("insert");
        stmt.addAttribute("id", "insert");
        stmt.addAttribute("parameterType", entityName);
//        if (persistence.getPkProperties().size() > 0) {
//            stmt.addAttribute("useGeneratedKeys", "true");
//            String keyProperty = persistence.getPkProperties().stream().map(PropertyDto::getColumnName)
//                    .collect(Collectors.joining(", "));
//            stmt.addAttribute("keyProperty", keyProperty);
//        }
        newLineWithIndent(stmt);
        stmt.addText("INSERT INTO ").addText(persistence.getTableName()).addText(" (");
        stmt.addElement("include").addAttribute("refid", "all");
        stmt.addText(") VALUES (");
        newLineWithIndent(stmt);
        stmt.addText(persistence.getProperties().stream().map(prop -> "#{" + prop.getPropertyName() + "}")
                .collect(Collectors.joining(", ")));
        stmt.addText(")");

        sourceCodeLines = StringUtils.splitLineByLine(Dom4jUtils.toSourceCode(stmt));
        return this;
    }

}