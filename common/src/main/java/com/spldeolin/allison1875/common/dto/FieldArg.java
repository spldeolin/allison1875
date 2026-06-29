package com.spldeolin.allison1875.common.dto;

import java.util.function.BiConsumer;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author Deolin 2021-05-26
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FieldArg {

    String description;

    String typeQualifier;

    String fieldName;

    BiConsumer<ClassOrInterfaceDeclaration, FieldDeclaration> moreOperation;

    public void validate() {
        List<String> invalids = Lists.newArrayList();
        if (StringUtils.isBlank(typeQualifier)) {
            invalids.add("FieldArg.typeQualifier must not be blank");
        }
        if (StringUtils.isBlank(fieldName)) {
            invalids.add("FieldArg.fieldName must not be blank");
        }
        if (!invalids.isEmpty()) {
            throw new Allison1875Exception(String.join(", ", invalids));
        }
    }

}
