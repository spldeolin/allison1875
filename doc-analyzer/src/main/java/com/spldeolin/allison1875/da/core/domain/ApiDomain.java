package com.spldeolin.allison1875.da.core.domain;

import java.util.Collection;
import java.util.function.Consumer;
import org.springframework.util.CollectionUtils;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.da.core.enums.BodyTypeEnum;
import com.spldeolin.allison1875.da.core.enums.MethodTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2019-12-02
 */
@Data
@Accessors(fluent = true)
public class ApiDomain {

    private Collection<MethodTypeEnum> method;

    private Collection<String> uri;

    private String description;

    private Collection<UriFieldDomain> pathVariableFields;

    private Collection<UriFieldDomain> requestParamFields;

    private BodyTypeEnum requestBodyType;

    private Collection<BodyFieldDomain> requestBodyFields;

    private JsonSchema requestBodyChaosJsonSchema;

    private BodyTypeEnum responseBodyType;

    private Collection<BodyFieldDomain> responseBodyFields;

    private JsonSchema responseBodyChaosJsonSchema;

    private String author;

    private String codeSourceLocation;

    public Collection<BodyFieldDomain> listRequestBodyFieldsFlatly() {
        Collection<BodyFieldDomain> result = Lists.newArrayList();
        if (requestBodyFields == null) {
            return Lists.newArrayList();
        }
        for (BodyFieldDomain field : requestBodyFields) {
            result.add(field);
            addAllChildren(field, result);
        }
        return result;
    }

    public Collection<BodyFieldDomain> listResponseBodyFieldsFlatly() {
        Collection<BodyFieldDomain> result = Lists.newArrayList();
        if (responseBodyFields == null) {
            return Lists.newArrayList();
        }
        for (BodyFieldDomain field : responseBodyFields) {
            result.add(field);
            addAllChildren(field, result);
        }
        return result;
    }

    /**
     * 执行这个方法后，这个对象中每个BodyFieldDomain.linkName均会有值
     */
    public void setAllBodyFieldLinkNames() {
        Consumer<BodyFieldDomain> action = field -> {
            String fieldName = field.fieldName();
            if (fieldName != null) {
                StringBuilder linkName = new StringBuilder(fieldName);
                appendParentName(field, linkName);
                field.linkName(linkName.toString());
            }
        };
        listRequestBodyFieldsFlatly().forEach(action);
        listResponseBodyFieldsFlatly().forEach(action);
    }

    private void appendParentName(BodyFieldDomain child, StringBuilder linkName) {
        BodyFieldDomain parent = child.parentField();
        if (parent != null) {
            String linkPart = parent.fieldName();
            if (parent.jsonType().isArrayLike()) {
                linkPart += "[0]";
            }
            linkName.insert(0, linkPart + ".");
            appendParentName(parent, linkName);
        }
    }

    private void addAllChildren(BodyFieldDomain parent, Collection<BodyFieldDomain> container) {
        if (!CollectionUtils.isEmpty(parent.fields())) {
            for (BodyFieldDomain child : parent.fields()) {
                container.add(child);
                this.addAllChildren(child, container);
            }
        }
    }

}