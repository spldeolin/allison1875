package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.DeleteByKeyProc;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;
import lombok.Getter;

/**
 * 根据外键删除
 *
 * 表中每有几个外键，这个Proc就生成几个方法，以_id结尾的字段算作外键
 *
 * @author Deolin 2020-07-19
 */
public class DeleteByKeyXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final Collection<DeleteByKeyProc> deleteByKeyProcs;

    @Getter
    private Collection<String> sourceCodeLines;

    public DeleteByKeyXmlProc(PersistenceDto persistence, Collection<DeleteByKeyProc> deleteByKeyProcs) {
        this.persistence = persistence;
        this.deleteByKeyProcs = deleteByKeyProcs;
    }

    public DeleteByKeyXmlProc process() {
        if (persistence.getIsDeleteFlagExist()) {
            sourceCodeLines = Lists.newArrayList();
            for (DeleteByKeyProc deleteByKeyProc : deleteByKeyProcs) {
                Element stmt = new DefaultElement("update");
                stmt.addAttribute("id", deleteByKeyProc.getMethodName());
                addParameterType(stmt, deleteByKeyProc.getKey());
                newLineWithIndent(stmt);
                stmt.addText("UPDATE ").addText(persistence.getTableName());
                newLineWithIndent(stmt);
                stmt.addText("SET ").addText(PersistenceGeneratorConfig.getInstance().getDeletedSql());
                newLineWithIndent(stmt);
                stmt.addText("WHERE");
                newLineWithIndent(stmt);
                if (persistence.getIsDeleteFlagExist()) {
                    stmt.addText(PersistenceGeneratorConfig.getInstance().getNotDeletedSql());
                    newLineWithIndent(stmt);
                    stmt.addText("AND ");
                }
                stmt.addText(
                        deleteByKeyProc.getKey().getColumnName() + " = #{" + deleteByKeyProc.getKey().getPropertyName()
                                + "}");
                stmt.addText(BaseConstant.NEW_LINE);
                sourceCodeLines.addAll(Dom4jUtils.toSourceCodeLines(stmt));
            }
        }
        return this;
    }

}