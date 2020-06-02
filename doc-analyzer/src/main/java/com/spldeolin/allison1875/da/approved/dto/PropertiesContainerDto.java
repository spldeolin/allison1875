package com.spldeolin.allison1875.da.approved.dto;

import java.util.Collection;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import com.google.common.collect.Lists;
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
    private Collection<PropertyDto> dendriformProperties;

    /**
     * 平铺的JavabeanProperty
     */
    private Collection<PropertyDto> flatProperties;

    public PropertiesContainerDto(String javabeanQualifier, Collection<PropertyDto> dendriformProperties) {
        this.javabeanQualifier = javabeanQualifier;
        this.dendriformProperties = dendriformProperties;
        flat();
        buildAllPath();
    }

    private void flat() {
        flatProperties = Lists.newLinkedList();
        if (dendriformProperties != null) {
            for (PropertyDto firstFloor : dendriformProperties) {
                flatProperties.add(firstFloor);
                flat(firstFloor, flatProperties);
            }
        }

        Collection<PropertyDto> newFlatProperties = Lists.newArrayList();
        for (PropertyDto flatProperty : flatProperties) {
            PropertyDto newDto = new PropertyDto();
            BeanUtils.copyProperties(flatProperty, newDto);
            newDto.setChildren(null);
            newFlatProperties.add(newDto);
        }
        flatProperties = newFlatProperties;
    }

    private void buildAllPath() {
        if (flatProperties == null) {
            flat();
        }
        for (PropertyDto prop : flatProperties) {
            String name = prop.getName();
            StringBuilder path = new StringBuilder(name);
            this.insertToHead(prop, path);
            if (prop.getJsonType().isArrayLike()) {
                path.append("[0]");
            }
            prop.setPath(path.toString());
        }
    }

    private void insertToHead(PropertyDto prop, StringBuilder path) {
        PropertyDto parent = prop.getParent();
        if (parent != null) {
            String linkPart = parent.getName();
            if (parent.getJsonType().isArrayLike()) {
                linkPart += "[0]";
            }
            path.insert(0, linkPart + ".");
            this.insertToHead(parent, path);
        }
    }

    private void flat(PropertyDto parent, Collection<PropertyDto> props) {
        if (!CollectionUtils.isEmpty(parent.getChildren())) {
            for (PropertyDto child : parent.getChildren()) {
                props.add(child);
                this.flat(child, props);
            }
        }
    }

}