package com.spldeolin.allison1875.docanalyzer.dto;

import java.util.Collection;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.docanalyzer.enums.JsonTypeEnum;
import lombok.Data;

/**
 * @author Deolin 2020-06-03
 */
@Data
@JsonInclude(Include.NON_NULL)
public class PropertyDto {

    private Long id;

    private String path;

    private String name;

    private String description;

    private JsonTypeEnum jsonType;

    private Boolean isFloat;

    private String datetimePattern;

    private Boolean isEnum;

    private Collection<ValidatorDto> validators;

    /**
     * e.g.: 1.12.20.21.22
     */
    @JsonInclude
    private List<Long> ancestorIds;

    @JsonInclude
    private Long parentId;

    private Collection<Long> childIds;

    public static PropertyDto fromTreeNode(PropertyTreeNodeDto treeNode) {
        PropertyDto result = new PropertyDto();
        result.setId(treeNode.getId());
        result.setName(treeNode.getName());
        result.setDescription(treeNode.getDescription());
        result.setJsonType(treeNode.getJsonType());
        result.setIsFloat(treeNode.getIsFloat());
        result.setDatetimePattern(treeNode.getDatetimePattern());
        result.setValidators(treeNode.getValidators());
        if (treeNode.getParent() != null) {
            result.setParentId(treeNode.getParent().getId());
        }
        Collection<Long> childUuids = Lists.newArrayList();
        for (PropertyTreeNodeDto child : treeNode.getChildren()) {
            childUuids.add(child.getId());
        }
        result.setChildIds(childUuids);
        return result;
    }

}