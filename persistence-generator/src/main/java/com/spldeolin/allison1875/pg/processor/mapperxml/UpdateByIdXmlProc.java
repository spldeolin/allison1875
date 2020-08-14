package com.spldeolin.allison1875.pg.processor.mapperxml;

import java.util.Collection;
import java.util.stream.Collectors;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.spldeolin.allison1875.pg.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.pg.javabean.PersistenceDto;
import com.spldeolin.allison1875.pg.javabean.PropertyDto;
import com.spldeolin.allison1875.pg.processor.mapper.UpdateByIdProc;
import com.spldeolin.allison1875.pg.util.Dom4jUtils;
import lombok.Getter;

/**
 * 根据ID更新数据，忽略值为null的属性
 *
 * @author Deolin 2020-07-19
 */
public class UpdateByIdXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final String entityName;

    private final UpdateByIdProc updateByPkProc;

    @Getter
    private Collection<String> sourceCodeLines;

    public UpdateByIdXmlProc(PersistenceDto persistence, String entityName, UpdateByIdProc updateByPkProc) {
        this.persistence = persistence;
        this.entityName = entityName;
        this.updateByPkProc = updateByPkProc;
    }

    public UpdateByIdXmlProc process() {
        if (updateByPkProc.getGenerateOrNot() && persistence.getPkProperties().size() > 0) {
            Element stmt = new DefaultElement("update");
            stmt.addAttribute("id", "updateById");
            stmt.addAttribute("parameterType", entityName);
            newLineWithIndent(stmt);
            stmt.addText("UPDATE ").addText(persistence.getTableName());
            Element setTag = stmt.addElement("set");
            for (PropertyDto nonPk : persistence.getNonPkProperties()) {
                Element ifTag = setTag.addElement("if");
                ifTag.addAttribute("test", nonPk.getPropertyName() + "!=null");
                ifTag.addText(nonPk.getColumnName() + " = #{" + nonPk.getPropertyName() + "},");
            }
            newLineWithIndent(stmt);
            stmt.addText("WHERE");
            newLineWithIndent(stmt);
            if (PersistenceGeneratorConfig.getInstace().getNotDeletedSql() != null) {
                stmt.addText(PersistenceGeneratorConfig.getInstace().getNotDeletedSql());
                newLineWithIndent(stmt);
                stmt.addText("AND ");
            }
            stmt.addText(persistence.getPkProperties().stream()
                    .map(pk -> pk.getColumnName() + " = #{" + pk.getPropertyName() + "}")
                    .collect(Collectors.joining(" AND ")));
            sourceCodeLines = Dom4jUtils.toSourceCodeLines(stmt);
        }
        return this;
    }

}