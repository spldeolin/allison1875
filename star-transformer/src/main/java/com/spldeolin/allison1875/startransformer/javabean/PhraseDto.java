package com.spldeolin.allison1875.startransformer.javabean;

import java.util.List;
import java.util.Map;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2023-05-02
 */
@Data
@Accessors(chain = true)
public class PhraseDto {

    private Boolean isOneToOne;

    private String dtEntityQualifier;

    private String dtEntityName;

    private String dtDesignName;

    private String fk;

    private String fkTypeQualifier;

    private List<String> keys;

    private List<String> mkeys;

    private Map<String, String> entityFieldTypesEachFieldName = Maps.newHashMap();

}