package com.spldeolin.allison1875.pg.processor.mapperxml;

import java.util.Collection;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.pg.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.pg.javabean.PersistenceDto;
import com.spldeolin.allison1875.pg.javabean.PropertyDto;
import com.spldeolin.allison1875.pg.util.Dom4jUtils;
import lombok.Getter;

/**
 * 根据外键删除
 *
 * 表中每有几个外键，这个Proc就生成几个方法，以_id结尾的字段算作外键
 *
 * @author Deolin 2020-07-19
 */
public class DeleteByFkXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    @Getter
    private Collection<String> sourceCodeLines;

    public DeleteByFkXmlProc(PersistenceDto persistence) {
        this.persistence = persistence;
    }

    public DeleteByFkXmlProc process() {
        String deletedSql = PersistenceGeneratorConfig.getInstace().getDeletedSql();
        if (deletedSql != null) {
            if (persistence.getFkProperties().size() > 0) {
                sourceCodeLines = Lists.newArrayList();
                for (PropertyDto fk : persistence.getFkProperties()) {
                    Element stmt = new DefaultElement("update");
                    stmt.addAttribute("id", "deleteBy" + StringUtils.upperFirstLetter(fk.getPropertyName()));
                    addParameterType(stmt, fk);
                    newLineWithIndent(stmt);
                    stmt.addText("UPDATE ").addText(persistence.getTableName());
                    newLineWithIndent(stmt);
                    stmt.addText("SET ").addText(deletedSql);
                    newLineWithIndent(stmt);
                    stmt.addText("WHERE ");
                    newLineWithIndent(stmt);
                    if (PersistenceGeneratorConfig.getInstace().getNotDeletedSql() != null) {
                        stmt.addText(PersistenceGeneratorConfig.getInstace().getNotDeletedSql());
                        newLineWithIndent(stmt);
                        stmt.addText("AND ");
                    }
                    stmt.addText(fk.getColumnName() + " = #{" + fk.getPropertyName() + "}");
                    stmt.addText(BaseConstant.NEW_LINE);
                    sourceCodeLines.addAll(StringUtils.splitLineByLine(Dom4jUtils.toSourceCode(stmt)));
                }

            }
        }
        return this;
    }

}