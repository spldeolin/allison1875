package com.spldeolin.allison1875.da.dto;

import java.util.Collection;
import java.util.Map;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.da.converter.PropertyConverter;
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

    private Map<String, PropertyDto> flatPropertiesMap;

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
            PropertyDto dto = PropertyConverter.converter(child);
            flatProperties.add(dto);
            flatPropertiesMap.put(dto.getUuid(), dto);
            this.flatRecursively(child);
        }
    }

    private void buildAllPath() {
        for (PropertyDto dto : flatProperties) {
            String name = dto.getName();
            StringBuilder path = new StringBuilder(name);
            if (dto.getJsonType().isArrayLike()) {
                path.append("[0]");
            }
            this.insertToHeadRecursively(dto, path);
            dto.setPath(path.toString());
        }
    }

    private void insertToHeadRecursively(PropertyDto dto, StringBuilder path) {
        PropertyDto parent = flatPropertiesMap.get(dto.getParentUuid());
        if (parent != null) {
            String linkPart = parent.getName();
            if (parent.getJsonType().isArrayLike()) {
                linkPart += "[0]";
            }
            path.insert(0, linkPart + ".");
            this.insertToHeadRecursively(parent, path);
        }
    }

}