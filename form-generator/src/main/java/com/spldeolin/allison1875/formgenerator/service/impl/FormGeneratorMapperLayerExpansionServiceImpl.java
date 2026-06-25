package com.spldeolin.allison1875.formgenerator.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.DesignMetaDTO;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.PropertyDTO;
import com.spldeolin.allison1875.querytransformer.dto.ChainAnalysisDTO;
import com.spldeolin.allison1875.querytransformer.dto.ExpandParamRetval;
import com.spldeolin.allison1875.querytransformer.dto.ExpandedFieldDTO;
import com.spldeolin.allison1875.querytransformer.service.MapperLayerExpansionService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-06-26
 */
@Slf4j
public class FormGeneratorMapperLayerExpansionServiceImpl implements MapperLayerExpansionService {

    private List<ItemDef> sortableItems = Collections.emptyList();

    public void setSortableItems(List<ItemDef> sortableItems) {
        this.sortableItems = sortableItems;
    }

    @Override
    public ExpandParamRetval expandParam(ChainAnalysisDTO chainAnalysis) {
        ExpandParamRetval retval = new ExpandParamRetval();
        retval.getExpandedFields().add(new ExpandedFieldDTO()
                .setTypeQualifier("java.lang.String")
                .setFieldName("sortBy")
                .setDescription("排序字段")
                .setSourceExpression("req.getSortBy() != null ? req.getSortBy().getCode() : null"));
        retval.getExpandedFields().add(new ExpandedFieldDTO()
                .setTypeQualifier("java.lang.Boolean")
                .setFieldName("isAsc")
                .setDescription("是否正序")
                .setSourceExpression("req.getIsAsc()"));
        return retval;
    }

    @Override
    public List<String> expandOrderByLines(ChainAnalysisDTO chainAnalysis, DesignMetaDTO designMeta, boolean isJoin) {
        Map<String, PropertyDTO> properties = designMeta.getProperties();
        List<String> lines = Lists.newArrayList();
        lines.add("<choose>");
        lines.add("  <when test=\"sortBy != null\">");

        lines.add("    ORDER BY");
        lines.add("    <choose>");
        for (ItemDef item : sortableItems) {
            PropertyDTO property = properties.get(item.getName());
            if (property == null) {
                continue;
            }
            String columnName = (isJoin ? "t1." : "") + property.getColumnName();
            lines.add("      <when test=\"sortBy == '" + item.getName() + "'\">" + columnName + "</when>");
        }
        PropertyDTO createdAtProp = properties.get("createdAt");
        if (createdAtProp != null) {
            String col = (isJoin ? "t1." : "") + createdAtProp.getColumnName();
            lines.add("      <when test=\"sortBy == 'createdAt'\">" + col + "</when>");
        }
        PropertyDTO updatedAtProp = properties.get("updatedAt");
        if (updatedAtProp != null) {
            String col = (isJoin ? "t1." : "") + updatedAtProp.getColumnName();
            lines.add("      <when test=\"sortBy == 'updatedAt'\">" + col + "</when>");
        }
        lines.add("      <otherwise>" + (isJoin ? "t1." : "") + "id</otherwise>");
        lines.add("    </choose>");

        lines.add("    <choose>");
        lines.add("      <when test=\"isAsc != null and isAsc\">ASC</when>");
        lines.add("      <otherwise>DESC</otherwise>");
        lines.add("    </choose>");

        lines.add("  </when>");
        lines.add("  <otherwise>");
        lines.add("    ORDER BY " + (isJoin ? "t1." : "") + "id DESC");
        lines.add("  </otherwise>");
        lines.add("</choose>");
        return lines;
    }

}
