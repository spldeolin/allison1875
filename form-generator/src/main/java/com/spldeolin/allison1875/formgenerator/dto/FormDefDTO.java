package com.spldeolin.allison1875.formgenerator.dto;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2025-08-12
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FormDefDTO {

    @NotNull
    String id;

    // TODO 校验表名、类名最小合法性
    @NotNull
    String name;

    @NotNull
    String title;

    @NotNull
    String desc;

    @NotEmpty
    @Valid
    List<@NotNull ItemDefDTO> items;

}