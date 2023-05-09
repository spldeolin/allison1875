package com.spldeolin.allison1875.startransformer.javabean;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2023-05-02
 */
@Data
@Accessors(chain = true)
public class PhraseDto {

    private Boolean isOneToOne;

    private String dtEntityName;

    private String dtDesignName;

    private String dtDesignQulifier;

    private String fk;

    private List<String> keys;

    private List<String> mkeys;

}