package com.spldeolin.allison1875.startransformer.javabean;

import java.util.List;
import java.util.Map;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2023-05-02
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhraseDto {

    Boolean isOneToOne;

    String dtEntityQualifier;

    String dtEntityName;

    String dtDesignName;

    String fk;

    String fkTypeQualifier;

    List<String> keys;

    List<String> mkeys;

    Map<String, String> entityFieldTypesEachFieldName = Maps.newHashMap();

}