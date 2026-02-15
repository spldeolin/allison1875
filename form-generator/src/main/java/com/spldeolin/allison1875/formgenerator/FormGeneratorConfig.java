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
     * 分页返回值的构造语句（占位符${total}代表总条数，${dtos}代表当前页数据列表）
     */
    @NotEmpty
    String pageResultConstruction;

    /**
     * 空的分页返回值的构造语句
     */
    @NotEmpty
    String pageResultEmptyConstruction;

}