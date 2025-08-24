package com.spldeolin.allison1875.formgenerator.dto;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.formgenerator.enums.ItemTypeEnum;
import com.spldeolin.allison1875.formgenerator.enums.ItemValidEnum;
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
public class ItemDefDTO {

    @NotNull
    String id;

    String name;

    @NotNull
    String title;

    @NotNull
    ItemTypeEnum type;

    @NotNull
    @Valid
    List<@NotNull ItemOptionDefDTO> options;

    List<ItemValidEnum> valids;

}