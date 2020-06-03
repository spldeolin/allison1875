package com.spldeolin.allison1875.da.converter;

import java.util.Collection;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.da.dto.PropertyDto;
import com.spldeolin.allison1875.da.dto.PropertyTreeNodeDto;

/**
 * @author Deolin 2020-06-03
 */
public class PropertyConverter {

    public static PropertyDto converter(PropertyTreeNodeDto node) {
        PropertyDto result = new PropertyDto();
        result.setUuid(node.getUuid());
        result.setName(node.getName());
        result.setDescription(node.getDescription());
        result.setJsonType(node.getJsonType());
        result.setJsonFormat(node.getJsonFormat());
        result.setRequired(node.getRequired());
        result.setValidators(node.getValidators());
        if (node.getParent() != null) {
            result.setParentUuid(node.getParent().getUuid());
        }
        Collection<String> childUuids = Lists.newArrayList();
        for (PropertyTreeNodeDto child : node.getChildren()) {
            childUuids.add(child.getUuid());
        }
        result.setChildUuids(childUuids);
        return result;
    }

}
