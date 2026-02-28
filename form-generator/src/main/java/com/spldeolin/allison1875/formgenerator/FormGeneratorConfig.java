package com.spldeolin.allison1875.formgenerator;

import java.io.File;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2023-05-05
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FormGeneratorConfig {

    /**
     * DSL.yml文件的相对路径（相对于pom所在basedir的相对路径 或 绝对路径 皆可）
     */
    @NotNull
    File dslPath = new File("./forms.yml");

    /**
     * 使用doc-analyzer生成接口文档
     */
    @NotNull
    Boolean enableDocAnalyzer = false;

    /**
     * Controller类@RequestMapping路径的代码片段（占位符${formName}代表表单名称）
     */
    @NotEmpty
    String controllerRequestMapping = "/api/v1/${formName}";

    /**
     * 构造分页返回值的代码片段（占位符${total}代表总条数，${dtos}代表当前页数据列表）
     */
    @NotEmpty
    String pageResultConstruction;

    /**
     * 构造空的分页返回值的代码片段
     */
    @NotEmpty
    String pageResultEmptyConstruction;

    /**
     * 生成短UUID的的代码片段
     */
    @NotEmpty
    String shortUuidGeneration = "UUID.randomUUID().toString().replaceAll(\"-\", \"\").toLowerCase()";

    /**
     * 判断列表是否为empty的代码片段（占位符${list}代表列表）
     */
    @NotEmpty
    String collectionEmptyCheck = "CollectionUtils.isEmpty(${list})";

}