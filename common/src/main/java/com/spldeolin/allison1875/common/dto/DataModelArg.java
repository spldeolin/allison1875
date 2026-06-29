package com.spldeolin.allison1875.common.dto;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-05-26
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataModelArg {

    Path sourceRoot;

    String packageName;

    String className;

    String description;

    String author;

    final List<FieldArg> fieldArgs = Lists.newArrayList();

    BiConsumer<CompilationUnit, ClassOrInterfaceDeclaration> moreOperation;

    FileExistenceResolutionEnum dataModelExistenceResolution;

    public void validate() {
        List<String> invalids = Lists.newArrayList();
        if (sourceRoot == null) {
            invalids.add("DataModelArg.sourceRoot must not be null");
        }
        if (StringUtils.isBlank(packageName)) {
            invalids.add("DataModelArg.packageName must not be blank");
        }
        if (StringUtils.isBlank(className)) {
            invalids.add("DataModelArg.className must not be blank");
        }
        if (StringUtils.isBlank(author)) {
            invalids.add("DataModelArg.author must not be blank");
        }
        if (dataModelExistenceResolution == null) {
            invalids.add("DataModelArg.dataModelExistenceResolution must not be null");
        }
        for (FieldArg fieldArg : fieldArgs) {
            fieldArg.validate();
        }
        if (!invalids.isEmpty()) {
            throw new Allison1875Exception(String.join(", ", invalids));
        }
    }

}
