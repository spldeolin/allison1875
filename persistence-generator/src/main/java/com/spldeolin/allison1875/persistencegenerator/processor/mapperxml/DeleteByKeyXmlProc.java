package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import static com.spldeolin.allison1875.base.constant.BaseConstant.SINGLE_INDENT;

import java.util.Collection;
import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.DeleteByKeyProc;

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

    private Collection<String> sourceCodeLines;

    public DeleteByKeyXmlProc(PersistenceDto persistence, Collection<DeleteByKeyProc> deleteByKeyProcs) {
        this.persistence = persistence;
        this.deleteByKeyProcs = deleteByKeyProcs;
    }

    public DeleteByKeyXmlProc process() {
        if (persistence.getIsDeleteFlagExist()) {
            sourceCodeLines = Lists.newArrayList();
            for (DeleteByKeyProc deleteByKeyProc : deleteByKeyProcs) {
                List<String> xmlLines = Lists.newArrayList();
                xmlLines.add(String.format("<update id=\"%s\" parameterType=\"%s\">", deleteByKeyProc.getMethodName(),
                        deleteByKeyProc.getKey().getJavaType().getName().replaceFirst("java\\.lang\\.", "")));
                xmlLines.add(SINGLE_INDENT + "<!-- @formatter:off -->");
                xmlLines.add(SINGLE_INDENT + "UPDATE " + persistence.getTableName());
                xmlLines.add(SINGLE_INDENT + "SET " + PersistenceGeneratorConfig.getInstance().getDeletedSql());
                xmlLines.add(SINGLE_INDENT + "WHERE " +
                        deleteByKeyProc.getKey().getColumnName() + " = #{" + deleteByKeyProc.getKey().getPropertyName()
                        + "}");
                xmlLines.add(SINGLE_INDENT + "<!-- @formatter:on -->");
                xmlLines.add("</update>");
                sourceCodeLines = xmlLines;
            }
        }
        return this;
    }

    public Collection<String> getSourceCodeLines() {
        return this.sourceCodeLines;
    }

}