package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.List;
import com.spldeolin.allison1875.querytransformer.enums.ChainMethodEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2023-05-02
 */
@Data
@Accessors(chain = true)
public class PhraseDto {

    private ChainMethodEnum chainMethod;

    private String dtEntityName;

    private String dtDesignName;

    private String dtDesignQulifier;

    private List<String> omKeys;

}