package com.spldeolin.allison1875.da.dto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 一个javabean内所有property（包括所有嵌套的property）的容器
 *
 * @author Deolin 2020-05-08
 */
@Data
@Accessors(chain = true)
public class PropertiesContainerDto {

    private String javabeanQualifier;

    /**
     * 树状的JavabeanProperty
     */
    private Collection<PropertyTreeNodeDto> dendriformProperties;

    /**
     * 平铺的JavabeanProperty
     */
    private Collection<PropertyDto> flatProperties;

    private Map<Long, PropertyDto> flatPropertiesMap;

    public PropertiesContainerDto(String javabeanQualifier, Collection<PropertyTreeNodeDto> dendriformProperties) {
        this.javabeanQualifier = javabeanQualifier;
        this.dendriformProperties = dendriformProperties;
        flat();
        buildAllPath();
    }

    private void flat() {
        flatProperties = Lists.newLinkedList();
        flatPropertiesMap = Maps.newHashMap();
        if (dendriformProperties != null) {
            PropertyTreeNodeDto tempParent = new PropertyTreeNodeDto();
            tempParent.setChildren(dendriformProperties);
            flatRecursively(tempParent);
        }
    }

    private void flatRecursively(PropertyTreeNodeDto parent) {
        for (PropertyTreeNodeDto child : parent.getChildren()) {
            PropertyDto dto = PropertyDto.fromTreeNode(child);
            flatProperties.add(dto);
            flatPropertiesMap.put(dto.getId(), dto);
            this.flatRecursively(child);
        }
    }

    private void buildAllPath() {
        for (PropertyDto dto : flatProperties) {
            StringBuilder path = new StringBuilder(dto.getName());
            if (dto.getJsonType().isArrayLike()) {
                path.append("[0]");
            }
            List<Long> ancestorIds = Lists.newArrayList();

            this.insertNameToHeadRecursively(dto, path, ancestorIds);

            dto.setPath(path.toString());
            dto.setAncestorIds(ancestorIds);
        }
    }

    private void insertNameToHeadRecursively(PropertyDto dto, StringBuilder path, List<Long> ancestorIds) {
        PropertyDto parent = flatPropertiesMap.get(dto.getParentId());
        if (parent != null) {
            String linkPart = parent.getName();
            if (parent.getJsonType().isArrayLike()) {
                linkPart += "[0]";
            }
            path.insert(0, linkPart + ".");

            Long idPart = parent.getId();
            if (idPart != null) {
                ancestorIds.add(0, parent.getId());
            }
            this.insertNameToHeadRecursively(parent, path, ancestorIds);
        }
    }

}