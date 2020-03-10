package com.spldeolin.allison1875.da.core.definition;

import java.util.Collection;
import java.util.function.Consumer;
import org.springframework.util.CollectionUtils;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.da.core.enums.BodyStructureEnum;
import com.spldeolin.allison1875.da.core.enums.MethodTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2019-12-02
 */
@Data
@Accessors(fluent = true)
public class ApiDefinition {

    private Collection<MethodTypeEnum> method;

    private Collection<String> uri;

    private String description;

    private Collection<UriFieldDefinition> pathVariableFields;

    private Collection<UriFieldDefinition> requestParamFields;

    private BodyStructureEnum requestBodyStructure;

    private Collection<BodyFieldDefinition> requestBodyFields;

    private JsonSchema requestBodyChaosJsonSchema;

    private BodyStructureEnum responseBodyStructure;

    private Collection<BodyFieldDefinition> responseBodyFields;

    private JsonSchema responseBodyChaosJsonSchema;

    private String author;

    private String sourceCode;

    public Collection<BodyFieldDefinition> listRequestBodyFieldsFlatly() {
        Collection<BodyFieldDefinition> result = Lists.newArrayList();
        if (requestBodyFields == null) {
            return Lists.newArrayList();
        }
        for (BodyFieldDefinition field : requestBodyFields) {
            result.add(field);
            addAllChildren(field, result);
        }
        return result;
    }

    public Collection<BodyFieldDefinition> listResponseBodyFieldsFlatly() {
        Collection<BodyFieldDefinition> result = Lists.newArrayList();
        if (responseBodyFields == null) {
            return Lists.newArrayList();
        }
        for (BodyFieldDefinition field : responseBodyFields) {
            result.add(field);
            addAllChildren(field, result);
        }
        return result;
    }

    /**
     * 执行这个方法后，这个对象中的每个BodyFieldDefinition.linkName均会有值
     */
    public void setAllBodyFieldLinkNames() {
        Consumer<BodyFieldDefinition> action = field -> {
            String fieldName = field.getFieldName();
            if (fieldName != null) {
                StringBuilder linkName = new StringBuilder(fieldName);
                appendParentName(field, linkName);
                field.setLinkName(linkName.toString());
            }
        };
        listRequestBodyFieldsFlatly().forEach(action);
        listResponseBodyFieldsFlatly().forEach(action);
    }

    private void appendParentName(BodyFieldDefinition child, StringBuilder linkName) {
        BodyFieldDefinition parent = child.getParentField();
        if (parent != null) {
            String linkPart = parent.getFieldName();
            if (parent.getJsonType().isArrayLike()) {
                linkPart += "[0]";
            }
            linkName.insert(0, linkPart + ".");
            appendParentName(parent, linkName);
        }
    }

    private void addAllChildren(BodyFieldDefinition parent, Collection<BodyFieldDefinition> container) {
        if (!CollectionUtils.isEmpty(parent.getChildFields())) {
            for (BodyFieldDefinition child : parent.getChildFields()) {
                container.add(child);
                this.addAllChildren(child, container);
            }
        }
    }

}